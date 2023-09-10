package my.edu.utar.grp_nav;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar loadingProgressBar;
    private EditText passwordEditText;
    private EditText UsernameText;
    private EditText phoneText;
    private CheckBox showPasswordCheckBox;
    private List<String> login = new ArrayList<>();
    private Handler mHandler = new Handler();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        View parentLayout = findViewById(android.R.id.content);
        parentLayout.setOnTouchListener((v, event) -> {
            hideSoftKeyboard();
            return false;
        });


        // Get the TextView
        TextView textViewSignUp = findViewById(R.id.textViewSignUp);

        // Set the text with underline using Html.fromHtml
        textViewSignUp.setText(Html.fromHtml("<u>SignUp</u>"));


        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);

        login.add(""); // Placeholder for username
        login.add("");
        login.add("");

        UsernameText = findViewById(R.id.name);
        phoneText = findViewById(R.id.Phone);

        TextView text = findViewById(R.id.textViewSignUp);
        text.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        passwordEditText = findViewById(R.id.Password);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);

        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordEditText.setTransformationMethod(null);
            } else {
                passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
            }
        });

        Button btn = findViewById(R.id.btnlogin);
        btn.setOnClickListener(v -> {
            String enteredUsername = UsernameText.getText().toString();
            String enteredPassword = passwordEditText.getText().toString();
            String enteredPhone = phoneText.getText().toString();

            if (enteredUsername.isEmpty() || enteredPassword.isEmpty() || enteredPhone.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter all the information.", Toast.LENGTH_SHORT).show();
            } else {
                authenticateUser(enteredUsername, enteredPassword, enteredPhone);
            }
        });
    }

    private void authenticateUser(String username, String password, String phone) {
        Thread connectingThread = new Thread(() -> {
            HttpURLConnection hc = null;
            try {
                String apiUrl = "https://njnzgadebvlvexstbxnu.supabase.co/rest/v1/User_Registration?" +
                        "username=eq." + Uri.encode(username) +
                        "&password=eq." + Uri.encode(password) +
                        "&phoneno=eq." + Uri.encode(phone);
                URL url = new URL(apiUrl);
                hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod("GET");
                hc.setRequestProperty("apikey", getString(R.string.supabasekey));
                hc.setRequestProperty("Authorization", "Bearer " + getString(R.string.supabasekey));

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

                    if (jsonArray.length() == 0) {
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(() -> {
                            showToast("Login failed!");
                            UsernameText.setText("");
                            passwordEditText.setText("");
                            phoneText.setText("");
                            loadingProgressBar.setVisibility(View.GONE);
                        });
                    } else {
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(() -> {
                            saveResult(username,phone);
                            loadingProgressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        });
                    }

                } else {
                    Log.i("MainActivity2", "Response Code:" + hc.getResponseCode());
                }
                input.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (hc != null) {
                    hc.disconnect();
                }
            }
        });
        connectingThread.start();
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

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    private void saveResult(String Username,String Phone){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Name", Username);
        editor.putString("Phone Number", Phone);
        editor.apply();
    }


    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

}