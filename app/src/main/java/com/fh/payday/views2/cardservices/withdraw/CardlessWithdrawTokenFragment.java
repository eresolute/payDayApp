package com.fh.payday.views2.cardservices.withdraw;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fh.payday.R;

public class CardlessWithdrawTokenFragment extends Fragment {

    ImageView imgNext, imgPrev;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cardless_withdraw_token, container, false);
        init(view);

        view.findViewById(R.id.img_wait).setVisibility(View.GONE);
        view.findViewById(R.id.tv_await).setVisibility(View.GONE);

        imgNext.setOnClickListener(v -> {
            view.findViewById(R.id.img_next).setVisibility(View.GONE);
            view.findViewById(R.id.img_previous).setVisibility(View.GONE);
            view.findViewById(R.id.linear_layout).setVisibility(View.GONE);
            view.findViewById(R.id.img_wait).setVisibility(View.VISIBLE);
            view.findViewById(R.id.tv_await).setVisibility(View.VISIBLE);
            hold(view);
        });
        imgPrev.setOnClickListener(v -> (getActivity()).onBackPressed());

        return view;
    }

    private void init(View view) {
        imgNext = view.findViewById(R.id.img_next);
        imgPrev = view.findViewById(R.id.img_previous);
    }

    public void hold(View view) {
        Handler handler = new Handler();
        Runnable runnable = () -> {
            view.findViewById(R.id.img_next).setVisibility(View.VISIBLE);
            view.findViewById(R.id.img_previous).setVisibility(View.VISIBLE);
            view.findViewById(R.id.linear_layout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.img_wait).setVisibility(View.GONE);
            view.findViewById(R.id.tv_await).setVisibility(View.GONE);
            ((CardlessWithdrawalActivity)getActivity()).onToken();
        };
        handler.postDelayed(runnable, 1500);

    }
}
