package com.fh.payday.views2.bottombar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.views.adapter.HowToRegisterAdapter;

import java.util.Arrays;

public class HowToFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_how_to_reg, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        HowToRegisterAdapter howToRegisterAdapter = new HowToRegisterAdapter(Arrays.asList(
            getResources().getStringArray(R.array.registration_steps)));
        recyclerView.setAdapter(howToRegisterAdapter);
        return view;
    }
}
