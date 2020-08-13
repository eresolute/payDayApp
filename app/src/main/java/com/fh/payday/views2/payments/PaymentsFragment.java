package com.fh.payday.views2.payments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.datasource.models.payments.CountryCode;
import com.fh.payday.datasource.models.payments.ServiceCategory;
import com.fh.payday.datasource.models.payments.TypeId;
import com.fh.payday.utilities.ItemOffsetDecoration;
import com.fh.payday.viewmodels.payments.billpayment.PaymentsViewModel;
import com.fh.payday.views.adapter.OperatorAdapter;
import com.fh.payday.views2.payments.recharge.MobileRechargeActivity;
import com.fh.payday.views2.payments.recharge.mawaqif.MawaqifTopUpActivity;
import com.fh.payday.views2.payments.transport.TransportActivity;
import com.fh.payday.views2.payments.utilities.UtilitiesActivity;

import java.util.Arrays;

public class PaymentsFragment extends Fragment implements View.OnClickListener {

    private PaymentsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initViewModel();
        View view = inflater.inflate(R.layout.fragment_payments, container, false);

        if (getArguments() == null) {
            handleViews(view, -1);
        } else {
            int position = getArguments().getInt("position", 0);
            handleViews(view, position);
        }
        return view;
    }

    @Override
    public void onClick(View view) { }

    private void handleViews(View view, int position) {

        if (getActivity() == null) {
            return;
        }
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new ItemOffsetDecoration(2));

        switch (position) {
            case 0:
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
                recyclerView.setAdapter(new OperatorAdapter(index -> {
                    Intent intent = new Intent(getActivity(), MobileRechargeActivity.class);
                    switch (index) {
                        case 0:
                            intent.putExtra("operator", "du");
                            startActivity(intent);
                            break;
                        case 1:
                            intent.putExtra("operator", "etisalat");
                            startActivity(intent);
                            break;
                    }
                }, viewModel.getBillPayments().getValue(), 2));
                break;
            case 1:
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
                recyclerView.setAdapter(new OperatorAdapter(index -> {
                    Intent mIntent = new Intent(getActivity(), UtilitiesActivity.class);
                    switch (index) {
                        case 0:
                            mIntent.putExtra("operator", "fewa");
                            startActivity(mIntent);
                            break;
//                        case 1:
//                            mIntent.putExtra("operator", "dewa");
//                            startActivity(mIntent);
//                            break;
                        /*case 1:
                            mIntent.putExtra("operator", "aadc");
                            startActivity(mIntent);
                            break;*/
                    }
                }, viewModel.getUtilities().getValue(), 2));
                break;
            case 2:

                break;
            case 4:
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
                recyclerView.setAdapter(new OperatorAdapter(index -> {
                    switch (index) {
//                        case 0:
//                            intent.putExtra("operator", TransportActivity.Transport.DUBAI_POLICE);
//                           // startActivity(intent);
//                            break;
                        case 0:
                            Intent intent1 = new Intent(getActivity(), MawaqifTopUpActivity.class);
                            intent1.putExtra("operator", "mawaqif");
                            startActivity(intent1);
                            break;
                        case 1:
                            Intent intent = new Intent(getActivity(), TransportActivity.class);
                            intent.putExtra("operator", TransportActivity.Transport.SALIK);
                            startActivity(intent);
                            break;
//                        case 3:
//                            intent.putExtra("operator", TransportActivity.Transport.NOL);
//                            //startActivity(intent);
//                            break;
                    }
                }, viewModel.getTransport().getValue(), 2));
                break;
            case 3:
                if (getActivity() != null && getActivity() instanceof PaymentActivity) {
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    recyclerView.setAdapter(new OperatorAdapter(index -> {
                        Intent intent = new Intent(getActivity(), SelectOperatorActivity.class);
                        intent.putExtra("country_code", CountryCode.INDIA);

                        switch (index) {
                            case 0:
                                intent.putExtra("service", TypeId.TOP_UP);
                                intent.putExtra("category", ServiceCategory.PREPAID);
                                startActivity(intent);
                                break;
                            case 1:
                                intent.putExtra("service", TypeId.BILL_PAYMENT);
                                intent.putExtra("category", ServiceCategory.POSTPAID);
                                startActivity(intent);
                                break;
                            case 2:
                                intent.putExtra("service", TypeId.BILL_PAYMENT);
                                intent.putExtra("category", ServiceCategory.LANDLINE);
                                startActivity(intent);
                                break;
                            case 3:
                                intent.putExtra("service", TypeId.TOP_UP);
                                intent.putExtra("category", ServiceCategory.DTH);
                                startActivity(intent);
                                break;
                            case 4:
                                intent.putExtra("service", TypeId.TOP_UP);
                                intent.putExtra("category", ServiceCategory.INSURANCE);
                                startActivity(intent);
                                break;
                            case 5:
                                intent.putExtra("service", TypeId.TOP_UP);
                                intent.putExtra("category", ServiceCategory.GAS);
                                startActivity(intent);
                                break;
                            case 6:
                                intent.putExtra("service", TypeId.BILL_PAYMENT);
                                intent.putExtra("category", ServiceCategory.ELECTRICITY);
                                startActivity(intent);
                                break;
                        }
                    }, viewModel.getIndianServices(), 4));
                }
                break;
            default:
                recyclerView.setAdapter(new OperatorAdapter(
                        index -> ((PaymentActivity) getActivity()).onServiceSelected(index),
                        viewModel.getServices().getValue(), 3)
                );
                break;
        }
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(PaymentsViewModel.class);

        viewModel.setServices(Arrays.asList(

                new Item(getString(R.string.bill_payment), R.drawable.ic_mobile_recharge_and_bill_payment),
                new Item(getString(R.string.utilities), R.drawable.ic_utilities),
                new Item(getString(R.string.international_topup), R.drawable.ic_international_topup),
                new Item(getString(R.string.indian_bill_payment), R.drawable.ic_mobile_recharge_and_bill_payment),
                new Item(getString(R.string.transport), R.drawable.ic_roads_and_transport),
                //new Item(getString(R.string.indian_bill_payment), R.drawable.ic_calling_card_hdp)
                new Item("", 0)
        ));

        viewModel.setBillPayments(Arrays.asList(
                new Item(getString(R.string.du), R.drawable.ic_du),
                new Item(getString(R.string.etisalat), R.drawable.ic_etisalat_)
        ));

        viewModel.setUtilities(Arrays.asList(
                new Item(getString(R.string.fewa), R.drawable.ic_fewa_),
                // new Item("Dewa", R.drawable.ic_dewa),
                //new Item(getString(R.string.aadc), R.drawable.ic_addc)
                new Item("", 0)
        ));

        viewModel.setTransport(Arrays.asList(
                //new Item("Dubai Police", R.drawable.ic_dubai_police),
                new Item(getString(R.string.mawaqif_topup), R.mipmap.ic_mawaqif_img),
                new Item(getString(R.string.salik), R.drawable.ic_salik)
                //new Item("Nol", R.drawable.ic_nol)
        ));

        viewModel.setCallingCard(Arrays.asList(
                //new Item(getString(R.string.hello_card), R.drawable.ic_hello_card),
                new Item(getString(R.string.airtel), R.drawable.ic_five_card)
        ));

        viewModel.setIndianServices(Arrays.asList(
                new Item(getString(R.string.prepaid), R.drawable.ic_prepaid),
                new Item(getString(R.string.postpaid), R.drawable.ic_postpaid),
                new Item(getString(R.string.landline), R.drawable.ic_landline),
                new Item(getString(R.string.dth), R.drawable.ic_dth),
                new Item(getString(R.string.insurance), R.drawable.ic_insurance),
                new Item(getString(R.string.gas), R.drawable.ic_gas),
                new Item(getString(R.string.electricity), R.drawable.ic_electricity),
                new Item("", 0)
        ));
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECTED_OPERATOR: {
                    Operator operator = data.getParcelableExtra("data");
                    Activity activity = getActivity();
                    if (operator == null || activity == null) return;

                    Intent intent = new Intent(activity, IndianBillPaymentActivity.class);
                    intent.putExtra("selected_operator", operator);
                    intent.putExtra("type_id", data.getIntExtra("service", TypeId.BILL_PAYMENT));
                    startActivity(intent);
                    break;
                }
            }
        }
    }*/
}