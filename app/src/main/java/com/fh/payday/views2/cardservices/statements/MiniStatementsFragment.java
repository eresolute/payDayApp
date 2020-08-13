package com.fh.payday.views2.cardservices.statements;

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
import com.fh.payday.datasource.models.Item;
import com.fh.payday.views.shared.IconListAdapterVer2;

import java.util.ArrayList;
import java.util.List;

public class MiniStatementsFragment extends Fragment {


    public static MiniStatementsFragment newInstance() {
        return new MiniStatementsFragment();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mini_statements, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setAdapter(new IconListAdapterVer2(getMiniStatements(), (CardStatementTypeActivity) getActivity()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        return view;
    }

    public List<Item> getMiniStatements() {
        List<Item> monthStatements = new ArrayList<>();

        Context context = getContext();
        if (context == null) return monthStatements;

        String[] monthEmailStatement = context.getResources().getStringArray(R.array.mini_statements);
        for (String aMonthEmailStatement : monthEmailStatement)
            monthStatements.add(new Item(aMonthEmailStatement, R.drawable.ic_mini_statement));

        return monthStatements;
    }

    public interface OnItemClick{
        void onItemClick(String title, int index);
    }
}
