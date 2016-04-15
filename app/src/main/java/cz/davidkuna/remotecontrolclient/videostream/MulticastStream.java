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

import cz.davidkuna.remotecontrolclient.helpers.Network;
import cz.davidkuna.remotecontrolclient.socket.Relation;
import cz.davidkuna.remotecontrolclient.socket.StunConnection;

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
    private boolean useSTUN = false;
    private StunConnection stunConnection = null;
    private Thread stunWorker = null;
    private String mToken;
    private Relation mRelation = null;

    public MulticastStream(String address, int port) throws UnknownHostException, SocketException {
        super(port);
        mAddress = address;
        mPort = port;
    }

    public MulticastStream(String token, Relation relation) throws UnknownHostException, SocketException {
        this.mToken = token;
        this.mRelation = relation;
        useSTUN = true;
        stunWorker = new Thread(new Runnable()
        {
            @Override
            public void run() {
                String stunServer = "stun.sipgate.net";
                String relayServer = "http://punkstore.wendy.netdevelo.cz/RemoteControlRelayServer/";
                int port = 10000;
                stunConnection = new StunConnection(Network.getLocalInetAddress(), stunServer, port, relayServer);
                stunConnection.setRelation(mRelation);
                stunConnection.connect(mToken);
            }
        });
        stunWorker.start();
    }

    public void open()
    {
        if (useSTUN) {
            super.open(stunConnection.getSocket());
        }

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
                if (useSTUN) {
                    STUNworkerRun();
                } else {
                    workerRun();
                }
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

        if (stunWorker != null) {
            stunWorker.interrupt();
        }
    }

    private void workerRun()
    {

        String joinMessage = REQUEST_JOIN;
        while (mRunning) {
            if (mAddress != null) {
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
        }
    }

    private void STUNworkerRun()
    {
        String joinMessage = REQUEST_JOIN;
        while (mRunning) {
            stunConnection.send(joinMessage);
            SystemClock.sleep(JOIN_REQUEST_INTERVAL);
        }
    }
}
