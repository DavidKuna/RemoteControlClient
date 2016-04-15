package cz.davidkuna.remotecontrolclient.socket;

import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Timer;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

/**
 * Created by David Kuna on 14.4.16.
 */
public class UDPClient {
    String stunServer = "stun.sipgate.net";
    int port = 10000;
    MappedAddress ma;
    Timer timer;
    DatagramSocket initialSocket;

    public static void connect() {
        UDPClient inetserver = new UDPClient();
        try {
            InetAddress server = InetAddress.getLocalHost();
            inetserver.initialSocket = new DatagramSocket(new InetSocketAddress(server, 0));
            inetserver.initialSocket.setReuseAddress(true);
            inetserver.initialSocket.connect(InetAddress.getByName(inetserver.stunServer), inetserver.port);
            inetserver.bindingCommunicationInitialSocket();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("UDPClient", "something went wrong");
        }
    }

    private boolean bindingCommunicationInitialSocket()
            throws UtilityException, IOException, MessageHeaderParsingException, MessageAttributeParsingException {
        MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
        sendMH.generateTransactionID();

        ChangeRequest changeRequest = new ChangeRequest();
        sendMH.addMessageAttribute(changeRequest);

        byte[] data = sendMH.getBytes();
        DatagramPacket send = new DatagramPacket(data, data.length);
        initialSocket.send(send);
        Log.d("UDPClient", "Binding Request sent.");

        MessageHeader receiveMH = new MessageHeader();
        while (!(receiveMH.equalTransactionID(sendMH))) {
            DatagramPacket receive = new DatagramPacket(new byte[200], 200);
            initialSocket.receive(receive);
            receiveMH = MessageHeader.parseHeader(receive.getData());
            receiveMH.parseAttributes(receive.getData());
        }
        ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
        Log.d("UDPClient", ma.getAddress().toString());
        Log.d("UDPClient", ma.getPort() + "");
        ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
        if (ec != null) {
            Log.d("UDPClient", "Message header contains an Errorcode message attribute.");
            return true;
        }
        if (ma == null) {
            Log.d("UDPClient","Response does not contain a Mapped Address message attribute.");
            return true;
        }

        Thread heartBeat = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] data = new byte[1];
                DatagramPacket send = new DatagramPacket(data, data.length);
                while(true) {
                    try {
                        initialSocket.send(send);
                        Thread.sleep(500);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //heartBeat.start();

        boolean related = false;
        String token = "LKdf78s9df";
        URL relationServer = new URL("http://wendy:wendy@punkstore.wendy.netdevelo.cz/RemoteControlRelayServer/?token=" + token);
        while (!related) {
            Log.d("RELAY", "call " + relationServer);
            URLConnection yc = relationServer.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                Log.d("RELAY", inputLine);
            in.close();
            SystemClock.sleep(1000);
        }

        byte[] receiveData = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            initialSocket.receive(receivePacket);
            String sentence = new String(receivePacket.getData());
            Log.d("RECEIVED", sentence + " from " + receivePacket.getSocketAddress());
        }
    }
}
