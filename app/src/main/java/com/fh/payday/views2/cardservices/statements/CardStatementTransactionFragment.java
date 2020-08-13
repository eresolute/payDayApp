package com.fh.payday.views2.cardservices.statements;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.StatementHistory;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.ListUtilsKt;
import com.fh.payday.views.adapter.expandablelistadapter.CardStatementListAdapter;

import java.util.ArrayList;
import java.util.List;

public class CardStatementTransactionFragment extends Fragment implements ExpandableListView.OnGroupCollapseListener,
        ExpandableListView.OnGroupExpandListener {
    ExpandableListView listView;
    TextView tvTitle, textView;
    static String mTitle;
    static int mIndex;
    CardStatementTypeActivity activity;

    List<StatementHistory> transactions = new ArrayList<>();
    CardStatementListAdapter listAdapter;
    ProgressBar progressBar;

    public static CardStatementTransactionFragment newInstance(String title, int index) {
        mTitle = title;
        mIndex = index;
        return new CardStatementTransactionFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (CardStatementTypeActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_statement_transaction, container, false);
        addObservers();
        init(view);
        setTransactions();
        return view;
    }


    private void init(View view) {
        listView = view.findViewById(R.id.elv_card_transaction);
        tvTitle = view.findViewById(R.id.tv_title);
        textView = view.findViewById(R.id.text_view);
        progressBar = view.findViewById(R.id.progress_bar);
        tvTitle.setText(mTitle);

        User user = UserPreferences.Companion.getInstance().getUser(activity);
        if (user == null) return;

        activity.getViewModel().getStatementHistory(user.getToken(), user.getSessionId(), user.getRefreshToken(),
            user.getCustomerId(), getType(mIndex));

    }

    private String getType(int index) {
        String type = null;
        switch (index) {
            case 0:
                type = "THREE";
                break;
            case 1:
                type = "SIX";
                break;
            case 2:
                type = "NINE";
                break;
            case 3:
                type =  "TWELVE";
                break;
        }
        return type;
    }

    //Sets dummy address to ListView
    private void setTransactions() {

    }

    private void addObservers(){
        if (activity == null) return;

        activity.getViewModel().getStatementState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<List<StatementHistory>> state = event.getContentIfNotHandled();
            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            progressBar.setVisibility(View.GONE);

            if (state instanceof NetworkState2.Success) {
                transactions = ((NetworkState2.Success<List<StatementHistory>>) state).getData();
                if (ListUtilsKt.isEmptyList(transactions)){
                    textView.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.INVISIBLE);
                } else{
                    textView.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    listAdapter = new CardStatementListAdapter(transactions);
                    listView.setAdapter(listAdapter);

                    listView.setOnGroupExpandListener(this);
                    listView.setOnGroupCollapseListener(this);
                }

            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
                activity.onError(error.getMessage());
            } else if (state instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), new Throwable(ConstantsKt.CONNECTION_ERROR));
            }
        });
    }

    @Override
    public void onGroupCollapse(int i) {

    }

    @Override
    public void onGroupExpand(int i) {

    }
}