package cz.davidkuna.remotecontrolclient.socket;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import cz.davidkuna.remotecontrolclient.helpers.Network;
import cz.davidkuna.remotecontrolclient.helpers.Settings;

/**
 * Created by David Kuna on 26.2.16.
 */
public class RemoteControl {

    private final String TAG = "RemoteControl";
    private AsyncTask<Void, Void, Void> async_cient;
    private String commandMessage;
    private StunConnection stunConnection = null;
    private Settings settings;
    private Thread mWorker = null;

    public RemoteControl(Settings settings) {
        this.settings = settings;
        if (settings.isUseStun()) {
            stunConnection = new StunConnection(Network.getLocalInetAddress(),
                    settings.getStunServer(),
                    settings.getStunPort(),
                    settings.getRelayServer(),
                    settings.getControlToken());
        }
    }

    public void open() {
        if (stunConnection != null) {
            mWorker = new Thread(new Runnable() {
                @Override
                public void run() {
                    stunConnection.connect();
                }
            });
            mWorker.start();
        }
    }

    public void close() {
        if (stunConnection != null) {
            stunConnection.close();
        }
        if (mWorker != null) {
            mWorker.interrupt();
        }
    }

    @SuppressLint("NewApi")
    public void sendCommand(Command command) {
        commandMessage = new Gson().toJson(command);
        async_cient = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                if (stunConnection != null) {
                    if (stunConnection.isRelated()) {
                        stunConnection.send(commandMessage);
                    } else {
                        Log.d(TAG, "Relation is not completed. Cannot send.");
                    }
                } else {

                    DatagramSocket ds = null;

                    try {
                        ds = new DatagramSocket();
                        DatagramPacket dp;
                        dp = new DatagramPacket(commandMessage.getBytes(), commandMessage.length(),
                                InetAddress.getByName(settings.getServerAddress()), settings.getControlUDPPort());
                        ds.send(dp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (ds != null) {
                            ds.close();
                        }
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        };

        if (Build.VERSION.SDK_INT >= 11)
            async_cient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_cient.execute();

     }
}
