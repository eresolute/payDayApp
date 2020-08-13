package com.fh.payday.views2.settings.update_password;

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

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.PasswordValidator;
import com.fh.payday.viewmodels.SettingOptionViewModel;
import com.fh.payday.views2.settings.SettingOptionActivity;

import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.setErrorMessage;

public class ChangePasswordFragment extends Fragment {

    SettingOptionViewModel viewModel;
    MaterialButton btnConfirm;
    SettingOptionActivity activity;
    private TextInputLayout textInputLayout;
    private TextInputEditText etNewPassword;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SettingOptionActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && etNewPassword != null) {
            etNewPassword.setText(null);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(activity).get(SettingOptionViewModel.class);
        viewModel.setUser(UserPreferences.Companion.getInstance().getUser(requireContext()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_change_password_fragment, container, false);
        init(view);

        etNewPassword.addTextChangedListener(new TextWatcher() {
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
        btnConfirm.setOnClickListener(view1 -> {
            if (validateEditText()) {
                String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
                activity.getChangePasswordViewModel().setNewPassword(newPassword);
                etNewPassword.setText(null);
                viewModel.setSelected(2);
            }
        });

        return view;
    }

    private void init(View view) {
        btnConfirm = view.findViewById(R.id.btn_confirm);
        textInputLayout = view.findViewById(R.id.textInputLayout);
        etNewPassword = view.findViewById(R.id.et_new_password);
    }

    private boolean validateEditText() {
        User user = viewModel.getUser();
        if (user == null) return false;

        String oldPassword = activity.getChangePasswordViewModel().getOldPassword();
        if (etNewPassword.getText() != null && TextUtils.isEmpty(etNewPassword.getText().toString().trim())) {
            etNewPassword.requestFocus();
            setErrorMessage(textInputLayout, getString(R.string.empty_password));
            return false;
        } else if (etNewPassword.getText() != null && !PasswordValidator.Companion
                .validate(etNewPassword.getText().toString().trim())) {
            setErrorMessage(textInputLayout, getString(R.string.password_pattern_info));
            etNewPassword.requestFocus();
            return false;
        }else if (etNewPassword.getText() != null && etNewPassword.getText().toString().trim().equals(oldPassword)) {
            setErrorMessage(textInputLayout, getString(R.string.password_not_same_as_old));
            etNewPassword.requestFocus();
            return false;
        } else if (etNewPassword.getText() != null && etNewPassword.getText().toString()
            .contains(user.getUsername().replaceAll("\\d", ""))) {
            setErrorMessage(textInputLayout, getString(R.string.password_contains_userid));
            etNewPassword.requestFocus();
            return false;
        } else {
            clearErrorMessage(textInputLayout);
            return true;
        }
    }
}
