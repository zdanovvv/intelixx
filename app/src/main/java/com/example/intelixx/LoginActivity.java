package com.example.intelixx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. CEK AUTO LOGIN (Kalau sudah login, langsung masuk Main)
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            goToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        // Init Views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        // 2. LOGIKA LOGIN (Cek ke Database)
        btnLogin.setOnClickListener(v -> {
            String inputUser = etUsername.getText().toString().trim();
            String inputPass = etPassword.getText().toString().trim();

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                Toast.makeText(this, "Harap isi username dan password!", Toast.LENGTH_SHORT).show();
            } else {
                performLogin(inputUser, inputPass);
            }
        });

        // Pindah ke Halaman Daftar
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin(String inputLogin, String password) {
        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Gagal konek ke Server!", Toast.LENGTH_SHORT).show());
                    return;
                }

                // === UPDATE QUERY DISINI ===
                // Kita cek: Apakah input user cocok dengan kolom 'username' ATAU kolom 'npm'?
                String sql = "SELECT * FROM users WHERE (username = ? OR npm = ?) AND password = ?";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, inputLogin); // Cek sebagai Username
                stmt.setString(2, inputLogin); // Cek sebagai NPM
                stmt.setString(3, password);   // Cek Password

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // === LOGIN SUKSES ===
                    String usernameDB = rs.getString("username"); // Ambil username asli dari DB buat session

                    // Simpan Sesi Login
                    SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("username", usernameDB); // Simpan username yang benar (bukan input user yg mungkin NIM)
                    editor.apply();

                    conn.close();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    });

                } else {
                    // === LOGIN GAGAL ===
                    conn.close();
                    runOnUiThread(() -> Toast.makeText(this, "Username/NIM atau Password Salah!", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Terjadi Kesalahan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Tutup LoginActivity agar tidak bisa di-back
    }
}