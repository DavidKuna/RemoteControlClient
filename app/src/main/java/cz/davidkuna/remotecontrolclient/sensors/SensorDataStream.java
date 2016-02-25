package cz.davidkuna.remotecontrolclient.sensors;

import android.view.View;

import java.net.InetAddress;
import java.net.UnknownHostException;

import cz.davidkuna.remotecontrolclient.log.Logger;
import cz.davidkuna.remotecontrolclient.socket.UDPClient;
import cz.davidkuna.remotecontrolclient.socket.UDPListener;

/**
 * Created by David Kuna on 25.2.16.
 */
public class SensorDataStream {

    /**
     * Period of receivig sensor data
     */
    private final int DEF_FREQUENCY = 200;
    private final int DEF_SERVER_PORT = 8000;
    private final int DEF_LISTENER_PORT = 8001;

    private UDPClient client = null;
    private UDPListener listener = null;
    private Logger logger = null;
    private SensorDataInterpreter interpreter = null;
    private boolean active = false;

    private InetAddress serverAddress = null;
    private int interval = DEF_FREQUENCY;
    private int serverPort = DEF_SERVER_PORT;
    private int listenerPort = DEF_LISTENER_PORT;

    public SensorDataStream(Logger logger) {
        this.logger = logger;
        client = new UDPClient();
        listener = new UDPListener(logger);
    }

    public void start() {
        client.start(serverAddress, serverPort, interval);
        listener.runUdpServer(listenerPort, interpreter);
        active = true;
    }

    public void stop() {
        if (active) {
            active = false;
            client.stop();
            listener.stopUDPServer();
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
        if (!listener.isLoggerActive()) {
            listener.stopLogging();
        }
    }

    public void stopLogging() {
        if (listener.isLoggerActive()) {
            listener.stopLogging();
        }
    }

    public boolean isLoggerActive() {
        return listener.isLoggerActive();
    }
}
