package cz.davidkuna.remotecontrolclient.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import cz.davidkuna.remotecontrolclient.helpers.Settings;
import cz.davidkuna.remotecontrolclient.R;

public class MainActivity extends AppCompatActivity {

    private Settings settings = new Settings();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initButtonListeners();
        initRecords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecords();
    }

    private void initButtonListeners() {
        Button bConnect = (Button) findViewById(R.id.bConnect);
        bConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        Button bScanQR = (Button) findViewById(R.id.scanQRcode);
        bScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSettingsFromQR();
            }
        });

        findViewById(R.id.preferences).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadSettingsFromQR() {
        boolean isZxingInstalled = false;

        try
        {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.zxing.client.android", 0 );
            isZxingInstalled = true;
        }
        catch(PackageManager.NameNotFoundException e){
            isZxingInstalled = false;
        }
        // TODO isZxingInstalled
        try //If it is then intent Zxing application
        {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e){
            Toast.makeText(this,"Install Barcode Scanner First",Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                Gson gson = new Gson();
                settings = gson.fromJson(contents, Settings.class);
                connect();
                
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                Toast toast = Toast.makeText(this, "Scan was Cancelled!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();

            }
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

    private void initRecords() {
        listView = (ListView) findViewById(R.id.listView);
        ArrayList<String> SavedFiles = new ArrayList<>();
        for (String name: getApplicationContext().fileList()) {
            if (name.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{2}\\.log$")) {
                SavedFiles.add(name);
            }
        }
        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                SavedFiles);


        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                simulate(listView.getItemAtPosition(position).toString());
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void simulate(String fileName) {
        Intent intent = new Intent();
        intent.putExtra("fileName", fileName);
        intent.setClass(MainActivity.this, SimulationActivity.class);
        startActivity(intent);
    }

    private boolean connect() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (settings.getServerAddress() == null) {
            settings.setServerAddress(prefs.getString("server_ip", "127.0.0.1"));
        }

        if (settings.getSensorUDPPort() == 0) {
            settings.setSensorUDPPort(prefs.getInt("sensor_udp_port", 8000));
        }

        final int interval = Integer.valueOf(prefs.getString("request_interval", "200")); // miliseconds

        Intent intent = new Intent();
        intent.putExtra(ControlActivity.KEY_SENSOR_INTERVAL, interval);
        Gson gson = new Gson();
        intent.putExtra("settings", gson.toJson(settings));
        intent.setClass(MainActivity.this, ControlActivity.class);
        startActivity(intent);

        return true;
    }
}
