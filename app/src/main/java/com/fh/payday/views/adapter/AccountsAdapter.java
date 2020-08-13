package com.fh.payday.views.adapter;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.utilities.CardUtilsKt;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.utilities.OnCardSelectedListener;
import com.fh.payday.utilities.OnItemClickListener;

import java.util.List;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.ViewHolder> {

    private final OnCardSelectedListener listener;
    private final List<Card> cards;

    public AccountsAdapter(OnCardSelectedListener listener, List<Card> cards) {
        this.listener = listener;
        this.cards = cards;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.accounts_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.rootView.setOnClickListener(view -> listener.onCardSelected(position, cards.get(position)));
        holder.bind(cards.get(position));
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout rootView;
        private TextView tvCardName;
        private TextView tvCardNumber;
        private TextView tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.root_view);

            tvCardName = itemView.findViewById(R.id.tv_card_user_name);
            tvCardNumber = itemView.findViewById(R.id.tv_card_number);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }

        private void bind(Card card) {
            tvCardName.setText(card.getCardName());
            tvCardNumber.setText(CardUtilsKt.maskCardNumber(card.getCardNumber(), "#### XXXX XXXX ####"));
            tvAmount.setText(String.format(tvAmount.getContext().getString(R.string.amount_in_aed),
                NumberFormatterKt.getDecimalValue(card.getAvailableBalance())));
        }
    }
}
