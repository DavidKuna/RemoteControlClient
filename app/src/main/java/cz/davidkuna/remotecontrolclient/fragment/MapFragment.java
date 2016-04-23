package cz.davidkuna.remotecontrolclient.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import cz.davidkuna.remotecontrolclient.activity.LocationChangeableActivity;
import cz.davidkuna.remotecontrolclient.sensors.OnLocationChangedListener;
import cz.davidkuna.remotecontrolclient.R;

/**
 * Created by David Kuna on 27.2.16.
 */
public class MapFragment extends Fragment {

    private final double DEF_LATITUDE = 49.833062;
    private final double DEF_LONGITUDE = 18.164418;

    private MapView mapView;
    private GoogleMap map;
    private LocationChangeableActivity controlActivity;
    private float mLatitude;
    private float mLongitude;
    private float mRotation;
    private Marker marker;
    private PolylineOptions trackPoints;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String optionsString = bundle.getString("polylineoptions", null);
            Log.d("Fragment", optionsString);
            Gson gson = new Gson();
            trackPoints = gson.fromJson(optionsString, PolylineOptions.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String optionsString = bundle.getString("polylineoptions", null);
            Gson gson = new Gson();
            trackPoints = gson.fromJson(optionsString, PolylineOptions.class);
        }

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);

        LatLng position;

        if (trackPoints != null) {
            map.addPolyline(trackPoints);
            position = trackPoints.getPoints().get(0);
        } else {
            position = new LatLng(DEF_LATITUDE, DEF_LONGITUDE);
        }

        MapsInitializer.initialize(this.getActivity());

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 17);
        map.animateCamera(cameraUpdate);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarker()))
                .anchor(0.5f, 1);
        marker = map.addMarker(markerOptions);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        controlActivity = (LocationChangeableActivity)activity;
        controlActivity.setOnLocationChangedListener(new OnLocationChangedListener() {
            @Override
            public void OnChange(final float latitude, final float longitude, float rotation) {
                mLatitude = latitude;
                mLongitude = longitude;
                mRotation = rotation;

                if (map != null) {
                    controlActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LatLng position = new LatLng(mLatitude, mLongitude);
                            marker.setPosition(position);
                            marker.setRotation(mRotation);
                            map.moveCamera(CameraUpdateFactory.newLatLng(position));
                            map.animateCamera(CameraUpdateFactory.zoomTo(16));
                        }
                    });
                }
            }
        });
    }

    private Bitmap getMarker() {
        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_flight_24dp);
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
