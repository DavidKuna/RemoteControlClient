package cz.davidkuna.remotecontrolclient.log;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by David Kuna on 18.2.16.
 */
public class LogSource {

    private final static String TAG = "LogSource";
    ArrayList<LogRecord> data = new ArrayList<LogRecord>();
    private int index = 0;

    public LogSource(InputStream inputStream) {
        setDataSource(inputStream);
    }

    private LogSource setDataSource(InputStream inputStream) {
        try {
            if (inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    data.add(new LogRecord(receiveString));
                }

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {
            android.util.Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            android.util.Log.e("login activity", "Can not read file: " + e.toString());
        }

        return this;
    }

    public LogRecord getNext() {
        if (index + 1 <= data.size()) {
            index++;
            return data.get(index - 1);
        } else {
            return null;
        }
    }

}
