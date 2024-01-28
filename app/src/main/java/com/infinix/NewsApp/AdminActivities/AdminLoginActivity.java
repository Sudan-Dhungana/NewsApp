package com.infinix.NewsApp.AdminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.infinix.NewsApp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdminLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        EditText unameAdminTF, passwordAdminTF;
        Button btnAdminLogin;
        unameAdminTF = findViewById(R.id.adminUsernameTF);
        passwordAdminTF = findViewById(R.id.adminPasswordTF);
        btnAdminLogin = findViewById(R.id.adminLoginBtn);

        btnAdminLogin.setOnClickListener(view -> {
            // Get the input values as strings
            String username = unameAdminTF.getText().toString().trim();
            String password = passwordAdminTF.getText().toString().trim();

            // Initialize a boolean variable to indicate if the input is valid
            boolean isValid = true;

            // Check if the password is empty or less than 6 characters
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                // Set an error message on the password edit text
                passwordAdminTF.setError("Password must contain at least 6 characters");
                // Request focus on the password edit text
                passwordAdminTF.requestFocus();
                // Set isValid to false
                isValid = false;
            }

            // Check if the email is empty or not a valid email address
            //if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (username.equals("")) {
                // Set an error message on the email edit text
                unameAdminTF.setError("Please enter a valid username");
                // Request focus on the email edit text
                unameAdminTF.requestFocus();
                // Set isValid to false
                isValid = false;
            }

            // volley php url - laptop ipaddress
            String ip_address = getString(R.string.ip_address).trim();
            String url = ip_address + "NewsApp/Admin/login.php";

            // If the input is valid, proceed with your login service
            if (isValid) {
                StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean error = jsonObject.getBoolean("error");
                        String message = jsonObject.getString("message");
                        unameAdminTF.setText("");
                        passwordAdminTF.setText("");
                        Toast.makeText(AdminLoginActivity.this, message,
                                Toast.LENGTH_SHORT).show();

                        // Check if the response is successful
                        if (!error) {
                            // Start the dashboard activity
                            Intent intent = new Intent(AdminLoginActivity.this,
                                    AdminDashboard.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error! something went wrong! " +
                                        "It's not your fault, it's Sudan's fault.",
                                Toast.LENGTH_LONG).show();
                    }
                }, error -> Toast.makeText(AdminLoginActivity.this, "[Status]: " +
                        "Look's like something went wrong! by my side\nwait while Sudan fixes it! "
                        + error.toString(), Toast.LENGTH_LONG).show()
                ) {

                    @NonNull
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("username", username);
                        params.put("password", password);
                        return params;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(AdminLoginActivity.this);
                requestQueue.add(request);
            }
        });
    }
}