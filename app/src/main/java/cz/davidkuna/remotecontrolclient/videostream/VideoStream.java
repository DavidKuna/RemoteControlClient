package cz.davidkuna.remotecontrolclient.videostream;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import cz.davidkuna.remotecontrolclient.R;

public class VideoStream extends AppCompatActivity {

    private MjpegView mv;
    MulticastStream multicast;
    private final int mPort = 8080;
    private String mAddress;
    private AsyncTask<Void, Void, Void> async;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mAddress = prefs.getString("server_ip", "127.0.0.1");

        mv = new MjpegView(this);
        setContentView(mv);
        connect();
        mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        mv.showFps(true);

    }

    @SuppressLint("NewApi")
    public void connect()
    {
        async = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {

                try {
                    multicast = new MulticastStream(mAddress, mPort);
                    multicast.start();
                    setSource(new MjpegInputStream(multicast));
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        if (Build.VERSION.SDK_INT >= 11) async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async.execute();
    }

    private void setSource(MjpegInputStream source) {
        mv.setSource(source);
    }


    public void onPause() {
        super.onPause();
        mv.stopPlayback();
        multicast.stop();
    }
}
