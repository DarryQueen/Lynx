package com.catlynx.dzd.lynx;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    public static final String PREFS_NAME = "com.dzd.lynx.PREFS_FILES";
    public static final String HAS_SIGNED_UP = "com.dzd.lynx.HAS_SIGNED_UP";
    public static final String USER_NAME = "com.dzd.lynx.USER_NAME";
    public static final String USER_COMPANY = "com.dzd.lynx.USER_COMPANY";
    public static final int REQUEST_DISCOVERABLE = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // First-time login:
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean hasSignedUp =  settings.getBoolean(HAS_SIGNED_UP, false);

        if (!hasSignedUp) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ImageButton startButton = (ImageButton) rootView.findViewById(R.id.button_start);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextActivity();
                }
            });

            // Get user's name:
            SharedPreferences settings = getActivity().getSharedPreferences(
                    MainActivity.PREFS_NAME, MODE_PRIVATE);
            String name = settings.getString(MainActivity.USER_NAME, "John Doe");

            TextView helloText = (TextView) rootView.findViewById(R.id.textview_hello);
            helloText.setText("Hello, " + name.split(" ")[0] + "!");

            return rootView;
        }

        public void nextActivity() {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_DISCOVERABLE) {
                if (resultCode == 3600) {
                    Intent intent = new Intent(getActivity(), LinkActivity.class);
                    startActivity(intent);
                }
            }
        }
    }
}
