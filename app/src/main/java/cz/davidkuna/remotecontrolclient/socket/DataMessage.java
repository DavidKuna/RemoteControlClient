package cz.davidkuna.remotecontrolclient.socket;

import android.hardware.Sensor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;

/**
 * Created by David Kuna on 4.2.16.
 */
public class DataMessage extends JSONStringer {

    public static final String TYPE_ACCELEROMETER = "acc";
    public static final String TYPE_GYROSCOPE = "gyr";
    public static final String TYPE_COMPASS = "com";
    public static final String TYPE_GPS = "gps";

    private ArrayList<String[]> data = new ArrayList<String[]>();

    private String rawData;

    public DataMessage() {

    }

    public DataMessage(String data) {
        rawData = data;
        try {
            JSONArray jData = new JSONArray(data);
            for (int i = 0; i < jData.length(); i++) {
                JSONObject item = jData.getJSONObject(i);
                this.data.add(new String[] {item.getString("name"), item.getString("value")});
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public DataMessage addData(String name, String value) {
        String[] item = {name, value};
        data.add(item);

        return this;
    }

    public ArrayList<String[]> getData() {
        return data;
    }

    @Override
    public String toString() {
        return getJSON().toString();
    }

    public JSONArray getJSON() {
        return new JSONArray(data);
    }

    public String getRawData() {
        return rawData;
    }
}
