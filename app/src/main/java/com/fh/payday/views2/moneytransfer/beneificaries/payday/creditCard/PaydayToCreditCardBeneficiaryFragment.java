package com.fh.payday.views2.moneytransfer.beneificaries.payday.creditCard;

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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.moneytransfer.AddP2CBeneficiaryResponse;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.SharedViewModel;
import com.fh.payday.views.fragments.AlertDialogFragment;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity;
import com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday.PaydayToPaydayBeneficiaryFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.shared.BeneficiaryDetailFragment;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class PaydayToCreditCardBeneficiaryFragment extends Fragment implements OnOTPConfirmListener, AlertDialogFragment.OnConfirmListener {

    NonSwipeableViewPager viewPager;
    private static int NUM_PAGES = 4;
    SlidePagerAdapter adapter;
    EditBeneficiaryActivity activity;
    AddP2CBeneficiaryResponse responseData = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (EditBeneficiaryActivity) context;
    }

    public static PaydayToCreditCardBeneficiaryFragment newInstance() {
        return new PaydayToCreditCardBeneficiaryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.ADD)
            activity.setToolbarTitle(getString(R.string.add_beneficiary));
        else if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.EDIT)
            activity.setToolbarTitle(getString(R.string.edit_beneficiary));

        SharedViewModel model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.setSelected(0);
        model.getSelected().observe(this, index -> {
            if (index < 0) {
                viewPager.setCurrentItem(index);
            } else {
                activity.onBackPressed();
            }
        });

        activity.getViewModel().setPaydayBeneficiary(null);

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        addObserver();
        addEditObserver();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payday_to_credit_card, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        adapter = new SlidePagerAdapter(getFragmentManager(), this);

        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 3:
                        activity.setToolbarTitle(getString(R.string.verify_otp));
                        break;
                    default:
                        if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.ADD)
                            activity.setToolbarTitle(getString(R.string.add_beneficiary));
                        else if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.EDIT)
                            activity.setToolbarTitle(getString(R.string.edit_beneficiary));

                }

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });


        return view;
    }

    @Override
    public void onOtpConfirm(@NotNull String otp) {
        if (!TextUtils.isEmpty(otp)) {
            User user = UserPreferences.Companion.getInstance().getUser(activity);
            if (user == null) return;
            if (activity == null) return;
            if (activity.getViewModel().getCardNumber() == null || activity.getViewModel().getShortName() == null)
                return;

            if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.ADD) {
                activity.getViewModel().addP2CBeneficiary(
                        user.getCustomerId(),
                        activity.getViewModel().getCardNumber(),
                        "Muheeb",
                        activity.getViewModel().getShortName(),
                        otp);
            } else if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.EDIT) {
                if (activity.getViewModel().getSelectedBeneficiary() == null) return;

                activity.getViewModel().editPaydayBeneficiary(
                        user.getCustomerId(),
                        activity.getViewModel().getSelectedBeneficiary().getBeneficiaryId(),
                        activity.getViewModel().getCardNumber(),
                        activity.getViewModel().getShortName(),
                        otp);
            }
        }
    }

    @Override
    public void onConfirm(Dialog dialog) {
        activity.finish();
    }

    private void addObserver() {
        if (activity == null) return;

        activity.getViewModel().getAddPayday2CardBeneficiaryState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<AddP2CBeneficiaryResponse> response = event.getContentIfNotHandled();

            if (response == null) return;

            if (response instanceof NetworkState2.Loading) {
                activity.showProgress();
                return;
            }

            activity.hideProgress();

            if (response instanceof NetworkState2.Success) {
                responseData = ((NetworkState2.Success<AddP2CBeneficiaryResponse>) response).getData();
                activity.showMessage(getResources().getString(R.string.add_beneficiary_success),
                        R.drawable.ic_success_checked_blue,
                        R.color.blue,
                        dialog -> {
                            dialog.dismiss();
                            activity.finish();
                        });
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

    private void addEditObserver() {
        if (activity == null) return;

        activity.getViewModel().getEditPaydayBeneficiaryState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<String> response = event.getContentIfNotHandled();

            if (response == null) return;

            if (response instanceof NetworkState2.Loading) {
                activity.showProgress();
                return;
            }

            activity.hideProgress();

            if (response instanceof NetworkState2.Success) {
                String message = ((NetworkState2.Success<String>) response).getData();
                if (message != null)
                activity.showMessage(message,
                        R.drawable.ic_success_checked_blue,
                        R.color.blue,
                        dialog -> {
                            dialog.dismiss();
                            activity.onBackPressed();
                        });
            } else if (response instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) response;
                activity.onError(error.getMessage());
            } else if (response instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), new Throwable(ConstantsKt.CONNECTION_ERROR));
            }

        });
    }

    private static class SlidePagerAdapter extends FragmentStatePagerAdapter {
        WeakReference<PaydayToCreditCardBeneficiaryFragment> weakReference;

        SlidePagerAdapter(FragmentManager fm, PaydayToCreditCardBeneficiaryFragment context) {
            super(fm);
            weakReference = new WeakReference<>(context);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ShortNameFragment.newInstance(position);
                case 1:
                    return CardNumberFragment.newInstance(position);
                case 2:
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("option", PaydayToPaydayBeneficiaryFragment.BENEFICIARY.P2C);
                    bundle.putInt("index", 2);
                    BeneficiaryDetailFragment fragment = new BeneficiaryDetailFragment();
                    fragment.setArguments(bundle);
                    return fragment;
                case 3:
                    return new OTPFragment.Builder(weakReference.get())
                            .setTitle(weakReference.get().getString(R.string.enter_otp))
                            .setPinLength(6)
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


}
