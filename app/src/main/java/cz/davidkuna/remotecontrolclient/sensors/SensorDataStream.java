package cz.davidkuna.remotecontrolclient.sensors;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import cz.davidkuna.remotecontrolclient.helpers.Network;
import cz.davidkuna.remotecontrolclient.helpers.Settings;
import cz.davidkuna.remotecontrolclient.log.Logger;
import cz.davidkuna.remotecontrolclient.socket.DataMessage;
import cz.davidkuna.remotecontrolclient.socket.Relation;
import cz.davidkuna.remotecontrolclient.socket.StunConnection;
import cz.davidkuna.remotecontrolclient.videostream.MulticastStream;

/**
 * Created by David Kuna on 25.2.16.
 */
public class SensorDataStream implements Relation {

    /**
     * Period of receivig sensor data
     */
    private final int DEF_FREQUENCY = 200;

    private static final int MAX_UDP_DATAGRAM_LEN = 1096;

    private MulticastStream multicastStream = null;
    private Logger logger = null;
    private SensorDataInterpreter interpreter = null;
    private Thread mWorker = null;
    private boolean active = false;
    private boolean loggerActive = false;

    private InetAddress serverAddress = null;
    private int interval = DEF_FREQUENCY;
    private int serverPort;

    public SensorDataStream(Logger logger) {
        this.logger = logger;
    }

    public void start(Settings settings) {
        try {
            if (settings.isUseStun()) {
                StunConnection connection = new StunConnection(Network.getLocalInetAddress(),
                        settings.getStunServer(),
                        settings.getStunPort(),
                        settings.getRelayServer(),
                        settings.getSensorToken());
                connection.setRelation(this);
                multicastStream = new MulticastStream(connection);
            } else {
                multicastStream = new MulticastStream(settings.getServerAddress(), settings.getSensorUDPPort());
                onRelationCreated();
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    public void onRelationCreated() {
        multicastStream.open();
        active = true;
        mWorker = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                receive();
            }
        });
        mWorker.start();
    }

    private void receive() {

        while(active)
        {
            try {
                String messageString = multicastStream.receiveDatagram();
                if (!messageString.equals("beat") && messageString != "beat" && messageString.length() > 8) {
                    DataMessage message = new DataMessage(messageString);
                    if (loggerActive) {
                        logger.log(message);
                    }
                    interpreter.processData(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (active) {
            active = false;
            mWorker.interrupt();
            try {
                multicastStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setServerAddress(InetAddress address) {
        serverAddress = address;
    }

    public void setServerAddress(String address) {
        try {
            serverAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setServerPort(int port) {
        this.serverPort = port;
    }

    public void setInterpreter(SensorDataInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void startLogging() {
        logger.setFileName(logger.getNewFileName());
        loggerActive = true;
        Log.d("UDPListener", "Start recording to file " + logger.getFileName());
    }

    public void stopLogging() {
        loggerActive = false;
    }

    public boolean isLoggerActive() {
        return loggerActive;
    }
}
