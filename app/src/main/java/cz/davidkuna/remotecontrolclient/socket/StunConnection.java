package cz.davidkuna.remotecontrolclient.socket;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.test.DiscoveryInfo;
import de.javawi.jstun.util.UtilityException;

/**
 * Created by David Kuna on 27.2.16.
 */
public class StunConnection {

    private final String TAG = "StunConnection";
    InetAddress iaddress;
    String stunServer;
    int port;
    int timeoutInitValue = 30000; //ms
    MappedAddress ma = null;
    ChangedAddress ca = null;
    boolean nodeNatted = true;
    DatagramSocket socketTest1 = null;
    DiscoveryInfo di = null;

    private boolean serverActive = true;
    private static final int MAX_UDP_DATAGRAM_LEN = 4096;

    public StunConnection(InetAddress iaddress , String stunServer, int port) {

        this.iaddress = iaddress;
        this.stunServer = stunServer;
        this.port = port;
        di = new DiscoveryInfo(iaddress);

    }

    public boolean connect() {
        int timeSinceFirstTransmission = 0;
        int timeout = timeoutInitValue;
        while (true) {
            try {
                // Test 1 including response
                socketTest1 = new DatagramSocket(new InetSocketAddress(iaddress, 0));
                socketTest1.setReuseAddress(true);
                socketTest1.connect(InetAddress.getByName(stunServer), port);
                socketTest1.setSoTimeout(timeout);

                System.out.println("!!!!! SocketAddress: " + socketTest1.getLocalSocketAddress());

                MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
                sendMH.generateTransactionID();

                ChangeRequest changeRequest = new ChangeRequest();
                sendMH.addMessageAttribute(changeRequest);

                byte[] data = sendMH.getBytes();
                DatagramPacket send = new DatagramPacket(data, data.length);
                socketTest1.send(send);
                Log.d(TAG,"Test 1: Binding Request sent.");

                MessageHeader receiveMH = new MessageHeader();
                while (!(receiveMH.equalTransactionID(sendMH))) {
                    DatagramPacket receive = new DatagramPacket(new byte[200], 200);
                    socketTest1.receive(receive);
                    receiveMH = MessageHeader.parseHeader(receive.getData());
                    receiveMH.parseAttributes(receive.getData());
                }

                ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
                ca = (ChangedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ChangedAddress);
                ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
                if (ec != null) {
                    di.setError(ec.getResponseCode(), ec.getReason());
                    Log.d(TAG, "Message header contains an Errorcode message attribute.");
                    return false;
                }
                if ((ma == null) || (ca == null)) {
                    Log.d(TAG, "The server is sending an incomplete response (Mapped Address and Changed Address message attributes are missing). The client should not retry.");
                    Log.d(TAG, "Response does not contain a Mapped Address or Changed Address message attribute.");
                    return false;
                } else {
                    di.setPublicIP(ma.getAddress().getInetAddress());
                    if ((ma.getPort() == socketTest1.getLocalPort()) && (ma.getAddress().getInetAddress().equals(socketTest1.getLocalAddress()))) {
                        Log.d(TAG, "Node is not natted.");
                        nodeNatted = false;
                    } else {
                        Log.d(TAG, "Node is natted.");
                    }

                    byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
                    DatagramPacket incoming = new DatagramPacket(lMsg, lMsg.length);
                    while(serverActive)
                    {
                        socketTest1.receive(incoming);
                        byte[] receivedData = incoming.getData();
                        String s = new String(receivedData, 0, incoming.getLength());
                        Log.i("UDP packet received", incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);
                    }
                }
            } catch (SocketTimeoutException ste) {
                if (timeSinceFirstTransmission < 7900) {
                    Log.d(TAG, "Test 1: Socket timeout while receiving the response.");
                    timeSinceFirstTransmission += timeout;
                    int timeoutAddValue = (timeSinceFirstTransmission * 2);
                    if (timeoutAddValue > 1600) timeoutAddValue = 1600;
                    timeout = timeoutAddValue;
                } else {
                    // node is not capable of udp communication
                    Log.d(TAG, "Test 1: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
                    di.setBlockedUDP();
                    Log.d(TAG, "Node is not capable of UDP communication.");
                    return false;
                }
            } catch (UtilityException e) {
                e.printStackTrace();
            } catch (MessageHeaderParsingException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MessageAttributeParsingException e) {
                e.printStackTrace();
            }
        }
    }

    public MappedAddress getMappedAddress() {
        return ma;
    }
}
