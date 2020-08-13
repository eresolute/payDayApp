package com.fh.payday.views2.moneytransfer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.datasource.models.CustomerSummary;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.ListUtilsKt;
import com.fh.payday.utilities.OnCardSelectedListener;
import com.fh.payday.views.adapter.AccountsAdapter;

public class AccountsFragment extends Fragment {

    private InternationalMoneyTransfer activity;
    MoneyTransferType transferType;
    private RecyclerView recyclerView;

    private OnCardSelectedListener listener = (index, card) -> {

        if (transferType == null) return;
        if (activity == null || activity.getViewModel() == null) return;

        switch (transferType){
            case INTERNATIONAL:
                break;
            case LOCAL:
                break;
            case PAYDAY:
                activity.getViewModel().setSelectedCard(card);
//                activity.onAccountSelected();
                break;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (InternationalMoneyTransfer) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) return;
        transferType = activity.getViewModel().getTransferType();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_from_account, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        AccountsAdapter accountsFragment = new AccountsAdapter(listener, activity.getViewModel().getCards());
        recyclerView.setAdapter(accountsFragment);

        User user = new UserPreferences().getUser(activity);
        if (user != null && ListUtilsKt.isEmptyList(activity.getViewModel().getCards())) {
            activity.getViewModel().fetchCustomerSummary(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
            addObserver();
        }
        return view;
    }

    private void addObserver() {
        activity.getViewModel().getCustomerSummaryState().observe(this, event -> {
            if(event == null) return;

            NetworkState2<CustomerSummary> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (state instanceof NetworkState2.Success) {
               CustomerSummary data = ((NetworkState2.Success<CustomerSummary>) state).getData();
               if (data == null) return;

               if (data.getCards().size() > 0) {
                   if (recyclerView.getAdapter() != null)
                       recyclerView.getAdapter().notifyDataSetChanged();

//                   activity.getViewModel().setSelectedCard(data.getCards().get(0));
                   activity.getViewModel().setSelectedCard(activity.getCard());
//                   activity.onAccountSelected();
               }
            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;

                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
                try {
                    int code = Integer.parseInt(error.getMessage());
                    updateCredentials(code);

                } catch (NumberFormatException e) {
                    activity.onError(error.getMessage());
                }

            } else if (state instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> error = (NetworkState2.Failure<?>) state;
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void updateCredentials(int code) {
        if(code == 399) {
            activity.onError("Your Emirates Id is expired or about to expire");
        }
    }
}
