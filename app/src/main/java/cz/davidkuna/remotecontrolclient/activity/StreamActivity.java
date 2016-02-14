package cz.davidkuna.remotecontrolclient.activity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.VideoView;

import java.io.IOException;

import cz.davidkuna.remotecontrolclient.R;

public class StreamActivity extends AppCompatActivity  {

    public static final int PORT = 8554;
    MediaPlayer mediaPlayer;
    SurfaceHolder surfaceHolder;
    VideoView playerSurfaceView;
    protected SharedPreferences mSharedPreferences;

    //String videoSrc = "rtsp://192.168.1.100:8554/stream";
    String paramsUrl = "/?h264=200-20-320-240";
    String videoSrc = "rtsp://192.168.1.105:8554";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_stream);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        class VideoStream implements Runnable, SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {

            @Override
            public void run() {
                playerSurfaceView = (VideoView)findViewById(R.id.playersurface);

                surfaceHolder = playerSurfaceView.getHolder();
                surfaceHolder.addCallback(this);


                String serverIP = mSharedPreferences.getString(SettingsActivity.KEY_SERVER_IP, "127.0.0.1");
                //videoSrc = "rtsp://" + serverIP + ":" + PORT + paramsUrl;
            }

            @Override
            public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void surfaceCreated(SurfaceHolder arg0) {

                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDisplay(surfaceHolder);
                    Log.d("StreamActivity", "Connecting to " + videoSrc);
                    mediaPlayer.setDataSource(videoSrc);
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                    mediaPlayer.prepare();


                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        }

        Thread connectThread = new Thread(new VideoStream());
        connectThread.start();
    }


}
