package cz.davidkuna.remotecontrolclient.sensors;

/**
 * Created by David Kuna on 27.2.16.
 */
public interface OnLocationChangedListener {
    void OnChange(float latitude, float longitude, float rotation);
}
