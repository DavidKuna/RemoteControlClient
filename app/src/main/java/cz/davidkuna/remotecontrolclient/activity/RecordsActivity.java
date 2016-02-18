package cz.davidkuna.remotecontrolclient.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import cz.davidkuna.remotecontrolclient.R;

public class RecordsActivity extends AppCompatActivity {

    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        listView = (ListView) findViewById(R.id.listView);

        showSavedFiles();
    }

    void showSavedFiles() {
        String[] SavedFiles = getApplicationContext().fileList();
        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                SavedFiles);


        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
