package com.infinix.NewsApp.UserLoginRegister;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.infinix.NewsApp.MainActivities.activity_feed;
import com.infinix.NewsApp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserLoginActivity extends AppCompatActivity {
    TextView typingText;
    ImageView imgIcon;
    Handler handler = new Handler();
    TextView txtRegister, forgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        init();
        typingAnim();
        imageAnim();
    }

    public void imageAnim() {
        imgIcon.setOnClickListener(view -> {
            ObjectAnimator animator = ObjectAnimator.ofFloat(imgIcon, "translationY", 0f, 10f);
            // Set the duration of the animation
            animator.setDuration(900);
            // Set the interpolator of the animation
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            // Set the repeat count of the animation
            animator.setRepeatCount(ValueAnimator.INFINITE);
            // Set the repeat mode of the animation
            animator.setRepeatMode(ValueAnimator.REVERSE);
            // Start the animation
            animator.start();
        });
    }


    public void init() {

        EditText username_edittext = findViewById(R.id.username_edittext);
        EditText password_edittext = findViewById(R.id.password_edittext);

        Button login_button = findViewById(R.id.login_button);

        // for register account
        txtRegister = findViewById(R.id.register_textview);
        forgotPass = findViewById(R.id.forgotPass);
        txtRegister.setOnClickListener(view -> {
            Intent intent = new Intent(UserLoginActivity.this, UserRegisterActivity.class);
            startActivity(intent);
            finish();
        });

        login_button.setOnClickListener(view -> {
            // Create an ObjectAnimator object that animates the translationY property of the login_button
            ObjectAnimator animator = ObjectAnimator.ofFloat(login_button, "translationY", 0f, 10f);
            // Set the duration of the animation
            animator.setDuration(900);
            // Set the interpolator of the animation
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            // Set the repeat count of the animation
            animator.setRepeatCount(ValueAnimator.INFINITE);
            // Set the repeat mode of the animation
            animator.setRepeatMode(ValueAnimator.REVERSE);
            // Start the animation
            animator.start();

            // Get the input values as strings
            String username = username_edittext.getText().toString().trim();
            String password = password_edittext.getText().toString().trim();

            // Check if the email is empty or not a valid email address
            //i f (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            // Validate the email format using a regular expression
            String usernamePattern = "^[A-Za-z0-9_-]{3,15}$";
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

            if (TextUtils.isEmpty(username)) {
                // Set an error message on the email edit text
                username_edittext.setError("Please enter a username/email");
                // Request focus on the email edit text
                username_edittext.requestFocus();
            }
            // Check if the password is empty or less than 6 characters
            if (TextUtils.isEmpty(password) || password.length()
                    < getResources().getInteger(R.integer.min_password_length)) {
                // Set an error message on the password edit text
                password_edittext.setError("Password is empty or less than 6 characters");
            }
            // Split the input by space to handle space-separated input
            String[] parts = username.split("\\s+");
            for (String part : parts) {
                if (TextUtils.isEmpty(part)) {
                    // Skip empty parts (e.g., if there are multiple spaces between words)
                    continue;
                }
                if (username.matches(emailPattern)) {
                    // Set an error message on the username edit text
                    if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                        username_edittext.setError("Please enter a valid email address");
                        // Request focus on the username edit text
                        username_edittext.requestFocus();
                    }
                } else if (!part.matches(usernamePattern)) {
                    username_edittext.setError("Please enter a valid username");
                    // Request focus on the username edit text
                    username_edittext.requestFocus();
                }
            }

            String ip_address = getString(R.string.ip_address).trim();
            String url = ip_address + "NewsApp/login.php";

            // Find the ProgressBar by its ID
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            RequestQueue requestQueue = Volley.newRequestQueue(UserLoginActivity.this);
            StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    //boolean error = jsonObject.getBoolean("error");
                    //String message = jsonObject.getString("message");
                    //username_edittext.setText("");
                    //password_edittext.setText("");
                    //Toast.makeText(UserLoginActivity.this, message, Toast.LENGTH_SHORT).show();

                    // Hide the ProgressBar
                    progressBar.setVisibility(View.GONE);

                    // Check if the response is successful
                    if (jsonObject.getBoolean("error")) {
                        Toast.makeText(UserLoginActivity.this,
                                jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        password_edittext.setText("");
                    } else {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        int user_id = jsonObject.getInt("user_id");
                        String user_name = jsonObject.getString("user_name");
                        String user_email = jsonObject.getString("user_email");
                        String session_id = jsonObject.getString("session_id");

                        // store id in sharedPref
                        SharedPreferences sharedPreferences = getSharedPreferences
                                ("user_session", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("session_id", session_id);
                        editor.putString("username", user_name);
                        editor.apply();

                        // Start the activity feed
                        Intent intent = new Intent(UserLoginActivity.this, activity_feed.class);
                        intent.putExtra("user_id", user_id);
                        intent.putExtra("user_name", user_name);
                        intent.putExtra("user_email", user_email);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "[JSONException]: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    // Hide the ProgressBar
                    progressBar.setVisibility(View.GONE);
                }
            }, error -> {
                Toast.makeText(UserLoginActivity.this, "[Status]: " +
                    "Look's like something went wrong! by my side\nwait while Sudan fixes it! "
                    + error.toString(), Toast.LENGTH_LONG).show();
                // Hide the ProgressBar
                progressBar.setVisibility(View.GONE);
            }) {
                @NonNull
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("username", username);
                    params.put("password", password);
                    return params;
                }
            };
            requestQueue.add(request);
        });

        // forgot pass implementation
        forgotPass.setOnClickListener(view -> openForgotPasswordLink());
    }

    private void openForgotPasswordLink() {
        // Replace the URL with your forgot password link
        String ip = getString(R.string.ip_address).trim();
        String forgotPasswordUrl = ip + "NewsApp/forgotPassword.php";

        // Create an Intent with ACTION_VIEW to open the URL in a web browser
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(forgotPasswordUrl));

        // Start the activity
        startActivity(intent);
    }

    public void typingAnim() {

        imgIcon = findViewById(R.id.logo);
        typingText = findViewById(R.id.news_app);

        TypingRunnable typingRunnable = new TypingRunnable();
        // Set an OnClickListener for your image
        imgIcon.setOnClickListener(v -> {
            // Remove any pending callbacks from the Handler
            handler.removeCallbacks(typingRunnable);

            // Reset the TextView text
            typingText.setText("");

            // Reset the Runnable index
            typingRunnable.index = 0;

            // Post the Runnable to start the animation
            handler.post(typingRunnable);
        });
    }

    // Create a Runnable object that appends one letter at a time
    class TypingRunnable implements Runnable {
        // The index of the current letter to be typed
        int index = 0;
        // The text to be typed
        String text = "😎 NewsApp ☕";

        @Override
        public void run() {
            // Check if there are more letters to type
            if (index < text.length()) {
                // Append the next letter to the TextView
                typingText.append(String.valueOf(text.charAt(index)));

                // Increment the index
                index++;

                // Post this Runnable again with a fixed delay
                handler.postDelayed(this, 100);
            }
        }
    }
}