package com.fh.payday.views2.transactionhistory;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.views.shared.IconListAdapter;

import java.util.List;

public class TransactionHistoryFragment extends Fragment implements OnItemClickListener {
    RecyclerView recyclerView;
    List<Item> listItem;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_history, container, false);
        init(view);
        recyclerView.setAdapter(new IconListAdapter(listItem, this));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return view;
    }

    @Override
    public void onItemClick(int index) {
        Fragment fragment = new TransactionHistoryOptionFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("index",index);
        fragment.setArguments(bundle);
        ((TransactionHistoryActivity)getActivity()).replaceFragment(fragment);
    }

    private void init(View view){
        recyclerView = view.findViewById(R.id.recycler_view);
        listItem = DataGenerator.getTransactionHistory(getContext());
    }
}

