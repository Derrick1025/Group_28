package my.edu.utar.grp_nav;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GetStartActivity extends AppCompatActivity {

    private static final String SHARED_PREFS_NAME = "user_onboarding_prefs";
    private static final String KEY_USER_SEEN_ONBOARDING = "user_seen_onboarding";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_get_start);

        // Check if user has seen onboarding
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        boolean hasUserSeenOnboarding = sharedPreferences.getBoolean(KEY_USER_SEEN_ONBOARDING, false);

        if (hasUserSeenOnboarding) {
            // Navigate to the main activity
            startActivity(new Intent(GetStartActivity.this, LoginActivity.class));
            finish();
            return;
        }


        // Handle the "Get Start" button click
        Button getStartButton = findViewById(R.id.button);
        getStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save in SharedPreferences that the user has seen onboarding
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_USER_SEEN_ONBOARDING, true);
                editor.apply();

                // Navigate to the main activity
                startActivity(new Intent(GetStartActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}