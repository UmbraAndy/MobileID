/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.kreolab.mobileid.R;
import io.kreolab.mobileid.model.RegistrationCenter;
import timber.log.Timber;


public class RegistrationCenterLocationFragment extends Fragment implements OnMapReadyCallback {

    //private GoogleMap googleMap;

    @BindView(R.id.centerLocationMap)
    MapView centerLocationMap;

    private GoogleMap mGoogleMap;

    private static final String LOCATION_FRAGMENTS_TAG ="locationSelector";

    @BindView(R.id.showSelectionDialogFab)
    FloatingActionButton displaySelectionDialog;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    private final List<Marker> markerList = new ArrayList<>();
    private OnLaunchLocationListener mOnLaunchLocationListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnLaunchLocationListener  = (OnLaunchLocationListener) context;
        }
        catch (Exception ex){
            throw new RuntimeException(context.toString()
                    + " must implement OnLaunchLocationListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registration_center_location, container, false);
        ButterKnife.bind(this, view);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        //set up fab
        displaySelectionDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DialogFragment newFragment = new SelectLocationFragment();
//                newFragment.show(getChildFragmentManager(), LOCATION_FRAGMENTS_TAG);
                mOnLaunchLocationListener.launchSelection();
            }
        });


        //set up map view
        centerLocationMap.onCreate(mapViewBundle);
        centerLocationMap.getMapAsync(this);
        return view;


    }

    @Override
    public void onStart() {
        super.onStart();
        centerLocationMap.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        centerLocationMap.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        centerLocationMap.onResume();
    }

    @Override
    public void onPause() {
        centerLocationMap.onPause();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        centerLocationMap.onSaveInstanceState(mapViewBundle);
    }


    @Override
    public void onDestroy() {
        centerLocationMap.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        centerLocationMap.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    public interface OnLaunchLocationListener {
        void launchSelection();
    }

    public void addMarkersAndMoveToLocation(List<RegistrationCenter> centers) {
        addMarker(centers);
        zoomToShowAllMarkers();
    }

    private void addMarker(List<RegistrationCenter> centers) {
        //clear markers in list
        markerList.clear();
        if (mGoogleMap != null && (centers != null && centers.size() > 0)) {
            //clear previous markers
            mGoogleMap.clear();
            for (RegistrationCenter registrationCenter : centers) {
                LatLng regCenterLatLng = new LatLng(registrationCenter.getLatitude(),
                        registrationCenter.getLongitude()
                );
                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(regCenterLatLng).title(registrationCenter.getName())
                );
                markerList.add(marker);
            }
        }
    }

    public void zoomToShowAllMarkers() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (mGoogleMap != null) {
            for (Marker marker : markerList) {
                builder.include(marker.getPosition());
            }
            int padding = 128; // offset from edges of the map in pixels
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), padding);
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }
}
