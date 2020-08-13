package com.fh.payday.views2.settings.update_card_pin;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.datasource.models.ResetPin;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.PinResetViewModel;
import com.fh.payday.viewmodels.SettingOptionViewModel;
import com.fh.payday.views.fragments.AlertDialogFragment;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views2.settings.SettingOptionActivity;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class EditCardPinFragment extends Fragment implements OnOTPConfirmListener, AlertDialogFragment.OnConfirmListener {

    SlidePagerAdapter adapter;
    SettingOptionActivity activity;
    NonSwipeableViewPager viewPager;
    SettingOptionViewModel viewModel;
    private static final int NUM_PAGES = 3;
    PinResetViewModel pinResetViewModel;
    int index = 0;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(activity).get(SettingOptionViewModel.class);
        pinResetViewModel = ViewModelProviders.of(activity).get(PinResetViewModel.class);

        if (getArguments() == null) return;
        index = getArguments().getInt("index");

        viewModel.getSelected().observe(this, index -> viewPager.setCurrentItem(index));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_edit_card_pin_fragment, container, false);
        init(view);

        viewPager.setAdapter(adapter);

        addObservers();

        return view;
    }

    private void init(View view) {
        viewPager = view.findViewById(R.id.view_pager);
        adapter = new SlidePagerAdapter(getFragmentManager(), this);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                    case 1:
                        if (index == 1) {
                            activity.setToolbarTitle(getString(R.string.set_pin));
                        } else if (index == 12) {
                            activity.setToolbarTitle(getString(R.string.service_requests));
                        }
                        break;
                    case 2:
                        activity.setToolbarTitle(getString(R.string.verify_otp));
                        break;
                    default:
                        throw new IllegalStateException("Invalid page position");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onOtpConfirm(@NotNull String otp) {

        User user = UserPreferences.Companion.getInstance().getUser(activity);
        if (user == null) return;

        try (InputStream stream = activity.getAssets().open(ConstantsKt.PUBLIC_KEY)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((bytesRead = stream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            byte[] byteArray = output.toByteArray();
            if (activity.getPinResetViewModel().getPin().getValue() == null || activity.getPinResetViewModel().getOldPin().getValue() == null)
                return;
            pinResetViewModel.changePin(user.getToken(), user.getSessionId(), user.getRefreshToken(), byteArray, user.getCustomerId(),
                activity.getPinResetViewModel().getPin().getValue(), activity.getPinResetViewModel().getOldPin().getValue(), otp);
        } catch (IOException e) {
            activity.showMessage(getString(R.string.something_went_wrong));
        }
    }

    @Override
    public void onConfirm(Dialog dialog) {
        activity.finish();
    }

    private static class SlidePagerAdapter extends FragmentStatePagerAdapter {

        private WeakReference<EditCardPinFragment> weakReference;

        SlidePagerAdapter(FragmentManager fm, EditCardPinFragment context) {
            super(fm);
            this.weakReference = new WeakReference<>(context);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new OldCardPinFragment();
                case 1:
                    return new ChangeCardPinFragment();
              /*  case 2:
                    return new ConfirmCardPinFragment();*/
                case 2:
                    return new OTPFragment.Builder(weakReference.get())
                        .setPinLength(6)
                        .setTitle(weakReference.get().getString(R.string.enter_otp))
                        .setInstructions(weakReference.get().getString(R.string.otp_sent_to_registered_number))
                        .setHasCardView(false)
                        .setButtonTitle(weakReference.get().getString(R.string.submit))
                        .build();

                default:
                    throw new IllegalStateException("Invalid page position");
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    private void addObservers() {
        pinResetViewModel.getPinResponseState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<ResetPin> pinResponse = event.getContentIfNotHandled();


            if (pinResponse == null) return;

            if (pinResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }
            activity.hideProgress();

            if (pinResponse instanceof NetworkState2.Success) {
                String secretKey = ((NetworkState2.Success<ResetPin>) pinResponse).getData().getSecret();

                User user = UserPreferences.Companion.getInstance().getUser(activity);
                if (user == null) return;

                if (user.getToken().equals(secretKey)) {
                    activity.showMessage(getString(R.string.pin_changed),
                            R.drawable.ic_success_checked,
                            R.color.blue,
                            dialog -> {
                                dialog.dismiss();
                                activity.finish();
                            }
                    );
                } else {
                    activity.showMessage(getString(R.string.internal_server_error),
                        R.drawable.ic_error,
                        R.color.colorError,
                        dialog -> {
                            dialog.dismiss();
                            activity.finish();
                        }
                    );
                }
            } else if (pinResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<ResetPin> error = (NetworkState2.Error<ResetPin>) pinResponse;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());
            } else if (pinResponse instanceof NetworkState2.Failure) {
                NetworkState2.Failure<ResetPin> error = (NetworkState2.Failure<ResetPin>) pinResponse;
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }
}
