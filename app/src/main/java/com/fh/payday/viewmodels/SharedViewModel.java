package com.fh.payday.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.LinkedHashMap;

public class SharedViewModel  extends ViewModel {
    private final MutableLiveData<Integer> selected = new MutableLiveData<>();
    private final MutableLiveData<LinkedHashMap<String, String>> map = new MutableLiveData<>();

    public void setSelected(Integer i) {
        selected.setValue(i);
    }

    public MutableLiveData<Integer> getSelected() {
        return selected;
    }

    public void setMap(String key, String value) {
        if (map.getValue() == null) {
            map.setValue(new LinkedHashMap<>());
        }
        map.getValue().put(key, value);
    }

    public MutableLiveData<LinkedHashMap<String, String>> getMap() {
        return map;
    }
}