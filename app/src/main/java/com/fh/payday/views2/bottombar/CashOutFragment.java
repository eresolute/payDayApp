package com.fh.payday.views2.bottombar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.datasource.models.AtmLocator;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.views.adapter.AtmLocationsAdapter;

import java.util.ArrayList;
import java.util.List;

public class CashOutFragment extends Fragment implements OnMapReadyCallback, OnItemClickListener
{
    SupportMapFragment mapFragment;
    RecyclerView rvAtmLocations;
    List<AtmLocator> location = new ArrayList<>();
    GoogleMap map;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_out, container, false);
        rvAtmLocations = view.findViewById(R.id.rv_atm_location);
        setAddresses();
        return view;
    }

    private void setAddresses() {
        location = DataGenerator.getAtmLocation(getContext());
        rvAtmLocations.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvAtmLocations.setAdapter(new AtmLocationsAdapter(location, this));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        mapFragment = (SupportMapFragment)fm.findFragmentById(R.id.map);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        for (AtmLocator locator : location) {
            LatLng place = new LatLng(locator.getLat(), locator.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(place).title(locator.getName()));
        }

        //Repositions Camera to first questions in the list
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.get(0).getLat(), location.get(0).getLongitude()), 13));
    }

    @Override
    public void onItemClick(int index) {
        LatLng place = new LatLng(location.get(index).getLat(), location.get(index).getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 13));
    }
}
