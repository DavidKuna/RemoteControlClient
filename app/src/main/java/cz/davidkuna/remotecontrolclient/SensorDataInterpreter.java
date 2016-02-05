package cz.davidkuna.remotecontrolclient;

import android.content.Context;
import android.hardware.Sensor;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by David Kuna on 5.2.16.
 */
public class SensorDataInterpreter {

    public Context context;
    private SensorDataListener listener;
    private String accelerometerData;
    private String gyroscopeData;

    public SensorDataInterpreter(Context context) {
        this.context = context;
        listener = null;
    }

    public void processData(String data) {
        try {
            JSONArray jData = new JSONArray(data);
            for (int i = 0; i < jData.length(); i++) {
                JSONArray item = jData.getJSONArray(i);

                switch (item.getString(0)) {
                    case Sensor.TYPE_ACCELEROMETER + "" :
                        accelerometerData = item.getString(1);
                        break;
                    case Sensor.TYPE_GYROSCOPE + "" :
                        gyroscopeData = item.getString(1);
                        break;
                }
            }

            listener.onDataChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getAccelerometerData() {
        return accelerometerData;
    }

    public String getGyroscopeData() {
        return gyroscopeData;
    }

    public void setSensorDataListener(SensorDataListener listener) {
        this.listener = listener;
    }
}
