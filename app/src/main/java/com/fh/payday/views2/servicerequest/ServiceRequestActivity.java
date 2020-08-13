package com.fh.payday.views2.servicerequest;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.datasource.models.CustomerSummary;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.AccessMatrix;
import com.fh.payday.utilities.AccessMatrixKt;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.ItemOffsetDecoration;
import com.fh.payday.utilities.ListUtilsKt;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.viewmodels.SettingOptionViewModel;
import com.fh.payday.views.adapter.OperatorAdapter;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;
import com.fh.payday.views2.settings.SettingOptionActivity;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class ServiceRequestActivity extends BaseActivity implements OnItemClickListener {

    private CardView cardView;
    private ProgressBar progressBar;
    private RecyclerView rvServiceRequest;
    private SettingOptionViewModel viewModel;
    private CustomerSummary summary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OperatorAdapter adapter = new OperatorAdapter(this, getItems(), 2);
        viewModel = ViewModelProviders.of(this).get(SettingOptionViewModel.class);
        rvServiceRequest.addItemDecoration(new ItemOffsetDecoration(2));
        rvServiceRequest.setLayoutManager(new GridLayoutManager(this, 2));
        rvServiceRequest.setAdapter(adapter);
        addObserver();
        handleBottomBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCustomerSummary();
    }

    private void getCustomerSummary() {
        hideNoInternetView();
        if (!isNetworkConnected()) {
            hideRV();
            showNoInternetView(() -> {
                getCustomerSummary();
                return Unit.INSTANCE;
            });
            return;
        }

        User user = UserPreferences.Companion.getInstance().getUser(this);
        if (user == null) return;

        long id = (long) user.getCustomerId();
        viewModel.fetchCustomerSummary(user.getToken(), user.getSessionId(), user.getRefreshToken(), id);
    }

    @Override
    public int getLayout() {
        return R.layout.activity_service_request;
    }

    @Override
    public void init() {
        cardView = findViewById(R.id.card_view);
        progressBar = findViewById(R.id.progress_bar);
        rvServiceRequest = findViewById(R.id.rv_service_request);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.service_requests));

        findViewById(R.id.toolbar_back).setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onItemClick(int index) {
        if (!hasAccess(index)) return;

        if (summary != null && summary.getEmiratesUpperLimitWarning() != null) {
            if (summary.getEmiratesUpperLimitWarning() != null && !summary.getEmiratesUpperLimitWarning().isEmpty() && index == 2 || index == 3) {
                showWarning(summary.getEmiratesUpperLimitWarning());
                return;
            }
        }

        if (index == 2) {
            Intent intent = new Intent(this, SettingOptionActivity.class);
            intent.putExtra("index", 12);
            startActivity(intent);
        } /*else if (index == 4) {

        }*/ else {
            Intent intent = new Intent(this, ServiceRequestOptionActivity.class);
            intent.putExtra("index", index);
            startActivity(intent);
        }
    }

    @Override
    public void showProgress() {
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
        progressBar.setVisibility(View.VISIBLE);
        rvServiceRequest.setVisibility(View.GONE);
    }

    @Override
    public void hideProgress() {
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.grey_400));
        progressBar.setVisibility(View.GONE);
        rvServiceRequest.setVisibility(View.VISIBLE);
    }

    private void hideRV() {
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
        rvServiceRequest.setVisibility(View.GONE);
    }

    private boolean hasAccess(int index) {
        Card card = summary != null && ListUtilsKt.isNotNullAndEmpty(summary.getCards())
                ? summary.getCards().get(0) : null;

        if (card == null) return false;

        switch (index) {
            case 0: {
                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getACTIVATE_CARD())) {
                    return true;
                }
                showCardStatusError(card.getCardStatus());
                return false;
            }
            case 1: {
                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getCARD_TRANSACTION_DISPUTE())) {
                    return true;
                }
                showCardStatusError(card.getCardStatus());
                return false;
            }
            case 2: {
                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getCHANGE_PIN())) {
                    return true;
                }
                showCardStatusError(card.getCardStatus());
                return false;
            }
            case 3: {
                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getBLOCK_CARD())) {
                    return true;
                }
                showCardStatusError(card.getCardStatus());
                return false;
            }
            default: {
                showCardStatusError(null);
                return false;
            }
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

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("serviceRequestHelp"));
    }

    private void addObserver() {

        viewModel.getCustomerSummaryState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<CustomerSummary> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                showProgress();
                return;
            }

            hideProgress();

            if (state instanceof NetworkState2.Success) {
                CustomerSummary data = ((NetworkState2.Success<CustomerSummary>) state).getData();
                if (data == null) return;
                summary = data;
            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;

                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                }
            } else if (state instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> failure = (NetworkState2.Failure<?>) state;
                onFailure(findViewById(R.id.root_view), failure.getThrowable());
            } else {
                onFailure(findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private List<Item> getItems() {
        List<Item> items = new ArrayList<>();

        TypedArray icons = getResources().obtainTypedArray(R.array.service_requests_icons);
        String[] list = getResources().getStringArray(R.array.service_request_array);

        for (int i = 0; i < list.length; i++) {
            items.add(new Item(list[i], icons.getResourceId(i, 0)));
        }

        icons.recycle();

        return items;
    }
}
