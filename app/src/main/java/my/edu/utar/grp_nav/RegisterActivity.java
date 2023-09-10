package my.edu.utar.grp_nav;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private CheckBox showPasswordCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        passwordEditText = findViewById(R.id.inputPassword);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // If the checkbox is checked, display the password as plain text
                    passwordEditText.setTransformationMethod(null);
                } else {
                    // If the checkbox is unchecked, display the password as hidden with asterisks
                    passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        TextView text=findViewById(R.id.alreadyHaveAccount);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });

        Button btn=findViewById(R.id.btnRegister);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegistrationConfirmation();
            }
        });
    }


    private String getEmailInput() {
        EditText emailEditText = findViewById(R.id.inputEmail); // Replace with your actual EditText ID
        return emailEditText.getText().toString();
    }

    // Function to get the username input from an EditText field
    private String getUsernameInput() {
        EditText usernameEditText = findViewById(R.id.Username); // Replace with your actual EditText ID
        return usernameEditText.getText().toString();
    }

    private String getPhoneInput() {
        EditText phoneEditText = findViewById(R.id.Phone_no); // Replace with your actual EditText ID
        return phoneEditText.getText().toString();
    }

    // Function to get the password input from an EditText field
    private String getPasswordInput() {
        EditText passwordEditText = findViewById(R.id.inputPassword); // Replace with your actual EditText ID
        return passwordEditText.getText().toString();
    }

    public void showRegistrationConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setMessage("Confirm to register?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Get user input for registration (email, username, password)
                String email = getEmailInput();
                String username = getUsernameInput();
                String password = getPasswordInput();
                String phone = getPhoneInput();

                if (email.isEmpty() || username.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                    // Display a message if any field is empty or contains only spaces
                    Toast.makeText(RegisterActivity.this, "Please fill in all the required information", Toast.LENGTH_SHORT).show();
                    return; // Do not proceed with registration
                }

                // Create a RegisterThread to perform user registration
                RegisterActivity.Register registerThread = new RegisterActivity.Register(email, username, password, phone);
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
        private String Email;
        private String Username;
        private String Password;
        private String Phone;

        HttpURLConnection urlConnection = null;

        public Register(String Email, String Username, String Password, String Phone) {
            this.Email = Email;
            this.Username = Username;
            this.Password = Password;
            this.Phone = Phone;
        }

        @Override
        public void run() {
            try {
                URL url = new URL("https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/User_Registration");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("email", Email);
                jsonObject.put("phoneno", Phone);
                jsonObject.put("username", Username);
                jsonObject.put("password", Password);

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
                            Toast.makeText(RegisterActivity.this, "Register Succeeded", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
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