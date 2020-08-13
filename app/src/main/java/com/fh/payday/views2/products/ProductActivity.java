package com.fh.payday.views2.products;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.datasource.models.CustomerSummary;
import com.fh.payday.utilities.AccessMatrix;
import com.fh.payday.utilities.AccessMatrixKt;
import com.fh.payday.utilities.ListUtilsKt;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.viewmodels.LoanViewModel;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.loan.apply.ApplyLoanActivity;
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment;

import java.util.Map;

import kotlin.Unit;

import static com.fh.payday.utilities.UrlUtilsKt.LOAN_TC_URL;

public class ProductActivity extends BaseActivity implements OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayout() {
        return R.layout.activity_product;
    }

    @Override
    public void init() {
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.products));

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        CustomerSummary summary = getIntent().getParcelableExtra("data");

        if (summary != null) {
            if (ListUtilsKt.isEmptyList(summary.getCards()) || (!ListUtilsKt.isEmptyList(summary.getCards()) && !ListUtilsKt.isEmptyList(summary.getLoans())))
                recyclerView.setAdapter(new ProductAdapter(DataGenerator.getProducts(this, false), this));
            else
                recyclerView.setAdapter(new ProductAdapter(DataGenerator.getProducts(this, true), this));

        } else {
            finish();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        handleBottomBar();
    }

    @Override
    public void onItemClick(int index) {
        switch (index) {
            case 0: {
                CustomerSummary summary = getIntent().getParcelableExtra("data");
                AccessMatrix.Companion.getAPPLY_LOAN();
                Map<String, Boolean> service;
                if (summary != null) {
                    if (ListUtilsKt.isEmptyList(summary.getCards()) || (!ListUtilsKt.isEmptyList(summary.getCards()) && !ListUtilsKt.isEmptyList(summary.getLoans()))) {
                        service = AccessMatrix.Companion.getAPPLY_TOPUP_LOAN();
                    } else
                        service = AccessMatrix.Companion.getAPPLY_LOAN();
                    if (!ListUtilsKt.isEmptyList(summary.getCards()) && !ListUtilsKt.isEmptyList(summary.getLoans())) {
                        if (AccessMatrixKt.hasAccess(summary.getCards().get(0).getCardStatus(), service)) {
                            showTermsConditions(summary);
                        } else showCardStatusError(summary.getCards().get(0).getCardStatus());

                    } else if ((ListUtilsKt.isEmptyList(summary.getCards())))
                        showTermsConditions(summary);
                    else {
                        if (AccessMatrixKt.hasAccess(summary.getCards().get(0).getCardStatus(), service)) {
                            showTermsConditions(summary);
                        } else showCardStatusError(summary.getCards().get(0).getCardStatus());
                    }
                }
                break;
            }
            default: {
                /*Intent intent = new Intent(this, ProductOptionActivity.class);
                intent.putExtra("index", index);
                startActivity(intent);*/
            }
        }
    }

    private void showTermsConditions(CustomerSummary summary) {
        TermsAndConditionsDialog dialog = new TermsAndConditionsDialog.Builder()
                .setTermsConditionsLink(true)
                .attachPositiveListener(() -> {
                    if (summary != null) {
                        if (ListUtilsKt.isEmptyList(summary.getCards()) || (!ListUtilsKt.isEmptyList(summary.getCards()) && !ListUtilsKt.isEmptyList(summary.getLoans()))) {
                            Intent intent = new Intent(this, ApplyLoanActivity.class);
                            intent.putExtra("product_type", LoanViewModel.TOPUP_LOAN)
                                    .putExtra("loan_number", summary.getLoans().get(0).getLoanNumber());
                            startActivity(intent);
                        } else
                            startActivity(new Intent(this, ApplyLoanActivity.class));

                    }
                    return Unit.INSTANCE;
                })
                .attachLinkListener(() -> {
                    TermsConditionsDialogFragment termsAndConditionsDialog = TermsConditionsDialogFragment
                            .Companion.newInstance(LOAN_TC_URL, getString(R.string.close));
                    termsAndConditionsDialog.show(getSupportFragmentManager(), "dialog");
                    return Unit.INSTANCE;
                })
                .build();
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "dialog");

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

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startActivity(new Intent(v.getContext(), HelpActivity.class)));
    }

}
