package com.catlynx.dzd.lynx.handshakerdetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

public class HandShakeDetector {
    private static final double THRESHOLD_ANGLE = Math.PI / 4;

    private SensorManager mgr;
    private long lastShakeTimestamp = 0;
    private boolean horizontal = false;
    private double highThresh;
    private long gapTime;
    private HandShakeDetector.Callback cb = null;

    public HandShakeDetector(Context ctxt, double highThresh,
                             long gapTime, HandShakeDetector.Callback cb) {
        this.highThresh = highThresh;
        this.gapTime = gapTime;
        this.cb = cb;

        mgr = (SensorManager) ctxt.getSystemService(Context.SENSOR_SERVICE);
        // Acceleration listener:
        mgr.registerListener(listener,
                mgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_UI);
        // Orientation listener:
        mgr.registerListener(listener,
                mgr.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_UI);
    }

    public void close() {
        mgr.unregisterListener(listener);
    }

    private void isShaking() {
        long now = SystemClock.uptimeMillis();
        lastShakeTimestamp = now;

        if (cb != null && horizontal) cb.shakingStarted();
    }

    private void isNotShaking() {
        long now = SystemClock.uptimeMillis();

        if (lastShakeTimestamp > 0) {
            if (now - lastShakeTimestamp > gapTime) {
                lastShakeTimestamp = 0;

                if (cb != null) cb.shakingStopped();
            }
        }
    }

    private boolean isHorizontal(double xGrav, double yGrav, double zGrav) {
        double xzGrav = Math.sqrt(Math.pow(xGrav, 2)
                + Math.pow(zGrav, 2));
        return Math.abs(yGrav / xzGrav) >= Math.tan(THRESHOLD_ANGLE);
    }

    public static interface Callback {
        void shakingStarted();
        void shakingStopped();
    }

    private SensorEventListener listener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent e) {
            if (e.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                // Absolute acceleration:
                double acceleration = Math.sqrt(Math.pow(e.values[0], 2)
                        + Math.pow(e.values[1], 2)
                        + Math.pow(e.values[2], 2));

                if (acceleration > highThresh) {
                    isShaking();
                } else {
                    isNotShaking();
                }
            } else if (e.sensor.getType() == Sensor.TYPE_GRAVITY) {
                horizontal = isHorizontal(e.values[0], e.values[1], e.values[2]);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Unused.
        }
    };
}
