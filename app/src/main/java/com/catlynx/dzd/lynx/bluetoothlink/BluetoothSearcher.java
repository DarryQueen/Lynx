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

    public BluetoothSearcher(Context context, BluetoothAdapter bluetoothAdapter,
                             BluetoothSearcher.Callback cb, BluetoothSearcher.PairChecker pc) {
        this.mBluetoothAdapter = bluetoothAdapter;
        this.mContext = context;
        this.cb = cb;
        this.pc = pc;
    }

    // Before calling this, make sure discoverable is turned on.
    public void startListen() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.startDiscovery();
        mContext.registerReceiver(mReceiver, filter);
    }

    public void stopListen() {
        mBluetoothAdapter.cancelDiscovery();
        mContext.unregisterReceiver(mReceiver);
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
                    stopListen();
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
