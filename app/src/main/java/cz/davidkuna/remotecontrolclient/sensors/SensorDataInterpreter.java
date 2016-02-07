package cz.davidkuna.remotecontrolclient.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import cz.davidkuna.remotecontrolclient.socket.DataMessage;

/**
 * Created by David Kuna on 5.2.16.
 */
public class SensorDataInterpreter {

    public Context context;
    private SensorDataEventListener listener;
    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    private Compass compass;
    private GPS location;

    public SensorDataInterpreter(Context context) {
        this.context = context;
        listener = null;

        accelerometer = new Accelerometer();
        gyroscope = new Gyroscope();
        compass = new Compass();
        location = new GPS();
    }

    public void processData(DataMessage data) {
        for (String[] item : data.getData()) {
            switch (item[0]) {
                case DataMessage.TYPE_ACCELEROMETER :
                    accelerometer.setData(item[1]);
                    break;
                case DataMessage.TYPE_GYROSCOPE :
                    gyroscope.setData(item[1]);
                    break;
                case DataMessage.TYPE_COMPASS :
                    compass.setData(item[1]);
                    break;
                case DataMessage.TYPE_GPS :
                    location.setData(item[1]);
                    break;
            }
        }

        listener.onDataChanged();
    }

    public Accelerometer getAccelerometer() {
        return accelerometer;
    }

    public Gyroscope getGyroscopeData() {
        return gyroscope;
    }

    public Compass getCompass() {
        return compass;
    };

    public GPS getLocation() {
        return location;
    }

    public void setSensorDataListener(SensorDataEventListener listener) {
        this.listener = listener;
    }
}
