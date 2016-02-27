package cz.davidkuna.remotecontrolclient.sensors;

import cz.davidkuna.remotecontrolclient.OnLocationChangedListener;
import cz.davidkuna.remotecontrolclient.socket.DataMessage;

/**
 * Created by David Kuna on 5.2.16.
 */
public class SensorDataInterpreter {

    private SensorDataEventListener listener = null;
    private OnLocationChangedListener locationListener = null;
    private Accelerometer accelerometer = null;
    private Gyroscope gyroscope = null;
    private Compass compass = null;
    private GPS location = null;

    public SensorDataInterpreter() {
       initSensors();
    }

    public SensorDataInterpreter(SensorDataEventListener eventListener) {
        initSensors();
        this.listener = eventListener;
    }

    private void initSensors() {
        accelerometer = new Accelerometer();
        gyroscope = new Gyroscope();
        compass = new Compass();
        location = new GPS();
    }

    public void processData(DataMessage data) {
        for (String[] item : data.getData()) {
            switch (item[0]) {
                case DataMessage.TYPE_ACCELEROMETER :
                    accelerometer.setData(item[1]);
                    break;
                case DataMessage.TYPE_GYROSCOPE :
                    gyroscope.setData(item[1]);
                    break;
                case DataMessage.TYPE_COMPASS :
                    compass.setData(item[1]);
                    break;
                case DataMessage.TYPE_GPS :
                    location.setData(item[1]);
                    if (locationListener != null) {
                        locationListener.OnChange(location.getLatitude(), location.getLongitude(), compass.getDegree());
                    }
                    break;
            }
        }

        listener.onDataChanged();
    }

    public Accelerometer getAccelerometer() {
        return accelerometer;
    }

    public Gyroscope getGyroscopeData() {
        return gyroscope;
    }

    public Compass getCompass() {
        return compass;
    };

    public GPS getLocation() {
        return location;
    }

    public void setSensorDataListener(SensorDataEventListener listener) {
        this.listener = listener;
    }

    public void setOnLocationChanged(OnLocationChangedListener eventListener) {
        locationListener = eventListener;
    }
}
