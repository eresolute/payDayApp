package com.fh.payday.views2.settings.update_mobile_number;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.OnAtmPinConfirmListener;
import com.fh.payday.viewmodels.SettingOptionViewModel;
import com.fh.payday.views.fragments.AlertDialogFragment;
import com.fh.payday.views.shared.AtmCommonPinFragment;
import com.fh.payday.views2.settings.SettingOptionActivity;

import java.lang.ref.WeakReference;

public class UpdateMobileFragment extends Fragment implements  AlertDialogFragment.OnConfirmListener {

    NonSwipeableViewPager viewPager;
    SlidePagerAdapter adapter;
    SettingOptionViewModel viewModel;
    SettingOptionActivity activity;

    private static final int NUM_PAGES = 3;
    private OnAtmPinConfirmListener pinConfirmListener = pin ->{
        DialogFragment newFragment = AlertDialogFragment
                .newInstance("Mobile Number Update Successful",
                        R.drawable.ic_success_checked,
                        R.color.colorAccent,
                        this);
        newFragment.show(activity.getSupportFragmentManager(), "dialog");
    };

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
        viewModel.getSelected().observe(this, index-> viewPager.setCurrentItem(index));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_mobile_number, container, false);
        init(view);

        viewPager.setAdapter(adapter);

        return view;
    }

    private void init(View view) {
        viewPager = view.findViewById(R.id.view_pager);
        adapter = new SlidePagerAdapter(getFragmentManager(), this);
    }



    @Override
    public void onConfirm(Dialog dialog) {
        activity.finish();
    }

    private static class SlidePagerAdapter extends FragmentStatePagerAdapter {

        private WeakReference<UpdateMobileFragment> weakReference;

        SlidePagerAdapter(FragmentManager fm, UpdateMobileFragment context) {
            super(fm);
            this.weakReference = new WeakReference<>(context);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new NewMobileFragment();
                case 1:
                    return new ConfirmMobileNumberFragment();
                case 2:
                    return new AtmCommonPinFragment.Builder(weakReference.get().pinConfirmListener)
                            .setPinLength(4)
                            .setTitle(weakReference.get().getString(R.string.enter_atm_pin))
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
