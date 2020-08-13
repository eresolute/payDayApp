package com.fh.payday.views2.settings.update_link_account;

import android.content.Context;
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
import com.fh.payday.datasource.models.Option;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.views.adapter.ProductAdapter;
import com.fh.payday.views2.settings.SettingOptionActivity;

import java.util.List;

public class LinkAccountFragment extends Fragment implements OnItemClickListener {

    SettingOptionActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SettingOptionActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_link_account, container, false);

        List<Option> link = DataGenerator.getAccountOptions(getContext());
        RecyclerView rvList = view.findViewById(R.id.rv_list);
        rvList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvList.setAdapter(new ProductAdapter(link, this));

        return view;
    }

    @Override
    public void onItemClick(int index) {
        //activity.replaceFragment(new LinkAccountNumberFragment());
    }


}
