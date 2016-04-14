package cz.davidkuna.remotecontrolclient.videostream;

import android.nfc.Tag;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by David Kuna on 21.2.16.
 */
public class MulticastStream extends UDPInputStream {

    public final static String REQUEST_JOIN = "join";
    public final static String TAG = "MulticastStream";
    public static final int JOIN_REQUEST_INTERVAL = 10000; //miliseconds

    private Thread mWorker = null;
    private DatagramSocket ds = null;
    private volatile boolean mRunning = false;
    private String mAddress;
    private int mPort;
    private int localPort;

    public MulticastStream(String address, int port) throws UnknownHostException, SocketException {
        super(port);
        mAddress = address;
        mPort = port;
    }

    public void open(int localPort) {
        this.localPort = localPort;
        open();
    }
    public void open()
    {
        if (mRunning)
        {
            throw new IllegalStateException("Multicast is already open");
        }

        mRunning = true;
        mWorker = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                workerRun();
            } // run()
        });
        mWorker.start();
    }

    public void close() throws IOException {
        super.close();

        if (!mRunning)
        {
            throw new IllegalStateException("Multicast is already closed");
        }

        mRunning = false;
        ds.close();
        mWorker.interrupt();
    }

    private void workerRun()
    {

        String joinMessage = REQUEST_JOIN;
        while (mRunning) {
            try {
                ds = new DatagramSocket();
                DatagramPacket dp;
                dp = new DatagramPacket(joinMessage.getBytes(), joinMessage.length(), InetAddress.getByName(mAddress), mPort);
                Log.d(TAG, "Send join to " + mAddress + ":" + mPort);
                ds.send(dp);
                SystemClock.sleep(JOIN_REQUEST_INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ds != null) {
                    ds.close();
                }
            }
        }
    } // mainLoop()
}
