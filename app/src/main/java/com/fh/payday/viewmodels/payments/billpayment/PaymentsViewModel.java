package com.fh.payday.viewmodels.payments.billpayment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.fh.payday.datasource.models.Item;

import java.util.ArrayList;
import java.util.List;

public class PaymentsViewModel extends ViewModel {

    private MutableLiveData<List<Item>> services = new MutableLiveData<>();
    private MutableLiveData<List<Item>> billPayments = new MutableLiveData<>();
    private MutableLiveData<List<Item>> utilities = new MutableLiveData<>();
    private MutableLiveData<List<Item>> transport = new MutableLiveData<>();
    private MutableLiveData<List<Item>> callingCard = new MutableLiveData<>();
    private List<Item> indianServices = new ArrayList<>();


    public LiveData<List<Item>> getServices() {
        return services;
    }

    public void setServices(@NonNull List<Item> services) {
        this.services.setValue(services);
    }

    public LiveData<List<Item>> getBillPayments() {
        return billPayments;
    }

    public void setBillPayments(@NonNull List<Item> billPayments) {
        this.billPayments.setValue(billPayments);
    }

    public LiveData<List<Item>> getUtilities() {
        return utilities;
    }

    public void setUtilities(List<Item> utilities) {
        this.utilities.setValue(utilities);
    }

    public LiveData<List<Item>> getTransport() {
        return transport;
    }

    public void setTransport(List<Item> transport) {
        this.transport.setValue(transport);
    }

    public LiveData<List<Item>> getCallingCard() {
        return callingCard;
    }

    public void setCallingCard(List<Item> callingCard) {
        this.callingCard.setValue(callingCard);
    }

    public void setIndianServices(List<Item> items) {
        this.indianServices = items;
    }

    public List<Item> getIndianServices() {
        return indianServices;
    }


}
