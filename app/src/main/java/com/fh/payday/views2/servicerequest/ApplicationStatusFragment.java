package com.fh.payday.views2.servicerequest;

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
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.views.adapter.ProductAdapter;

public class ApplicationStatusFragment extends Fragment implements OnItemClickListener {
    RecyclerView rvApplicationOptions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_application_status, container, false);
        init(view);

        rvApplicationOptions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvApplicationOptions.setAdapter(new ProductAdapter(DataGenerator.getApplicationOptions(), this ));

        return view;
    }

    private void init(View view) {
        rvApplicationOptions = view.findViewById(R.id.rv_application_status);
    }

    @Override
    public void onItemClick(int index) {

    }
}
