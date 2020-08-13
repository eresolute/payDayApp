package com.fh.payday.views2.moneytransfer.beneificaries.shared;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.viewmodels.SharedViewModel;

public class MobileFragment extends Fragment {

    private SharedViewModel model;
    int index;
    private TextInputEditText etMobile;


    public static MobileFragment newInstance(int position) {

        Bundle args = new Bundle();

        MobileFragment fragment = new MobileFragment();
        args.putInt("index", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        index = getArguments().getInt("index");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mobile, container, false);
        init(view);


        return view;
    }

    private void init(View view) {
        etMobile = view.findViewById(R.id.et_mobile);

        view.findViewById(R.id.img_next).setOnClickListener( v-> {
            model.setMap("Mobile Number", etMobile.getText().toString());
            model.setSelected(index + 1);
        });

        view.findViewById(R.id.img_previous).setOnClickListener( v-> {
            model.setSelected(index - 1);
        });
    }
}
