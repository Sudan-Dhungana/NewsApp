package com.infinix.NewsApp.UserLoginRegister;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.infinix.NewsApp.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText editTextEmail;
    Button btnPasswordSend, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextEmail = findViewById(R.id.editTextEmail);
        btnPasswordSend = findViewById(R.id.btnPasswordSend);
        btnCancel = findViewById(R.id.btnCancel);

        btnPasswordSend.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString();
            Toast.makeText(this, "Feature coming soon...", Toast.LENGTH_SHORT).show();
        });

        btnCancel.setOnClickListener(view -> {
            Intent intent = new Intent(this, UserLoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}