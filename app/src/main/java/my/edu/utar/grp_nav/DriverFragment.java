package my.edu.utar.grp_nav;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DriverFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private ListView listViewDatabaseContent;
    private TextView noRecordTextView;
    private ImageView sortIcon;
    private Button btnCreateRides;
    private Handler mHandler = new Handler();
    private List<String> dataList = new ArrayList<>();
    private List<String> booking_list = new ArrayList<>();
    private List<LinearLayout> viewsToKeep = new ArrayList<>();
    private String[] components;
    private MyCustomAdapter adapter;
    private ProgressBar loadingProgressBar;
    private ProgressBar passengerloadingProgressBar;

    private String mParam1;
    private String mParam2;

    public DriverFragment() {
        // Required empty public constructor
    }

    public static DriverFragment newInstance(String param1, String param2) {
        DriverFragment fragment = new DriverFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_driver, container, false);
        noRecordTextView = rootView.findViewById(R.id.noRecordTextView);
        sortIcon = rootView.findViewById(R.id.sortdropdown);
        btnCreateRides = rootView.findViewById(R.id.buttonCreateRides);
        loadingProgressBar = rootView.findViewById(R.id.loadingProgressBar); // Initialize the ProgressBar
        passengerloadingProgressBar = rootView.findViewById(R.id.passegerloading);

        listViewDatabaseContent = rootView.findViewById(R.id.carpoolcontentlistview);
        MyThread connectingThread = new MyThread(mHandler,"Create_Carpool");
        connectingThread.start();

        adapter = new MyCustomAdapter(dataList);
        listViewDatabaseContent.setAdapter(adapter);
        registerForContextMenu(listViewDatabaseContent);

        listViewDatabaseContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the clicked item data
                String clickedItem = dataList.get(position);

                // Split the clicked item data into individual components
                components = clickedItem.split(";");

                MyThread connectingThread = new MyThread(mHandler,"booking");
                connectingThread.start();
            }
        });

        sortIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSortMenu(view);
            }
        });

        btnCreateRides.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), CreateRides.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private class MyThread extends Thread {
        private Handler mHandler;
        private String table_name;

        public MyThread(Handler handler, String tablename) {
            mHandler = handler;
            table_name = tablename;
        }

        public void run() {
            try {
                String apiUrl;
                if (table_name.equals("Create_Carpool")) {
                    apiUrl = "https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/Create_Carpool?select=*";
                    URL url = new URL(apiUrl);
                    HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestMethod("GET");
                    hc.setRequestProperty("apikey", getString(R.string.supabasekey));
                    hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.supabasekey));

                    // Show the loading indicator
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingProgressBar.setVisibility(View.VISIBLE);
                        }
                    });

                    InputStream input = hc.getInputStream();
                    String result = readStream(input);

                    if (hc.getResponseCode() == 200) {
                        JSONArray jsonArray = new JSONArray(result);

                        dataList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String date = jsonObject.getString("Date");
                            String time = jsonObject.getString("Time");
                            String startingLoc = jsonObject.getString("Starting_loc");
                            String destination = jsonObject.getString("Destination");
                            String price = jsonObject.getString("Price");
                            String num_passenger = jsonObject.getString("Num_passenger");
                            String item = date + ";" + time + ";" + startingLoc + ";" + destination + ";" + price + ";" + num_passenger;
                            dataList.add(item);
                            Log.i("datalist", item);
                        }

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (dataList.isEmpty()) {
                                    listViewDatabaseContent.setVisibility(View.GONE);
                                    noRecordTextView.setVisibility(View.VISIBLE);
                                } else {
                                    listViewDatabaseContent.setVisibility(View.VISIBLE);
                                    noRecordTextView.setVisibility(View.GONE);
                                    adapter.notifyDataSetChanged();
                                }

                                // Hide the loading indicator
                                loadingProgressBar.setVisibility(View.GONE);
                            }
                        });

                    } else {
                        Log.i("MainActivity2", "Response Code:" + hc.getResponseCode());
                    }
                    input.close();

                } else if (table_name.equals("booking")) {
                    try {
                        String rideDate = null;
                        String rideTime = null;
                        if (components != null && components.length >= 2) {
                            rideDate = components[0];
                            rideTime = components[1];

                            // Split the date into day, month, and year
                            String[] dateParts = rideDate.split("/");
                            if (dateParts.length == 3) {
                                int day = Integer.parseInt(dateParts[0]);
                                int month = Integer.parseInt(dateParts[1]);
                                int year = Integer.parseInt(dateParts[2]);

                                // Remove leading "0" from the month if it exists
                                rideDate = String.format(Locale.getDefault(), "%d/%d/%d", day, month, year);
                            }
                        }
                        Log.i("Date",rideDate);
                        Log.i("Time",rideTime);


                        //apiUrl = "https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/booking_test?select=*";
                        //apiUrl = "https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/booking_test?select=component1,compenent2 ";
                        apiUrl = "https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/booking?" +
                                "ride_date=eq." + Uri.encode(rideDate) +
                                "&ride_time=eq." + Uri.encode(rideTime);
                        URL url = new URL(apiUrl);
                        HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                        //set the api key, can retrieve the info (JSON array returned) and print out ; if dun have the api key / supabase key then cannot retrieve the data from supabase
                        hc.setRequestMethod("GET");
                        hc.setRequestProperty("apikey",getString(R.string.supabasekey));
                        hc.setRequestProperty("Authorization" ,"Bearer " + getString(R.string.supabasekey));

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                passengerloadingProgressBar.setVisibility(View.VISIBLE);
                            }
                        });

                        //read data coming from connection
                        InputStream input = hc.getInputStream();
                        //call readStream() function to read
                        String result = readStream(input);

                        //check the data from web server to see whether request get success or not
                        if(hc.getResponseCode() == 200){ // check if success HTTP request, successfully accessed a web API, successfully read from the webpage
                            //OK response code

                            //result:response come from web server
                            Log.i("MainActivity2","Response: "+result);
                            JSONArray jsonArray = new JSONArray(result);


                            booking_list.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String ride_date = jsonObject.getString("ride_date");
                                String ride_time = jsonObject.getString("ride_time");
                                String name = jsonObject.getString("passenger_name");
                                String telephone = jsonObject.getString("p_num");
                                String address = jsonObject.getString("p_address");
                                String item = name + ";" + telephone + ";" + address;
                                booking_list.add(item);
                                Log.i("booking_list", item);
                            }

                            // Update the UI with the booking_list data
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (booking_list.isEmpty()) {
                                        passengerloadingProgressBar.setVisibility(View.GONE);
                                        showToast("No records for passenger.");
                                    } else {
                                        // Display the booking_list data in the AlertDialog
                                        // Inside your MyThread class or any appropriate place
                                        // Display the booking_list data in the AlertDialog

                                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
                                        LayoutInflater inflater = getLayoutInflater();
                                        View dialogView = inflater.inflate(R.layout.passenger_info, null);
                                        dialogBuilder.setView(dialogView);

                                        LinearLayout passengerInfo = dialogView.findViewById(R.id.passengerContainer);


                                        for (String bookingItem : booking_list) {
                                            String[] passengerData = bookingItem.split(";"); // Split into name and phone number
                                            if (passengerData.length == 3) {
                                                String passengerName = passengerData[0]; // Extract passenger name
                                                String phoneNumber = passengerData[1]; // Extract phone number
                                                String address = passengerData[2];

                                                // Create a new LinearLayout for each passenger entry
                                                int heightInPixels = 300;
                                                LinearLayout passengerListLayout = new LinearLayout(requireContext());
                                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                                        LinearLayout.LayoutParams.MATCH_PARENT, // Width (MATCH_PARENT to fill the parent)
                                                        heightInPixels// Height (You can adjust this as needed)
                                                );
                                                passengerListLayout.setLayoutParams(layoutParams);
                                                passengerListLayout.setOrientation(LinearLayout.HORIZONTAL);
                                                passengerListLayout.setPadding(30, 10, 30, 20);


                                                String formattedPhoneNumber = format_phone_num(phoneNumber);
                                                Log.d("PhoneNumberFormatting", "Input: " + phoneNumber + ", Formatted: " + formattedPhoneNumber);

                                                // Create TextView for passenger name
                                                TextView passengerNameTextView = new TextView(requireContext());
                                                passengerNameTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                                                passengerNameTextView.setText(passengerName + "\n" + formattedPhoneNumber);
                                                passengerNameTextView.setTextColor(Color.BLACK);
                                                passengerNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);



                                                // Create TextView for address
                                                TextView addressTextView = new TextView(requireContext());
                                                LinearLayout.LayoutParams phoneLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                                addressTextView.setLayoutParams(phoneLayoutParams);
                                                addressTextView.setText(address);
                                                addressTextView.setTextColor(Color.BLACK);
                                                addressTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

                                                // Create "Cancel" button
                                                Button cancelButton = new Button(requireContext());
                                                cancelButton.setText("Cancel");
                                                final int position = booking_list.indexOf(bookingItem);
                                                cancelButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        deleteInfo(passengerName,formattedPhoneNumber);
                                                        //remove the passengerinfo
                                                        passengerInfo.removeView(passengerListLayout);
                                                    }
                                                });

                                                // Add TextViews and button to passenger entry layout
                                                passengerListLayout.addView(passengerNameTextView);
                                                passengerListLayout.addView(addressTextView);
                                                passengerListLayout.addView(cancelButton);

                                                // Add passenger entry layout to the passenger container
                                                passengerInfo.addView(passengerListLayout);

                                                // Add some spacing between passenger entries
                                                if (booking_list.indexOf(bookingItem) < booking_list.size() - 1) {
                                                    View spacingView = new View(requireContext());
                                                    spacingView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 16));
                                                    passengerInfo.addView(spacingView);
                                                }
                                            }
                                        }

                                        for (LinearLayout viewToKeep : viewsToKeep) {
                                            passengerInfo.addView(viewToKeep);
                                        }

                                        passengerloadingProgressBar.setVisibility(View.GONE);
                                        AlertDialog dialog = dialogBuilder.create();
                                        dialog.show();

                                    }
                                }
                            });

                        }
                        input.close();

                    } catch (IOException e ) {
                        e.printStackTrace();

                    }

                }


            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu); // Create a context menu layout (e.g., "context_menu.xml")
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.delete: // Define the "Delete" option in your context menu layout (R.menu.context_menu)
                // Get the clicked item data
                String clicked_item = dataList.get(info.position);

                // Split the clicked item data into individual components
                String[] components = clicked_item.split(";");
                showToast(components[0]+components[1]);

                // Extract the relevant data for deletion (e.g., passengerName and phoneNumber)
                String date = components[0];
                String time = components[1];

                // Call the deleteInfo method to delete the item
                deleteCarpool(date, time);
                deleteBooking(date,time);

                // Remove the item from your data list
                dataList.remove(info.position);

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteCarpool(String date, String time) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // Define your Supabase API URL for deleting rows
                        String apiUrl = "https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/Create_Carpool?" +
                                "Date=eq." + Uri.encode(date) +
                                "&Time=eq." + Uri.encode(time);
                        URL url = new URL(apiUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("DELETE");
                        connection.setRequestProperty("apikey", getString(R.string.supabasekey));
                        connection.setRequestProperty("Authorization", "Bearer " + getString(R.string.supabasekey));

                        int responseCode = connection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                            // Deletion was successful, update UI on the main thread
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast("Deleted successfully");
                                    // You may want to refresh your UI here
                                }
                            });
                        } else {
                            // Deletion failed, update UI on the main thread
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast("Failed to delete row");
                                }
                            });
                        }

                        // Close the connection
                        connection.disconnect();

                    } catch (Exception e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast("Error: " + e.getMessage());
                            }
                        });
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteBooking(String date, String time) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // Define your Supabase API URL for deleting rows
                        String apiUrl = "https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/booking?" +
                                "ride_date=eq." + Uri.encode(date) +
                                "&ride_time=eq." + Uri.encode(time);
                        URL url = new URL(apiUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("DELETE");
                        connection.setRequestProperty("apikey", getString(R.string.supabasekey));
                        connection.setRequestProperty("Authorization", "Bearer " + getString(R.string.supabasekey));

                        int responseCode = connection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                            // Deletion was successful, update UI on the main thread
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast("Booking Deleted successfully");
                                    // You may want to refresh your UI here
                                }
                            });
                        } else {
                            // Deletion failed, update UI on the main thread
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast("Failed to delete row");
                                }
                            });
                        }

                        // Close the connection
                        connection.disconnect();

                    } catch (Exception e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast("Error: " + e.getMessage());
                            }
                        });
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void deleteInfo(String passengerName, String phoneNumber) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String formated_pnum = clean_format_pnum(phoneNumber);

                        // Define your Supabase API URL for deleting rows
                        String apiUrl = "https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/booking?" +
                                "passenger_name=eq." + Uri.encode(passengerName) +
                                "&p_num=eq." + Uri.encode(formated_pnum);
                        URL url = new URL(apiUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("DELETE");
                        connection.setRequestProperty("apikey", getString(R.string.supabasekey));
                        connection.setRequestProperty("Authorization", "Bearer " + getString(R.string.supabasekey));

                        int responseCode = connection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                            // Deletion was successful, update UI on the main thread
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast("Deleted successfully");
                                    // You may want to refresh your UI here
                                }
                            });
                        } else {
                            // Deletion failed, update UI on the main thread
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast("Failed to delete row");
                                }
                            });
                        }

                        // Close the connection
                        connection.disconnect();

                    } catch (Exception e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast("Error: " + e.getMessage());
                            }
                        });
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String clean_format_pnum(String phoneNumber) {
        // Remove all non-digit characters (including "-")
        String cleanedNumber = phoneNumber.replaceAll("[^0-9]", "");

        // Remove leading "0" if present
        if (cleanedNumber.startsWith("0")) {
            cleanedNumber = cleanedNumber.substring(1);
        }

        return cleanedNumber;
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

    private void showSortMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.sort_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.ascending:
                        // Sort by date in ascending order
                        sortAscending();
                        showToast("Sorted by Date (Ascending)");
                        return true;
                    case R.id.descending:
                        // Sort by date in descending order
                        sortDescending();
                        showToast("Sorted by Date (Descending)");
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }

    private void sortAscending() {
        Collections.sort(dataList, new Comparator<String>() {
            SimpleDateFormat format_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            @Override
            public int compare(String data1, String data2) {
                String[] split1 = data1.split(";");
                String[] split2 = data2.split(";");

                try {
                    Date date1 = format_date.parse(split1[0].trim());
                    Date date2 = format_date.parse(split2[0].trim());
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        adapter.notifyDataSetChanged();
    }

    private void sortDescending() {
        Collections.sort(dataList, new Comparator<String>() {
            SimpleDateFormat format_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            @Override
            public int compare(String data1, String data2) {
                String[] split1 = data1.split(";");
                String[] split2 = data2.split(";");

                try {
                    Date date1 = format_date.parse(split1[0].trim());
                    Date date2 = format_date.parse(split2[0].trim());
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        adapter.notifyDataSetChanged();
    }

    private String format_phone_num(String phoneNumber) {
        if (phoneNumber.length() == 9) {
            return "0" + phoneNumber.substring(0, 2) + "-" + phoneNumber.substring(2, 9);
        } else {
            return phoneNumber; // Return as is if not 9 digits
        }
    }



    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private class MyCustomAdapter extends ArrayAdapter<String> {
        public MyCustomAdapter(List<String> data) {
            super(requireContext(), R.layout.list_item, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            // Get references to views
            TextView basicInfo= convertView.findViewById(R.id.basicInfo);
            TextView furtherinfo = convertView.findViewById(R.id.furtherInfo);
            ImageView expandIcon = convertView.findViewById(R.id.expandicon);

            // Get the current item's data
            String element = getItem(position);
            String[] split = element.split(";");

            String date = split[0].trim();
            String time = split[1].trim();
            String startingLoc = split[2].trim();
            String destination = split[3].trim();
            String price = split[4].trim();
            String numppl = split[5].trim();

            SimpleDateFormat input_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat output_date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String format_date = "";
            try {
                Date parsedDate = input_date.parse(date);
                format_date = output_date.format(parsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Convert time to AM/PM format
            SimpleDateFormat input_time = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat output_time = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String formatt_time = "";
            try {
                Date parsedDate = input_time.parse(time);
                formatt_time = output_time.format(parsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            double double_price = Double.parseDouble(price);
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            String format_price = decimalFormat.format(double_price);
            // Set text for basic information
            String datentime = format_date + "   " + formatt_time;
            basicInfo.setText(datentime);

            // Set text for detailed information
            String otherinfo = "Starting Location: " + startingLoc
                    + "\nDestination: " + destination
                    + "\nPrice: RM" + price
                    + "\nAvailable passenger: " + numppl;
            furtherinfo.setText(otherinfo);

            furtherinfo.setVisibility(View.GONE);

            // Set click listener on the expand icon
            expandIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Toggle visibility of detailedInfoTextView
                    if (furtherinfo.getVisibility() == View.VISIBLE) {
                        furtherinfo.setVisibility(View.GONE);
                        expandIcon.setImageResource(R.drawable.ic_expand);
                    } else {
                        furtherinfo.setVisibility(View.VISIBLE);
                        expandIcon.setImageResource(R.drawable.ic_collapse);
                    }
                }
            });

            return convertView;
        }
    }
}