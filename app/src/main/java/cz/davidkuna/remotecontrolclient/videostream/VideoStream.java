package cz.davidkuna.remotecontrolclient.videostream;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import cz.davidkuna.remotecontrolclient.R;

public class VideoStream extends AppCompatActivity {

    private MjpegView mv;
    private String URL = "http://192.168.1.106:8080";
    private AsyncTask<Void, Void, Void> async;
    private boolean serverActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        super.onCreate(savedInstanceState);

        try {
            UDPListener listener = new UDPListener();

            new MjpegInputStream(new UDPInputStream(null, 9000));

            //listener.runUdpServer(9000);
            mv = new MjpegView(this);
            setContentView(mv);
            connect();
            mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mv.showFps(false);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    public void connect()
    {
        serverActive = true;
        async = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {

                try {
                    setSource(MjpegInputStream.read(new URL(URL)));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        Log.d("UDPListener", "Executing");
        if (Build.VERSION.SDK_INT >= 11) async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async.execute();
    }

    private void setSource(MjpegInputStream source) {
        mv.setSource(source);
    }


    public void onPause() {
        super.onPause();
        //mv.stopPlayback();
    }
}
