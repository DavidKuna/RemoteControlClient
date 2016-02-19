package cz.davidkuna.remotecontrolclient.log;

import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import cz.davidkuna.remotecontrolclient.sensors.SensorDataInterpreter;

/**
 * Created by David Kuna on 18.2.16.
 */
public class Simulator {

    private final static String TAG = "Simulator";
    private AsyncTask<Void, Void, Void> async;
    private LogSource log;
    private SensorDataInterpreter dataInterpreter;

    public Simulator(LogSource source, SensorDataInterpreter dataInterpreter) {
        log = source;
        this.dataInterpreter = dataInterpreter;
    }

    public void run() {
        async = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                long delay = 0;

                LogRecord nextRecord = null;
                LogRecord currentRecord = log.getNext();
                while ((nextRecord = log.getNext()) != null) {
                    SystemClock.sleep(delay);
                    //TODO fire event onDataChange
                    dataInterpreter.processData(currentRecord.getMessage());
                    Log.d(TAG, "Delay: " + delay + " Message: " + currentRecord.getMessage().toString());
                    delay = nextRecord.getTime() - currentRecord.getTime();
                    currentRecord = nextRecord;
                }
                return null;
            }

        };
        Log.d(TAG, "Executing");
        if (Build.VERSION.SDK_INT >= 11) async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async.execute();
    }
}
