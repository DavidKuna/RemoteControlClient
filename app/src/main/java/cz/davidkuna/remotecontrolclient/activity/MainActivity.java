package cz.davidkuna.remotecontrolclient.activity;

import android.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

import cz.davidkuna.remotecontrolclient.R;
import cz.davidkuna.remotecontrolclient.SensorDataInterpreter;
import cz.davidkuna.remotecontrolclient.SensorDataListener;
import cz.davidkuna.remotecontrolclient.socket.UDPClient;
import cz.davidkuna.remotecontrolclient.socket.UDPListener;

public class MainActivity extends AppCompatActivity {

    protected UDPClient client;
    protected UDPListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connect();
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeFragmentTextView(int id, String s) {
        TextView tv = (TextView)this.findViewById(id);
        tv.setText(s);
    }

    private boolean connect() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        InetAddress serverIp;
        final int interval = 100; // miliseconds

        try {
            serverIp = InetAddress.getByName(prefs.getString("server_ip", "127.0.0.1"));
        } catch (UnknownHostException e) {
            Log.d("ERROR", e.toString());
            Toast.makeText(this, "Unknown server host", Toast.LENGTH_SHORT).show();
            return false;
        }

        client = new UDPClient(serverIp, 8000);
        listener = new UDPListener();
        final SensorDataInterpreter sensorDataInterpreter = new SensorDataInterpreter(this);
        sensorDataInterpreter.setSensorDataListener(new SensorDataListener() {
            @Override
            public void onDataChanged() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeFragmentTextView(R.id.tvAccelerometerX, sensorDataInterpreter.getAccelerometerData());
                        changeFragmentTextView(R.id.tvGryroscopeX, sensorDataInterpreter.getGyroscopeData());
                    }
                });
            }
        });
        listener.runUdpServer(8001, sensorDataInterpreter);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(interval);
                        client.sendMessage("getSensorData");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

        return true;
    }

    private void disconnect() {
        listener.stopUDPServer();
    }
}
