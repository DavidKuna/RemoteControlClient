package cz.davidkuna.remotecontrolclient.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import cz.davidkuna.remotecontrolclient.R;
import cz.davidkuna.remotecontrolclient.fragment.MapFragment;
import cz.davidkuna.remotecontrolclient.log.LogRecord;
import cz.davidkuna.remotecontrolclient.log.LogSource;
import cz.davidkuna.remotecontrolclient.log.Simulator;
import cz.davidkuna.remotecontrolclient.sensors.OnLocationChangedListener;
import cz.davidkuna.remotecontrolclient.sensors.SensorDataEventListener;
import cz.davidkuna.remotecontrolclient.sensors.SensorDataInterpreter;

public class SimulationActivity extends LocationChangeableActivity {

    private SensorDataInterpreter sensorDataInterpreter = null;
    private LineChart chart;
    private ArrayList<Entry> valsCompRoll = new ArrayList<Entry>();
    private ArrayList<Entry> valsCompPitch = new ArrayList<Entry>();
    private ArrayList<String> xVals = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);

        sensorDataInterpreter = new SensorDataInterpreter(eventListener);

        String fileName = getIntent().getStringExtra("fileName");
        if (fileName != null && fileName.isEmpty() == false) {
            initChart(fileName);
        }
    }

    private void initGoogleMaps(PolylineOptions options) {
        Gson gson = new Gson();
        Bundle bundle = new Bundle();
        bundle.putString("polylineoptions", gson.toJson(options));

        MapFragment fragment = new MapFragment();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_google_map, fragment);
        fragmentTransaction.commit();
    }

    private void initChart(String fileName) {
        chart = (LineChart) findViewById(R.id.chartRoll);
        int scale = 1;
        try {
            LogRecord record;
            LogSource source = new LogSource(this.openFileInput(fileName));
            PolylineOptions options = new PolylineOptions();

            options.color(Color.parseColor("#CC0000FF"));
            options.width(5);
            options.visible(true);

            int i = 0;
            while ((record = source.getNext()) != null) {
                if (i % scale == 0) {
                    sensorDataInterpreter.processData(record.getMessage());

                    valsCompRoll.add(new Entry((float) sensorDataInterpreter.getAttitude().getRoll(), i / scale));
                    valsCompPitch.add(new Entry((float) sensorDataInterpreter.getAttitude().getPitch(), i / scale));

                    xVals.add(StrictMath.round(i / (2 * scale)) + "s");

                    options.add( new LatLng(sensorDataInterpreter.getLocation().getLatitude(),
                            sensorDataInterpreter.getLocation().getLongitude() ) );
                }
                i++;
            }

            initGoogleMaps(options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        LineDataSet setCompRoll = new LineDataSet(valsCompRoll, "Roll");
        setCompRoll.setAxisDependency(YAxis.AxisDependency.LEFT);
        setCompRoll.setDrawValues(false);
        setCompRoll.setDrawCircles(false);

        LineDataSet setCompPitch = new LineDataSet(valsCompPitch, "Pitch");
        setCompPitch.setAxisDependency(YAxis.AxisDependency.LEFT);
        setCompPitch.setColors(new int[]{R.color.theme_green}, getBaseContext());
        setCompPitch.setDrawValues(false);
        setCompPitch.setDrawCircles(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setCompRoll);
        dataSets.add(setCompPitch);

        LineData data = new LineData(xVals, dataSets);
        chart.setData(data);
        chart.invalidate();
    }

    private void updateChart() {

    }

    private void simulate(String fileName) {
        try {

            Simulator simulation = new Simulator(new LogSource(this.openFileInput(fileName)), sensorDataInterpreter);
            simulation.run();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private SensorDataEventListener eventListener = new SensorDataEventListener() {
        @Override
        public void onDataChanged() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                }
            });
        }
    };

    @Override
    public void setOnLocationChangedListener(OnLocationChangedListener listener) {

    }
}
