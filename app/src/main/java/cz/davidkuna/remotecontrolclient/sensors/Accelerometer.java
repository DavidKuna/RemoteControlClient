package cz.davidkuna.remotecontrolclient.sensors;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by David Kuna on 6.2.16.
 */
public class Accelerometer {

    private float x;
    private float y;
    private float z;

    public Accelerometer setData(String data) {
        try {
            JSONArray array = new JSONArray(data);
            x = Float.valueOf(array.getString(0));
            y = Float.valueOf(array.getString(1));
            z = Float.valueOf(array.getString(2));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}
