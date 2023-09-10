package my.edu.utar.grp_nav;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load the PassengerFragment as the default fragment.
        if (savedInstanceState == null) {
            loadFragment(new PassengerFragment());
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_chat:
                                loadFragment(new PassengerFragment());
                                return true;
                            case R.id.navigation_driver:
                                isValidationPassedForDriver();
                                return true;
                            case R.id.navigation_profile:
                                loadFragment(new ProfileFragment());
                                return true;
                        }
                        return false;
                    }
                });

        // Load the initial fragment
        //loadFragment(new PassengerFragment());
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
    private boolean isValidationPassedForDriver() {
        try {
            Thread myThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        //////////////////////////////////////////////////////////////////////////////////////////////////////////
                        //Test （write)
                     /*   SharedPreferences prefQ1 = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE);

                        // Use the editor to put data (key, value)
                        SharedPreferences.Editor prefEditor = prefQ1.edit();
                        prefEditor.putString("PhoneNumber", "16548444");
                        prefEditor.apply(); // Commit or apply the changes*/

                        //////////////////////////////////////////////////////////////////////////////////////////////////////////

                        // Get the SharedPreferences using the Activit y's context (read)
                        SharedPreferences prefQ1Read = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

                        // Retrieve the value associated with the key "PhoneNumber"
                        String phoneNumber = prefQ1Read.getString("Phone Number", "");
                        Log.i("phone num",phoneNumber);

                        String apiUrl = "https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/Driver_Registration?" +
                                "phone_no=eq." + Uri.encode(phoneNumber);
                        URL url = new URL(apiUrl);
                        HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                        hc.setRequestMethod("GET");
                        hc.setRequestProperty("apikey", getString(R.string.supabasekey));
                        hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.supabasekey));

                        InputStream input = hc.getInputStream();
                        String result = readStream(input);

                        if (hc.getResponseCode() == 200) {
                            JSONArray jsonArray = new JSONArray(result);

                            // Check if the JSONArray is not empty
                            boolean isPhoneNumberExists = jsonArray.length() > 0;

                            // Handle the data and update UI as needed
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (isPhoneNumberExists) {
                                        loadFragment(new DriverFragment());
                                    } else {
                                        showToast("You have not registered as a driver yet.\n\n" + "  Please register at Profile section :)");
                                    }
                                }
                            });
                        } else {
                            Log.i("MainActivity2", "Response Code:" + hc.getResponseCode());
                        }
                        input.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // Start the background thread
            myThread.start();

            // Since the network operation is asynchronous, we cannot return a value directly.
            // Instead, you can handle the result in the UI callback and take action accordingly.
            return false; // Return false by default, as the result is determined asynchronously
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false in case of exceptions
        }
    }


    private String readStream(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }



    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
