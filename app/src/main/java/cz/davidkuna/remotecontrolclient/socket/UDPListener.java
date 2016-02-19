package cz.davidkuna.remotecontrolclient.socket;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import cz.davidkuna.remotecontrolclient.log.Logger;
import cz.davidkuna.remotecontrolclient.sensors.SensorDataInterpreter;

/**
 * Created by David Kuna on 6.1.16.
 */
public class UDPListener {
    private AsyncTask<Void, Void, Void> async;
    private boolean serverActive = true;
    private static final int MAX_UDP_DATAGRAM_LEN = 4096;
    private Logger logger = null;
    private boolean loggerActive = false;

    public UDPListener(Logger logger) {
        this.logger = logger;
    }

     @SuppressLint("NewApi")
    public void runUdpServer(final int serverPort, final SensorDataInterpreter dataInterpreter)
    {
        serverActive = true;
        async = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                String lText;
                byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
                DatagramPacket incoming = new DatagramPacket(lMsg, lMsg.length);
                DatagramSocket ds = null;

                try
                {
                    ds = new DatagramSocket(serverPort);

                    while(serverActive)
                    {
                        ds.receive(incoming);
                        byte[] data = incoming.getData();
                        String s = new String(data, 0, incoming.getLength());
                        Log.i("UDP packet received", incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);
                        DataMessage message = new DataMessage(s);
                        if (loggerActive) {
                            logger.log(message);
                        }
                        dataInterpreter.processData(message);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (ds != null)
                    {
                        ds.close();
                    }
                }

                return null;
            }
        };
        Log.d("UDPListener", "Executing");
        if (Build.VERSION.SDK_INT >= 11) async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async.execute();
    }

    public void stopUDPServer()
    {
        stopLogging();
        serverActive = false;
    }

    public UDPListener startLogging() {
        logger.setFileName(logger.getNewFileName());
        loggerActive = true;
        Log.d("UDPListener", "Start recording to file " + logger.getFileName());
        return this;
    }

    public UDPListener stopLogging() {
        loggerActive = false;

        return this;
    }

    public boolean isLoggerActive() {
        return loggerActive;
    }
}
