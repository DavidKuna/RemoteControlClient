package cz.davidkuna.remotecontrolclient.log;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cz.davidkuna.remotecontrolclient.socket.DataMessage;

/**
 * Created by David Kuna on 18.2.16.
 */
public class Logger {

    private Context context;
    private String fileName;

    public final static String FILE_EXT = ".log";

    public Logger(Context context) {
        this.context = context;
    }

    public Logger log(DataMessage data) {

        JSONObject record = new JSONObject();
        try {
            record.put("time", System.currentTimeMillis());
            record.put("data", data.getJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeToFile(record.toString());
        return this;
    }

    public Logger setFileName(String fileName) {
        this.fileName = fileName;

        return this;
    }

    public String getFileName() {
        return fileName;
    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_APPEND));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public String readFromFile() {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(fileName);

            if (inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public String getNewFileName() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(Calendar.getInstance().getTime());

        return formattedDate + FILE_EXT;
    }
}
