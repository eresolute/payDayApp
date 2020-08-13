package com.fh.payday.views2.servicerequest.activatecard;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.datasource.models.CustomerSummary;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.utilities.CardUtilsKt;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.ListUtilsKt;
import com.fh.payday.utilities.OnAtmPinConfirmListener;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.ServiceRequestViewModel;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views2.servicerequest.ServiceRequestOptionActivity;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import kotlin.Unit;

import static com.fh.payday.utilities.ConstantsKt.CARD_ACTIVE;

public class ActivateCardFragment extends Fragment implements OnOTPConfirmListener {

    private Switch toggleStatus;
    private TextView tvCardNumber, tvCardType, tvCardStatus;
    private ServiceRequestOptionActivity activity;
    private ServiceRequestViewModel viewModel;
    private ActivateCardPinFragment pinConfirmFragment;
    private ConstraintLayout constraintLayout;

    private OnAtmPinConfirmListener pinConfirmListener = pin -> {
        activity.setToolbarText(getString(R.string.verify_otp));

        User user = viewModel.getUser();

        if (user == null) return;
        if (viewModel.getAtmPin() != null && viewModel.getAtmPin().equals(pin)) {
            activity.getViewModel().generateOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
        } else {
            pinConfirmFragment.setConfirmError(getString(R.string.pin_does_not_match));
        }
    };
    private OnAtmPinConfirmListener pinListener = pin -> {
        if (TextUtils.isEmpty(pin) || viewModel.getUser() == null) return;
        viewModel.setAtmPin(pin);
    /*    pinConfirmFragment = new AtmCommonPinFragment.Builder(pinConfirmListener)
            .setPinLength(4)
            .setTitle(getString(R.string.confirm_pin))
            .setOnResumeListener(() -> activity.setToolbarText(getString(R.string.atm_pin)))
            .build();
        activity.replaceFragment(pinConfirmFragment);*/
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ServiceRequestOptionActivity) context;
        viewModel = activity.getViewModel();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.setToolbarText(getString(R.string.service_requests));
        getCards();

    }

    private void getCards() {
        activity.hideNoInternetView();
        if (activity!= null && !activity.isNetworkConnected()) {
            constraintLayout.setVisibility(View.GONE);
            activity.showNoInternetView(() -> {
                getCards();
                return Unit.INSTANCE;
            });
            return;
        }
        constraintLayout.setVisibility(View.VISIBLE);
        if (viewModel.getUser() != null) {
            if (viewModel.getSummary() == null) {
                viewModel.fetchCustomerSummary(viewModel.getUser().getToken(), viewModel.getUser().getSessionId(), viewModel.getUser().getRefreshToken(),
                    viewModel.getUser().getCustomerId());
            } else {
                handleCardInfo(viewModel.getSummary());
            }

            addSummaryObserver();
            addOtpObserver();
            addActivateObserver();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activate_card, container, false);
        init(view);

        toggleStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonView.setChecked(viewModel.getCardStatus());
            if (!viewModel.getCardStatus()) {
                buttonView.setChecked(false);
                pinConfirmFragment = new ActivateCardPinFragment.Builder(pinListener)
                    .setConfirmListener(pinConfirmListener)
                    .setPinLength(4)
                    .setTitle(getString(R.string.enter_atm_pin_salik))
                    .setConfirmTitle(getString(R.string.confirm_pin))
                    .setOnResumeListener(() -> activity.setToolbarText(getString(R.string.atm_pin)))
                    .build();
                activity.replaceFragment(pinConfirmFragment);
            }
        });

        return view;
    }

    private void init(View view) {
        constraintLayout = view.findViewById(R.id.root_view);
        toggleStatus = view.findViewById(R.id.toggle_status);
        tvCardNumber = view.findViewById(R.id.tv_card_number);
        tvCardType = view.findViewById(R.id.tv_card_name);
        tvCardStatus = view.findViewById(R.id.tv_card_status);
    }

    @Override
    public void onOtpConfirm(@NotNull String otp) {
        User user = viewModel.getUser();
        if (user == null || TextUtils.isEmpty(otp) || otp.length() < 6) return;

        byte[] bytes = getKeyBytes();
        if (bytes == null) return;

        activity.getViewModel().activateCard(user.getToken(), user.getSessionId(),
            user.getRefreshToken(), user.getCustomerId(), otp, bytes);
    }

    private byte[] getKeyBytes() {
        if (activity == null) return null;

        try (InputStream stream = activity.getAssets().open(ConstantsKt.PUBLIC_KEY)) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            while ((bytesRead = stream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            return output.toByteArray();
        } catch (IOException e) {
            return new byte[8192];
        }
    }

    private void addSummaryObserver() {
        viewModel.getCustomerSummaryState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<CustomerSummary> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (state instanceof NetworkState2.Success) {
                NetworkState2.Success<CustomerSummary> success = (NetworkState2.Success<CustomerSummary>) state;
                if (success.getData() != null) {
                    handleCardInfo(success.getData());
                }
            } else if (state instanceof NetworkState2.Error) {
                if (activity != null) {
                    NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                    if (error.isSessionExpired()) {
                        activity.onSessionExpired(error.getMessage());
                        return;
                    }

                    try {
                        int code = Integer.parseInt(error.getMessage());
                        updateCredentials(code);

                    } catch (NumberFormatException e) {
                        activity.onError(error.getMessage());
                    }
                }
            } else if (state instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view),  ((NetworkState2.Failure<CustomerSummary>) state).getThrowable());
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void handleCardInfo(CustomerSummary summary) {
        if (ListUtilsKt.isEmptyList(summary.getCards())) return;

        Card card = summary.getCards().get(0);
        tvCardType.setText(card.getCardType());
        tvCardNumber.setText(CardUtilsKt.maskCardNumber(card.getCardNumber(), "xxxx xxxx xxxx ####"));
        tvCardStatus.setText(card.getCardStatus());
        toggleStatus.setChecked(CARD_ACTIVE.equalsIgnoreCase(card.getCardStatus()));

        /*if (STATUS_ACTIVE.equalsIgnoreCase(card.getCardStatus())) {
            toggleStatus.setChecked(true);
            toggleStatus.setEnabled(false);
        } else {
            toggleStatus.setChecked(false);
        }*/
    }

    private void addOtpObserver() {
        activity.getViewModel().getOtpRequestState().observe(activity, otp -> {
            if (otp == null) return;

            NetworkState2<String> state = otp.getContentIfNotHandled();

            if (state == null) return;
            if (state instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (state instanceof NetworkState2.Success) {
                activity.replaceFragment(new OTPFragment.Builder(this)
                        .setTitle(getString(R.string.enter_otp))
                        .setButtonTitle(getString(R.string.submit))
                        .setPinLength(6)
                        .setInstructions(getString(R.string.otp_sent_to_registered_number))
                        .setHasCardView(false)
                        .build());
            } else if (state instanceof NetworkState2.Error) {
                if (activity != null) {
                    NetworkState2.Error<String> error = (NetworkState2.Error<String>) state;
                    if (error.isSessionExpired()) {
                        activity.onSessionExpired(error.getMessage());
                        return;
                    }
                    activity.onError(error.getMessage());
                }
            } else if (state instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view),  ((NetworkState2.Failure<String>) state).getThrowable());
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void addActivateObserver() {
        viewModel.getActivateCardState().observe(activity, event -> {
            if (event == null) return;

            NetworkState2<String> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (state instanceof NetworkState2.Success) {
                NetworkState2.Success<String> success = (NetworkState2.Success<String>) state;

                if (success.getData() != null) {
                    activity.showMessage(success.getData(), R.drawable.ic_success_checked, R.color.colorAccent,
                        dialog -> {
                            try {
                                dialog.dismiss();
                                activity.finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                }
            } else if (state instanceof NetworkState2.Error) {
                if (activity != null) {
                    NetworkState2.Error<String> error = (NetworkState2.Error<String>) state;
                    if (error.isSessionExpired()) {
                        activity.onSessionExpired(error.getMessage());
                        return;
                    }
                    activity.onError(error.getMessage());
                }
            } else if (state instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view), ((NetworkState2.Failure<String>) state).getThrowable());
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
}
