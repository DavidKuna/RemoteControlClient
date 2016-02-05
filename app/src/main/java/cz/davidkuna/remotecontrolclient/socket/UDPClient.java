package cz.davidkuna.remotecontrolclient.socket;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by David Kuna on 6.1.16.
 */
public class UDPClient {

    private AsyncTask<Void, Void, Void> async_cient;
    public String Message;
    private InetAddress serverIp;
    private int serverPort;

    public UDPClient(InetAddress serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    @SuppressLint("NewApi")
    public void sendMessage(String message) {
        Message = message;
        async_cient = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DatagramSocket ds = null;

                try {
                    ds = new DatagramSocket();
                    DatagramPacket dp;
                    dp = new DatagramPacket(Message.getBytes(), Message.length(), serverIp, serverPort);
                    ds.setBroadcast(true);
                    ds.send(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
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
