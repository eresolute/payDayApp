package com.fh.payday.views2.settings.update_password;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.BiometricPreferences;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.SettingOptionViewModel;
import com.fh.payday.views.fragments.AlertDialogFragment;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views2.settings.SettingOptionActivity;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class EditPasswordFragment extends Fragment implements OnOTPConfirmListener, AlertDialogFragment.OnConfirmListener {

    SlidePagerAdapter adapter;
    NonSwipeableViewPager viewPager;
    SettingOptionViewModel viewModel;
    private static final int NUM_PAGES = 2;

    private SettingOptionActivity activity;

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

//    @Override
//    public void onDestroyView() {
//        if (viewPager.getCurrentItem() == 0) {
//            super.onDestroyView();
//            activity = null;
//        } else {
//            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
//        }
//    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(activity).get(SettingOptionViewModel.class);
        viewModel.getSelected().observe(this, index -> {
            if (index != null)
                viewPager.setCurrentItem(index);
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_edit_password_fragment, container, false);
        init(view);
        addObservers();
        adapter = new SlidePagerAdapter(getFragmentManager(), this);

        viewPager.setAdapter(adapter);

        return view;
    }

    private void init(View view) {
        viewPager = view.findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        activity.setToolbarTitle(getString(R.string.change_password));
                        break;
                   /* case 1:
                        activity.setToolbarTitle(getString(R.string.change_password));
                        break;
                    case 2:
                        activity.setToolbarTitle(getString(R.string.change_password));
                        break;*/
                    case 1:
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
        String oldPassword = activity.getChangePasswordViewModel().getOldPassword();
        String newPassword = activity.getChangePasswordViewModel().getNewPassword();
        User user = activity.getChangePasswordViewModel().getUser();

        if (user != null && oldPassword != null && newPassword != null) {
            activity.getChangePasswordViewModel().updatePassword(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                user.getCustomerId(),  oldPassword, newPassword, otp);
        }
    }

    @Override
    public void onConfirm(Dialog dialog) {
        activity.logout();
    }

    private static class SlidePagerAdapter extends FragmentStatePagerAdapter {

        private WeakReference<EditPasswordFragment> weakReference;

        SlidePagerAdapter(FragmentManager fm, EditPasswordFragment context) {
            super(fm);
            this.weakReference = new WeakReference<>(context);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CurrentPasswordFragment();
              /*  case 1:
                    return new ChangePasswordFragment();
                case 2:
                    return new ConfirmPasswordFragment();*/
                case 1:
                    return new OTPFragment.Builder(weakReference.get())
                        .setPinLength(6)
                        .setTitle(weakReference.get().getString(R.string.enter_otp))
                        .setHasCardView(false)
                        .setInstructions(weakReference.get().getString(R.string.otp_sent_to_registered_number))
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

        activity.getChangePasswordViewModel().getUpdatePasswordState().observe(activity, event -> {
            if (event == null) return;

            NetworkState2<?> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (state instanceof NetworkState2.Success) {
                DialogFragment newFragment = AlertDialogFragment
                    .newInstance(getString(R.string.password_changed), R.drawable.ic_success_checked, R.color.colorAccent, this);
                newFragment.setCancelable(false);
                newFragment.show(activity.getSupportFragmentManager(), "dialog");


                User user = UserPreferences.Companion.getInstance().getUser(activity);
                if (user == null) return;


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    try {
                        String text1 = user.getUsername();
                        String text2 = activity.getChangePasswordViewModel().getNewPassword();

                        if (text2 == null) return;

                        boolean isBiometricAuth = BiometricPreferences.Companion.getInstance().isBiometricAuthEnabled(activity);
                        BiometricPreferences.Companion.getInstance()
                            .setBiometricCredentials(activity, text1, text2, isBiometricAuth);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }

                if (Integer.parseInt(error.getErrorCode()) == 814 || Integer.parseInt(error.getErrorCode()) == 816) {
                    activity.showMessage(error.getMessage(), R.drawable.ic_error, R.color.colorError, dialog -> {
                        dialog.dismiss();
                        viewModel.setSelected(0);
                    });
                    return;
                }
                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());

            } else if (state instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), new Throwable(ConstantsKt.CONNECTION_ERROR));
            }
        });
    }


}
