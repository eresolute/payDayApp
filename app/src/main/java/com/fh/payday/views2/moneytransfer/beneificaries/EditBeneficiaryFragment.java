package com.fh.payday.views2.moneytransfer.beneificaries;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.moneytransfer.Beneficiary;
import com.fh.payday.datasource.models.moneytransfer.ui.EditBeneficiaryOptions;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.views.adapter.expandablelistadapter.EditBeneficiaryAdapter;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday.PaydayToPaydayBeneficiaryFragment;

import java.util.ArrayList;
import java.util.List;

public class EditBeneficiaryFragment extends Fragment {

    ExpandableListView elEditBeneficiary;
    EditBeneficiaryAdapter adapter;
    EditBeneficiaryActivity activity;
    private List<Beneficiary> list = new ArrayList<>();
    private List<Beneficiary> localList = new ArrayList<>();
    private List<Beneficiary> internationalList = new ArrayList<>();
    private List<Beneficiary> loanList = new ArrayList<>();
    private boolean isEnabled = true;

    private EditBeneficiaryActivity.OnBeneficiaryClick editListener = new EditBeneficiaryActivity.OnBeneficiaryClick() {
        @Override
        public void onBeneficiaryClickListener(int position, Beneficiary beneficiary) {
            activity.getViewModel().setBeneficiaryOption(EditBeneficiaryActivity.OPTION.EDIT);
            switch (position) {
                case 0:
                    activity.getViewModel().setSelectedBeneficiary(beneficiary);
                    activity.replaceFragment(PaydayToPaydayBeneficiaryFragment.newInstance());
                    break;
                case 1:
                    //activity.replaceFragment(PaydayToCreditCardBeneficiaryFragment.newInstance());
                    break;
                case 2:
                    //activity.replaceFragment(LocalBeneficiaryEditFragment.newInstance());
                    break;
         /*   case 3:
                activity.replaceFragment(InternationalBeneficiaryEditFragment.newInstance());
                break;*/

            }
        }

        @Override
        public void onBeneficiaryClickListener(int position, Beneficiary beneficiary, boolean enabled) {

        }
    };

    private EditBeneficiaryActivity.OnBeneficiaryClick deleteListener = new EditBeneficiaryActivity.OnBeneficiaryClick() {
        @Override
        public void onBeneficiaryClickListener(int position, Beneficiary beneficiary) {
            activity.getViewModel().setBeneficiaryOption(EditBeneficiaryActivity.OPTION.DELETE);
            switch (position) {
                case 0:
                    activity.getViewModel().setSelectedBeneficiary(beneficiary);
                    User user = UserPreferences.Companion.getInstance().getUser(activity);
                    if (user == null) return;
                    activity.getViewModel().generateDeleteOtp(user.getCustomerId());
                    break;
            }
        }

        @Override
        public void onBeneficiaryClickListener(int position, Beneficiary beneficiary, boolean enabled) {

        }
    };

