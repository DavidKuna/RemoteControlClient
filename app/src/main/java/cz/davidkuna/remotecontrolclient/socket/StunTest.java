package cz.davidkuna.remotecontrolclient.socket;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import cz.davidkuna.remotecontrolclient.helpers.LoggerFactory;

/**
 * Created by David Kuna on 28.2.16.
 */
public class StunTest {

    private boolean serverActive = true;
    private static final int MAX_UDP_DATAGRAM_LEN = 4096;

    public StunTest() {


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String stunServer = "stun.sipgate.net";
                int port = 10000;
                try {
                    InetAddress address = InetAddress
                            .getByName("0.0.0.0");
                    StunConnection stun =  new StunConnection(address, stunServer, port);
                    stun.connect();
                   //listen(stun.getMappedAddress().getPort());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    private void listen(int serverPort) {

        byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
        DatagramPacket incoming = new DatagramPacket(lMsg, lMsg.length);
        DatagramSocket ds = null;

        try
        {
            ds = new DatagramSocket(serverPort + 1);
            Log.i("SunTest", "Listening on port " + serverPort);
            while(serverActive)
            {
                ds.receive(incoming);
                byte[] data = incoming.getData();
                String s = new String(data, 0, incoming.getLength());
                Log.i("UDP packet received", incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);
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
    }
}
