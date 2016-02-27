package cz.davidkuna.remotecontrolclient.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Locale;

import cz.davidkuna.remotecontrolclient.OnLocationChangedListener;
import cz.davidkuna.remotecontrolclient.fragment.MapFragment;
import cz.davidkuna.remotecontrolclient.sensors.Attitude;
import cz.davidkuna.remotecontrolclient.socket.Command;
import cz.davidkuna.remotecontrolclient.socket.RemoteControl;
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

    public static String TAG = "ControlActivity";
    public static String KEY_SERVER_ADDRESS = "serverAddress";
    public static String KEY_SERVER_PORT = "serverPort";
    public static String KEY_SENSOR_INTERVAL = "interval";

    private VideoStream videoStream = null;
    private RemoteControl remoteControl = null;
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

        Bundle extras = getIntent().getExtras();
        serverAddress = extras.getString(KEY_SERVER_ADDRESS);
        int serverCommandPort = extras.getInt(KEY_SERVER_PORT);

        initButtons();
        initSensorDataStream();
        remoteControl = new RemoteControl(serverAddress, serverCommandPort);

        startVideoStream();
        startSensorDataStream();
        initGoogleMaps();
    }

    private void startVideoStream() {
        MjpegView videoView = (MjpegView) findViewById(R.id.cameraView);
        videoStream = new VideoStream(videoView, serverAddress);
    }

    private void initGoogleMaps() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_google_map, new MapFragment());
        fragmentTransaction.commit();
    }

    private void setLocation() {

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

    private void initButtons() {
        final ImageButton bLog = (ImageButton) findViewById(R.id.bLog);
        bLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sensorDataStream.isLoggerActive()) {
                    bLog.clearAnimation();
                    sensorDataStream.stopLogging();
                    Toast.makeText(getApplicationContext(), R.string.record_saved, Toast.LENGTH_SHORT).show();
                } else {
                    bLog.startAnimation(getBlinkingAnimation());
                    sensorDataStream.startLogging();
                    Toast.makeText(getApplicationContext(), R.string.logging_started, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.controlUp).setOnClickListener(controlButtonOnClick);
        findViewById(R.id.controlRight).setOnClickListener(controlButtonOnClick);
        findViewById(R.id.controlDown).setOnClickListener(controlButtonOnClick);
        findViewById(R.id.controlLeft).setOnClickListener(controlButtonOnClick);
    }

    private Animation getBlinkingAnimation() {
        final Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);

        return animation;
    };

    private View.OnClickListener controlButtonOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Command command = new Command();
            switch (v.getId()) {
                case R.id.controlUp : command.setName(Command.MOVE_UP);
                    break;
                case R.id.controlRight: command.setName(Command.MOVE_RIGHT);
                    break;
                case R.id.controlDown: command.setName(Command.MOVE_DOWN);
                    break;
                case R.id.controlLeft: command.setName(Command.MOVE_LEFT);
                    break;
            }

            remoteControl.sendCommand(command);
        }
    };

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

    public void setOnLocationChangedListener(OnLocationChangedListener listener) {
        if (sensorDataInterpreter != null) {
            sensorDataInterpreter.setOnLocationChanged(listener);
        }
    }
}
