package com.fh.payday.views2.payments.transport;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.payments.Beneficiaries;
import com.fh.payday.datasource.models.payments.ServiceCategory;
import com.fh.payday.datasource.models.payments.transport.MawaqifBillDetail;
import com.fh.payday.datasource.models.payments.transport.Operator;
import com.fh.payday.datasource.models.payments.transport.OperatorDetails;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.TextViewUtilsKt;
import com.fh.payday.viewmodels.Add2BeneficiaryViewModel;
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog;
import com.fh.payday.views2.payments.BeneficiaryBottomSheet;
import com.fh.payday.views2.shared.activity.Add2BeneficiaryActivity;

import java.util.ArrayList;
import java.util.List;

import static com.fh.payday.utilities.ConstantsKt.CONNECTION_ERROR;
import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.setErrorMessage;
import static com.fh.payday.views2.shared.activity.Add2BeneficiaryActivityKt.REQUEST_CODE_ADD_BENEFICIARY;

public class TransportPaymentNumberFragment extends Fragment {
    private TransportActivity activity;
    private TextInputLayout textInputLayout;
    private TextInputEditText etAccountNo;
    private Button btnNext;
    private String operator;
    private String accessKey;
    private int typeId;
    private User user;

    private TextView tvBeneficiary;
    private TextView tvAdd2Beneficiary;
    private ArrayList<Beneficiaries> beneficiariesList = new ArrayList<>();
    private Add2BeneficiaryViewModel beneficiaryViewModel;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (TransportActivity) context;
        beneficiaryViewModel = ViewModelProviders.of(this).get(Add2BeneficiaryViewModel.class);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (activity == null) return;
        if (activity.getTransportViewModel() == null) return;
        if (isVisibleToUser && activity.getTransportViewModel().getDataClear()) {
            etAccountNo.setText(null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transport_payment_no, container, false);
        init(view);
        operator = "mawaqif";
        typeId = 1;
        user = UserPreferences.Companion.getInstance().getUser(activity);
        addObserver();
        addDetailObserver();
        addBillStateObserver();
        addTextWatcher();
        btnNext.setOnClickListener(v -> {
            if (etAccountNo.getText() != null && validateEditText(etAccountNo.getText().toString())) {

                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.card_view), getString(R.string.no_internet_connectivity));
                    return;
                }
                activity.getTransportViewModel().setAccountNumber(etAccountNo.getText().toString());
                activity.getTransportViewModel().operatorRequest(user.getToken(), user.getSessionId(),
                        user.getRefreshToken(), user.getCustomerId(),typeId, "971");
            }
        });

        if (activity != null && typeId > 0 && operator != null) {
            activity.getTransportViewModel().getBeneficiaryAccounts(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                    user.getCustomerId(), typeId, operator);
            addBeneficiariesObserver();
        }

        return view;
    }

    private void addObserver() {

        activity.getTransportViewModel().getOperatorState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<List<Operator>> operatorState = event.getContentIfNotHandled();

            if (operatorState == null) return;

            if (operatorState instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));

                return;
            }
            btnNext.setEnabled(false);
            //activity.hideProgress();
            if (operatorState instanceof NetworkState2.Success) {
                List<Operator> data = ((NetworkState2.Success<List<Operator>>) operatorState).getData();
                if (data != null)
                    for (int i = 0; i < data.size(); i++) {
                        if (data.get(i).getServiceProvider().equalsIgnoreCase(operator)) {
                            activity.getTransportViewModel().setOperator(operator);
                            accessKey = data.get(i).getAccessKey();
                            activity.getTransportViewModel().operatorDetails(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                                    user.getCustomerId(), data.get(i).getAccessKey(), typeId, "flexi", "971");
                            break;
                        }
                    }
            } else if (operatorState instanceof NetworkState2.Error) {
                activity.hideProgress();
                btnNext.setEnabled(true);
                NetworkState2.Error<List<Operator>> error = (NetworkState2.Error<List<Operator>>) operatorState;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
//                activity.onError(error.getMessage());
                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());

            } else if (operatorState instanceof NetworkState2.Failure) {
                activity.hideProgress();
                btnNext.setEnabled(true);
                NetworkState2.Failure<List<Operator>> error = (NetworkState2.Failure<List<Operator>>) operatorState;
                activity.onFailure(activity.findViewById(R.id.card_view), error.getThrowable());
            } else {
                activity.hideProgress();
                btnNext.setEnabled(true);
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void addDetailObserver() {

        activity.getTransportViewModel().getOperatorDetailState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<OperatorDetails> operatorDetailState = event.getContentIfNotHandled();

            if (operatorDetailState == null) return;

            if (operatorDetailState instanceof NetworkState2.Loading) {
                //activity.showProgress("Processing");
                return;
            }

            if (operatorDetailState instanceof NetworkState2.Success) {

                OperatorDetails data = ((NetworkState2.Success<OperatorDetails>) operatorDetailState).getData();

                if (data != null && etAccountNo.getText() != null && operator.equalsIgnoreCase("mawaqif")) {
                    activity.getTransportViewModel().mawaqifBill(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                            user.getCustomerId(), accessKey, "getBalance", typeId, etAccountNo.getText().toString(),
                            data.getFlexiKey(), Integer.parseInt(data.getTypeKey()));
                } else {
                    activity.hideProgress();
                    btnNext.setEnabled(true);
                }

            } else if (operatorDetailState instanceof NetworkState2.Error) {
                activity.hideProgress();
                btnNext.setEnabled(true);
                NetworkState2.Error<OperatorDetails> error = (NetworkState2.Error<OperatorDetails>) operatorDetailState;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
//                activity.onError(error.getMessage());
                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());
            } else if (operatorDetailState instanceof NetworkState2.Failure) {
                activity.hideProgress();
                btnNext.setEnabled(true);
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            } else {
                activity.hideProgress();
                btnNext.setEnabled(true);
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }

        });
    }

    private void addBillStateObserver() {

        activity.getTransportViewModel().getMawaqifBillState().observe(this, event -> {
            if (event == null) {
                return;
            }
            NetworkState2<MawaqifBillDetail> billState = event.getContentIfNotHandled();

            if (billState == null) {
                return;
            }

            if (billState instanceof NetworkState2.Loading) {
                btnNext.setEnabled(false);
                return;
            }

            activity.hideProgress();
            btnNext.setEnabled(true);

            if (billState instanceof NetworkState2.Success) {

                MawaqifBillDetail data = ((NetworkState2.Success<MawaqifBillDetail>) billState).getData();
                if (data != null) {

                    if (data.getPvtAmount() != null && etAccountNo.getText() != null) {
                        activity.getTransportViewModel().setAccountNumber(etAccountNo.getText().toString());
                        activity.onPaymentNumber();
                    } else {
                        activity.onError(getString(R.string.generic_payment_error));
                    }
                }
            } else if (billState instanceof NetworkState2.Error) {
                NetworkState2.Error<MawaqifBillDetail> error = (NetworkState2.Error<MawaqifBillDetail>) billState;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
//                activity.onError(error.getMessage());
                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());
            } else if (billState instanceof NetworkState2.Failure) {
                NetworkState2.Failure<MawaqifBillDetail> error = (NetworkState2.Failure<MawaqifBillDetail>) billState;
                activity.onFailure(activity.findViewById(R.id.card_view),error.getThrowable() );
            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void init(View view) {
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.enter_pvt_number);
        etAccountNo = view.findViewById(R.id.et_account_number);
        textInputLayout = view.findViewById(R.id.textInputLayout);
        btnNext = view.findViewById(R.id.btn_next);
        tvBeneficiary = view.findViewById(R.id.tv_beneficiary);
        tvAdd2Beneficiary = view.findViewById(R.id.tv_add_to_beneficiary);
    }

    private boolean validateEditText(String accountNo) {
        if (TextUtils.isEmpty(accountNo)) {
            setErrorMessage(textInputLayout,getString(R.string.please_enter_pvt_no));
            etAccountNo.requestFocus();
            return false;
        } else if (accountNo.length() < 8) {
            setErrorMessage(textInputLayout, getString(R.string.invalid_pvt_no));
            etAccountNo.requestFocus();
            return false;
        } else {
            clearErrorMessage(textInputLayout);
            return true;
        }
    }

    private void addTextWatcher() {
        etAccountNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //activity.getViewModel().setMobileNumber(String.valueOf(s));
                clearErrorMessage(textInputLayout);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 4) {
                    filterAccountNo(s.toString());
                    if (activity.getTransportViewModel().getSelectedAccessKey() == null)
                        tvAdd2Beneficiary.setVisibility(View.GONE);
                } else {
                    tvAdd2Beneficiary.setVisibility(View.GONE);
                    if (beneficiariesList != null && beneficiariesList.size() > 0) {
                        tvBeneficiary.setVisibility(View.VISIBLE);
                        if (user != null) {
                            activity.getTransportViewModel().getBeneficiaryAccounts(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                                    user.getCustomerId(), typeId, operator);
                        }
                    }

                }

            }
        });
    }
    private void filterAccountNo(String text) {
        int status = 0;
        for (Beneficiaries item : beneficiariesList) {
            boolean isNumberFound = item.getMobileNumber().contains(text);
            if (isNumberFound) {
                status = 2;
                break;
            } else status = 1;
        }
        if (status == 1 || status == 0 || beneficiariesList == null || beneficiariesList.size() < 1) {
            tvBeneficiary.setVisibility(View.GONE);
            tvAdd2Beneficiary.setVisibility(View.VISIBLE);
            TextViewUtilsKt.setTextWithUnderLine(tvAdd2Beneficiary, tvAdd2Beneficiary.getText().toString());
            Intent mIntent = new Intent(activity, Add2BeneficiaryActivity.class);
            tvAdd2Beneficiary.setOnClickListener(v -> {
                if (validateEditText(etAccountNo.getText().toString().trim())) {
                    int len = etAccountNo.getText().toString().length();
                    String accessKey = activity.getTransportViewModel().getSelectedAccessKey();
                    mIntent.putExtra("mobileNo", etAccountNo.getText().toString().trim());
                    mIntent.putExtra("access_key", accessKey);
                    mIntent.putExtra("number_len", len);
                    mIntent.putExtra("category", "pvt");
                    mIntent.putExtra("issue", ServiceCategory.BILL_PAYMENT);
                    startActivityForResult(mIntent, REQUEST_CODE_ADD_BENEFICIARY);
                }
            });

        } else {
            tvBeneficiary.setVisibility(View.VISIBLE);
            tvAdd2Beneficiary.setVisibility(View.GONE);
        }
    }

    private void addBeneficiariesObserver() {

        TransportActivity activity = this.activity;
        if (activity == null) return;

        activity.getTransportViewModel().getBeneficiariesState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<ArrayList<Beneficiaries>> state = event.peekContent();
            if (state == null) return;

            tvBeneficiary.setVisibility(View.GONE);
            tvAdd2Beneficiary.setVisibility(View.GONE);

            if (state instanceof NetworkState2.Success) {
                ArrayList<Beneficiaries> data = ((NetworkState2.Success<ArrayList<Beneficiaries>>) state).getData();
                onSuccess(data);
            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error error = ((NetworkState2.Error) state);
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(((NetworkState2.Error<ArrayList<Beneficiaries>>) state).getMessage());
                }

                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());
            }
        });
    }

    private void onSuccess(@Nullable ArrayList<Beneficiaries> beneficiaries) {
        if (beneficiaries == null || beneficiaries.size() < 1) return;
        beneficiariesList = beneficiaries;
        tvBeneficiary.setVisibility(View.VISIBLE);
        tvAdd2Beneficiary.setVisibility(View.GONE);
        TextViewUtilsKt.setTextWithUnderLine(tvBeneficiary, tvBeneficiary.getText().toString());
        tvBeneficiary.setOnClickListener(v -> {
            BeneficiaryBottomSheet bottomSheet = BeneficiaryBottomSheet.Companion
                    .newInstance(beneficiariesList, getLogo(operator), (mobileNo, s2, s3) -> {
                        etAccountNo.setText(mobileNo);
                        return null;
                    });
            bottomSheet.attachPopUpListener((beneficiary, view) -> {
                PopupMenu menu = showPopUpMenu(beneficiary, view);
                menu.show();

                menu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.edit:
                            bottomSheet.dismiss();
                            Intent mIntent = new Intent(activity, Add2BeneficiaryActivity.class);
                            int len = etAccountNo.getText().toString().length();
                            mIntent.putExtra("beneficiary_data", beneficiary);
                            mIntent.putExtra("action", "edit");
                            mIntent.putExtra("number_len", len);
                            mIntent.putExtra("category", "pvt");
                            startActivityForResult(mIntent, REQUEST_CODE_ADD_BENEFICIARY);
                            return true;
                        case R.id.delete:
                            bottomSheet.dismiss();
                            addUpdateObserver();
                            showDeleteConfirmation(beneficiary);
                            return true;
                        case R.id.enable:
                            bottomSheet.dismiss();
                            addUpdateObserver();
                            showEnableConfirmation(beneficiary);
                            return true;
                        default:
                            return false;
                    }
                });
                return null;
            });

            bottomSheet.show(activity.getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    private PopupMenu showPopUpMenu(Beneficiaries beneficiary, View view) {
        PopupMenu menu = new PopupMenu(activity, view);
        menu.inflate(R.menu.menu_beneficiary);

        if (beneficiary.getEnabled())
            menu.getMenu().getItem(2).setTitle(R.string.disable);
        else
            menu.getMenu().getItem(2).setTitle(R.string.enable);

        return menu;
    }

    private void showDeleteConfirmation(Beneficiaries beneficiary) {
        TermsAndConditionsDialog termsAndCondDialog = new TermsAndConditionsDialog.Builder()
                .setMessage(String.format(getString(R.string.delele_beneficiary_confirmation), beneficiary.getShortName()))
                .setMessageGravity(Gravity.CENTER)
                .setPositiveText(getString(R.string.delete))
                .setNegativeText(getString(R.string.cancel))
                .attachPositiveListener(() -> {
                    User user = UserPreferences.Companion.getInstance().getUser(requireContext());
                    if (user == null) return null;

                    beneficiaryViewModel.deleteBeneficiary(user.getToken(), user.getSessionId(),
                            user.getRefreshToken(), user.getCustomerId(), Long.parseLong(beneficiary.getBeneficiaryId().toString()));
                    return null;
                }).build();

        termsAndCondDialog.setCancelable(false);
        termsAndCondDialog.show(getFragmentManager(), termsAndCondDialog.getTag());
    }

    private void showEnableConfirmation(Beneficiaries beneficiary) {
        String message, positiveLabel;
        if (beneficiary.getEnabled()) {
            message = String.format(getString(R.string.enable_beneficiary_confirmation), getString(R.string.disable), beneficiary.getShortName());
        } else {
            message = String.format(getString(R.string.enable_beneficiary_confirmation), getString(R.string.enable), beneficiary.getShortName());
        }

        if (beneficiary.getEnabled())
            positiveLabel = getString(R.string.disable);
        else
            positiveLabel = getString(R.string.enable);

        TermsAndConditionsDialog termsAndCondDialog = new TermsAndConditionsDialog.Builder()
                .setMessage(message)
                .setMessageGravity(Gravity.CENTER)
                .setPositiveText(positiveLabel)
                .setNegativeText(getString(R.string.cancel))
                .attachPositiveListener(() -> {
                    User user = UserPreferences.Companion.getInstance().getUser(requireContext());
                    if (user == null) return null;

                    beneficiaryViewModel.enableBeneficiary(user.getToken(), user.getSessionId(),
                            user.getRefreshToken(), user.getCustomerId(), Long.parseLong(beneficiary.getBeneficiaryId().toString()),
                            !beneficiary.getEnabled());
                    return null;
                }).build();

        termsAndCondDialog.setCancelable(false);
        termsAndCondDialog.show(getFragmentManager(), termsAndCondDialog.getTag());
    }

    @DrawableRes
    private int getLogo(@Nullable  String operator) {
        if (operator == null) return R.drawable.ic_operator;

        switch (operator.toLowerCase()) {
            case "mawaqif": {
                return R.drawable.ic_mawaqif;
            }
            default: return R.drawable.ic_operator;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            tvAdd2Beneficiary.setVisibility(View.GONE);
            tvBeneficiary.setVisibility(View.VISIBLE);
            if (user != null) {
                activity.getTransportViewModel().getBeneficiaryAccounts(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                        user.getCustomerId(), typeId, operator);
            }
        }
    }

    private void addUpdateObserver() {
        beneficiaryViewModel.getDeleteBeneficiaryState().observe(activity, event -> {
            if(event == null) return;

            NetworkState2<String> state = event.getContentIfNotHandled();
            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (state instanceof NetworkState2.Success) {
                String message = ((NetworkState2.Success<String>) state).getData();
                if (message == null) return;
                if (user != null) {
                    activity.getTransportViewModel().getBeneficiaryAccounts(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                            user.getCustomerId(), typeId, operator);
                }
                activity.showMessage(message);
            } else if(state instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error =(NetworkState2.Error<String>) state;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }

                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());

            } else if (state instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.card_view), ((NetworkState2.Failure<String>) state).getThrowable());
            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), CONNECTION_ERROR);
            }

        });

        beneficiaryViewModel.getEnableBeneficiaryState().observe(activity, event -> {
            if(event == null) return;

            NetworkState2<String> state = event.getContentIfNotHandled();
            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (state instanceof NetworkState2.Success) {
                String message = ((NetworkState2.Success<String>) state).getData();
                if (message == null) return;
                activity.showMessage(message);
                if (user != null) {
                    activity.getTransportViewModel().getBeneficiaryAccounts(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                            user.getCustomerId(), typeId, operator);
                }
            } else if(state instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error =(NetworkState2.Error<String>) state;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }

                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());

            } else if (state instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.card_view), ((NetworkState2.Failure<String>) state).getThrowable());
            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), CONNECTION_ERROR);
            }

        });
    }

}
