package com.example.intelixx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // Tambah ini
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink; // Tambah ini

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CEK SESSION (AUTO LOGIN)
        SharedPreferences session = getSharedPreferences("UserSession", MODE_PRIVATE);
        if (session.getBoolean("isLoggedIn", false)) {
            goToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink); // Init Register Link

        // LOGIKA LOGIN
        btnLogin.setOnClickListener(v -> {
            String inputUser = etUsername.getText().toString().trim();
            String inputPass = etPassword.getText().toString().trim();

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show();
            } else {
                // AMBIL DATA YANG SUDAH DI-REGISTER
                SharedPreferences userData = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                String registeredUser = userData.getString("saved_username", null);
                String registeredPass = userData.getString("saved_password", null);

                // CEK: Apakah cocok dengan "admin" ATAU cocok dengan user yang barusan daftar?
                boolean isAdmin = inputUser.equals("admin") && inputPass.equals("123456");
                boolean isUserValid = inputUser.equals(registeredUser) && inputPass.equals(registeredPass);

                if (isAdmin || isUserValid) {
                    // SIMPAN SESSION LOGIN
                    SharedPreferences.Editor editor = session.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                } else {
                    Toast.makeText(this, "Username atau Password salah!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // PINDAH KE HALAMAN REGISTER
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}