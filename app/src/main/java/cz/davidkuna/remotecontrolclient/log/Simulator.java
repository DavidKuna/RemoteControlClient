package cz.davidkuna.remotecontrolclient.log;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by David Kuna on 18.2.16.
 */
public class Simulator implements Runnable {

    private final static String TAG = "Simulator";
    private LogSource log;

    public Simulator(LogSource source) {
        log = source;
    }

    @Override
    public void run() {
        long delay = 0;

        LogRecord nextRecord = null;
        LogRecord currentRecord = log.getNext();
        while((nextRecord = log.getNext()) != null) {
            SystemClock.sleep(delay);
            //TODO fire event onDataChange
            Log.d(TAG, "Delay: " + delay + " Message: " + currentRecord.getMessage().toString());
            delay = nextRecord.getTime() - currentRecord.getTime();
            currentRecord = nextRecord;
        }
    }
}
