package cz.davidkuna.remotecontrolclient.sensors;

import android.content.Context;
import android.hardware.Sensor;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by David Kuna on 5.2.16.
 */
public class SensorDataInterpreter {

    public Context context;
    private SensorDataEventListener listener;
    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    private Compass compass;

    public SensorDataInterpreter(Context context) {
        this.context = context;
        listener = null;

        accelerometer = new Accelerometer();
        gyroscope = new Gyroscope();
        compass = new Compass();
    }

    public void processData(String data) {
        try {
            JSONArray jData = new JSONArray(data);
            for (int i = 0; i < jData.length(); i++) {
                JSONArray item = jData.getJSONArray(i);

                switch (item.getString(0)) {
                    case Sensor.TYPE_ACCELEROMETER + "" :
                        accelerometer.setData(item.getString(1));
                        break;
                    case Sensor.TYPE_GYROSCOPE + "" :
                        gyroscope.setData(item.getString(1));
                        break;
                    case Sensor.TYPE_ORIENTATION + "" :
                        compass.setData(item.getString(1));
                        break;
                }
            }

            listener.onDataChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    public void setSensorDataListener(SensorDataEventListener listener) {
        this.listener = listener;
    }
}
