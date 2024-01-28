package com.infinix.NewsApp.AdminActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.infinix.NewsApp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class AdminDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();

        welcomeMessage();
        logoutAdmin(); // logout part for the admin
    }

    public void welcomeMessage() {
        TextView showUserName = findViewById(R.id.showUserName);
        // Get the Intent that started this activity
        Intent i = getIntent();
        // Get the user name from the Intent
        String username = i.getStringExtra("username");

        // Set the text of the showUserName TextView
        String welcome = "Welcome, ";
        showUserName.setText(welcome);
        showUserName.append(username);
    }

    public void logoutAdmin() {
        ImageView logoutUser = findViewById(R.id.logoutUser);
        logoutUser.setOnClickListener(v -> {
            // Get the RequestQueue
            RequestQueue queue = Volley.newRequestQueue(this);

            SharedPreferences sharedPreferences = getSharedPreferences
                    ("user_session", MODE_PRIVATE);
            String session_id = sharedPreferences.getString("session_id", null);

            // Define the URL for logout
            String url = getString(R.string.ip_address).trim() + "NewsApp/Admin/logout.php?=session_id=" + session_id;

            // Create a StringRequest for logout
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        // Parse the JSON response
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            // Check if there is an error
                            if (jsonObject.getBoolean("error")) {
                                // Show an error message
                                Toast.makeText(AdminDashboard.this,
                                        jsonObject.getString("message"),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Show a success message
                                Toast.makeText(AdminDashboard.this,
                                        jsonObject.getString("message"),
                                        Toast.LENGTH_SHORT).show();
                                // Redirect to login activity
                                Intent intent = new Intent(AdminDashboard.this,
                                        AdminLoginActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> {
                // Handle error
                Toast.makeText(AdminDashboard.this, "That didn't work!",
                        Toast.LENGTH_SHORT).show();
            });

            // Add the request to the RequestQueue
            queue.add(stringRequest);
        });
    }

    @Override
    public void onBackPressed() {
        // Do nothing or show a message
        Toast.makeText(this, "You cannot go back to the login activity",
                Toast.LENGTH_SHORT).show();
    }
}

