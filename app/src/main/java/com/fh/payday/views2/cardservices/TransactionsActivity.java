package com.fh.payday.views2.cardservices;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.transactionhistory.CardTransactions;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.ListUtilsKt;
import com.fh.payday.utilities.TransactionType;
import com.fh.payday.viewmodels.cardservices.TransactionViewModel;
import com.fh.payday.views.adapter.TransactionAdapter;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

import org.jetbrains.annotations.Nullable;

public class TransactionsActivity extends BaseActivity {

    private TransactionViewModel viewModel;
    private RecyclerView recyclerView;
    @Nullable
    private User user;
    private TextView textView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        user = UserPreferences.Companion.getInstance().getUser(this);
        if (user == null) {
            onBackPressed();
            return;
        }

        viewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        super.onCreate(savedInstanceState);

        addObserver();
        handleBottomBar();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_transactions;
    }

    @Override
    public void init() {
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvAmount = findViewById(R.id.tv_amount);
        recyclerView = findViewById(R.id.recycler_view);
        textView = findViewById(R.id.text_view);
        progressBar = findViewById(R.id.progress_bar);
        TransactionType transactionType = (TransactionType) getIntent().getSerializableExtra("transaction_type");

        if (transactionType == TransactionType.SALARIES_CREDITED) {
            toolbar_title.setText(R.string.salaries_credited);
            tvTitle.setText(R.string.salaries_credited_date);
            tvAmount.setText(R.string.amount);
//                recyclerView.setAdapter(new TransactionAdapter(getSalariesCredited()));
            if (user != null)
                viewModel.getSalariesCredited(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
        } else {
            toolbar_title.setText(R.string.transactions);
            tvTitle.setText(R.string.description_date);
            tvAmount.setText(R.string.amount);
            if (user != null)
                viewModel.getTransactionDetail(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
        }
    }

    private void handleBottomBar() {

        findViewById(R.id.btm_home).setOnClickListener(view -> {
            Intent i = new Intent(view.getContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        findViewById(R.id.btm_menu_branch).setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), LocatorActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        findViewById(R.id.btm_menu_support).setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), ContactUsActivity.class);
            startActivity(i);
        });

        findViewById(R.id.btm_menu_loan_calc).setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), LoanCalculatorActivity.class);
            startActivity(i);
        });

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("transactionHistoryHelp"));
    }

    private void addObserver() {
        viewModel.getTransactionState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<CardTransactions> transactionState = event.getContentIfNotHandled();

            if (transactionState == null) return;

            if (transactionState instanceof NetworkState2.Loading) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            progressBar.setVisibility(View.GONE);

            if (transactionState instanceof NetworkState2.Success) {
                CardTransactions data = ((NetworkState2.Success<CardTransactions>) transactionState).getData();
                if (data == null) return;
                if (ListUtilsKt.isEmptyList(data.getTransactions())) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    textView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);
                    recyclerView.setAdapter(new TransactionAdapter(data.getTransactions(), 1));
                }
            } else if (transactionState instanceof NetworkState2.Error) {
                NetworkState2.Error<CardTransactions> error = (NetworkState2.Error<CardTransactions>) transactionState;
                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }
                onError(error.getMessage());
            } else if (transactionState instanceof NetworkState2.Failure) {
                onFailure(findViewById(R.id.layout), ConstantsKt.CONNECTION_ERROR);
            } else {
                onFailure(findViewById(R.id.layout), ConstantsKt.CONNECTION_ERROR);
            }
        });

        viewModel.getSalariesNetworkState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<CardTransactions> transactionState = event.getContentIfNotHandled();

            if (transactionState == null) return;

            if (transactionState instanceof NetworkState2.Loading) {
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            progressBar.setVisibility(View.GONE);

            if (transactionState instanceof NetworkState2.Success) {
                CardTransactions data = ((NetworkState2.Success<CardTransactions>) transactionState).getData();
                if (data == null) return;
                if (ListUtilsKt.isEmptyList(data.getTransactions())) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    textView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);
                    recyclerView.setAdapter(new TransactionAdapter(data.getTransactions(), 2));
                }
            } else if (transactionState instanceof NetworkState2.Error) {
                NetworkState2.Error<CardTransactions> error = (NetworkState2.Error<CardTransactions>) transactionState;
                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }
                onError(error.getMessage());
            } else if (transactionState instanceof NetworkState2.Failure) {
                onFailure(findViewById(R.id.layout), ConstantsKt.CONNECTION_ERROR);
            } else {
                onFailure(findViewById(R.id.layout), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }
}
