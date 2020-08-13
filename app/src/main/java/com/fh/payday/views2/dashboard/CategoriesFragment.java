package com.fh.payday.views2.dashboard;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.datasource.models.CustomerSummary;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.datasource.models.Loan;
import com.fh.payday.utilities.AccessMatrix;
import com.fh.payday.utilities.AccessMatrixKt;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.ListUtilsKt;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.viewmodels.MainViewModel;
import com.fh.payday.views.adapter.dashboard.ServiceAdapter;
import com.fh.payday.views2.cardservices.CardServiceActivity;
import com.fh.payday.views2.intlRemittance.InternationalRemittanceActivity;
import com.fh.payday.views2.loan.LoanServicesActivity;
import com.fh.payday.views2.moneytransfer.InternationalMoneyTransfer;
import com.fh.payday.views2.moneytransfer.MoneyTransferActivity;
import com.fh.payday.views2.moneytransfer.MoneyTransferType;
import com.fh.payday.views2.offer.OfferActivity;
import com.fh.payday.views2.payments.PaymentActivity;
import com.fh.payday.views2.payments.recharge.MobileRechargeActivity;
import com.fh.payday.views2.transactionhistoryv2.TransactionHistoryV2Activity;

import java.util.List;

public class CategoriesFragment extends Fragment {

    private RecyclerView rvItemList;
    private LinearLayoutManager layoutManager;
    private ImageView imgPrev, imgNext;
    private RecyclerView.SmoothScroller smoothScroller;

    private OnItemClickListener listenerCategories = this::setCategories;
    private OnItemClickListener listenerFrequent = this::setFrequentCategories;

    private MainViewModel viewModel;
    private MainActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        viewModel = ViewModelProviders.of((MainActivity) context).get(MainViewModel.class);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    private View.OnClickListener scrollCategoryListener = view -> {
        int start = layoutManager.findFirstVisibleItemPosition() - 1 < 0 ? 0 : layoutManager.findFirstVisibleItemPosition() - 1;
        if (view.getId() == R.id.img_prev) {

            smoothScroller.setTargetPosition(start);
            rvItemList.getLayoutManager().startSmoothScroll(smoothScroller);
        } else if (view.getId() == R.id.img_next) {

            smoothScroller.setTargetPosition(layoutManager.findLastVisibleItemPosition() + 1);
            rvItemList.getLayoutManager().startSmoothScroll(smoothScroller);
        }
    };

    private View.OnClickListener scrollFrequentListener = view -> {

        int start = layoutManager.findFirstVisibleItemPosition() - 1 < 0 ? 0 : layoutManager.findFirstVisibleItemPosition() - 1;
        if (view.getId() == R.id.img_prev) {

            smoothScroller.setTargetPosition(start);
            rvItemList.getLayoutManager().startSmoothScroll(smoothScroller);
        } else if (view.getId() == R.id.img_next) {

            smoothScroller.setTargetPosition(layoutManager.findLastVisibleItemPosition() + 1);
            rvItemList.getLayoutManager().startSmoothScroll(smoothScroller);
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        init(view);
        List<Item> itemList;
        OnItemClickListener listener;
        View.OnClickListener scrollListener;

        smoothScroller = new LinearSmoothScroller(getContext()) {
            @Override
            protected int getHorizontalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_ANY;
            }
        };

        int i = getArguments().getInt("position");

        if (i == 0) {

            itemList = DataGenerator.getServices(getContext());
            listener = listenerFrequent;
            scrollListener = scrollFrequentListener;
        } else {

            itemList = DataGenerator.getFrequentServices(getContext());
            listener = listenerCategories;
            scrollListener = scrollCategoryListener;
        }


        setRecyclerView(itemList, listener);
        imgNext.setOnClickListener(scrollListener);
        imgPrev.setOnClickListener(scrollListener);

        return view;
    }

