package com.fh.payday.views2.bottombar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.datasource.models.QuestionBank;
import com.fh.payday.views.adapter.expandablelistadapter.ExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;

public class FaqFragment extends Fragment implements ExpandableListView.OnGroupCollapseListener,
        ExpandableListView.OnGroupExpandListener
{
    List<QuestionBank> questions = new ArrayList<>();

    ExpandableListView listView;
    ExpandableListAdapter listAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq, container, false);
        listView = view.findViewById(R.id.expandedList);

        setQuestions();

        return view;
    }

    //Sets dummy address to ListView
    private void setQuestions() {
        questions = DataGenerator.getQuestions(getContext());

        listAdapter = new ExpandableListAdapter(getContext(), questions);
        listView.setAdapter(listAdapter);

        listView.setOnGroupExpandListener(this);
        listView.setOnGroupCollapseListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onGroupCollapse(int i) {
    }

    @Override
    public void onGroupExpand(int i) {
    }
}
