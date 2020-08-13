package com.fh.payday.views2.moneytransfer.beneificaries.international;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity;

public class SelectCountryFragment extends Fragment {

    private EditBeneficiaryActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (EditBeneficiaryActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    public SharedViewModel model;
    public int index;

    public static SelectCountryFragment newInstance(int position) {

        Bundle args = new Bundle();
        args.putInt("index", position);
        SelectCountryFragment fragment = new SelectCountryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(activity).get(SharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_country, container, false);
        init(view);

        if (getArguments() != null) {
            index = getArguments().getInt("index");
        }
        return view;
    }

    private void init(View view) {
        TextInputEditText etCountry = view.findViewById(R.id.et_country);

        view.findViewById(R.id.img_next).setOnClickListener(v -> {
            model.setMap("Country", etCountry.getText().toString());
            model.setSelected(index + 1);
        });

        view.findViewById(R.id.img_previous).setOnClickListener(v -> {
            if (index == 0)
                activity.onBackPressed();
        });
    }
}
