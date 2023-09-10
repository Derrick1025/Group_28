package my.edu.utar.grp_nav;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;



public class DriverRegistration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);

        Button btn=findViewById(R.id.btnRegister);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegistrationConfirmation();
            }
        });
    }

    // Function to get the username input from an EditText field
    private String getUsernameInput() {
        EditText usernameEditText = findViewById(R.id.username); // Replace with your actual EditText ID
        return usernameEditText.getText().toString();
    }

    private String getPhoneInput() {
        EditText phoneEditText = findViewById(R.id.phone_no); // Replace with your actual EditText ID
        return phoneEditText.getText().toString();
    }

    // Function to get the password input from an EditText field
    private String getCarModelInput() {
        EditText carmodelEditText = findViewById(R.id.car_model); // Replace with your actual EditText ID
        return carmodelEditText.getText().toString();
    }

    private String getCarColorInput() {
        EditText carcolorEditText = findViewById(R.id.car_color); // Replace with your actual EditText ID
        return carcolorEditText.getText().toString();
    }

    private String getCarPlateNoInput() {
        EditText carplatenoEditText = findViewById(R.id.carplate_no); // Replace with your actual EditText ID
        return carplatenoEditText.getText().toString();
    }

    public void showRegistrationConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverRegistration.this);
        builder.setMessage("Confirm to register?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String username = getUsernameInput();
                String phone = getPhoneInput();
                String carmodel = getCarModelInput();
                String carcolor = getCarColorInput();
                String carplateno = getCarPlateNoInput();

                // Create a RegisterThread to perform user registration
                DriverRegistration.Register registerThread = new DriverRegistration.Register(username, phone, carmodel,carcolor,carplateno);
                registerThread.start();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private class Register extends Thread {
        private String Username;
        private String Phone;
        private String Carmodel;
        private String Carcolor;
        private String Carplateno;

        HttpURLConnection urlConnection = null;

        public Register(String Username, String Phone, String Carmodel, String Carcolor, String Carplateno) {
            this.Username = Username;
            this.Phone = Phone;
            this.Carmodel = Carmodel;
            this.Carcolor = Carcolor;
            this.Carplateno = Carplateno;
        }

        @Override
        public void run() {
            try {
                URL url = new URL("https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/Driver_Registration");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", Username);
                jsonObject.put("phone_no", Phone);
                jsonObject.put("car_model", Carmodel);
                jsonObject.put("car_color", Carcolor);
                jsonObject.put("carplate_no", Carplateno);

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
                            Toast.makeText(DriverRegistration.this, "Register Succeeded", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(DriverRegistration.this,LoginActivity.class));
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
}