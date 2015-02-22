package com.catlynx.dzd.lynx;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.catlynx.dzd.lynx.bluetoothlink.BluetoothLinker;
import com.catlynx.dzd.lynx.bluetoothlink.BluetoothSearcher;
import com.catlynx.dzd.lynx.bluetoothlink.SocketMessenger;
import com.catlynx.dzd.lynx.handshakerdetector.HandShakeDetector;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LinkActivity extends ActionBarActivity {
    private static final String BLUETOOTH_HOTNAME = "LynxWatch_Microwaved";
    private static final String BLUETOOTH_COLDNAME = "LynxWatch_FrozenPizza";

    private boolean isShaking;
    private String userBluetoothName;

    // Handshake detector:
    HandShakeDetector mDetector;

    // Bluetooth fields:
    private SocketMessenger socketMessenger;
    private BluetoothLinker btLinker;
    private BluetoothSearcher btSearcher;

    // UI fragment:
    Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);
        if (savedInstanceState == null) {
            mFragment = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mFragment)
                    .commit();
        }

        mDetector = new HandShakeDetector(this, 5, 200, shakeCallback);
        BluetoothAdapter.getDefaultAdapter().enable();

        isShaking = false;
        userBluetoothName = BluetoothAdapter.getDefaultAdapter().getName();

        socketMessenger = new SocketMessenger("HELLO", mMessageListener);
        btLinker = new BluetoothLinker(BluetoothAdapter.getDefaultAdapter(),
                socketMessenger);
        btSearcher = new BluetoothSearcher(this,
                BluetoothAdapter.getDefaultAdapter(), btLinker, pairChecker);
        btLinker.setParentListener(btSearcher.getParentListener());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDetector.close();
        btSearcher.kill();
        socketMessenger.kill();
    }

    // React to a new mate:
    private SocketMessenger.MessageListener mMessageListener
            = new SocketMessenger.MessageListener() {
        @Override
        public void receiveMessage(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((PlaceholderFragment) mFragment).updateConnections(message);
                }
            });
        }
    };

    // Check if the other device is "hot":
    private BluetoothSearcher.PairChecker pairChecker = new BluetoothSearcher.PairChecker() {
        @Override
        public boolean checkMates(BluetoothDevice device) {
            return device.getName() != null && device.getName().equals(BLUETOOTH_HOTNAME);
        }
    };

    // When shaken, heat up for a duration:
    private HandShakeDetector.Callback shakeCallback = new HandShakeDetector.Callback() {
        @Override
        public void shakingStarted() {
            // If not already listening:
            if (!BluetoothAdapter.getDefaultAdapter().getName().equals(BLUETOOTH_HOTNAME)) {
                Log.d("ui", "Device shaking started...");
                isShaking = true;
                btSearcher.startListen();
                BluetoothAdapter.getDefaultAdapter().setName(BLUETOOTH_HOTNAME);
            }
        }

        @Override
        public void shakingStopped() {
            isShaking = false;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // If is listening and not shaking:
                    if (!isShaking &&
                            BluetoothAdapter.getDefaultAdapter()
                                    .getName().equals(BLUETOOTH_HOTNAME)) {
                        Log.d("ui", "Stopped shaking...");
                        btSearcher.stopListen();
                        BluetoothAdapter.getDefaultAdapter().setName(BLUETOOTH_COLDNAME);
                    }
                }
            }, 15 * 1000);
        }
    };

    /**
     * A placeholder fragment containing a view with an adapter.
     */
    public static class PlaceholderFragment extends Fragment {
        private List<String> mConnections = new LinkedList<String>();
        private ListAdapter mAdapter;
        private ListView mListView;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_link, container, false);

            mAdapter = new ArrayAdapter<String>(
                    getActivity().getApplicationContext(),
                    R.layout.listview_item,
                    R.id.user_item,
                    mConnections
            );

            // Ratchet testing:
            updateConnections("John Doe");
            updateConnections("Jane Smith");

            mListView = (ListView) rootView.findViewById(R.id.listview_mates);
            mListView.setAdapter(mAdapter);

            return rootView;
        }

        public void updateConnections(String connection) {
            mConnections.add(connection);
            ((ArrayAdapter) mAdapter).notifyDataSetChanged();
        }
    }
}
