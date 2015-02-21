package com.catlynx.dzd.lynx.handshakerdetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

public class HandShakeDetector {
    private SensorManager mgr = null;
    private long lastShakeTimestamp = 0;
    private double highThresh;
    private double lowThresh;
    private long gapTime;
    private HandShakeDetector.Callback cb = null;

    public HandShakeDetector(Context ctxt, double highThresh,
                             long gapTime, HandShakeDetector.Callback cb) {
        this.highThresh = highThresh;
        this.lowThresh = lowThresh;
        this.gapTime = gapTime;
        this.cb = cb;

        mgr = (SensorManager) ctxt.getSystemService(Context.SENSOR_SERVICE);
        mgr.registerListener(listener,
                mgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    public void close() {
        mgr.unregisterListener(listener);
    }

    private void isShaking() {
        long now = SystemClock.uptimeMillis();
        lastShakeTimestamp = now;

        if (cb != null) cb.shakingStarted();
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

    public static interface Callback {
        void shakingStarted();
        void shakingStopped();
    }

    private SensorEventListener listener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent e) {
            if (e.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) {
                return;
            }

            // Absolute acceleration:
            double acceleration = Math.sqrt(Math.pow(e.values[0], 2)
                    + Math.pow(e.values[1], 2)
                    + Math.pow(e.values[2], 2));

            if (acceleration > highThresh) {
                isShaking();
            } else {
                isNotShaking();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Unused.
        }
    };
}
