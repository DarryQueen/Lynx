package com.catlynx.dzd.lynx;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.catlynx.dzd.lynx.bluetoothlink.BluetoothLinker;
import com.catlynx.dzd.lynx.bluetoothlink.BluetoothSearcher;
import com.catlynx.dzd.lynx.bluetoothlink.SocketMessenger;
import com.catlynx.dzd.lynx.handshakerdetector.HandShakeDetector;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;


public class LinkActivity extends ActionBarActivity {
    private static final String BLUETOOTH_HOTNAME = "LynxWatch_Microwaved";
    private static final String BLUETOOTH_COLDNAME = "LynxWatch_FrozenPizza";

    private static final String DELIMITER = "$~!delimiter!~$";

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

        // Get user's name:
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        String name = settings.getString(MainActivity.USER_NAME, "John Doe");
        String company = settings.getString(MainActivity.USER_COMPANY, "McKinsey & Company");
        String message = name + DELIMITER + company;

        // Set up shake detection;
        mDetector = new HandShakeDetector(this, 5, 200, shakeCallback);
        isShaking = false;

        // Set up Bluetooth:
        BluetoothAdapter.getDefaultAdapter().enable();
        userBluetoothName = BluetoothAdapter.getDefaultAdapter().getName();

        socketMessenger = new SocketMessenger(message, mMessageListener);
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
                    LinkActivity.User user = parseUser(message);
                    ((PlaceholderFragment) mFragment).updateConnections(user);
                }
            });
        }
    };
    private LinkActivity.User parseUser(String message) {
        String[] parts = message.split(Pattern.quote(DELIMITER));
        Log.d("shake_detector", parts[0]);
        return new User(parts[0], parts[1]);
    }

    // Check if the other device is "hot":
    private BluetoothSearcher.PairChecker pairChecker = new BluetoothSearcher.PairChecker() {
        @Override
        public boolean checkMates(BluetoothDevice device) {
            return device.getName() != null && device.getName().contains(BLUETOOTH_HOTNAME);
        }
    };

    // When shaken, heat up for a duration:
    private HandShakeDetector.Callback shakeCallback = new HandShakeDetector.Callback() {
        @Override
        public void shakingStarted() {
            // If not already listening:
            if (!BluetoothAdapter.getDefaultAdapter().getName().contains(BLUETOOTH_HOTNAME)) {
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
                                    .getName().contains(BLUETOOTH_HOTNAME)) {
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
        private List<User> mConnections = new LinkedList<User>();
        private ListAdapter mAdapter;
        private ListView mListView;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_link, container, false);

            mAdapter = new UserArrayAdapter(getActivity(), mConnections);

            // Ratchet testing:
            updateConnections(new User("John Doe", "Google"));
            updateConnections(new User("Jane Smith", "Smith Co."));

            mListView = (ListView) rootView.findViewById(R.id.listview_mates);
            mListView.setAdapter(mAdapter);

            // Set button behavior:
            ImageButton button = (ImageButton) rootView.findViewById(R.id.button_finish);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "New friends!", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            });

            return rootView;
        }

        public void updateConnections(User user) {
            mConnections.add(user);
            ((ArrayAdapter) mAdapter).notifyDataSetChanged();
        }

        private static class UserArrayAdapter extends ArrayAdapter<User> {
            private Context mContext;
            private List<User> mUsers;
            private int mLayoutViewResourceId;

            public UserArrayAdapter(Context context, List<User> users) {
                super(context, R.layout.listview_item, users);
                mContext = context;
                mUsers = users;
                mLayoutViewResourceId = R.layout.listview_item;
            }

            @Override
            public View getView(final int position, View convertView, final ViewGroup parent) {
                final User user = mUsers.get(position);

                if (convertView == null) {
                    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                    convertView = inflater.inflate(mLayoutViewResourceId, parent, false);
                }

                TextView nameView = (TextView) convertView.findViewById(R.id.user_name);
                TextView companyView = (TextView) convertView.findViewById(R.id.user_company);

                nameView.setText(user.getName());
                companyView.setText(user.getCompany());

                return convertView;
            }
        }
    }

    public static class User {
        private String name, company;

        public User(String name, String company) {
            this.name = name;
            this.company = company;
        }

        public String getName() {
            return name;
        }
        public String getCompany() {
            return company;
        }
    }
}
