package com.example.intelixx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init Views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Action Klik Login
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show();
            } else {
                // LOGIKA LOGIN SEDERHANA (Hardcoded)
                // Nanti bisa diganti dengan cek Database/API
                if (username.equals("admin") && password.equals("123456")) {
                    loginSuccess();
                } else {
                    Toast.makeText(this, "Username atau Password salah!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginSuccess() {
        Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show();

        // Pindah ke MainActivity (Halaman Utama Parkir)
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

        // Finish agar user tidak bisa kembali ke halaman login pakai tombol back
        finish();
    }
}