package com.fh.payday.views2.moneytransfer.beneificaries.international;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.SharedViewModel;
import com.fh.payday.views.fragments.AlertDialogFragment;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity;
import com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday.PaydayToPaydayBeneficiaryFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.shared.AccountNumberFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.shared.AddressFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.shared.BeneficiaryDetailFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.shared.BeneficiaryNameFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.shared.MobileFragment;
import com.fh.payday.views2.moneytransfer.beneificaries.shared.SelectBankFragment;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class InternationalBeneficiaryEditFragment extends Fragment implements OnOTPConfirmListener, AlertDialogFragment.OnConfirmListener {

    NonSwipeableViewPager viewPager;
    SlidePagerAdapter adapter;
    private static final int NUM_PAGES = 8;
    public static final String TAG = InternationalBeneficiaryEditFragment.class.getName();
    private EditBeneficiaryActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (EditBeneficiaryActivity) context;
    }
    public static InternationalBeneficiaryEditFragment newInstance() {
        return new InternationalBeneficiaryEditFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() == null) {
            return;
        }

        SharedViewModel model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getSelected().observe(this, index -> viewPager.setCurrentItem(index));

        activity.getViewModel().setPaydayBeneficiary(null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_international_edit_beneficiary, container, false);
        init(view);

        viewPager.setAdapter(adapter);

        return view;
    }

    private void init(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        adapter = new SlidePagerAdapter(this, getFragmentManager());
    }

    @Override
    public void onOtpConfirm(@NotNull String otp) {
        /*DialogFragment newFragment = AlertDialogFragment
                .newInstance("Successfully Edited",
                        R.drawable.ic_success_checked,
                        R.color.colorAccent,
                        this);
        newFragment.show(getFragmentManager(), "dialog");*/
    }

    @Override
    public void onConfirm(Dialog dialog) {
        getActivity().finish();
    }

    private static class SlidePagerAdapter extends FragmentStatePagerAdapter {

        private WeakReference<InternationalBeneficiaryEditFragment> weakReference;

        SlidePagerAdapter(InternationalBeneficiaryEditFragment context, FragmentManager fm) {
            super(fm);
            this.weakReference = new WeakReference<>(context);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SelectCountryFragment.newInstance(position);
                case 1:
                    return SelectBankFragment.newInstance(position);
                case 2:
                    return BeneficiaryNameFragment.newInstance(position);
                case 3:
                    return AccountNumberFragment.newInstance(position);
                case 4:
                    return AddressFragment.newInstance(position);
                case 5:
                    return MobileFragment.newInstance(position);
                case 6:
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("option", PaydayToPaydayBeneficiaryFragment.BENEFICIARY.INTL);
                    bundle.putInt("index", 6);
                    BeneficiaryDetailFragment fragment = new BeneficiaryDetailFragment();
                    fragment.setArguments(bundle);
                    return fragment;
                case 7:
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
