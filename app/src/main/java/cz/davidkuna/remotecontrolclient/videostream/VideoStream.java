package cz.davidkuna.remotecontrolclient.videostream;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class VideoStream{

    private final String TAG = "VideoStream";
    private MjpegView mv;
    MulticastStream multicast = null;
    private final int mPort = 8080;
    private String mAddress;
    private AsyncTask<Void, Void, Void> async;

    public VideoStream(MjpegView mjpegView, String serverIp) {

        mAddress = serverIp;
        mv = mjpegView;
        mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        mv.showFps(true);

        try {
            multicast = new MulticastStream(mAddress, mPort);
            multicast.open();
            setSource(new MjpegInputStream(multicast));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void setSource(MjpegInputStream source) {
        mv.setSource(source);
    }

    public void close() {
        try {
            multicast.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mv.stopPlayback();
    }
}
