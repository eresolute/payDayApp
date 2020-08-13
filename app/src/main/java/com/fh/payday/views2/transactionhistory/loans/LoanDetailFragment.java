package com.fh.payday.views2.transactionhistory.loans;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.views.shared.ListAdapter;

public class LoanDetailFragment extends Fragment {
    RecyclerView recyclerView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loan_detail, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new ListAdapter(DataGenerator.getLoanDetails(getContext()), getContext(), getString(R.string.loan_details)));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return view;
    }
}
