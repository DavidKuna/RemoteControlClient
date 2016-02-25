package cz.davidkuna.remotecontrolclient.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.FileNotFoundException;

import cz.davidkuna.remotecontrolclient.R;
import cz.davidkuna.remotecontrolclient.log.LogSource;
import cz.davidkuna.remotecontrolclient.log.Simulator;
import cz.davidkuna.remotecontrolclient.sensors.SensorDataInterpreter;

public class SimulationActivity extends AppCompatActivity {

    private SensorDataInterpreter sensorDataInterpreter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);

        try {
            String fileName = getIntent().getStringExtra("fileName");
            if (fileName.isEmpty() == false) {
                simulate(fileName);
            }
        } catch (NullPointerException e) {

        }
    }

    private void simulate(String fileName) {
        try {
            Simulator simulation = new Simulator(new LogSource(this.openFileInput(fileName)), sensorDataInterpreter);
            simulation.run();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
