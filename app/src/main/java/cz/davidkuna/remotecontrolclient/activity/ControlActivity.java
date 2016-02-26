package cz.davidkuna.remotecontrolclient.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Locale;

import cz.davidkuna.remotecontrolclient.sensors.Attitude;
import cz.davidkuna.remotecontrolclient.view.AttitudeIndicator;
import cz.davidkuna.remotecontrolclient.view.GyroVisualizer;
import cz.davidkuna.remotecontrolclient.R;
import cz.davidkuna.remotecontrolclient.log.Logger;
import cz.davidkuna.remotecontrolclient.sensors.SensorDataEventListener;
import cz.davidkuna.remotecontrolclient.sensors.SensorDataInterpreter;
import cz.davidkuna.remotecontrolclient.sensors.SensorDataStream;
import cz.davidkuna.remotecontrolclient.videostream.MjpegView;
import cz.davidkuna.remotecontrolclient.videostream.VideoStream;

public class ControlActivity extends AppCompatActivity {

    public static String KEY_SERVER_ADDRESS = "serverAddress";
    public static String KEY_SERVER_PORT = "serverPort";
    public static String KEY_SENSOR_INTERVAL = "interval";

    private VideoStream videoStream = null;
    private SensorDataStream sensorDataStream = null;
    private SensorDataInterpreter sensorDataInterpreter = null;
    private GyroVisualizer mGyroView = null;
    private AttitudeIndicator mAttitudeView = null;
    private TextView roll = null;
    private TextView pitch = null;
    private TextView yaw = null;

    private String serverAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                serverAddress = extras.getString(KEY_SERVER_ADDRESS);
            }
        } else {
            serverAddress = (String) savedInstanceState.getSerializable(KEY_SERVER_ADDRESS);
        }

        startVideoStream();

        initSensorDataStream();
        startSensorDataStream();
    }

    private void startVideoStream() {
        MjpegView videoView = (MjpegView) findViewById(R.id.cameraView);
        videoStream = new VideoStream(videoView, serverAddress);
    }

    private void initSensorDataStream() {
        sensorDataInterpreter = new SensorDataInterpreter(eventListener);
        sensorDataStream = new SensorDataStream(new Logger(this));
        sensorDataStream.setInterpreter(sensorDataInterpreter);

        //mGyroView = (GyroVisualizer) findViewById(R.id.visualizer);
        mAttitudeView = (AttitudeIndicator) findViewById(R.id.aiView);
        pitch = (TextView) findViewById(R.id.pitchValueText);
        roll = (TextView) findViewById(R.id.rollValueText);
        yaw = (TextView) findViewById(R.id.yawValueText);
    }

    private void startSensorDataStream() {
        sensorDataStream.setServerAddress(serverAddress);
        sensorDataStream.start();
    }

    private SensorDataEventListener eventListener = new SensorDataEventListener() {
        @Override
        public void onDataChanged() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mGyroView != null) {
                        mGyroView.setAcceleration(sensorDataInterpreter.getAccelerometer());
                        mGyroView.setGyroRotation(sensorDataInterpreter.getGyroscopeData());
                    }
                    Attitude attitude = new Attitude(sensorDataInterpreter.getAccelerometer());
                    attitude.setYaw(sensorDataInterpreter.getCompass().getDegree() - 180);
                    mAttitudeView.setAttitude(attitude);
                    pitch.setText(String.format(Locale.US, "%3.0f\u00B0", attitude.getPitch()));
                    roll.setText(String.format(Locale.US, "%3.0f\u00B0", attitude.getRoll()));
                    yaw.setText(String.format(Locale.US, "%3.0f\u00B0", attitude.getYaw()));
                }
            });
        }
    };

    private void onLogButtonClick(View v) {
        if (sensorDataStream.isLoggerActive()) {
            sensorDataStream.stopLogging();
        } else {
            sensorDataStream.startLogging();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoStream != null) {
            videoStream.close();
        }

        if (sensorDataStream != null) {
            sensorDataStream.stop();
        }
    }
}
