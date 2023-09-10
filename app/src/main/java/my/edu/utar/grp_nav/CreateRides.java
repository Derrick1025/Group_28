package my.edu.utar.grp_nav;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateRides extends AppCompatActivity {

    private EditText editDate,editTime,destloc,price,range,pax,startingloc;
    private Button uploadbutton;
    private Geocoder geocoder;
    Handler mHandler = new Handler();
    private SharedPreferences sharedPreferences;
    private SharedPreferences sharedPref;
    private int num;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_rides);

        editDate = findViewById(R.id.editDateLeft);
        editTime = findViewById(R.id.editTimeRight);
        startingloc = findViewById(R.id.startingloc);
        //carpoolname = findViewById(R.id.carpoolname);
        destloc = findViewById(R.id.destination);
        price = findViewById(R.id.price);
        pax = findViewById(R.id.pax);
        LinearLayout createRidescreen = findViewById(R.id.linearLayout);
        uploadbutton = findViewById(R.id.DriverInfoSaveButton);
        geocoder = new Geocoder(this);
        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        createRidescreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(v);
                return false;
            }
        });

        // Set inputType to none to prevent keyboard from showing
        editDate.setInputType(InputType.TYPE_NULL);
        editTime.setInputType(InputType.TYPE_NULL);
        startingloc.setInputType(InputType.TYPE_NULL);
        destloc.setInputType(InputType.TYPE_NULL);
        startingloc.setText("");


        SharedPreferences sharedPref = getSharedPreferences("savetext", Context.MODE_PRIVATE);
        String savedStartingLoc = sharedPref.getString("startingloc", "");
        if (!savedStartingLoc.isEmpty()) {
            startingloc.setText(savedStartingLoc);
        }

        Intent intent = getIntent();
        if (intent != null) {

            String startinglocData = intent.getStringExtra("searchQueryKey");
            if (startinglocData != null && !startinglocData.isEmpty()) {
                if (checkpreferences()) {
                    Log.d("CreateRides", "Intent data: " + startinglocData);

                    startingloc.setText(startinglocData);
                    String startingLoc = startingloc.getText().toString().trim();

                    sharedPref = getSharedPreferences("savetext", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editortext = sharedPref.edit();
                    editortext.putString("startingloc", startingLoc);
                    editortext.apply();

                    SharedPreferences sharedPreferences = getSharedPreferences("checkvalid", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Log.d("CreateRides", "Startingloca content: " + startingloc.getText().toString());
                }

                else {
                    destloc.setText(startinglocData);

                }
            }
        }



        editDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    showDatePicker();
                }
                return true; // Consume the touch event
            }
        });

        editTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    showTimePicker();
                }
                return true; // Consume the touch event
            }
        });

        startingloc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Launch Google Maps Intent
                    hideKeyboard(v);


                    SharedPreferences sharedPreferences = getSharedPreferences("checkvalid", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("First", "yes");
                    editor.apply();

                    Intent intent = new Intent(CreateRides.this, MapsActivity.class);
                    intent.putExtra("from", "createrides");  // replace "KEY_NAME" with your key and "Value" with the actual value you want to pass
                    startActivity(intent);


                }

                return true; // Consume the touch event


            }
        });

        destloc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Launch Google Maps Intent
                    hideKeyboard(v);
                    Intent intent = new Intent(CreateRides.this, MapsActivity.class);
                    intent.putExtra("from", "createrides");  // replace "KEY_NAME" with your key and "Value" with the actual value you want to pass
                    startActivity(intent);

                }
                return true; // Consume the touch event
            }
        });


        uploadbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = editTime.getText().toString().trim();
                String date = editDate.getText().toString().trim();
                //String carpoolName = carpoolname.getText().toString().trim();
                String startingLoc = startingloc.getText().toString().trim();
                String destination = destloc.getText().toString().trim();

                String getprice = price.getText().toString().trim();
                String num_passenger = pax.getText().toString().trim();

                if (!isValidPriceFormat(getprice)) {
                    showToast("Price should have at most 2 decimal places.");
                    return;
                }

                if (startingLoc.isEmpty() || destination.isEmpty() ||
                        getprice.isEmpty() ||  num_passenger.isEmpty()) {
                    showToast("Please fill in all information.");
                } else {
                    // Create a JSON object to hold user inputs
                    JSONObject jsonData = new JSONObject();
                    try {


                        // retrieve phone number from shared_preferences
                        SharedPreferences prefQ1Read = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

                        // Retrieve the value associated with the key "PhoneNumber"
                        String phoneNumber = prefQ1Read.getString("Phone Number", "");
                        Log.i("phone num",phoneNumber);

                        //jsonData.put("carpool_name", carpoolName);
                        jsonData.put("Date", date);
                        jsonData.put("Time", time);
                        jsonData.put("Starting_loc", startingLoc);
                        jsonData.put("Destination", destination);
                        jsonData.put("Price", getprice);
                        jsonData.put("Num_passenger", num_passenger);
                        jsonData.put("driver_pnum", phoneNumber);
                        // You can add more attributes as needed

                        // Create a thread to send the JSON data to Supabase
                        MyThread connectingThread = new MyThread(jsonData, mHandler);
                        connectingThread.start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
    private boolean checkpreferences() {
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences("checkvalid", Context.MODE_PRIVATE);

        // Check if SharedPreferences is empty
        return !sharedPreferences.getAll().isEmpty(); //true is got data ; false is no data
    }



    private class MyThread extends Thread {
        private JSONObject data;
        private Handler mHandler;

        public MyThread(JSONObject jsonData, Handler handler) {
            data = jsonData;
            mHandler = handler;
        }

        public void run() {
            try {

                URL url = new URL("https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/Create_Carpool");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set the request method and headers
                connection.setRequestMethod("POST");
                connection.setRequestProperty("apikey", getString(R.string.supabasekey));
                connection.setRequestProperty("Authorization", "Bearer " + getString(R.string.supabasekey));
                connection.setRequestProperty("Content-Type", "application/json");

                // Enable input/output streams
                connection.setDoOutput(true);
                connection.setDoInput(true);

                // Write JSON data to the output stream
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(data.toString());
                writer.flush();

                // Get response code
                int responseCode = connection.getResponseCode();

                // Handle the response based on the response code
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Successfully inserted data
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Carpool created successfully!");
                            SharedPreferences sharedPreferences = getSharedPreferences("savetext", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
                            Intent intent = new Intent(CreateRides.this, DriverFragment.class);
                            startActivity(intent);
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Failed to insert data to Supabase. Response Code: " + responseCode);
                        }
                    });
                }

                // Close streams and disconnect
                writer.close();
                connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Error occurred while sending data to Supabase.");
                    }
                });
            }
        }
    }



    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.DatePicker_Style,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int month, int day) {
                        // Handle the selected date here
                        String Date = day + "/" + (month + 1) + "/" + year;
                        editDate.setText(Date);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.TimePicker_Style,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int min) {
                        // Handle the selected time here
                        String Time = String.format("%02d:%02d", hour, min);
                        editTime.setText(Time);
                    }
                }, hour, minute, false);

        timePickerDialog.show();
    }


    private void hideKeyboard(View v){
        View view = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(destloc.getWindowToken(), 0);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidPriceFormat(String price) {
        // Regular expression to match a number with at most 2 decimal places
        // and no more than 1 leading zero before the decimal point
        String regex = "^[0-9]+(\\.[0-9]{1,2})?$";
        return price.matches(regex);
    }


    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

}