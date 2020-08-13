package com.fh.payday.views2.moneytransfer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.datasource.models.moneytransfer.ui.TransferSummary;
import com.fh.payday.utilities.CardUtilsKt;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.utilities.TextViewUtilsKt;
import com.fh.payday.viewmodels.TransferViewModel;
import com.fh.payday.views.adapter.MoneyTransferSummaryAdapter;

import java.util.ArrayList;
import java.util.List;

public class TransferSummaryFragment extends Fragment {

    private InternationalMoneyTransfer activity;
    private RecyclerView recyclerView;
    private TextView tvCardName, tvCardNumber, tvAmount, tvToname, tvToCard, tvToMobile, tvToAmount, tvIban, tvBalanceLeft,tvChargesVal,tvVatVal,tvTAmountVal,tvCharges,tvVat,tvTAmount;
    private View view3,view4,view5;
    private ImageView imgEditTo, imgEditAmount;
    private Card cards;
    private Double amount;

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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (activity != null) {
            activity.hideKeyboard();
        }

        if (isVisibleToUser) {
            setFromData();
            setToData(recyclerView);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer_summary, container, false);


        MaterialButton btnConfirm = view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
            TransferViewModel viewModel = activity.getViewModel();

            if (viewModel.getTransferType() != MoneyTransferType.PAYDAY) {
                if (viewModel.getAmountLeft() == null) return;
            }

