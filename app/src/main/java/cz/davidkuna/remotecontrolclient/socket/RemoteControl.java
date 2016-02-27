package cz.davidkuna.remotecontrolclient.socket;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import com.google.gson.Gson;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by David Kuna on 26.2.16.
 */
public class RemoteControl {

    private AsyncTask<Void, Void, Void> async_cient;
    private String address;
    private int port;
    private String Message;

    public RemoteControl(String serverAddress, int port) {
        this.address = serverAddress;
        this.port = port;
    }

    @SuppressLint("NewApi")
    public void sendCommand(Command command) {
        Message = new Gson().toJson(command);
        async_cient = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DatagramSocket ds = null;

                try {
                    ds = new DatagramSocket();
                    DatagramPacket dp;
                    dp = new DatagramPacket(Message.getBytes(), Message.length(), InetAddress.getByName(address), port);
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
