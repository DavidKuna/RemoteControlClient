package cz.davidkuna.remotecontrolclient.sensors;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by David Kuna on 6.2.16.
 */
public class Compass implements ISensor {

    private float degree;

    public Compass setData(String data) {
        degree = Float.valueOf(data);

        return this;
    }

    public float getDegree() {
        return 180 - degree;
    }
}
