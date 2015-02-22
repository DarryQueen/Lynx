package com.catlynx.dzd.lynx.bluetoothlink;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SocketMessenger implements BluetoothLinker.SocketHandler {
    private String mMessage;
    private SocketMessenger.MessageListener mMessageListener;

    public SocketMessenger(String message, SocketMessenger.MessageListener messageListener) {
        mMessage = message;
        mMessageListener = messageListener;
    }

    @Override
    public void manageConnectedSocket(BluetoothSocket socket) {
        ConnectedThread connectedThread = new ConnectedThread(socket);
        connectedThread.write(mMessage.getBytes());
    }

    public static interface MessageListener {
        public void receiveMessage(String message);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams:
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {}

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream until an exception occurs:
            while (true) {
                try {
                    // Read from the InputStream:
                    bytes = mmInStream.read(buffer);
//                    // Send the obtained bytes to the UI activity
//                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }

            String message = new String(buffer, StandardCharsets.UTF_8);
            Log.d("bluetooth", message);
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {}
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {}
        }
    }
}
