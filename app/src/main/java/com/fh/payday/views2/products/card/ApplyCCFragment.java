package com.fh.payday.views2.products.card;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.datasource.models.shared.ListModel;
import com.fh.payday.views.fragments.AlertDialogFragment;
import com.fh.payday.views.shared.ListAdapter;
import com.fh.payday.views2.products.ProductOptionActivity;

import java.util.List;

public class ApplyCCFragment extends Fragment implements AlertDialogFragment.OnConfirmListener {
    RecyclerView recyclerView;
    Button btnApply;
    ProductOptionActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ProductOptionActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }


    public static ApplyCCFragment newInstance() {
        return new ApplyCCFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apply_cc, container, false);
        List<ListModel> listData = DataGenerator.getCCDetails(getContext());
        btnApply = view.findViewById(R.id.btn_apply);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new ListAdapter(listData, activity, getString(R.string.apply_cc)));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        btnApply.setOnClickListener(v -> {
            DialogFragment newFragment = AlertDialogFragment
                    .newInstance("Your Application is Under process", R.drawable.ic_success_checked, R.color.colorAccent, this);
            newFragment.show(getFragmentManager(), "dialog");
        });
        return view;
    }

    @Override
    public void onConfirm(Dialog dialog) {
        if (getActivity() == null) return;

        getActivity().finish();
    }
}
