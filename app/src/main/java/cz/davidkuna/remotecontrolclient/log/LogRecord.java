package cz.davidkuna.remotecontrolclient.log;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cz.davidkuna.remotecontrolclient.socket.DataMessage;

/**
 * Created by David Kuna on 18.2.16.
 */
public class LogRecord {

    private long time;
    private DataMessage message;

    public LogRecord(String data) {
        try {
            JSONObject jData = new JSONObject(data);
            time = jData.getLong("time");
            message = new DataMessage(jData.getString("data").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getTime() {
        return time;
    }

    public DataMessage getMessage() {
        return message;
    }
}
