package com.fh.payday.views2.moneytransfer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.BalanceEventBus;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.utilities.AccessMatrix;
import com.fh.payday.utilities.AccessMatrixKt;
import com.fh.payday.utilities.ItemOffsetDecoration;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.views.adapter.OperatorAdapter;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.intlRemittance.InternationalRemittanceActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class MoneyTransferActivity extends BaseActivity implements View.OnClickListener, OnItemClickListener {

    public static final int TRANSFER_CODE = 100;
    TextView toolbarTitle, imgEditBeneficiary, imgCCTransfer, imgLocalTransfer, imgAddBeneficiary, imgPayday, tvCardBalance;
    ImageView toolbarBack;
    Card card;
    RecyclerView rvOptions;
    private String balanceLeft = null;

    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbarTitle.setText(getString(R.string.money_transfer));

        toolbarBack.setOnClickListener(this);
        imgEditBeneficiary.setOnClickListener(this);
        imgAddBeneficiary.setOnClickListener(this);

        Disposable d = BalanceEventBus.INSTANCE
            .toObservable()
            .subscribe(newBalance -> {
                balanceLeft = String.valueOf(newBalance);
                tvCardBalance.setText(String.format(getString(R.string.amount_in_aed),
                    NumberFormatterKt.getDecimalValue(balanceLeft)));
            });

        disposable.add(d);
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_money_transfer;
    }

    @Override
    public void init() {
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarBack = findViewById(R.id.toolbar_back);
        imgEditBeneficiary = findViewById(R.id.img_edit_beneficiary);
        imgAddBeneficiary = findViewById(R.id.img_add_beneficiary);
        rvOptions = findViewById(R.id.rv_trans_options);
//        imgCCTransfer = findViewById(R.id.img_cc_transfer);
//        imgLocalTransfer = findViewById(R.id.img_local_transfer);
//        imgPayday = findViewById(R.id.img_payday_payday);

        setOptionsList();
        handleCardInfo();
        handleBottomBar();
    }

    private void setOptionsList() {
        rvOptions.setAdapter(new OperatorAdapter(this, getItems(), 2));
        rvOptions.addItemDecoration(new ItemOffsetDecoration(3));
        rvOptions.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private List<Item> getItems() {
        String []items = getResources().getStringArray(R.array.money_transfer_options);
        TypedArray icons = getResources().obtainTypedArray(R.array.icon_money_transfer);
        ArrayList<Item> itemList = new ArrayList<>();

        for (int i=0; i < items.length; i++) {
            itemList.add(new Item(items[i], icons.getResourceId(i, 0)));
        }

        icons.recycle();
        itemList.add(new Item("", 0));
        return itemList;
    }

    private void handleCardInfo() {
        tvCardBalance = findViewById(R.id.tv_card_balance);

        card = getIntent().getParcelableExtra("card");
        if (card != null) {
            tvCardBalance.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(card.getAvailableBalance())));
        } else {
            tvCardBalance.setText(String.format(getString(R.string.amount_in_aed), "0.00"));
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

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("moneyTransferHelp"));
    }

