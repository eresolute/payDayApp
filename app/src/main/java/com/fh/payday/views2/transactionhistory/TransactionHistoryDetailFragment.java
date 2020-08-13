package com.fh.payday.views2.transactionhistory;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.CreditDebit;
import com.fh.payday.datasource.models.TransactionHistory;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.shared.ListModel;
import com.fh.payday.datasource.models.transactionhistory.CardTransactions;
import com.fh.payday.datasource.models.transactionhistory.LoanTransaction;
import com.fh.payday.datasource.models.transactionhistory.TransactionDate;
import com.fh.payday.datasource.models.transactionhistory.Transactions;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.DateTime;
import com.fh.payday.utilities.ListUtilsKt;
import com.fh.payday.utilities.TextViewUtilsKt;
import com.fh.payday.views2.moneytransfer.TransactionHistoryAdapter;
import com.fh.payday.views2.shared.custom.DialogInfoFragment;
import com.fh.payday.views2.transactionhistoryv2.TransactionHistoryV2Activity;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class TransactionHistoryDetailFragment extends Fragment {
    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView textView;
    //TextView tvType;
    TransactionHistoryV2Activity activity;
    int index;
    private OnTransactionClickListener listener = transaction -> {

        ArrayList<ListModel> list = new ArrayList<>();
        list.add(new ListModel(getString(R.string.reference_no), transaction.getTransactionReferenceNumber()));
        list.add(new ListModel(getString(R.string.transaction_amount), String.format(getString(R.string.amount_in_aed),transaction.getTransactionAmount())));
        list.add(new ListModel(getString(R.string.merchant_name), transaction.getMerchantName()));
        list.add(new ListModel(getString(R.string.transaction_date),
                DateTime.Companion.parse(transaction.getTransactionDateTime(), "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a"))
        );
        DialogInfoFragment dialog = DialogInfoFragment.Companion.getInstance(transaction.getTransactionDescription(),
                list,
                false,
                getString(R.string.create_dispute),
                getString(R.string.ok)
        );

        dialog.show(getFragmentManager(), "");
    };

    private OnLoanTransactionClickListener listenerLoans = transaction -> {

        ArrayList<ListModel> list = new ArrayList<>();
        list.add(new ListModel(getString(R.string.transaction_type), transaction.getTransactionType()));
        list.add(new ListModel(getString(R.string.transaction_amount), String.format(getString(R.string.amount_in_aed),transaction.getTransactionAmount())));
        list.add(new ListModel(getString(R.string.outstanding_amount),  String.format(getString(R.string.amount_in_aed),transaction.getOutStandingLoanAmount())));
        list.add(new ListModel(getString(R.string.transaction_date),
                DateTime.Companion.parse(transaction.getTransactionDate(), "yyyy-MM-dd", "dd/MM/yyyy"))
        );
        DialogInfoFragment dialog = DialogInfoFragment.Companion.getInstance(transaction.getTransactionDescription(),
                list,
                false,
                getString(R.string.create_dispute),
                getString(R.string.ok)
        );

        dialog.show(getFragmentManager(), "");
    };

    private OnSalaryTransactionClickListener listenerSalary = transaction -> {

        ArrayList<ListModel> list = new ArrayList<>();

        String merchant = transaction.getMerchantName() == null ? "" : transaction.getMerchantName();
        String description = transaction.getTransactionDescription() == null ? "" : transaction.getMerchantName();

        list.add(new ListModel(getString(R.string.reference_no), transaction.getTransactionReferenceNumber()));
        list.add(new ListModel(getString(R.string.transaction_amount), String.format(getString(R.string.amount_in_aed),transaction.getTransactionAmount())));
        list.add(new ListModel(getString(R.string.merchant_name), merchant));
        list.add(new ListModel(getString(R.string.transaction_date),
                DateTime.Companion.parse(transaction.getTransactionDateTime(), "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a"))
        );
        DialogInfoFragment dialog = DialogInfoFragment.Companion.getInstance(description,
                list,
                false,
                getString(R.string.create_dispute),
                getString(R.string.ok)
        );

        dialog.show(getFragmentManager(), "");
    };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (TransactionHistoryV2Activity) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        index = getArguments() != null ? getArguments().getInt("index") : 0;
        setTextTitle(index);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_history_detail, container, false);
        init(view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return view;
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        textView = view.findViewById(R.id.text_view);
    }

    private void setTextTitle(int index) {
        User user = UserPreferences.Companion.getInstance().getUser(activity);
        if (user == null) return;

        switch (index) {
            case 2:
            case 3:
            case 0:
                addCardObservable();
                if (activity.getViewModel().getCardTransaction() == null) {

                    activity.getViewModel().getTransactionDate().observe(activity, transactionDate -> {
//                        toggleVisibility(View.GONE, View.VISIBLE, View.GONE);
                        if (transactionDate != null) {
                            getCardTransactions(transactionDate, user);
                        }
                    });

                } else {
                    TransactionHistory transactions = activity.getViewModel().getCardTransaction();
                    if(ListUtilsKt.isEmptyList(transactions.getTransactions())) {
                        activity.setCreditDebit(transactions.getCreditDebit());
                        toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                        return;
                    } else {
                        switch (index) {
                            case 0:
                                recyclerView.setAdapter(new TransactionHistoryAdapter(new ArrayList<>(transactions.getTransactions()), listener));
                                break;
                            case 2:
                                recyclerView.setAdapter(new TransactionHistoryAdapter(new ArrayList<>(transactions.getP2P()), listener));
                                break;
                            case 3:
                                recyclerView.setAdapter(new TransactionHistoryAdapter(new ArrayList<>(transactions.getBillPayments()), listener));
                                break;
                        }
                    }

                    activity.setCreditDebit(activity.getViewModel().getCardTransaction().getCreditDebit());
                    toggleVisibility(View.VISIBLE,View.GONE,View.GONE);
                }

                break;
            case 1:
                toggleVisibility(View.GONE, View.VISIBLE, View.GONE);
                if (ListUtilsKt.isEmptyList(activity.getViewModel().getLoanTransaction())){
                    addObservable();
                    String loanNumber = activity.getViewModel().getLoanNumber();
                    if (loanNumber == null || TextUtils.isEmpty(loanNumber)) {
                        toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                        return;
                    }
                    getLoanTransaction(loanNumber, user);
                } else {
                    recyclerView.setAdapter(new LoanTransactionAdapter(activity.getViewModel().getLoanTransaction(), listenerLoans));
                    if (ListUtilsKt.isEmptyList(activity.getViewModel().getLoanTransaction()))
                        toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                    else
                        toggleVisibility(View.VISIBLE, View.GONE, View.GONE);
                }
                break;
//            case 2:
//                toggleVisibility(View.GONE, View.VISIBLE, View.GONE);
//                if (activity.getViewModel().getCardTransaction() == null){
//                    activity.getViewModel().getTransactionDate().observe(activity, transactionDate -> {
//                        toggleVisibility(View.GONE, View.VISIBLE, View.GONE);
//                        if (transactionDate != null) {
//                            getCardTransactions(transactionDate, user);
//                        }
//                    });
//
//                } else {
//                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
//                    if (ListUtilsKt.isEmptyList(activity.getViewModel().getCardTransaction().getP2P()))
//                        toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
//                    else
//                        toggleVisibility(View.VISIBLE, View.GONE, View.GONE);
//
//                }
//                break;
            /* case 3:
             *//*toggleVisibility(View.GONE, View.VISIBLE, View.GONE);
                setOptions(getResources().getString(R.string.calling_card), R.drawable.ic_calling_card_white);
                recyclerView.setAdapter(new TransactionHistoryDetailAdapter(new ArrayList<>()));*//*
                toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                break;*/
//            case 3:
//                toggleVisibility(View.GONE, View.VISIBLE, View.GONE);
//                if (activity.getViewModel().getCardTransaction() == null){
//                    activity.getViewModel().getTransactionDate().observe(activity, transactionDate -> {
//                        toggleVisibility(View.GONE, View.VISIBLE, View.GONE);
//                        if (transactionDate != null) {
//                            getCardTransactions(transactionDate, user);
//                        }
//                    });
//
//                } else {
//                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
//                    recyclerView.setAdapter(new TransactionHistoryAdapter(new ArrayList<>(activity.getViewModel().getCardTransaction().getBillPayments())));
//                    if (ListUtilsKt.isEmptyList(activity.getViewModel().getCardTransaction().getBillPayments()))
//                        toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
//                    else
//                        toggleVisibility(View.VISIBLE, View.GONE, View.GONE);
//                }
//                break;
            case 4:
                if (activity.getViewModel().getSalariesCredited() == null) {
                    fetchSalariesCredited(user);
                    addSalariesObserver();
                } else {
                    List<Transactions> transactions = activity.getViewModel().getSalariesCredited();
                    if(transactions == null || ListUtilsKt.isEmptyList(transactions)) {
                        toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                        return;
                    }
                    recyclerView.setAdapter(new CardTransactionAdapter(transactions));
                    toggleVisibility(View.VISIBLE,View.GONE,View.GONE);
                }
                break;
        }
    }

    private void getCardTransactions(TransactionDate transactionDate, User user) {
        if (activity == null) return;

        activity.hideNoInternetView();
        if (!activity.isNetworkConnected()) {
            activity.showNoInternetView(() -> {
                getCardTransactions(transactionDate, user);
                return Unit.INSTANCE;
            });
            return;
        }

        activity.getViewModel().getCardTransactions(user.getToken(), user.getSessionId(), user.getRefreshToken(),
            user.getCustomerId(), transactionDate.getFromDate(), transactionDate.getToDate());
    }

    private void getLoanTransaction(String loanNumber, User user) {
        if (activity == null) return;

        activity.hideNoInternetView();
        if (!activity.isNetworkConnected()) {
            activity.showNoInternetView(() -> {
                getLoanTransaction(loanNumber, user);
                return Unit.INSTANCE;
            });
            return;
        }

        activity.getViewModel().getLoanTransaction(user.getToken(), user.getSessionId(), user.getRefreshToken(),
            user.getCustomerId());
    }

    private void getMoneyTransfer(TransactionDate transactionDate, User user) {
        if (activity == null) return;

        activity.hideNoInternetView();
        if (!activity.isNetworkConnected()) {
            activity.showNoInternetView(() -> {
                getMoneyTransfer(transactionDate, user);
                return Unit.INSTANCE;
            });
            return;
        }

        activity.getViewModel().getMoneyTransfer(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId(),
            transactionDate.getFromDate(),transactionDate.getToDate(),"moneytransfer");
    }

    private void getPayments(TransactionDate transactionDate, User user) {
        if (activity == null) return;

        activity.hideNoInternetView();
        if (!activity.isNetworkConnected()) {
            activity.showNoInternetView(() -> {
                getPayments(transactionDate, user);
                return Unit.INSTANCE;
            });
            return;
        }

        activity.getViewModel().getPayments(user.getToken(), user.getSessionId(), user.getRefreshToken(),
            user.getCustomerId(), transactionDate.getFromDate(),
            transactionDate.getToDate(),"billpayment");
    }

    private void fetchSalariesCredited(User user) {
        if (activity == null) return;

        activity.hideNoInternetView();
        if (!activity.isNetworkConnected()) {
            activity.showNoInternetView(() -> {
                fetchSalariesCredited(user);
                return Unit.INSTANCE;
            });
            return;
        }

        activity.getViewModel().fetchSalariesCredited(user.getToken(), user.getSessionId(),
            user.getRefreshToken(), user.getCustomerId());
    }

    private void toggleVisibility(int recyclerVisibility, int progressVisibility, int textVisibility) {
        recyclerView.setVisibility(recyclerVisibility);
        progressBar.setVisibility(progressVisibility);
        textView.setVisibility(textVisibility);
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
    }

    private void addCardObservable() {
        activity.getViewModel().getCardTransactions().observe(this, event -> {
            if (event == null) return;

            NetworkState2<TransactionHistory> cardState = event.peekContent();

            if (cardState == null) return;

            if (cardState instanceof NetworkState2.Loading) {
                showProgress();
                return;
            }

            hideProgress();

            if (cardState instanceof NetworkState2.Success) {
                TransactionHistory cardTransactions = ((NetworkState2.Success<TransactionHistory>) cardState).getData();
                if (cardTransactions == null) {
                    toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                    return;
                }
                if (ListUtilsKt.isEmptyList(cardTransactions.getTransactions())) {
                    activity.setCreditDebit(new CreditDebit(0.0, 0.0));
                    toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                } else {
                    onSuccess(cardTransactions);
                }

            } else if (cardState instanceof NetworkState2.Error) {
                toggleVisibility(View.GONE, View.GONE, View.GONE);

                NetworkState2.Error<TransactionHistory> error = (NetworkState2.Error<TransactionHistory>) cardState;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }

                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());

            } else if (cardState instanceof NetworkState2.Failure) {
                toggleVisibility(View.GONE, View.GONE, View.GONE);
                NetworkState2.Failure<TransactionHistory> failure = (NetworkState2.Failure<TransactionHistory>) cardState;
                activity.onFailure(activity.findViewById(R.id.root_view),  failure.getThrowable());
            } else {
                toggleVisibility(View.GONE, View.GONE, View.GONE);
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }

        });
    }

    private void onSuccess(TransactionHistory cardTransactions) {
        switch (index) {
            case 0: {
                activity.setCreditDebit(cardTransactions.getCreditDebit());

                if (cardTransactions.getTransactions().isEmpty()) {
                    toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                    return;
                }
                recyclerView.setAdapter(new TransactionHistoryAdapter(new ArrayList<>(cardTransactions.getTransactions()), listener));
                toggleVisibility(View.VISIBLE, View.GONE, View.GONE);
                break;
            }

            case 2: {
                activity.setCreditDebit(cardTransactions.getCreditDebit());
                if (cardTransactions.getP2P().isEmpty()) {
                    toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                    return;
                }
                recyclerView.setAdapter(new TransactionHistoryAdapter(new ArrayList<>(cardTransactions.getP2P()), listener));
                toggleVisibility(View.VISIBLE, View.GONE, View.GONE);
                break;
            }

            case 3: {
                activity.setCreditDebit(cardTransactions.getCreditDebit());
                if (cardTransactions.getBillPayments().isEmpty()) {
                    toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                    return;
                }
                recyclerView.setAdapter(new TransactionHistoryAdapter(new ArrayList<>(cardTransactions.getBillPayments()), listener));
                toggleVisibility(View.VISIBLE, View.GONE, View.GONE);
                break;
            }
        }
    }

    private void addObservable() {
        activity.getViewModel().getLoanTransactionState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<List<LoanTransaction>> loanState = event.getContentIfNotHandled();

            if (loanState == null) return;

            if (loanState instanceof NetworkState2.Loading) {
                return;
            }
            progressBar.setVisibility(View.GONE);

            if (loanState instanceof NetworkState2.Success) {
                List<LoanTransaction> loanTransactions = ((NetworkState2.Success<List<LoanTransaction>>) loanState).getData();
                if(ListUtilsKt.isNotEmptyList(loanTransactions)) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(new LoanTransactionAdapter(loanTransactions, listenerLoans));
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                }
            } else if (loanState instanceof NetworkState2.Error) {
                toggleVisibility(View.GONE, View.GONE, View.VISIBLE);

                NetworkState2.Error<List<LoanTransaction>> error = (NetworkState2.Error<List<LoanTransaction>>) loanState;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }

