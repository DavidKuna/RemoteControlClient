package cz.davidkuna.remotecontrolclient.activity;

import android.support.v7.app.AppCompatActivity;

import cz.davidkuna.remotecontrolclient.sensors.OnLocationChangedListener;

/**
 * Created by David Kuna on 27.2.16.
 */
public abstract class LocationChangeableActivity extends AppCompatActivity {
    public abstract void setOnLocationChangedListener(OnLocationChangedListener listener);
}
