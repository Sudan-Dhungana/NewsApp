package com.infinix.NewsApp.SplashScreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.infinix.NewsApp.R;
import com.infinix.NewsApp.UserLoginRegister.UserLoginActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000; // Delay time in milliseconds (2 seconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay for the specified time and navigate to the main activity
        new Handler().postDelayed(() -> {
            // Start the main activity
            Intent intent = new Intent(SplashActivity.this, UserLoginActivity.class);
            startActivity(intent);
            finish(); // Close the splash activity to prevent going back to it
        }, SPLASH_DELAY_MS);
    }
}
