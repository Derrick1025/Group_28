package my.edu.utar.grp_nav;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditProfile extends AppCompatActivity {


    private SharedPreferences sharedPreferences;
    private EditText oldpassword, newpassword, confirmpassword;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Button save = findViewById(R.id.btnSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                String username = preferences.getString("Name", "");
                oldpassword = findViewById(R.id.oldpassword);
                newpassword = findViewById(R.id.newpassword);
                confirmpassword = findViewById(R.id.Confirmnewpassword);
                String old_password = oldpassword.getText().toString().trim();
                String new_password = newpassword.getText().toString().trim();
                String confirm_password = confirmpassword.getText().toString().trim();
                if(new_password.equals(confirm_password)){
                    authenticatePassword(username,old_password,new_password);
                    showToast("Password updated successfully!");
                    Intent intent = new Intent(EditProfile.this, ProfileFragment.class);
                    startActivity(intent);
                }
                else{
                    showToast("Please make sure both new passwords are same!");
                }


            }
        });
    }

    private void authenticatePassword(String username, String oldPassword, String newPassword) {
        new Thread(() -> {
            HttpURLConnection hc = null;
            try {
                // Define your Supabase API URL for updating rows
                String apiUrl = "https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/User_Registration?" +
                        "username=eq." + Uri.encode(username) +
                        "&password=eq." + Uri.encode(oldPassword);

                URL url = new URL(apiUrl);
                hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod("PATCH");
                hc.setRequestProperty("apikey", getString(R.string.supabasekey));
                hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.supabasekey));
                hc.setRequestProperty("Content-Type", "application/json");
                hc.setRequestProperty("Prefer", "return=minimal");

                JSONObject passwordData = new JSONObject();
                passwordData.put("password", newPassword);

                OutputStreamWriter writer = new OutputStreamWriter(hc.getOutputStream(), "UTF-8");
                writer.write(passwordData.toString());
                writer.close();

                int responseCode = hc.getResponseCode();

                // Check if the request was successful
                if (responseCode == 200 || responseCode == 204) {
                    runOnUiThread(() -> showToast("Password updated successfully!"));

                } else {
                    String responseBody = readStream(hc.getErrorStream());
                    Log.e("EditProfile", "Response Code: " + responseCode + " | Response Body: " + responseBody);
                    runOnUiThread(() -> showToast("Failed to update password. Please try again."));
                }
            } catch (IOException | JSONException e) {
                Log.e("EditProfile", "Error while updating password", e);
                runOnUiThread(() -> showToast("An error occurred. Please try again."));
            } finally {
                if (hc != null) {
                    hc.disconnect();
                }
            }
        }).start();
    }

    private void showToast(String message) {
        Toast.makeText(EditProfile.this, message, Toast.LENGTH_SHORT).show();
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
            Log.e("EditProfile", "Error reading stream", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e("EditProfile", "Error closing stream", e);
            }
        }
        return stringBuilder.toString();
    }

// Initialize SharedPreferences for saving the image URI




}
