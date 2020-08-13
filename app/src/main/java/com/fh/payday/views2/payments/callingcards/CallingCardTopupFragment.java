package com.fh.payday.views2.payments.callingcards;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.datasource.models.Plan;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.views.adapter.PlanAdapter;

import java.util.List;

public class CallingCardTopupFragment extends Fragment {

    Button btnNext;
    public List<Plan> callingCards = DataGenerator.getCallingCards();
    RecyclerView recyclerView;
    CallingCardActivity activity;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (CallingCardActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    private OnItemClickListener listener = index -> Toast.makeText(activity, ""+index, Toast.LENGTH_SHORT).show();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calling_card_topup, container, false);

        init(view);
        setRecyclerView();

        if (getActivity() != null) {
            btnNext.setOnClickListener(v -> ((CallingCardActivity) getActivity()).onTopupSelected());
        }

        return view;
    }

    private void setRecyclerView() {
        PlanAdapter callingCardTopupAdapter = new PlanAdapter(listener, callingCards, -1);
        recyclerView.setAdapter(callingCardTopupAdapter);
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        btnNext = view.findViewById(R.id.btn_next);
    }
}
