package com.catlynx.dzd.lynx.bluetoothlink;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class BluetoothLinker implements BluetoothSearcher.Callback {
    private static final String LINK_NAME = "com.dzd.lynx.bluetooth_link";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mMate;
    private BluetoothLinker.SocketHandler mSocketHandler;
    private BluetoothLinker.ParentListener mParentListener;

    public BluetoothLinker(BluetoothAdapter bluetoothAdapter,
                           BluetoothLinker.SocketHandler socketHandler) {
        mBluetoothAdapter = bluetoothAdapter;
        mMate = null;
        mSocketHandler = socketHandler;
    }

    public void setParentListener(BluetoothLinker.ParentListener parentListener) {
        mParentListener = parentListener;
    }

    private void startLink() {
        UUID uuid = generateUuid();
        Log.d("bluetooth", "Generated uuid: " + uuid);
        if (isHost()) {
            Thread hostThread = new AcceptThread(uuid);
            hostThread.start();
        } else {
            Thread guestThread = new ConnectThread(uuid, mMate);
            Thread delayer = new DelayThread(guestThread, 5 * 1000);
            delayer.start();
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
        String guestName = (isHost()) ? mMate.getAddress(): mBluetoothAdapter.getAddress();
        String uniqueString = hostName + guestName;

        UUID uuid = UUID.nameUUIDFromBytes(uniqueString.getBytes());
        return uuid;
    }

    @Override
    public void mateFound(BluetoothDevice mate) {
        mMate = mate;
        startLink();
    }

    public static interface SocketHandler {
        public void manageConnectedSocket(BluetoothSocket socket);
    }
    public static interface ParentListener {
        public void endListen();
    }

    // Thread to execute as host.
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(UUID uuid) {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(LINK_NAME, uuid);
            } catch (IOException e) {}
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned:
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted:
                if (socket != null) {
                    // Do work to manage the connection in a separate thread:
                    Log.d("bluetooth", "Socket accepted.");
                    mParentListener.endListen();
                    mSocketHandler.manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {}
                    return;
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {}
        }
    }

    // Thread to execute as guest:
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;

        public ConnectThread(UUID uuid, BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice:
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {}
            mmSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; try workaround:
                try {
                    mmSocket = (BluetoothSocket) mmDevice.getClass()
                            .getMethod("createInsecureRfcommSocket", new Class[]{int.class})
                            .invoke(mmDevice, 1);
                    mmSocket.connect();
                } catch (NoSuchMethodException e) {
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                } catch (IOException e) {}

                // Unable to connect; close the socket and get out:
                try {
                    mmSocket.close();
                } catch (IOException closeException) {}
                return;
            }

            // Do work to manage the connection in a separate thread:
            Log.d("bluetooth", "Socket accepted.");
            mParentListener.endListen();
            mSocketHandler.manageConnectedSocket(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {}
        }
    }

    // Thread to delay another thread:
    private class DelayThread extends Thread {
        private Thread mmThread;
        private int mmDelay;

        public DelayThread(Thread otherThread, int delay) {
            mmThread = otherThread;
            mmDelay = delay;
        }

        public void run() {
            SystemClock.sleep(mmDelay);
            mmThread.start();
        }
    }
}
