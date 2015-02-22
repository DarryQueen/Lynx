package com.catlynx.dzd.lynx.bluetoothlink;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * This is the class that will search for the Bluetooth companion.
 */
public class BluetoothSearcher {
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    private BluetoothSearcher.Callback cb;
    private BluetoothSearcher.PairChecker pc;

    private BluetoothLinker.ParentListener mParentListener = new BluetoothLinker.ParentListener() {
        @Override
        public void endListen() {
            stopListen();
        }
    };

    public BluetoothSearcher(Context context, BluetoothAdapter bluetoothAdapter,
                             BluetoothSearcher.Callback cb, BluetoothSearcher.PairChecker pc) {
        this.mBluetoothAdapter = bluetoothAdapter;
        this.mContext = context;
        this.cb = cb;
        this.pc = pc;
    }

    public BluetoothLinker.ParentListener getParentListener() {
        return mParentListener;
    }

    // Before calling this, make sure discoverable is turned on.
    public void startListen() {
        Log.d("bluetooth", "Starting bluetooth search...");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.startDiscovery();
        mContext.registerReceiver(mReceiver, filter);
    }

    public void stopListen() {
        Log.d("bluetooth", "Stopping bluetooth search...");
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    public void kill() {
        // Shut up.
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {}
        stopListen();
    }

    // Create a BroadcastReceiver for when devices are found:
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                Log.d("bluetooth", device.getName() + " " + rssi);
                if (pc.checkMates(device) && passesThreshold(rssi)) {
                    cb.mateFound(device);
                }
            }
        }

        private boolean passesThreshold(short rssi) {
            return true;
        }
    };

    // Defines an interface that responds to when a mate is found:
    public static interface Callback {
        void mateFound(BluetoothDevice device);
    }

    // Defines an interface that checks if a device is pairable:
    public static interface PairChecker {
        boolean checkMates(BluetoothDevice device);
    }
}
