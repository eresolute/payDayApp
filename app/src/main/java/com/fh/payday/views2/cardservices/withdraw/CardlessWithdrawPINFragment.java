package com.fh.payday.views2.cardservices.withdraw;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fh.payday.R;

public class CardlessWithdrawPINFragment extends Fragment {

    ImageView imgNext, imgPrev;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cardless_withdraw_pin, container, false);
        init(view);

        imgNext.setOnClickListener(v -> ((CardlessWithdrawalActivity)getActivity()).onPinEnter());
        imgPrev.setOnClickListener(v-> (getActivity()).onBackPressed());

        return view;
    }

    private void init(View view) {
        imgNext = view.findViewById(R.id.img_next);
        imgPrev = view.findViewById(R.id.img_previous);
    }
}
