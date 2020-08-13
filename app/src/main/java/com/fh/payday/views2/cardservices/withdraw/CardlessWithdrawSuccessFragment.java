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

public class CardlessWithdrawSuccessFragment extends Fragment {

    ImageView imgSuccess;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cardless_withdraw_success, container, false);
        init(view);

        imgSuccess.setOnClickListener(v -> (getActivity()).finish());

        return view;
    }

    private void init(View view) {
        imgSuccess = view.findViewById(R.id.img_success);
    }
}
