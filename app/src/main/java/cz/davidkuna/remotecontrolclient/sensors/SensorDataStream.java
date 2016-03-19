package cz.davidkuna.remotecontrolclient.sensors;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import cz.davidkuna.remotecontrolclient.log.Logger;
import cz.davidkuna.remotecontrolclient.socket.DataMessage;
import cz.davidkuna.remotecontrolclient.videostream.MulticastStream;

/**
 * Created by David Kuna on 25.2.16.
 */
public class SensorDataStream {

    /**
     * Period of receivig sensor data
     */
    private final int DEF_FREQUENCY = 200;
    private final int DEF_SERVER_PORT = 8001;
    private final int DEF_LISTENER_PORT = 8001;

    private static final int MAX_UDP_DATAGRAM_LEN = 1096;

    private MulticastStream multicastStream = null;
    private Logger logger = null;
    private SensorDataInterpreter interpreter = null;
    private Thread mWorker = null;
    private boolean active = false;
    private boolean loggerActive = false;

    private InetAddress serverAddress = null;
    private int interval = DEF_FREQUENCY;
    private int serverPort = DEF_SERVER_PORT;
    private int listenerPort = DEF_LISTENER_PORT;

    public SensorDataStream(Logger logger) {
        this.logger = logger;
    }

    public void start() {
        try {
            multicastStream = new MulticastStream(serverAddress.getHostAddress(), serverPort);
            multicastStream.open(listenerPort);
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
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    private void receive() {

        while(active)
        {
            try {
                String messageString = multicastStream.receiveDatagram();
                DataMessage message = new DataMessage(messageString);
                if (loggerActive) {
                    logger.log(message);
                }
                interpreter.processData(message);
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

    public void setListenerPort(int port) {
        this.listenerPort = port;
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