    private EditBeneficiaryActivity.OnBeneficiaryClick isEnableListener = new EditBeneficiaryActivity.OnBeneficiaryClick() {
        @Override
        public void onBeneficiaryClickListener(int position, Beneficiary beneficiary) {
        }

        @Override
        public void onBeneficiaryClickListener(int position, Beneficiary beneficiary, boolean enabled) {
            activity.getViewModel().setBeneficiaryOption(EditBeneficiaryActivity.OPTION.ENABLE);
            switch (position) {
                case 0:
                    activity.getViewModel().setSelectedBeneficiary(beneficiary);
                    User user = UserPreferences.Companion.getInstance().getUser(activity);
                    if (user == null) return;
                    isEnabled = enabled;
                    activity.getViewModel().generateEnableOtp(user.getCustomerId());
                    break;
            }

        }
    };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (EditBeneficiaryActivity) context;

    }

    @Override
    public void onResume() {
        super.onResume();
        activity.setToolbarTitle(getString(R.string.edit_beneficiary));
    }

    public static EditBeneficiaryFragment newInstance() {
        return new EditBeneficiaryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_beneficiary_fragament, container, false);
        init(view);

        addPaydayBeneficiaryObserver();
        getBeneficiaries();

        return view;
    }

    public void init(View view) {
        elEditBeneficiary = view.findViewById(R.id.el_beneficiary_option);

        addOtpObserver();
        addEnableOtpObserver();
        deleteObserver();
        changeStatusObserver();
    }

    private void getBeneficiaries() {
        User user = UserPreferences.Companion.getInstance().getUser(activity);
        if (user == null) return;
        activity.getTransferViewModel().getP2PBeneficiaries(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
    }

    private void addPaydayBeneficiaryObserver() {
        activity.getTransferViewModel().getBeneficiaryState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<List<Beneficiary>> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (state instanceof NetworkState2.Success) {

                list = ((NetworkState2.Success<List<Beneficiary>>) state).getData();
                List<EditBeneficiaryOptions> options = DataGenerator.getEditBeneficiaryData(activity, list, localList, internationalList, loanList);
                adapter = new EditBeneficiaryAdapter(getContext(), options, editListener, deleteListener, isEnableListener);
                elEditBeneficiary.setAdapter(adapter);


            } else if (state instanceof NetworkState2.Error) {

                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                try {
                    int code = Integer.parseInt(error.getMessage());
                    updateCredentials(code);

                } catch (NumberFormatException e) {
                    activity.onError(error.getMessage());
                }
            } else if (state instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void updateCredentials(int code) {
        if (code == 399) {
            activity.onError("Your Emirates Id is expired or about to expire");
        }
    }

    private void addOtpObserver() {
        if (activity == null) return;
        activity.getViewModel().getDeleteOtpState().observe(this, event -> {

            if (event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();

            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (otpResponse instanceof NetworkState2.Success) {
                activity.replaceFragment(new OTPFragment.Builder(otp -> {
                    User user = UserPreferences.Companion.getInstance().getUser(activity);
                    if (user == null) return;
                    if (activity.getViewModel().getSelectedBeneficiary() == null) return;
                    if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.DELETE)
                        activity.getViewModel().deleteBeneficiary(user.getCustomerId(), activity.getViewModel().getSelectedBeneficiary().getBeneficiaryId(), otp);
                }).setTitle(getString(R.string.enter_otp))
                        .setPinLength(6)
                        .setHasCardView(false)
                        .setOnResumeListener(() -> activity.setToolbarTitle(getString(R.string.verify_otp)))
                        .build());
            } else if (otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;
                activity.onError(error.getMessage());
            } else if (otpResponse instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void addEnableOtpObserver() {
        if (activity == null) return;
        activity.getViewModel().getEnableOtpState().observe(this, event -> {

            if (event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();

            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (otpResponse instanceof NetworkState2.Success) {
                activity.replaceFragment(new OTPFragment.Builder(otp -> {
                    User user = UserPreferences.Companion.getInstance().getUser(activity);
                    if (user == null) return;
                    if (activity.getViewModel().getSelectedBeneficiary() == null) return;
                    if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.ENABLE)
                        activity.getViewModel().changeBeneficiaryStatus(user.getCustomerId(), activity.getViewModel().getSelectedBeneficiary().getBeneficiaryId(), isEnabled, otp);
                }).setTitle(getString(R.string.enter_otp))
                        .setPinLength(6)
                        .setHasCardView(false)
                        .setOnResumeListener(() -> activity.setToolbarTitle(getString(R.string.verify_otp)))
                        .build());
            } else if (otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;
                activity.onError(error.getMessage());
            } else if (otpResponse instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void deleteObserver() {
        if (activity == null) return;
        activity.getViewModel().getDeleteBeneficiaryState().observe(activity, event -> {

            if (event == null) return;

            NetworkState2<String> deleteBeneficiaryResponse = event.getContentIfNotHandled();

            if (deleteBeneficiaryResponse == null) return;

            if (deleteBeneficiaryResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (deleteBeneficiaryResponse instanceof NetworkState2.Success) {
                if (((NetworkState2.Success<String>) deleteBeneficiaryResponse).getData() == null)
                    return;
                String message = ((NetworkState2.Success<String>) deleteBeneficiaryResponse).getData();
                if (message != null && !TextUtils.isEmpty(message)) {
                    activity.showMessage(message,
                            R.drawable.ic_success_checked,
                            R.color.blue,
                            dialog -> {
                                dialog.dismiss();
                                activity.finish();
                            });
                }
            } else if (deleteBeneficiaryResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) deleteBeneficiaryResponse;
                activity.onError(error.getMessage());
            } else if (deleteBeneficiaryResponse instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void changeStatusObserver() {
        if (activity == null) return;
        activity.getViewModel().getChangeBeneficiaryState().observe(activity, event -> {

            if (event == null) return;

            NetworkState2<String> changeStatusResponse = event.getContentIfNotHandled();

            if (changeStatusResponse == null) return;

            if (changeStatusResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (changeStatusResponse instanceof NetworkState2.Success) {
                if (((NetworkState2.Success<String>) changeStatusResponse).getData() == null)
                    return;
                String message = ((NetworkState2.Success<String>) changeStatusResponse).getData();
                if (message != null && !TextUtils.isEmpty(message)) {
                    activity.showMessage(message,
                            R.drawable.ic_success_checked,
                            R.color.blue,
                            dialog -> {
                                dialog.dismiss();
                                activity.finish();
                            });
                }
            } else if (changeStatusResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) changeStatusResponse;
                activity.onError(error.getMessage());
            } else if (changeStatusResponse instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

}