    private void setRecyclerView(List<Item> list, OnItemClickListener listener) {
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvItemList.setLayoutManager(layoutManager);
        rvItemList.setAdapter(new ServiceAdapter(listener, list));

        rvItemList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);


            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    imgPrev.setVisibility(View.INVISIBLE);
                } else {
                    imgPrev.setVisibility(View.VISIBLE);
                }

                if (layoutManager.findLastCompletelyVisibleItemPosition() == list.size() - 1) {
                    imgNext.setVisibility(View.INVISIBLE);
                } else {
                    imgNext.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private void setFrequentCategories(int index) {
        CustomerSummary summary = viewModel.getCustomerSummary();

        if (summary == null) return;
        if (summary.getEmiratesUpperLimitWarning() != null && !summary.getEmiratesUpperLimitWarning().isEmpty()) {
            activity.showWarning(summary.getEmiratesUpperLimitWarning());
            return;
        }
        if (ListUtilsKt.isEmptyList(summary.getCards())) {
            activity.showWarning(getString(R.string.loan_customer_warning));
            return;
        }

        Intent intent;
        switch (index) {
            case 0:
                if (getCard() == null) return;
                if (AccessMatrixKt.hasAccess(getCard().getCardStatus(), AccessMatrix.Companion.getINTERNATIOM_REMITTANCE())) {
                    intent = new Intent(getContext(), InternationalRemittanceActivity.class);
                    intent.putExtra("card", getCard());
                    startActivity(intent);
                } else {
                    activity.showCardStatusError(getCard().getCardStatus());
                }
                break;
            case 1:
                if (getCard() == null) return;
                if (AccessMatrixKt.hasAccess(getCard().getCardStatus(), AccessMatrix.Companion.getUTILITY_PAYMENT_LOCAL())) {
                    intent = new Intent(getContext(), MobileRechargeActivity.class);
                    intent.putExtra("operator", "du");
                    startActivity(intent);
                } else
                    activity.showCardStatusError(getCard().getCardStatus());
                break;
            case 2:
                if (getCard() == null) return;
                if (AccessMatrixKt.hasAccess(getCard().getCardStatus(), AccessMatrix.Companion.getUTILITY_PAYMENT_LOCAL())) {
                    intent = new Intent(getContext(), MobileRechargeActivity.class);
                    intent.putExtra("operator", "etisalat");
                    startActivity(intent);
                } else
                    activity.showCardStatusError(getCard().getCardStatus());
                break;
            case 3:
                intent = new Intent(getContext(), InternationalMoneyTransfer.class);
                intent.putExtra("title", MoneyTransferType.PAYDAY);
                if (getCard() == null) return;

                if (AccessMatrixKt.hasAccess(getCard().getCardStatus(), AccessMatrix.Companion.getP2P_SENDER())) {
                    intent.putExtra("card", getCard());
                    startActivity(intent);
                } else {
                    activity.showCardStatusError(getCard().getCardStatus());
                }
                break;
            case 4:
                if (AccessMatrixKt.hasAccess(getCard().getCardStatus(), AccessMatrix.Companion.getUTILITY_PAYMENT_LOCAL())) {
                    Intent intentIndiaPayment = new Intent(getContext(), PaymentActivity.class);
                    intentIndiaPayment.putExtra("index", 3);
                    startActivity(intentIndiaPayment);
                } else {
                    activity.showCardStatusError(getCard().getCardStatus());
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid index for activity");
        }

    }

    private void setCategories(int index) {
        CustomerSummary summary = viewModel.getCustomerSummary();
        String cardStatus = "";
        if (summary == null) return;
        if (ListUtilsKt.isEmptyList(summary.getCards()) && (index == 0 || index == 1 || index == 3 || index == 4 || index == 2)) {
            activity.showWarning(getString(R.string.loan_customer_warning));
            return;
        } else if (!ListUtilsKt.isEmptyList(summary.getCards()))
             cardStatus = summary.getCards().get(0).getCardStatus();
        Intent i;
        switch (index) {
            case 0:
                if (AccessMatrixKt.hasAccess(cardStatus, AccessMatrix.Companion.getTRANSACTION_HISTORY())) {
                    i = new Intent(getActivity(), TransactionHistoryV2Activity.class);
                    List<Card> cards = viewModel.getCustomerSummary().getCards();
                    List<Loan> loans = viewModel.getCustomerSummary().getLoans();

                    if (ListUtilsKt.isNotEmptyList(loans)) {
                        i.putExtra("loanNumber", loans.get(0).getLoanNumber());
                    }
                    if (ListUtilsKt.isNotEmptyList(cards)) {
                        i.putExtra("availableBalance", cards.get(0).getAvailableBalance());
                        i.putExtra("availableOverdraft", cards.get(0).getOverdraftLimit() != null ? cards.get(0).getOverdraftLimit() : "0.00");
                    }
                    startActivity(i);
                } else activity.showCardStatusError(cardStatus);

                break;
            case 1: {
                if (summary.getEmiratesUpperLimitWarning() != null && !summary.getEmiratesUpperLimitWarning().isEmpty()) {
                    activity.showWarning(summary.getEmiratesUpperLimitWarning());
                    return;
                }
                if (AccessMatrixKt.hasAccess(cardStatus, AccessMatrix.Companion.getUTILITY_PAYMENT_LOCAL())) {
                    i = new Intent(getActivity(), PaymentActivity.class);
                    startActivity(i);
                } else activity.showCardStatusError(cardStatus);

                break;
            }
            case 5:
                i = new Intent(getActivity(), LoanServicesActivity.class);

                if (viewModel.getCustomerSummary() != null) {
                    i.putExtra("summary", viewModel.getCustomerSummary());
                    List<Loan> loans = viewModel.getCustomerSummary().getLoans();
                    if (ListUtilsKt.isNotEmptyList(viewModel.getCustomerSummary().getLoans())) {
                        i.putExtra("loan_number", loans.get(0).getLoanNumber());
                    }
                }
                startActivity(i);
                break;
            case 2:
                if (summary.getEmiratesUpperLimitWarning() != null && !summary.getEmiratesUpperLimitWarning().isEmpty()) {
                    activity.showWarning(summary.getEmiratesUpperLimitWarning());
                    return;
                }
                if (getCard() != null) {
                    if (AccessMatrixKt.hasAccess(getCard().getCardStatus(), AccessMatrix.Companion.getLOCAL_MONEY_TRANSFER_TO_ACCOUNT())) {
                        i = new Intent(getActivity(), MoneyTransferActivity.class);
                        i.putExtra("card", getCard());
                        startActivity(i);
                    }else activity.showCardStatusError(getCard().getCardStatus());
                }
                break;
            case 3:
                if (getCard() != null) {
                    i = new Intent(getActivity(), CardServiceActivity.class);
                    i.putExtra("card", getCard());
                    i.putExtra("loan", getLoan());
                    i.putExtra("summary", summary);
                    startActivity(i);
                }
                break;
            case 4:
                if (ListUtilsKt.isEmptyList(summary.getCards()) || AccessMatrixKt.hasAccess(cardStatus, AccessMatrix.Companion.getOFFERS())) {
                    i = new Intent(getActivity(), OfferActivity.class);
                    i.putExtra("offers", summary);
                    startActivity(i);
                } else activity.showCardStatusError(cardStatus);
                break;
        }
    }

    private void init(View view) {
        rvItemList = view.findViewById(R.id.rv_categories);
        imgPrev = view.findViewById(R.id.img_prev);
        imgNext = view.findViewById(R.id.img_next);

        ImageViewCompat.setImageTintList(imgPrev, ColorStateList.valueOf(ContextCompat
            .getColor(imgPrev.getContext(), R.color.white)));
        ImageViewCompat.setImageTintList(imgNext, ColorStateList.valueOf(ContextCompat
            .getColor(imgNext.getContext(), R.color.white)));
    }

    @Nullable
    private Card getCard() {
        if (viewModel.getCustomerSummary() != null && ListUtilsKt.isNotEmptyList(viewModel.getCustomerSummary().getCards())) {
            return viewModel.getCustomerSummary().getCards().get(0);
        }

        return null;
    }

    @Nullable
    private Loan getLoan() {
        if (viewModel.getCustomerSummary() != null && ListUtilsKt.isNotEmptyList(viewModel.getCustomerSummary().getLoans())) {
            return viewModel.getCustomerSummary().getLoans().get(0);
        }

        return null;
    }

}
