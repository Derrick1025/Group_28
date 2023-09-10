package my.edu.utar.grp_nav;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// Step 1: CarpoolOfferDataFetcher class
public class CarpoolOfferDataFetcher extends AsyncTask<Void, Void, List<CarpoolData>> {
    private Context context;
    private Callback callback;

    // A callback interface to send the fetched data back to the CarpoolOffer activity
    public interface Callback {
        void onDataFetched(List<CarpoolData> data);
    }

    public CarpoolOfferDataFetcher(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected List<CarpoolData> doInBackground(Void... voids) {
        List<CarpoolData> carpoolDataList = new ArrayList<>();

        try {
            // Get the toLoc from the intent extras in the context.
            String toLocationValue = ((Activity) context).getIntent().getStringExtra("TO_LOCATION");

            // a. Fetch carpools
            URL carpoolURL = new URL("https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/Create_Carpool");
            HttpURLConnection carpoolConnection = (HttpURLConnection) carpoolURL.openConnection();
            carpoolConnection.setRequestProperty("apikey", context.getString(R.string.supabasekey));
            carpoolConnection.setRequestProperty("Authorization", "Bearer " + context.getString(R.string.supabasekey));
            JSONArray carpools = new JSONArray(IOUtils.toString(carpoolConnection.getInputStream(), "UTF-8"));

            for (int i = 0; i < carpools.length(); i++) {
                JSONObject carpool = carpools.getJSONObject(i);
                long driverPhone = carpool.getLong("driver_pnum");

                // Filter based on Destination and toLoc match, and Num_passenger != 0
                if(!carpool.getString("Destination").equals(toLocationValue) || carpool.getInt("Num_passenger") == 0) {
                    continue; // Skip to the next iteration
                }

                // b. Fetch driver details for each carpool
                URL driverURL = new URL("https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/Driver_Registration?phone_no=eq." + driverPhone);
                HttpURLConnection driverConnection = (HttpURLConnection) driverURL.openConnection();
                driverConnection.setRequestProperty("apikey", context.getString(R.string.supabasekey));
                driverConnection.setRequestProperty("Authorization", "Bearer " + context.getString(R.string.supabasekey));
                JSONArray drivers = new JSONArray(IOUtils.toString(driverConnection.getInputStream(), "UTF-8"));

                if (drivers.length() > 0) {
                    JSONObject driver = drivers.getJSONObject(0);

                    // Construct and add CarpoolData
                    CarpoolData carpoolData = new CarpoolData(
                            driver.getString("username"),
                            carpool.getString("Price"),
                            carpool.getString("Starting_loc"),
                            carpool.getString("Date"),
                            driver.getString("phone_no"),
                            carpool.getString("Time"),
                            driver.getString("car_model"),
                            driver.getString("car_color"),
                            driver.getString("carplate_no")
                    );
                    carpoolDataList.add(carpoolData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return carpoolDataList;
    }





    @Override
    protected void onPostExecute(List<CarpoolData> carpoolData) {
        super.onPostExecute(carpoolData);

        ProgressBar progressBar = ((Activity) context).findViewById(R.id.loadingProgressBar);
        TextView noRecordTextView = ((Activity) context).findViewById(R.id.noRecord);
        ListView offerList = ((Activity) context).findViewById(R.id.offerList);

        progressBar.setVisibility(View.GONE);  // Always hide the ProgressBar after fetching
        Log.d("CarpoolData", "Size of returned data: " + carpoolData.size());


        if (carpoolData == null || carpoolData.isEmpty()) {
            // If error occurred or no valid data fetched (all were Num_passenger = 0), show noRecordTextView
            offerList.setVisibility(View.GONE);
            noRecordTextView.setVisibility(View.VISIBLE);
            return;
        } else {
            noRecordTextView.setVisibility(View.GONE);  // Hide the noRecord TextView if there's valid data
        }

        callback.onDataFetched(carpoolData);
    }

}
