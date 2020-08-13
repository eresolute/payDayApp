package com.fh.payday.views2.moneytransfer.beneificaries.shared;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.BankListDialogKt;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.ListUtilsKt;
import com.fh.payday.viewmodels.SharedViewModel;
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity;
import com.fh.payday.views2.payments.SelectOperatorActivity;
import com.google.android.gms.vision.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SelectBankFragment extends Fragment {

    private SharedViewModel model;
    private int index;
    private TextInputEditText etBank;
    private TextInputLayout etBankLayout;
    private TextView tvBankName;
    private EditBeneficiaryActivity activity;
    private ProgressBar progressBar;
    private List<String> bankList = new ArrayList<>();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && tvBankName != null) {
            tvBankName.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_blue_rounded_border));
        }
    }

    OnBankSelectedListener listener = name -> {
              if (activity == null) return;
              activity.getViewModel().setSelectedBank(name);
              tvBankName.setText(name);
    };

    public static SelectBankFragment newInstance(int position) {
        
        Bundle args = new Bundle();
        args.putInt("index", position);
        SelectBankFragment fragment = new SelectBankFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (EditBeneficiaryActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(activity).get(SharedViewModel.class);
        index = getArguments().getInt("index");
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_bank, container, false);
        init(view);
        attachObserver();

        User user = UserPreferences.Companion.getInstance().getUser(activity);
        if (user != null && ListUtilsKt.isEmptyList(activity.getViewModel().getBankArrayList()))
            activity.getViewModel().getBankList(user.getCustomerId());

        return view;
    }

    private void init(View view) {

        etBank = view.findViewById(R.id.et_bank);
        etBankLayout = view.findViewById(R.id.textInputLayout);
        progressBar = view.findViewById(R.id.progress_bar);
        tvBankName = view.findViewById(R.id.tvBankName);

        view.findViewById(R.id.img_previous).setOnClickListener(v-> model.setSelected(index -1));

        view.findViewById(R.id.img_next).setOnClickListener(v-> {
            if (!TextUtils.isEmpty(tvBankName.getText().toString())) {
                model.setMap("Bank", tvBankName.getText().toString());
                model.setSelected(index + 1);
            } else {
                tvBankName.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_border_error));
            }
        });

        tvBankName.setOnClickListener(v -> {
            List<String> bankList = activity.getViewModel().getBankArrayList();
            if (ListUtilsKt.isEmptyList(bankList)) return;
            BankListDialogKt.showBanks(activity, bankList, listener);
        });

        tvBankName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvBankName.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_blue_rounded_border));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void attachObserver() {
        activity.getViewModel().getBankListState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<List<String>> state = event.getContentIfNotHandled();
            if (state == null) return;

            if(state instanceof NetworkState2.Loading) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            progressBar.setVisibility(View.GONE);

            if (state instanceof NetworkState2.Success) {

                bankList = ((NetworkState2.Success<List<String>>) state).getData();

                if (ListUtilsKt.isEmptyList(bankList)) return;

                activity.getViewModel().setBankArrayList(bankList);

            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                activity.onError(error.getMessage());
            } else if (state instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    public interface OnBankSelectedListener {
        void onBankSelect(String name);
    }
}
