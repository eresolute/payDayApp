package com.fh.payday.views2.cardservices.statements;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.MonthlyStatement;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.ListUtilsKt;

import java.util.List;

import kotlin.Unit;

public class MonthlyStatementsFragment extends Fragment {

    CardStatementTypeActivity activity;
    RecyclerView recyclerView;
    TextView textView;
    ProgressBar progressBar;

    public static MonthlyStatementsFragment newInstance() {
        return new MonthlyStatementsFragment();
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
        View view = inflater.inflate(R.layout.fragment_monthly_statements, container, false);

        init(view);
        addObservable();
        getMonthlyStatements();
        return view;
    }

    private void getMonthlyStatements() {
        activity.hideNoInternetView();
        if (activity!= null && !activity.isNetworkConnected()) {
            recyclerView.setVisibility(View.GONE);
            activity.showNoInternetView(() -> {
                getMonthlyStatements();
                return Unit.INSTANCE;
            });
            return;
        }
        User user = UserPreferences.Companion.getInstance().getUser(activity);
        if (user == null) return;
        activity.getViewModel().monthlyStatement(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
    }

    private void init(View view){
        recyclerView = view.findViewById(R.id.recycler_view);
        textView = view.findViewById(R.id.text_view);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void addObservable() {
        activity.getViewModel().getMonthlyStatementState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<List<MonthlyStatement>> loanState = event.getContentIfNotHandled();

            if (loanState == null) return;

            if (loanState instanceof NetworkState2.Loading) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            progressBar.setVisibility(View.GONE);

            if (loanState instanceof NetworkState2.Success) {
                List<MonthlyStatement> monthlyStatement = ((NetworkState2.Success<List<MonthlyStatement>>) loanState).getData();
                if (ListUtilsKt.isNotEmptyList(monthlyStatement)) {
                    if (monthlyStatement == null) return;
                    MonthlyStatementsAdapter cardStatementAdapter = new MonthlyStatementsAdapter(monthlyStatement);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(cardStatementAdapter);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                } else {
                    textView.setVisibility(View.VISIBLE);
                }
            } else if (loanState instanceof NetworkState2.Error) {
                textView.setVisibility(View.VISIBLE);
                NetworkState2.Error<List<MonthlyStatement>> error = (NetworkState2.Error<List<MonthlyStatement>>) loanState;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
                activity.onError(error.getMessage());
            } else if (loanState instanceof NetworkState2.Failure) {
                textView.setVisibility(View.VISIBLE);
                activity.onFailure(activity.findViewById(R.id.root_view), ((NetworkState2.Failure<List<MonthlyStatement>>) loanState).getThrowable());
            } else {
                textView.setVisibility(View.VISIBLE);
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

}