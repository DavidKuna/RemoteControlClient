package cz.davidkuna.remotecontrolclient.sensors;

/**
 * Created by David Kuna on 26.2.16.
 */
public class Attitude {

    private float alpha = 0.5f;

    /**
     * Roll angle (deg, -180..+180)
     */
    private double roll;

    /**
     * Roll angular speed (deg/s)
     */
    private float rollSpeed;

    /**
     * Pitch angle (deg, -180 to 180)
     */
    private  double pitch;

    /**
     * Pitch angular speed (deg / s)
     */
    private float pitchSpeed;

    /**
     * Yaw angle (deg, -180 to 180)
     */
    private  double yaw;

    /**
     * Yaw angular speed (deg/ s)
     */
    private float yawSpeed;

    public Attitude(){}

    public Attitude(Accelerometer accelerometer){
        double fXg = 0;
        double fYg = 0;
        double fZg = 0;
        //Low Pass Filter
        fXg = accelerometer.getX() * alpha + (fXg * (1.0 - alpha));
        fYg = accelerometer.getY() * alpha + (fYg * (1.0 - alpha));
        fZg = accelerometer.getZ() * alpha + (fZg * (1.0 - alpha));

        //Roll & Pitch Equations
        roll  = (Math.atan2(-fYg, fZg)*180.0)/Math.PI;
        pitch = (Math.atan2(fXg, Math.sqrt(fYg * fYg + fZg * fZg))*180.0)/Math.PI;

      }

    public Attitude(double roll, double pitch, double yaw, float rollSpeed, float pitchSpeed, float yawSpeed) {
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
        this.rollSpeed = rollSpeed;
        this.pitchSpeed = pitchSpeed;
        this.yawSpeed = yawSpeed;
    }

    /**
     * Updates the roll angle
     * @param roll Roll angle (deg, -180..+180)
     */
    public void setRoll(double roll) {
        this.roll = roll;
    }

    /**
     * Updates the pitch angle
     * @param pitch Pitch angle (deg, -180..+180)
     */
    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    /**
     * Updates the yaw angle
     * @param yaw Yaw angle (deg, -180..+180)
     */
    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    /**
     * @return Vehicle roll angle (deg, -180..+180)
     */
    public double getRoll() {
        return roll;
    }

    /**
     * @return Vehicle pitch angle (deg, -180 to 180)
     */
    public double getPitch() {
        return pitch;
    }

    /**
     * @return Vehicle yaw angle (deg, -180 to 180)
     */
    public double getYaw() {
        return yaw;
    }

    /**
     * @return Vehicle pitch angular speed (deg / s)
     */
    public float getPitchSpeed() {
        return pitchSpeed;
    }

    /**
     * Updates the pitch angular speed
     * @param pitchSpeed Pitch angular speed (deg/s)
     */
    public void setPitchSpeed(float pitchSpeed) {
        this.pitchSpeed = pitchSpeed;
    }

    /**
     * @return Vehicle roll angular speed (deg/s)
     */
    public float getRollSpeed() {
        return rollSpeed;
    }

    /**
     * Updates the roll angular speed
     * @param rollSpeed Roll angular speed (deg/s)
     */
    public void setRollSpeed(float rollSpeed) {
        this.rollSpeed = rollSpeed;
    }

    /**
     * @return Vehicle yaw angular speed (deg/ s)
     */
    public float getYawSpeed() {
        return yawSpeed;
    }

    /**
     * Updates the yaw angular speed
     * @param yawSpeed Yaw angular speed (deg/s)
     */
    public void setYawSpeed(float yawSpeed) {
        this.yawSpeed = yawSpeed;
    }

    @Override
    public String toString() {
        return "Attitude{" +
                "pitch=" + pitch +
                ", roll=" + roll +
                ", rollSpeed=" + rollSpeed +
                ", pitchSpeed=" + pitchSpeed +
                ", yaw=" + yaw +
                ", yawSpeed=" + yawSpeed +
                '}';
    }
}
