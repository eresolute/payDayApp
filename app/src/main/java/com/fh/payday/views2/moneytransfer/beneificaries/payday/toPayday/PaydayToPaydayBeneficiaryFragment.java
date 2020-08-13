package com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday;

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
import com.fh.payday.datasource.models.moneytransfer.AddP2PBeneficiaryResponse;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.SharedViewModel;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.AddBeneficiaryFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity;
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.shared.BeneficiaryDetailFragment;

import java.lang.ref.WeakReference;

public class PaydayToPaydayBeneficiaryFragment extends Fragment {

    public enum BENEFICIARY {P2P, P2C, LOCAL, INTL}

    NonSwipeableViewPager viewPager;
    SlidePagerAdapter adapter;
    EditSlidePagerAdapter editAdapter;
    EditBeneficiaryActivity activity;

    private OnOTPConfirmListener p2cListener = otp -> {
        if (TextUtils.isEmpty(otp) || activity == null || activity.getViewModel() == null) return;

        User user = UserPreferences.Companion.getInstance().getUser(activity);
        if (user == null) return;
        if (activity.getViewModel().getPaydayBeneficiary() == null) return;

        activity.getViewModel().addP2PBeneficiary(user.getCustomerId(), activity.getViewModel().getPaydayBeneficiary().getMobileNumber(), otp);
    };

    private OnOTPConfirmListener p2pListener = otp -> {
        if (TextUtils.isEmpty(otp) || activity == null || activity.getViewModel() == null) return;

        User user = UserPreferences.Companion.getInstance().getUser(activity);
        if (user == null) return;
        if (activity.getViewModel().getSelectedBeneficiary() == null || activity.getViewModel().getName() == null) return;
        activity.getViewModel().editP2PBeneficiary(user.getCustomerId(), activity.getViewModel().getSelectedBeneficiary().getBeneficiaryId(), activity.getViewModel().getName(), otp);
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (EditBeneficiaryActivity) context;
    }

    public static PaydayToPaydayBeneficiaryFragment newInstance() {
        return new PaydayToPaydayBeneficiaryFragment();
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
                activity.onBackPressed();
            } else {
                viewPager.setCurrentItem(index);
            }
        });

        activity.getViewModel().setPaydayBeneficiary(null);

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        addObserver();
        addEditP2PObserver();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payday_to_payday, container, false);
        viewPager = view.findViewById(R.id.viewPagerPayday);
        if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.ADD) {
            p2cBeneficiary();
        } else if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.EDIT ) {
            p2pBeneficiary();
        }
        return view;
    }


    private static class SlidePagerAdapter extends FragmentStatePagerAdapter {
        private static int NUM_PAGES = 3;

        WeakReference<PaydayToPaydayBeneficiaryFragment> weakReference;

        SlidePagerAdapter(FragmentManager fm, PaydayToPaydayBeneficiaryFragment context) {
            super(fm);
            weakReference = new WeakReference<>(context);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new BeneficiaryMobileFragment();
                case 1:
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("option", BENEFICIARY.P2P);
                    bundle.putInt("index", 1);
                    BeneficiaryDetailFragment fragment = new BeneficiaryDetailFragment();
                    fragment.setArguments(bundle);
                    return fragment;
                case 2:
                    return new OTPFragment.Builder(weakReference.get().p2cListener)
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

    private static class EditSlidePagerAdapter extends FragmentStatePagerAdapter {

        private static int EDIT_NUM_PAGES = 2;
        WeakReference<PaydayToPaydayBeneficiaryFragment> weakReference;

        EditSlidePagerAdapter(FragmentManager fm, PaydayToPaydayBeneficiaryFragment context) {
            super(fm);
            weakReference = new WeakReference<>(context);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new EditBeneficiaryNameFragment();
                case 1:
                    return new OTPFragment.Builder(weakReference.get().p2pListener)
                            .setTitle(weakReference.get().getString(R.string.enter_otp))
                            .setPinLength(6)
                            .build();
                default:
                    throw new IllegalStateException("Invalid page position");
            }
        }

        @Override
        public int getCount() {
            return EDIT_NUM_PAGES;
        }
    }

    private void addObserver() {
        if (activity == null) return;

        activity.getViewModel().getAddPayday2PaydayBeneficiaryState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<AddP2PBeneficiaryResponse> response = event.getContentIfNotHandled();

            if (response == null) return;
            if (response instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (response instanceof NetworkState2.Success) {
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

    private void addEditP2PObserver() {
        if (activity == null) return;

        activity.getViewModel().getEditP2PBeneficiaryState().observe(activity, event -> {
            if (event == null) return;

            NetworkState2<String> response = event.getContentIfNotHandled();

            if (response == null) return;

            if (response instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (response instanceof NetworkState2.Success) {
                String message = ((NetworkState2.Success<String>) response).getData();
                if (message == null)
                    return;
                activity.showMessage(message,
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
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), new Throwable(ConstantsKt.CONNECTION_ERROR));
            }

        });
    }


    private void p2cBeneficiary() {
        adapter = new SlidePagerAdapter(getFragmentManager(), this);
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 2:
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
    }

    private void p2pBeneficiary() {
        editAdapter = new EditSlidePagerAdapter(getFragmentManager(), this);
        viewPager.setAdapter(editAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 1:
                        activity.setToolbarTitle(getString(R.string.verify_otp));
                        break;
                    default:
                        activity.setToolbarTitle(getString(R.string.edit_beneficiary));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    public void backPress() {
        if (viewPager.getCurrentItem() == 0) {
            if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.ADD) {
                activity.replaceFragment(new AddBeneficiaryFragment());
            } else if (activity.getViewModel().getBeneficiaryOption() == EditBeneficiaryActivity.OPTION.EDIT ) {
                activity.replaceFragment(EditBeneficiaryFragment.newInstance());
            }

        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem()- 1);
        }

    }
}
