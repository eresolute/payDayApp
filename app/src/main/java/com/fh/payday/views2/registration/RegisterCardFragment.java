package com.fh.payday.views2.registration;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.datasource.models.PaydayCard;
import com.fh.payday.datasource.models.ui.RegisterInputWrapper;
import com.fh.payday.views2.paydaycardscanner.PaydayCardScannerActivity;

public class RegisterCardFragment extends Fragment {

    @Nullable
    private RegisterActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (RegisterActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && activity != null) {
            activity.getViewModel().getWrapper().setCardDetails(null);
            activity.getViewModel().setEmiratesDetailSet(true);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() == null) {
            throw new IllegalStateException("getActivity() return null ");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_card, container, false);
        MaterialButton btnManually = view.findViewById(R.id.btn_manually);
        btnManually.setOnClickListener(view1 -> skipCardScan());

        Intent intent = new Intent(getActivity(), PaydayCardScannerActivity.class);
        /*intent.putExtra("title", getString(R.string.position_payday_card));
        intent.putExtra("instruction", "");
        intent.putExtra("button_text","Scan");*/
        view.findViewById(R.id.btn_submit).setOnClickListener(v -> startActivityForResult(intent, RegisterActivity.REQUEST_SCAN_PAYDAY_CARD));

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == RegisterActivity.REQUEST_SCAN_PAYDAY_CARD) {
            PaydayCard paydayCard = data.getParcelableExtra("data");
            if (paydayCard == null) return;
            scanPaydayCard(paydayCard);
               /* File capturedData = (File) data.getSerializableExtra("data");
            scanPaydayCard(BitmapFactory.decodeFile(capturedData.getPath()));*/
        }
    }

    private void scanPaydayCard(PaydayCard paydayCard) {
        RegisterActivity activity = this.activity;
        if (activity == null)  return;
       // activity.showProgress(getString(R.string.processing));
        RegisterInputWrapper inputWrapper = activity.getViewModel().getInputWrapper();
        inputWrapper.setCardNumber(paydayCard.getCardNumber());
        inputWrapper.setCardName(paydayCard.getCardName());
        inputWrapper.setCardExpiry(paydayCard.getExpiry());
        activity.navigateUp();
        /*ImageScanHandler.Companion.getInstance().scanPaydayCard(bitmap, card -> {
            activity.hideProgress();
            if (card == null || !card.isValid()) {
                activity.onError(getString(R.string.card_scan_error));
                return;
            }

            RegisterInputWrapper inputWrapper = activity.getViewModel().getInputWrapper();
            inputWrapper.setCardNumber(card.getCardNumber());
            inputWrapper.setCardName(card.getCardName());
            inputWrapper.setCardExpiry(card.getExpiry());
            activity.navigateUp();
        });*/
    }

    private void skipCardScan() {
        if(activity == null) return;
        activity.getViewModel().setEmiratesDetailSet(true);
        RegisterInputWrapper wrapper = activity.getViewModel().getInputWrapper();

        wrapper.setCardNumber("");
        wrapper.setCardName("");
        wrapper.setCardExpiry("");
        activity.navigateUp();
    }
}