//    @Override
//    public void onClick(View view) {
//        Intent intentTransfer = new Intent(view.getContext(), InternationalMoneyTransfer.class);
//        Intent intentBeneficiary = new Intent(this, EditBeneficiaryActivity.class);
//
//        switch (view.getId()) {
//
//            case R.id.toolbar_back:
//                onBackPressed();
//                break;
//
//            case R.id.img_edit_beneficiary:
//                intentBeneficiary.putExtra("index", EditBeneficiaryActivity.OPTION.EDIT);
//                startActivity(intentBeneficiary);
//                break;
//
//            case R.id.img_add_beneficiary:
//                intentBeneficiary.putExtra("index", EditBeneficiaryActivity.OPTION.ADD);
//                startActivity(intentBeneficiary);
//                break;
//
//            case R.id.img_cc_transfer:
//                intentTransfer.putExtra("title", MoneyTransferType.CC);
//                intentTransfer.putExtra("card", card);
//
//                if (balanceLeft != null) {
//                    intentTransfer.putExtra("balanceLeft", balanceLeft);
//                }
//
//                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getLOCAL_MONEY_TRANSFER_TO_CREDIT_CARD())) {
//                    startActivityForResult(intentTransfer, TRANSFER_CODE);
//                } else {
//                    showCardStatusError(card.getCardStatus());
//                }
//                break;
//
//            case R.id.img_local_transfer:
//                intentTransfer.putExtra("title", MoneyTransferType.LOCAL);
//                intentTransfer.putExtra("card", card);
//                if (balanceLeft != null) {
//                    intentTransfer.putExtra("balanceLeft", balanceLeft);
//                }
//
//                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getLOCAL_MONEY_TRANSFER_TO_ACCOUNT())) {
//                    startActivityForResult(intentTransfer, TRANSFER_CODE);
//                } else {
//                    showCardStatusError(card.getCardStatus());
//                }
//                break;
//
//            case R.id.img_payday_payday:
//                intentTransfer.putExtra("title", MoneyTransferType.PAYDAY);
//                intentTransfer.putExtra("card", card);
//
//                if (balanceLeft != null) {
//                    intentTransfer.putExtra("balanceLeft", balanceLeft);
//                }
//
//                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getP2P_SENDER())) {
//                    startActivityForResult(intentTransfer, TRANSFER_CODE);
//                } else {
//                    showCardStatusError(card.getCardStatus());
//                }
//                break;
//
//            case R.id.transaction_history:
//                Intent thIntent = new Intent(this, TransactionHistoryActivity.class);
//                thIntent.putExtra("card", card);
//                if (balanceLeft != null) {
//                    thIntent.putExtra("balanceLeft", balanceLeft);
//                }
//                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getTRANSACTION_HISTORY())) {
//                    startActivity(thIntent);
//                } else {
//                    showCardStatusError(card.getCardStatus());
//                }
//                break;
//
//            case R.id.btm_menu_support:
//                startActivity(new Intent(view.getContext(), SupportActivity.class));
//                break;
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == TRANSFER_CODE) {
            if (data == null) return;
            balanceLeft = String.valueOf(data.getDoubleExtra("balance", 0.00));
            tvCardBalance.setText(String.format(getString(R.string.amount_in_aed),
                    NumberFormatterKt.getDecimalValue(data.getDoubleExtra("balance", 0.00))));
        }
    }

    @Override
    public void onItemClick(int index) {
        Intent intentTransfer = new Intent(this, InternationalMoneyTransfer.class);

        switch (index) {
            case 0:
                Intent intent = new Intent(this, InternationalRemittanceActivity.class);
                intent.putExtra("card", card);
                startActivityForResult(intent, TRANSFER_CODE);
                break;
            case 1:
                intentTransfer.putExtra("title", MoneyTransferType.PAYDAY);
                intentTransfer.putExtra("card", card);

                if (balanceLeft != null) {
                    intentTransfer.putExtra("balanceLeft", balanceLeft);
                }

                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getP2P_SENDER())) {
                    startActivityForResult(intentTransfer, TRANSFER_CODE);
                } else {
                    showCardStatusError(card.getCardStatus());
                }
                break;
            case 2:
                intentTransfer.putExtra("title", MoneyTransferType.LOCAL);
                intentTransfer.putExtra("card", card);
                if (balanceLeft != null) {
                    intentTransfer.putExtra("balanceLeft", balanceLeft);
                }

                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getLOCAL_MONEY_TRANSFER_TO_ACCOUNT())) {
                    startActivityForResult(intentTransfer, TRANSFER_CODE);
                } else {
                    showCardStatusError(card.getCardStatus());
                }
                break;
            case 3:
                intentTransfer.putExtra("title", MoneyTransferType.CC);
                intentTransfer.putExtra("card", card);

                if (balanceLeft != null) {
                    intentTransfer.putExtra("balanceLeft", balanceLeft);
                }

                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getLOCAL_MONEY_TRANSFER_TO_CREDIT_CARD())) {
                    startActivityForResult(intentTransfer, TRANSFER_CODE);
                } else {
                    showCardStatusError(card.getCardStatus());
                }
                break;

            case 4:
                Intent thIntent = new Intent(this, TransactionHistoryActivity.class);
                thIntent.putExtra("card", card);
                if (balanceLeft != null) {
                    thIntent.putExtra("balanceLeft", balanceLeft);
                }
                if (AccessMatrixKt.hasAccess(card.getCardStatus(), AccessMatrix.Companion.getTRANSACTION_HISTORY())) {
                    startActivity(thIntent);
                } else {
                    showCardStatusError(card.getCardStatus());
                }
                break;
        }
    }
}
