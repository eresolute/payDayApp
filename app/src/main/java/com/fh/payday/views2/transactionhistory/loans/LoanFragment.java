package com.fh.payday.views2.transactionhistory.loans;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fh.payday.R;
import com.fh.payday.views2.products.ProductOptionActivity;

public class LoanFragment extends Fragment {
    Button btnApply;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loan,container, false);
        btnApply = view.findViewById(R.id.btn_apply);
        btnApply.setOnClickListener(v -> {

            Intent i = new Intent(getContext(), ProductOptionActivity.class);
            i.putExtra("index", 1);
            ((LoanActivity)getActivity()).startActivity(i);


//            ((LoanActivity)getActivity()).replaceFragment(new LoanDetailFragment());
        });
        return view;
    }
}
