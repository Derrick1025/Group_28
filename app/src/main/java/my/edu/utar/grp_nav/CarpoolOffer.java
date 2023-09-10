package my.edu.utar.grp_nav;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CarpoolOffer extends AppCompatActivity {

    private ListView offerList;
    private List<CarpoolData> data;
    private List<CarpoolData> filteredData;
    private CarpoolAdapter adapter;
    private ImageView sortDropdown;
    private TextView sortBy;
    private ProgressBar progressBar;
    private String fromLocationValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpool_offer);

        offerList = findViewById(R.id.offerList);
        sortDropdown = findViewById(R.id.sortdropdown);
        sortBy = findViewById(R.id.sortby);
        progressBar = findViewById(R.id.loadingProgressBar);
        progressBar.setVisibility((View.VISIBLE));

        String selectedDate = getIntent().getStringExtra("SELECTED_DATE");
        String selectedTime = getIntent().getStringExtra("SELECTED_TIME");
        fromLocationValue = getIntent().getStringExtra("FROM_LOCATION");
        String toLocationValue = getIntent().getStringExtra("TO_LOCATION");
        String[] timeParts = selectedTime.split(":");
        int selectedHour = Integer.parseInt(timeParts[0]);
        int selectedMinute = Integer.parseInt(timeParts[1]);

        Log.d("CarpoolData", "Selected Date: " + selectedDate + ", Selected Time: " + selectedTime);
        // Fetch the carpool offer data from Supabase
        new CarpoolOfferDataFetcher(this, new CarpoolOfferDataFetcher.Callback() {
            @Override
            public void onDataFetched(List<CarpoolData> fetchedData) {

                data = fetchedData;  // Assign fetched data to the data variable

                // Filter the data list based on the time difference
                filteredData = new ArrayList<>();

                for (CarpoolData offer : data) {
                    String offerTime = offer.getTime();
                    String[] offerTimeParts = offerTime.split(":|\\s+");
                    int offerHour = Integer.parseInt(offerTimeParts[0]);
                    int offerMinute = Integer.parseInt(offerTimeParts[1]);

                    int offerTimeInMinutes = offerHour * 60 + offerMinute;
                    int selectedTimeInMinutes = selectedHour * 60 + selectedMinute;

                    // Calculate the time difference
                    int timeDifferenceInMinutes = offerTimeInMinutes - selectedTimeInMinutes;

                    // Filter the data list based on the date
                    // If the time difference is between 1 and 30 minutes (inclusive), add it to filteredData
                    if (offer.getDate().equals(selectedDate) && timeDifferenceInMinutes >= 0 && timeDifferenceInMinutes <= 30) {
                        filteredData.add(offer);
                    }
                }

                Log.d("CarpoolData", "Filtered data size: " + filteredData.size());

                // Use filteredData instead of data for your adapter
                adapter = new CarpoolAdapter(CarpoolOffer.this, filteredData);
                offerList.setAdapter(adapter);
                Log.d("CarpoolData", "Final filtered data: " + filteredData.toString());
                if (filteredData.size() == 0) {
                    TextView noRecordTextView = findViewById(R.id.noRecord);
                    progressBar.setVisibility((View.GONE));
                    noRecordTextView.setVisibility(View.VISIBLE);
                } else {
                    offerList.setAdapter(adapter);
                }
            }
        }).execute();

        // Sort dropdown click
        sortDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortMenu(v);
            }
        });

        sortBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortMenu(v);
            }
        });
    }

    private void showSortMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.sort_price, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.option1:
                        // Sort filteredData by price in ascending order
                        Collections.sort(filteredData, new Comparator<CarpoolData>() {
                            @Override
                            public int compare(CarpoolData o1, CarpoolData o2) {
                                float price1 = Float.parseFloat(o1.getPrice().replace("RM", "").trim());
                                float price2 = Float.parseFloat(o2.getPrice().replace("RM", "").trim());
                                return Float.compare(price1, price2);
                            }
                        });
                        break;
                    case R.id.option2:
                        // Sort filteredData by price in descending order
                        Collections.sort(filteredData, new Comparator<CarpoolData>() {
                            @Override
                            public int compare(CarpoolData o1, CarpoolData o2) {
                                float price1 = Float.parseFloat(o1.getPrice().replace("RM", "").trim());
                                float price2 = Float.parseFloat(o2.getPrice().replace("RM", "").trim());
                                return Float.compare(price2, price1);
                            }
                        });
                        break;
                }

                adapter.notifyDataSetChanged();
                return true;
            }
        });
        popup.show();
    }


    private class BookRideThread extends Thread {
        private String passengerName;
        private String passengerPhone;
        private String rideDate;
        private String rideTime;
        private String fromLocation;

        HttpURLConnection urlConnection = null;

        public BookRideThread(String passengerName, String passengerPhone, String rideDate, String rideTime, String fromLocation) {
            this.passengerName = passengerName;
            this.passengerPhone = passengerPhone;
            this.rideDate = rideDate;
            this.rideTime = rideTime;
            this.fromLocation = fromLocation;
        }

        @Override
        public void run() {
            try {
                URL url = new URL("https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/booking");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ride_date", rideDate);
                jsonObject.put("ride_time", rideTime);
                jsonObject.put("passenger_name", passengerName);
                jsonObject.put("p_num", passengerPhone);
                jsonObject.put("p_address", fromLocation);

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("apikey", getString(R.string.supabasekey));
                urlConnection.setRequestProperty("Authorization", "Bearer " + getString(R.string.supabasekey));

                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Prefer", "return=minimal");

                urlConnection.setDoOutput(true);

                OutputStream output = urlConnection.getOutputStream();
                output.write(jsonObject.toString().getBytes());
                output.flush();

                int code = urlConnection.getResponseCode();

                if (code == 201) {
                    // Successfully inserted
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CarpoolOffer.this, "Booking Succeeded", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CarpoolOffer.this, PassengerFragment.class);
                            startActivity(intent);
                        }
                    });
                } else {
                    throw new IOException("Invalid response from the server! code: " + code);
                }

                output.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }

    private String[] getPassengerDetails() {
        SharedPreferences sharedPref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String name = sharedPref.getString("Name", "");
        String phone = sharedPref.getString("Phone Number", "");

        return new String[] {name, phone};
    }


    public void showBookingConfirmation(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Confirm to book this carpool?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                CarpoolData selectedData = filteredData.get(position);

                String[] passengerDetails = getPassengerDetails();
                String passengerName = passengerDetails[0];
                String passengerPhone = passengerDetails[1];
                String rideDate = selectedData.getDate(); // Fetching Date from CarpoolData
                String rideTime = selectedData.getTime(); // Fetching Time from CarpoolData

                Log.d("BookingDebug", "Position: " + position + ", Date: " + rideDate + ", Time: " + rideTime);


                BookRideThread bookRideThread = new BookRideThread(passengerName, passengerPhone, rideDate, rideTime, fromLocationValue);
                bookRideThread.start();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

}