package com.infinix.NewsApp.UserLoginRegister;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class UserRegisterActivity extends AppCompatActivity {
    EditText edtUser, edtEmail, edtContact, edtAddress, edtPassword;
    TextView txtLoginClick;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        edtUser = findViewById(R.id.username_register);
        edtEmail = findViewById(R.id.email_register);
        edtContact = findViewById(R.id.contact_register);
        edtPassword = findViewById(R.id.password_register);
        edtAddress = findViewById(R.id.address_register);
        btnRegister = findViewById(R.id.login_button);
        txtLoginClick = findViewById(R.id.txtLoginClick);

        txtLoginClick.setOnClickListener(view -> {
            Intent intent = new Intent(this, UserLoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnRegister.setOnClickListener(view -> {
            String username = edtUser.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String contact = edtContact.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            // Validation code here...
            // validation
            String PHONE_REGEX = "^\\d{3}-?\\d{7}$";
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
            Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(email);
            boolean isValidEmail = matcher.matches();

            boolean isValid = true;
            if (TextUtils.isEmpty(username)) {
                edtUser.setError("Please enter valid username!");
                edtUser.requestFocus();
                isValid = false;
            } else if (TextUtils.isEmpty(email) || isValidEmail==false) {
                edtEmail.setError("Please enter valid email!");
                edtEmail.requestFocus();
                isValid = false;
            } else if (TextUtils.isEmpty(contact) || !contact.matches(PHONE_REGEX)) {
                edtContact.setError("Please enter valid contact!");
                edtContact.requestFocus();
                isValid = false;
            } else if (TextUtils.isEmpty(address)) {
                edtAddress.setError("Please enter valid address!");
                edtAddress.requestFocus();
                isValid = false;
            } else if (TextUtils.isEmpty(password) || password.length() < 6) {
                edtPassword.setError("Please enter password, must contain at least 6 characters!");
                edtPassword.requestFocus();
                isValid = false;
            }

            if (isValid) {
                new CheckEmailExistsTask().execute(email);
                String verificationCode = generateVerificationCode();
                sendVerificationEmailInBackground(email, verificationCode);

                // Proceed with registration
                String ip_address = getString(R.string.ip_address).trim();
                String url = ip_address + "NewsApp/register.php";

                RequestQueue requestQueue = Volley.newRequestQueue(UserRegisterActivity.this);
                StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.getBoolean("error")) {
                            Toast.makeText(this, "[Error]: "
                                            + jsonObject.getString("message"),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(this, EmailVerificationActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("verificationCode", verificationCode);
                            startActivity(intent);
                            finish();
                            /*Toast.makeText(UserRegisterActivity.this,
                                    "Registration successfully! " +
                                            jsonObject.getString("message"),
                                    Toast.LENGTH_SHORT).show();*/
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "[JSONException]: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    error.printStackTrace();
                    Toast.makeText(UserRegisterActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
                    @NonNull
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("username", username);
                        params.put("email", email);
                        params.put("contact", contact);
                        params.put("address", address);
                        params.put("password", password);
                        params.put("verificationCode", verificationCode); // Include verification code in the request
                        return params;
                    }
                };
                requestQueue.add(request);
            }
        });
    }

    private static void sendVerificationEmailInBackground(final String toEmail, final String verificationCode) {
        AsyncTask<Void, Void, Void> sendEmailTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                sendVerificationEmail(toEmail, verificationCode);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // This method is called when the background task is completed.
                // You can add any post-execution code here, such as showing a message.
            }
        };

        sendEmailTask.execute();
    }

    private static void sendVerificationEmail(String toEmail, String verificationCode) {
        final String username = "kathmandusudan@gmail.com"; // Your Gmail email address
        final String password = "mgyqhwzdmiqtiych"; // Your Gmail password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Email Verification Code for NewsApp");
            message.setText("Your verification code is: " + verificationCode);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int codeLength = 6;
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < codeLength; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            codeBuilder.append(randomChar);
        }

        return codeBuilder.toString();
    }
    private boolean checkIfEmailExists(String email) {
        // Use JavaMail API to check if the email address exists
        boolean emailExists = false;
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", "smtp.gmail.com"); // Replace with your SMTP server
        properties.setProperty("mail.smtp.port", "587"); // Replace with the appropriate port

        Session session = Session.getDefaultInstance(properties, null);
        try {
            // Create a new InternetAddress instance with the email address
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate(); // Check if the email address is valid

            // Attempt to connect to the SMTP server and check if the email account exists
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.sendMessage(new MimeMessage(session), new Address[]{new InternetAddress(email)});
            transport.close();

            // If no exception is thrown, the email address exists
            emailExists = true;
        } catch (AddressException e) {
            // The email address is not valid
            emailExists = false;
        } catch (MessagingException e) {
            // The email address does not exist
            emailExists = false;
        }

        return emailExists;
    }
    private class CheckEmailExistsTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String email = params[0];
            return checkIfEmailExists(email);
        }

        @Override
        protected void onPostExecute(Boolean emailExists) {
            // Handle the result on the main thread
            //if (!emailExists) {
                // Display an error message to the user
            //    Toast.makeText(UserRegisterActivity.this, "Account with this email address doesn't exist.", Toast.LENGTH_SHORT).show();
            //}
        }
    }
}
