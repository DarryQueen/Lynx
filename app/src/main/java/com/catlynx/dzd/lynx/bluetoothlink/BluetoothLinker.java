package com.catlynx.dzd.lynx.bluetoothlink;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.UUID;

public class BluetoothLinker implements BluetoothSearcher.Callback {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mMate;

    public BluetoothLinker(BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
        mMate = null;
    }

    private void startLink() {
        if (isHost()) {

        }
    }

    private boolean isHost() {
        if (mMate == null) return false;
        return mBluetoothAdapter.getAddress().compareTo(mMate.getAddress()) < 0;
    }

    // This method generates a pseudo-random UUID from the two MAC addresses:
    private UUID generateUuid() {
        if (mMate == null) return null;
        String hostName = (isHost()) ? mBluetoothAdapter.getAddress() : mMate.getAddress();
        String pupilName = (isHost()) ? mMate.getAddress(): mBluetoothAdapter.getAddress();
        String uniqueString = hostName + pupilName;

        UUID uuid = UUID.nameUUIDFromBytes(uniqueString.getBytes());
        return uuid;
    }

    @Override
    public void mateFound(BluetoothDevice mate) {
        mMate = mate;
        Log.d("bluetooth", "Generated UUID: " + generateUuid());
    }
}
