package cz.davidkuna.remotecontrolclient.sensors;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by David Kuna on 7.2.16.
 */
public class GPS {

    private float latitude;
    private float longitude;

    public void setData(String data) {
        try {
            JSONArray array = new JSONArray(data);
            latitude = Float.valueOf(array.getString(0));
            longitude = Float.valueOf(array.getString(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }
}
