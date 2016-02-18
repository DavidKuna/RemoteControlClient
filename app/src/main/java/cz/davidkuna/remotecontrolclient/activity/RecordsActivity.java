package cz.davidkuna.remotecontrolclient.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.FileNotFoundException;

import cz.davidkuna.remotecontrolclient.R;
import cz.davidkuna.remotecontrolclient.log.LogSource;
import cz.davidkuna.remotecontrolclient.log.Simulator;

public class RecordsActivity extends AppCompatActivity {

    private final static String TAG = "RecordsAcitivity";
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        listView = (ListView) findViewById(R.id.listView);

        showSavedFiles();
    }

    private void showSavedFiles() {
        String[] SavedFiles = getApplicationContext().fileList();
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
        try {
            Thread simulation = new Thread(new Simulator(new LogSource(this.openFileInput(fileName))));
            simulation.run();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
