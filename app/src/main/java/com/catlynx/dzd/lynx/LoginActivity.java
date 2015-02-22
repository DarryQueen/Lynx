package com.catlynx.dzd.lynx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class LoginActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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
            View rootView = inflater.inflate(R.layout.fragment_login, container, false);

            final EditText nameView = (EditText) rootView.findViewById(R.id.login_name);
            final EditText companyView = (EditText) rootView.findViewById(R.id.login_company);

            ImageButton loginButton = (ImageButton) rootView.findViewById(R.id.button_login);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = "" + nameView.getText();
                    String company = "" + companyView.getText();
                    if (!name.equals("") && !company.equals("")) {
                        // Set as logged in:
                        SharedPreferences settings = getActivity().
                                getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();

                        editor.putBoolean(MainActivity.HAS_SIGNED_UP, true);
                        editor.putString(MainActivity.USER_NAME, name);
                        editor.putString(MainActivity.USER_COMPANY, company);

                        editor.commit();

                        //Restart the application:
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        getActivity().startActivity(intent);

                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), "Don't leave fields blank!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return rootView;
        }
    }
}