//                activity.onError(error.getMessage());
                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());

            } else if (loanState instanceof NetworkState2.Failure) {
                toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                 NetworkState2.Failure<List<LoanTransaction>> failure = (NetworkState2.Failure<List<LoanTransaction>>) loanState;
                activity.onFailure(activity.findViewById(R.id.root_view), failure.getThrowable());
            } else {
                toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void addSalariesObserver() {
        activity.getViewModel().getSalariesState().observe(this, event -> {
            if (activity == null) return;
            if (event == null) return;

            NetworkState2<CardTransactions> cardState = event.getContentIfNotHandled();
            if (cardState == null) return;

            if (cardState instanceof NetworkState2.Loading) {
                showProgress();
                return;
            }

            hideProgress();

            if (cardState instanceof NetworkState2.Success) {
                CardTransactions salariesCredited = ((NetworkState2.Success<CardTransactions>) cardState).getData();

                if (salariesCredited == null || ListUtilsKt.isEmptyList(salariesCredited.getTransactions())) {
                    toggleVisibility(View.GONE, View.GONE, View.VISIBLE);
                    return;
                }

                recyclerView.setAdapter(new CardTransactionAdapter(salariesCredited.getTransactions()));
                toggleVisibility(View.VISIBLE, View.GONE, View.GONE);
            } else if (cardState instanceof NetworkState2.Error) {
                toggleVisibility(View.GONE, View.GONE, View.GONE);

                NetworkState2.Error<CardTransactions> error = (NetworkState2.Error<CardTransactions>) cardState;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }

//                activity.onError(error.getMessage());
                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());

            } else if (cardState instanceof NetworkState2.Failure) {
                toggleVisibility(View.GONE, View.GONE, View.GONE);
                NetworkState2.Failure<CardTransactions> failure = (NetworkState2.Failure<CardTransactions>) cardState;
                activity.onFailure(activity.findViewById(R.id.root_view),  failure.getThrowable());
            } else {
                toggleVisibility(View.GONE, View.GONE, View.GONE);
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    public interface OnTransactionClickListener{
        void onTransactionClick(com.fh.payday.datasource.models.Transactions transaction);
    }

    public interface OnLoanTransactionClickListener{
        void onTransactionClick(LoanTransaction transaction);
    }

    public interface OnSalaryTransactionClickListener{
        void onTransactionClick(Transactions transaction);
    }

    /*private void setOptions(String title, int icon) {
        tvType.setText(title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            tvType.setCompoundDrawablesRelativeWithIntrinsicBounds(icon,0,0,0);
        } else {
            tvType.setCompoundDrawablesWithIntrinsicBounds(icon,0,0,0);
        }
    }*/
}
