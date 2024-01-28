package com.infinix.NewsApp.UserLoginRegister;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.infinix.NewsApp.R;

public class EmailVerificationActivity extends AppCompatActivity {

    private EditText verificationCodeEditText;
    private Button verifyButton, buttonCancel;
    private String userEmail;
    private String verificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        verificationCodeEditText = findViewById(R.id.editTextVerificationCode);
        verifyButton = findViewById(R.id.buttonVerify);
        buttonCancel = findViewById(R.id.buttonCancel);

        Intent intent = getIntent();
        if (intent != null) {
            userEmail = intent.getStringExtra("email");
            verificationCode = intent.getStringExtra("verificationCode");
        }

        verifyButton.setOnClickListener(view -> {
            String enteredCode = verificationCodeEditText.getText().toString().trim();
            if (enteredCode.equals(verificationCode)) {
                Toast.makeText(this, "Email verified successfully!\nRegistered Account success!", Toast.LENGTH_SHORT).show();
                // You can now proceed with user registration or any other actions.
                // Navigate back to UserLoginActivity
                Intent i = new Intent(this, UserLoginActivity.class);
                startActivity(i);
                finish(); // Finish the current activity
            } else {
                Toast.makeText(this, "Invalid verification code. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
        buttonCancel.setOnClickListener(view -> {
            Intent in = new Intent(this, UserRegisterActivity.class);
            startActivity(in);
            finish();
        });

    }
}
