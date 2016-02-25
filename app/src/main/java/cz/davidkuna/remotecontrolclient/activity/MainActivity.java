package cz.davidkuna.remotecontrolclient.activity;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import cz.davidkuna.remotecontrolclient.view.GyroVisualizer;
import cz.davidkuna.remotecontrolclient.R;
import cz.davidkuna.remotecontrolclient.log.LogSource;
import cz.davidkuna.remotecontrolclient.log.Logger;
import cz.davidkuna.remotecontrolclient.log.Simulator;
import cz.davidkuna.remotecontrolclient.sensors.SensorDataInterpreter;
import cz.davidkuna.remotecontrolclient.sensors.SensorDataEventListener;
import cz.davidkuna.remotecontrolclient.socket.UDPClient;
import cz.davidkuna.remotecontrolclient.socket.UDPListener;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initButtonListeners();
    }

    private void initButtonListeners() {
        Button bConnect = (Button) findViewById(R.id.bConnect);
        bConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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

    private boolean connect() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final int interval = Integer.valueOf(prefs.getString("request_interval", "200")); // miliseconds
        InetAddress serverIp;
        try {
            serverIp = InetAddress.getByName(prefs.getString("server_ip", "127.0.0.1"));
        } catch (UnknownHostException e) {
            Toast.makeText(this, "Unknown server host", Toast.LENGTH_SHORT).show();
            return false;
        }

        Intent intent = new Intent();
        intent.putExtra(ControlActivity.KEY_SERVER_ADDRESS, serverIp);
        intent.putExtra(ControlActivity.KEY_SENSOR_INTERVAL, interval);
        intent.setClass(MainActivity.this, ControlActivity.class);
        startActivity(intent);

        return true;
    }

}
