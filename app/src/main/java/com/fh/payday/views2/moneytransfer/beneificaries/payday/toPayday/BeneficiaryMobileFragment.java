package com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.moneytransfer.PaydayBeneficiary;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.MobileNoValidator;
import com.fh.payday.utilities.PhoneUtils;
import com.fh.payday.viewmodels.SharedViewModel;

public class BeneficiaryMobileFragment extends Fragment {

    SharedViewModel model;
    int index;
    EditText etMobileNumber;
    TextView tvError;
    ConstraintLayout layout;

    PaydayBeneficiaryActivity activity;
    private final static int CONTACT_PICKER_RESULT = 100;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (PaydayBeneficiaryActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addObserver();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beneficiary_mobile, container, false);
        init(view);

        view.findViewById(R.id.img_contact).setOnClickListener(v -> doLaunchContactPicker());


        etMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_blue_rounded_border));
                tvError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        return view;
    }

    private void init(View view) {
        etMobileNumber = view.findViewById(R.id.et_mobile_number);
        tvError = view.findViewById(R.id.tv_error);
        layout = view.findViewById(R.id.constraintLayout);

        if (activity.getViewModel().getSelectedBeneficiary() != null) {
            etMobileNumber.setText(activity.getViewModel().getSelectedBeneficiary().getCardAccountNo());
        }

        view.findViewById(R.id.img_next).setOnClickListener(v -> {
            activity.hideKeyboard();
            if (!TextUtils.isEmpty(etMobileNumber.getText().toString())
                    && etMobileNumber.getText().toString().length() == 10
                    && MobileNoValidator.Companion.validate(etMobileNumber.getText().toString())) {

                User user = UserPreferences.Companion.getInstance().getUser(activity);
                if (user == null) return;


                activity.getViewModel().getPaydayBeneficiary(user.getCustomerId(), etMobileNumber.getText().toString());

            } else {
                layout.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_border_error));
                tvError.setVisibility(View.VISIBLE);
            }
        });
        view.findViewById(R.id.img_previous).setOnClickListener(v -> activity.onBackPressed());

    }


    private void addObserver() {
        activity.getViewModel().getBeneficiary().observe(this, event -> {
            if (event == null) return;

            NetworkState2<PaydayBeneficiary> response = event.getContentIfNotHandled();

            if (response == null) return;

            if (response instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (response instanceof NetworkState2.Success) {
                PaydayBeneficiary beneficiary = ((NetworkState2.Success<PaydayBeneficiary>) response).getData();
                beneficiary.setMobileNumber(etMobileNumber.getText().toString());
                activity.getViewModel().setPaydayBeneficiary(beneficiary);
                activity.getViewModel().getPaydayBeneficiaryMap().put("Card Number", etMobileNumber.getText().toString());
                activity.navigateUp();
            } else if (response instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) response;
                activity.onError(error.getMessage());
            } else if (response instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> error = (NetworkState2.Failure<?>) response;
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), new Throwable(ConstantsKt.CONNECTION_ERROR));
            }
        });
    }

    public void doLaunchContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, CONTACT_PICKER_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CONTACT_PICKER_RESULT) {

                Uri contactData = null;
                if (data != null) {
                    contactData = data.getData();
                }
                Cursor cursor = null;
                if (contactData != null) {
                    cursor = activity.getContentResolver().query(contactData, null, null, null, null);
                }
                if (cursor.getCount() > 0) {

                    cursor.moveToFirst();

                    int hasPhone = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                    if (hasPhone > 0) {
                        String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        etMobileNumber.setText(PhoneUtils.Companion.extractNumber(number));
                    }
                } else {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_mobile_no));
                }
                cursor.moveToFirst();

            }
        }
    }

}



