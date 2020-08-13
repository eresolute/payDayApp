package com.fh.payday.views2.settings.update_card_pin;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.viewmodels.SettingOptionViewModel;
import com.fh.payday.views2.settings.SettingOptionActivity;

import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.setErrorMessage;

public class OldCardPinFragment extends Fragment {

    MaterialButton btnConfirm;
    SettingOptionViewModel viewModel;
    TextInputEditText etPin;
    TextInputLayout textInputLayout;
    TextView titleConfirmPin;
    TextInputLayout textInputLayout1;

    private SettingOptionActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SettingOptionActivity) context;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && etPin != null) {
            etPin.setText(null);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(SettingOptionViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_change_card_pin, container, false);
        init(view);

        btnConfirm.setOnClickListener(v -> {
            if (TextUtils.isEmpty(etPin.getText().toString().trim()) ||
                    etPin.getText().toString().trim().length() < 4 ||
                    !etPin.getText().toString().trim().matches("^[0-9]{4}$")) {

                setErrorMessage(textInputLayout, getString(R.string.please_enter_pin));
                etPin.requestFocus();
            } else {
                if (activity == null) return;
                activity.getPinResetViewModel().getOldPin().setValue(etPin.getText().toString());
                viewModel.setSelected(1);
            }
        });
        etPin.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                clearErrorMessage(textInputLayout);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return view;
    }

    private void init(View view) {
        etPin = view.findViewById(R.id.et_pin);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        textInputLayout = view.findViewById(R.id.textInputLayout);
        ((TextView)view.findViewById(R.id.title)).setText(getString(R.string.enter_old_pin));
        titleConfirmPin = view.findViewById(R.id.title_confirm_pin);
        textInputLayout1 = view.findViewById(R.id.textInputLayout1);
        titleConfirmPin.setVisibility(View.GONE);
        textInputLayout1.setVisibility(View.GONE);
    }
}
