package cz.davidkuna.remotecontrolclient.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

import cz.davidkuna.remotecontrolclient.GyroVisualizer;
import cz.davidkuna.remotecontrolclient.R;
import cz.davidkuna.remotecontrolclient.log.Logger;
import cz.davidkuna.remotecontrolclient.sensors.SensorDataInterpreter;
import cz.davidkuna.remotecontrolclient.sensors.SensorDataEventListener;
import cz.davidkuna.remotecontrolclient.socket.UDPClient;
import cz.davidkuna.remotecontrolclient.socket.UDPListener;

public class MainActivity extends AppCompatActivity {

    protected UDPClient client;
    protected UDPListener listener;
    private boolean connected = false;
    private boolean enabled = false;
    private ImageView ivCompass;
    private GyroVisualizer mGyroView;
    // record the compass picture angle turned
    private float currentDegree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ivCompass = (ImageView) findViewById(R.id.ivCompass);

        client = new UDPClient();
        listener = new UDPListener(new Logger(this));

        Button bConnect = (Button) findViewById(R.id.bConnect);
        bConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnectButtonClick(v);
            }
        });

        mGyroView = (GyroVisualizer) findViewById(R.id.visualizer);

        Button bLog = (Button) findViewById(R.id.bLog);
        bLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogButtonClick(v);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (connected) {
            this.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (enabled) {
            Button bConnect = (Button) findViewById(R.id.bConnect);
            bConnect.setActivated(true);
            this.connect();
        }
    }

    private void onConnectButtonClick(View v) {
        if (connected) {
            enabled = false;
            disconnect();
        } else {
            enabled = true;
            connect();
        }
    }

    private void onLogButtonClick(View v) {
        if (listener.isLoggerActive()) {
            listener.stopLogging();
        } else {
            listener.startLogging();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_records) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, RecordsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeFragmentTextView(int id, String s) {

        try {
            TextView tv = (TextView) this.findViewById(id);
            tv.setText(s);
        } catch (NullPointerException e){

        }
    }

    private void renderCompass(float degree) {

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        ivCompass.startAnimation(ra);
        Log.d("COMPASS", "From: " + currentDegree + " to: " + degree);
        currentDegree = -degree;
    }

    private boolean connect() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final int interval = Integer.valueOf(prefs.getString("request_interval", "200")); // miliseconds
        InetAddress serverIp;
        try {
            serverIp = InetAddress.getByName(prefs.getString("server_ip", "127.0.0.1"));
        } catch (UnknownHostException e) {
            Log.d("ERROR", e.toString());
            Toast.makeText(this, "Unknown server host", Toast.LENGTH_SHORT).show();
            return false;
        }

        final SensorDataInterpreter sensorDataInterpreter = new SensorDataInterpreter(this);
        sensorDataInterpreter.setSensorDataListener(new SensorDataEventListener() {
            @Override
            public void onDataChanged() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mGyroView != null) {
                            mGyroView.setAcceleration(
                                    sensorDataInterpreter.getAccelerometer().getX(),
                                    sensorDataInterpreter.getAccelerometer().getY()
                            );
                        }
                        changeFragmentTextView(R.id.tvAccelerometerX, String.valueOf(sensorDataInterpreter.getAccelerometer().getX()));
                        changeFragmentTextView(R.id.tvAccelerometerY, String.valueOf(sensorDataInterpreter.getAccelerometer().getY()));
                        changeFragmentTextView(R.id.tvAccelerometerZ, String.valueOf(sensorDataInterpreter.getAccelerometer().getZ()));

                        if (mGyroView != null) {
                            mGyroView.setGyroRotation(
                                    sensorDataInterpreter.getGyroscopeData().getX(),
                                    sensorDataInterpreter.getGyroscopeData().getY(),
                                    sensorDataInterpreter.getGyroscopeData().getZ()
                            );
                        }
                        changeFragmentTextView(R.id.tvGryroscopeX, String.valueOf(sensorDataInterpreter.getGyroscopeData().getX()));
                        changeFragmentTextView(R.id.tvGryroscopeY, String.valueOf(sensorDataInterpreter.getGyroscopeData().getY()));
                        changeFragmentTextView(R.id.tvGryroscopeZ, String.valueOf(sensorDataInterpreter.getGyroscopeData().getZ()));

                        renderCompass(sensorDataInterpreter.getCompass().getDegree());

                        changeFragmentTextView(R.id.tvLocation,
                                String.valueOf(sensorDataInterpreter.getLocation().getLatitude()) + " " +
                                        String.valueOf(sensorDataInterpreter.getLocation().getLongitude())

                        );
                    }
                });
            }
        });

        client.start(serverIp, 8000, interval);
        listener.runUdpServer(8001, sensorDataInterpreter);
        connected = true;

        return true;
    }

    private void disconnect() {
        listener.stopUDPServer();
        client.stop();
        connected = false;
    }
}
