package com.fh.payday.views2.moneytransfer.beneificaries.shared;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.moneytransfer.PaydayBeneficiary;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.viewmodels.SharedViewModel;
import com.fh.payday.views.adapter.moneytransfer.BeneficiaryDetailAdapter;
import com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday.PaydayBeneficiaryActivity;
import com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday.PaydayToPaydayBeneficiaryFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeneficiaryDetailFragment extends Fragment {

    SharedViewModel model;
    int index;
    RecyclerView rvBeneficiaryDetail;
    public static final String TAG = BeneficiaryDetailFragment.class.getName();
    List<Map.Entry<String, String>> list;
    private Map<String, String> map ;
    PaydayToPaydayBeneficiaryFragment.BENEFICIARY type;
    PaydayBeneficiaryActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (PaydayBeneficiaryActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        index = getArguments().getInt("index");

        addOtpObserver();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            map = new HashMap<>();
            list = new ArrayList<>();
            if (activity == null) return;
            if (activity.getViewModel() == null)
                return;

            if (getArguments().getSerializable("option") == PaydayToPaydayBeneficiaryFragment.BENEFICIARY.P2P) {
                if (activity.getViewModel().getPaydayBeneficiary() == null) return;

                PaydayBeneficiary beneficiary = activity.getViewModel().getPaydayBeneficiary();
                map.put("Name", beneficiary.getCustomerName());
                map.put("Account Status", beneficiary.getAccountStatus());
                map.put("Card Number", beneficiary.getCardAccountNumber());
                map.put("Mobile Number", beneficiary.getMobileNumber());
            } else if (getArguments().getSerializable("option") == PaydayToPaydayBeneficiaryFragment.BENEFICIARY.P2C){
                map = model.getMap().getValue();
            } else if (getArguments().getSerializable("option") == PaydayToPaydayBeneficiaryFragment.BENEFICIARY.LOCAL) {
                map = model.getMap().getValue();
            }

            for (Map.Entry<String, String> key: map.entrySet()) {
                list.add(key);
            }

            setRecyclerView(list);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beneficiary_detail, container, false);
        init(view);

        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {

            activity.hideKeyboard();

            User user = UserPreferences.Companion.getInstance().getUser(activity);
            if (user == null) return;
            if (activity == null) return;

            callService(user);

        });

        return view;
    }

    private void callService(User user) {

        if (getArguments() == null) return;
        if (user == null) return;

        PaydayToPaydayBeneficiaryFragment.BENEFICIARY type = (PaydayToPaydayBeneficiaryFragment.BENEFICIARY)getArguments().getSerializable("option");
        if (type == null) return;

        switch (type.name()) {
            case "P2P":
                if (activity.getViewModel().getPaydayBeneficiary() == null) return;
                activity.getViewModel().getOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
                break;
            case "P2C":
//                if (activity.getViewModel().getCardNumber() == null || activity.getViewModel().getShortName() == null ) return;
//                activity.getViewModel().generateOtp(user.getCustomerId());
                break;
            case "LOCAL":
                break;
            case "INTL":
                break;

        }
    }

    private void init(View view) {
        rvBeneficiaryDetail = view.findViewById(R.id.rv_beneficiary_detail);
    }

    private void setRecyclerView(List<Map.Entry<String, String>> list) {
        rvBeneficiaryDetail.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvBeneficiaryDetail.setAdapter(new BeneficiaryDetailAdapter(list));
    }

    private void addOtpObserver() {
        if (activity == null) return;
        activity.getViewModel().getOtpState().observe(this, event -> {

            if(event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();

            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (otpResponse instanceof NetworkState2.Success) {
                Log.i("index", ""+index);

            } else if(otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;
                activity.onError(error.getMessage());
            } else if (otpResponse instanceof NetworkState2.Failure) {
                NetworkState2.Failure<String> error = (NetworkState2.Failure<String>) otpResponse;
                activity.onFailure(activity.findViewById(R.id.card_view), getString(R.string.request_error));

            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }
}
