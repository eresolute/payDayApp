package com.fh.payday.views2.bottombar;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.datasource.models.Country;
import com.fh.payday.datasource.models.CurrencyConv;
import com.fh.payday.utilities.OnCurrencyItemClickListener;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.views.adapter.CurrencyConvAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CurrencyConverterFragment extends Fragment implements View.OnClickListener,
        OnCurrencyItemClickListener, OnItemClickListener {

    public android.app.Dialog dialog;
    public List<Country> currencyConvs = DataGenerator.getCountries();
    public OnCurrencyItemClickListener curListener;
    TextView tvFromCountry, tvToCountry;
    int id;
    private EditText etFromValue;
    private EditText etToValue;
    private TextView tvConversionStatus;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_currency_conv, container, false);

        tvFromCountry = view.findViewById(R.id.from_country);
        tvToCountry = view.findViewById(R.id.to_country);
        tvFromCountry.setOnClickListener(this);
        tvToCountry.setOnClickListener(this);

        etFromValue = view.findViewById(R.id.from_value);
        etToValue = view.findViewById(R.id.to_value);
        CurrencyConv conv = DataGenerator.convertCurrency(0);
        etFromValue.setHint(String.valueOf(conv.getFromValue()));
        etToValue.setHint(String.valueOf(conv.getToValue()));
        curListener = this;

        tvConversionStatus = view.findViewById(R.id.tv_conversion);

        view.findViewById(R.id.swap_currency).setOnClickListener(v -> swap());

        id = 1;
        onItemClick(0);
        id = 2;
        onItemClick(1);

        return view;
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.from_country) {
            id = 1;
            showCurrencyDialog(id);
        } else {
            id = 2;
            showCurrencyDialog(id);
        }
    }

    private void showCurrencyDialog(int id) {
        if (getActivity() == null) {
            return;
        }

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_currency_fragment_layout, null, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        CurrencyConvAdapter currencyConvAdapter = new CurrencyConvAdapter(currencyConvs, this, id);
        recyclerView.setAdapter(currencyConvAdapter);

        dialog = new android.app.Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        //dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(DialogInterface::dismiss);

        dialog.show();
    }

    @Override
    public void onCurrencyItemClick(@NotNull String currency, int flag, int id) {
        if (id == 1) {
            tvFromCountry.setText(currency);
            tvFromCountry.setCompoundDrawablesWithIntrinsicBounds(flag, 0, R.drawable.ic_down_arrow, 0);
            dialog.dismiss();

        } else {
            tvToCountry.setText(currency);
            tvToCountry.setCompoundDrawablesWithIntrinsicBounds(flag, 0, R.drawable.ic_down_arrow, 0);
            dialog.dismiss();
        }
    }

    @Override
    public void onItemClick(int index) {
        CurrencyConv conv = DataGenerator.convertCurrency(index);
        etFromValue.setHint(String.valueOf(conv.getFromValue()));
        etToValue.setHint(String.valueOf(conv.getToValue()));

        if (dialog !=null)
            curListener.onCurrencyItemClick(currencyConvs.get(index).getAbbr(), currencyConvs.get(index).getFlag(), id);
        else
        {
            if (index == 1) {
                tvFromCountry.setText(currencyConvs.get(index).getAbbr());
                tvFromCountry.setCompoundDrawablesWithIntrinsicBounds(currencyConvs.get(index).getFlag(), 0, R.drawable.ic_down_arrow, 0);
            } else {
                tvToCountry.setText(currencyConvs.get(index).getAbbr());
                tvToCountry.setCompoundDrawablesWithIntrinsicBounds(currencyConvs.get(index).getFlag(), 0, R.drawable.ic_down_arrow, 0);
            }
        }

        tvConversionStatus.setText(String.format(getString(R.string.conversion_value),
                etFromValue.getHint().toString(), tvFromCountry.getText().toString(),
                etToValue.getHint().toString(), tvToCountry.getText().toString()));
    }

    private void swap() {
        String fromValueHint = etToValue.getHint().toString();
        String toValueHint = etFromValue.getHint().toString();
        String fromValue = etToValue.getText().toString();
        String toValue = etFromValue.getText().toString();

        etFromValue.setText(fromValue);
        etToValue.setText(toValue);

        etFromValue.setHint(fromValueHint);
        etToValue.setHint(toValueHint);

        String fromCountryCode = tvToCountry.getText().toString();
        String toCountryCode = tvFromCountry.getText().toString();

        tvFromCountry.setText(fromCountryCode);
        tvToCountry.setText(toCountryCode);

        Drawable[] fromFlagDrawables = tvToCountry.getCompoundDrawables();
        Drawable[] toFlagDrawables = tvFromCountry.getCompoundDrawables();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            tvFromCountry.setCompoundDrawablesRelativeWithIntrinsicBounds(fromFlagDrawables[0], null, fromFlagDrawables[2], null);
            tvToCountry.setCompoundDrawablesRelativeWithIntrinsicBounds(toFlagDrawables[0], null, toFlagDrawables[2], null);
        } else {
            tvFromCountry.setCompoundDrawablesWithIntrinsicBounds(fromFlagDrawables[0], null, fromFlagDrawables[2], null);
            tvToCountry.setCompoundDrawablesWithIntrinsicBounds(toFlagDrawables[0], null, toFlagDrawables[2], null);
        }

        tvConversionStatus.setText(String.format(getString(R.string.conversion_value),
                etFromValue.getHint().toString(), tvFromCountry.getText().toString(),
                etToValue.getHint().toString(), tvToCountry.getText().toString()));
    }

}
