package cz.davidkuna.remotecontrolclient.fragment;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import cz.davidkuna.remotecontrolclient.OnLocationChangedListener;
import cz.davidkuna.remotecontrolclient.R;
import cz.davidkuna.remotecontrolclient.activity.ControlActivity;

/**
 * Created by David Kuna on 27.2.16.
 */
public class MapFragment extends Fragment {


    private MapView mapView;
    private GoogleMap map;
    private ControlActivity controlActivity;
    private float mLatitude;
    private float mLongitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);

        if (ActivityCompat.checkSelfPermission(container.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(container.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return null;
        }
        map.setMyLocationEnabled(true);

        MapsInitializer.initialize(this.getActivity());

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 30);
        map.animateCamera(cameraUpdate);

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        controlActivity = (ControlActivity)activity;
        controlActivity.setOnLocationChangedListener(new OnLocationChangedListener() {
            @Override
            public void OnChange(float latitude, float longitude) {
                mLatitude = latitude;
                mLongitude = longitude;

                if (map != null) {
                    controlActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLatitude, mLongitude)));
                            map.animateCamera(CameraUpdateFactory.zoomTo(16));
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