            if ((viewModel.getTransferType() == MoneyTransferType.LOCAL
                    || viewModel.getTransferType() == MoneyTransferType.CC)
                    && Double.parseDouble(viewModel.getAmountLeft()) < 0.0 ) {

                activity.showMessage(
                            getString(R.string.not_enough_balance),
                            R.drawable.ic_error,
                            R.color.colorError
                        );
                return;
            }
            activity.onConfirmTransfer();
        });

        init(view);

        imgEditTo.setOnClickListener(v -> activity.changeBeneficiary());
        imgEditAmount.setOnClickListener(v -> {
            activity.onBeneficiarySelected();
        });

        return view;
    }

    private void init(View view) {
        tvCardName = view.findViewById(R.id.tv_card_name);
        tvCardNumber = view.findViewById(R.id.tv_card_number);
        tvAmount = view.findViewById(R.id.tv_amount);
        tvToname = view.findViewById(R.id.tv_to_card_user_name);
        tvToCard = view.findViewById(R.id.tv_to_card_number);
        tvToMobile = view.findViewById(R.id.tv_to_mobile);
        tvIban = view.findViewById(R.id.tv_iban);
        tvToAmount = view.findViewById(R.id.tv_total_amount);
        tvChargesVal = view.findViewById(R.id.tv_charges_value);
        tvVatVal = view.findViewById(R.id.tv_vat_value);
        tvTAmountVal = view.findViewById(R.id.tv_tamount_value);
        tvCharges = view.findViewById(R.id.tv_charges);
        tvVat = view.findViewById(R.id.tv_vat);
        tvTAmount = view.findViewById(R.id.tv_tamount);
        view3 = view.findViewById(R.id.view3);
        view4 = view.findViewById(R.id.view4);
        view5 = view.findViewById(R.id.view5);
        imgEditAmount = view.findViewById(R.id.iv_amount_edit);
        imgEditTo = view.findViewById(R.id.iv_to_edit);
        tvBalanceLeft = view.findViewById(R.id.tv_balance_left);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }
    private void setFromData() {

        if (activity == null || activity.getViewModel() == null) return;
        cards = activity.getCard();

        if (cards == null) return;

        tvCardName.setText(cards.getCardName());
        tvCardNumber.setText(CardUtilsKt.maskCardNumber(cards.getCardNumber(), "XXXX XXXX XXXX ####"));
        tvAmount.setText(String.format(getString(R.string.amount_in_aed),
                NumberFormatterKt.getDecimalValue(activity.getViewModel().getCardBalance())));

    }


    private void setToData(RecyclerView recyclerView) {

        if (activity == null || activity.getViewModel() == null || activity.getCard() == null) return;
        TransferViewModel model = activity.getViewModel();
        MoneyTransferType transferType = model.getTransferType();
        Bundle bundle = getArguments();
        Card card = activity.getCard();

        if (bundle == null || transferType == null) return;

        List<TransferSummary> transferSummaryList;
        switch (transferType) {
            case INTERNATIONAL:
                transferSummaryList = DataGenerator.getIntlTransferSummary();
                break;
            case LOCAL:
                setDetails(MoneyTransferType.LOCAL, model);
                if (model.getAmount() == null || model.getCardBalance() == null) return;
                amount = Double.parseDouble(model.getAmount()) + ConstantsKt.TRANSFER_CHARGES + ConstantsKt.VAT;

                transferSummaryList = getLocalSummary();
                transferSummaryList.add(new TransferSummary(getString(R.string.total_amount),
                        String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(amount.toString()))));

                tvBalanceLeft.setVisibility(View.VISIBLE);
                Double balanceLeft = Double.parseDouble(model.getCardBalance()) - amount;

                activity.getViewModel().setAmountLeft(balanceLeft.toString());
                tvBalanceLeft.setText(String.format(getString(R.string.balance_after_transfer),
                        String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(balanceLeft.toString()))));
                break;
            case PAYDAY:
                setDetails(MoneyTransferType.PAYDAY, model);
                transferSummaryList = getPaydaySummary();
                break;
            case CC:
                tvChargesVal.setVisibility(View.VISIBLE);
                tvVatVal.setVisibility(View.VISIBLE);
                tvTAmountVal.setVisibility(View.VISIBLE);
                tvCharges.setVisibility(View.VISIBLE);
                tvVat.setVisibility(View.VISIBLE);
                tvTAmount.setVisibility(View.VISIBLE);
                view3.setVisibility(View.VISIBLE);
                view4.setVisibility(View.VISIBLE);
                view5.setVisibility(View.VISIBLE);

                setDetails(MoneyTransferType.CC, model);
                if (model.getAmount() == null || model.getCardBalance() == null) return;

                amount = Double.parseDouble(model.getAmount()) + ConstantsKt.TRANSFER_CHARGES + ConstantsKt.VAT;

                transferSummaryList = getCCSummary();
                transferSummaryList.add(new TransferSummary(getString(R.string.total_amount),
                        String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(amount.toString()))));
                tvTAmountVal.setText(String.format(getString(R.string.amount_in_aed),
                    NumberFormatterKt.getDecimalValue(amount.toString())));
                Double balanceLeft2 = Double.parseDouble(model.getCardBalance()) - amount;

                tvBalanceLeft.setVisibility(View.VISIBLE);
                activity.getViewModel().setAmountLeft(balanceLeft2.toString());

                tvBalanceLeft.setText(String.format(getString(R.string.balance_after_transfer),
                        String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(balanceLeft2.toString()))));

                break;
            default:
                transferSummaryList = new ArrayList<>();
        }

        if (!transferSummaryList.isEmpty()) {

            MoneyTransferSummaryAdapter moneyTransferSummaryAdapter = new MoneyTransferSummaryAdapter(transferSummaryList, transferSummaryList.size());
            recyclerView.setAdapter(moneyTransferSummaryAdapter);
        }
    }

    private void setDetails(MoneyTransferType type, TransferViewModel model) {
        if (model == null) return;
        switch (type) {
            case INTERNATIONAL:
                break;
            case PAYDAY:
                if (model.getSelectedP2PBeneficiary() == null || model.getAmount() == null) return;
                tvToname.setText(model.getSelectedP2PBeneficiary().getBeneficiaryName());
                TextViewUtilsKt.replaceZero(tvToCard,model.getSelectedP2PBeneficiary().getMobileNumber());
                tvToMobile.setText(String.format(getString(R.string.amount_in_aed),
                        NumberFormatterKt.getDecimalValue(model.getAmount())));
                tvToAmount.setText(String.format(getString(R.string.amount_in_aed),
                        NumberFormatterKt.getDecimalValue(model.getAmount())));
                break;
            case LOCAL:
                if (model.getSelectedLocalBeneficiary() == null || model.getAmount() == null) return;

                imgEditAmount.setVisibility(View.GONE);
                imgEditTo.setVisibility(View.GONE);

                tvToname.setText(model.getSelectedLocalBeneficiary().getBeneficiaryName());
                tvToCard.setText(model.getSelectedLocalBeneficiary().getBank());
                tvToMobile.setText(model.getSelectedLocalBeneficiary().getAccountNo());
                tvIban.setVisibility(View.VISIBLE);
                tvIban.setText(model.getSelectedLocalBeneficiary().getIBAN());
                tvToAmount.setText(String.format(getString(R.string.amount_in_aed),
                        NumberFormatterKt.getDecimalValue(model.getAmount())));
                break;
            case CC:
                if (model.getSelectedP2CBeneficiary() == null || model.getAmount() == null) return;

                imgEditAmount.setVisibility(View.GONE);
                imgEditTo.setVisibility(View.GONE);
                tvIban.setVisibility(View.GONE);
                tvToMobile.setVisibility(View.GONE);
                tvToname.setText(model.getSelectedP2CBeneficiary().getShortName());
                tvToCard.setText(CardUtilsKt.maskCardNumber(model.getSelectedP2CBeneficiary().getCreditCardNo(), "XXXX XXXX XXXX ####"));
                tvToAmount.setText(String.format(getString(R.string.amount_in_aed),
                        NumberFormatterKt.getDecimalValue(model.getAmount())));
                break;
        }
    }

    private List<TransferSummary> getLocalSummary() {
        List<TransferSummary> list = new ArrayList<>();
        list.add(new TransferSummary(getString(R.string.charges), String.format(getString(R.string.amount_in_aed), String.valueOf(ConstantsKt.TRANSFER_CHARGES))));
        list.add(new TransferSummary(getString(R.string.vat), String.format(getString(R.string.amount_in_aed), String.valueOf(ConstantsKt.VAT))));

        if (activity.getViewModel().getPurpose() != null)
            list.add(new TransferSummary(getString(R.string.description), activity.getViewModel().getPurpose()));

        return list;
    }

    private List<TransferSummary> getPaydaySummary() {

        List<TransferSummary> list = new ArrayList<>();
        if (activity.getViewModel().getPurpose() != null)
            list.add(new TransferSummary(getString(R.string.description), activity.getViewModel().getPurpose()));        return list;
    }

    private List<TransferSummary> getCCSummary() {
        tvChargesVal.setVisibility(View.VISIBLE);
        tvVatVal.setVisibility(View.VISIBLE);
        tvChargesVal.setText(String.format(getString(R.string.amount_in_aed),
            NumberFormatterKt.getDecimalValue(ConstantsKt.TRANSFER_CHARGES)));
        tvVatVal.setText(String.format(getString(R.string.amount_in_aed),
            NumberFormatterKt.getDecimalValue(ConstantsKt.VAT)));
        List<TransferSummary> list = new ArrayList<>();
        list.add(new TransferSummary(getString(R.string.charges), String.format(getString(R.string.amount_in_aed),
                String.valueOf(ConstantsKt.TRANSFER_CHARGES))));
        list.add(new TransferSummary(getString(R.string.vat), String.format(getString(R.string.amount_in_aed), String.valueOf(ConstantsKt.VAT))));
        if (activity.getViewModel().getPurpose() != null)
            list.add(new TransferSummary(getString(R.string.description), activity.getViewModel().getPurpose()));
        return list;
    }
}
