package cz.davidkuna.remotecontrolclient.videostream;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import cz.davidkuna.remotecontrolclient.helpers.Network;
import cz.davidkuna.remotecontrolclient.helpers.Settings;
import cz.davidkuna.remotecontrolclient.socket.Relation;
import cz.davidkuna.remotecontrolclient.socket.StunConnection;

public class VideoStream implements Relation {

    private final String TAG = "VideoStream";
    private MjpegView mv;
    MulticastStream multicast = null;
    private AsyncTask<Void, Void, Void> async;

    public VideoStream(MjpegView mjpegView, Settings settings) {

        mv = mjpegView;
        mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        mv.showFps(true);

        try {
            if (settings.isUseStun()) {
                StunConnection connection = new StunConnection(Network.getLocalInetAddress(),
                        settings.getStunServer(),
                        settings.getStunPort(),
                        settings.getRelayServer(),
                        settings.getCameraToken());
                connection.setRelation(this);
                multicast = new MulticastStream(connection);
            } else {
                multicast = new MulticastStream(settings.getServerAddress(), settings.getCameraUDPPort());
                onRelationCreated();
            }

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
        } catch (IllegalStateException e) {}
        mv.stopPlayback();
    }

    @Override
    public void onRelationCreated() {
        multicast.open();
        setSource(new MjpegInputStream(multicast));
    }
}
