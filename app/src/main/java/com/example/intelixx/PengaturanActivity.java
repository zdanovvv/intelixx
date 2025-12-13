package com.example.intelixx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class PengaturanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan);

        ImageView btnBack = findViewById(R.id.btnBack);
        LinearLayout btnChangePass = findViewById(R.id.btnChangePass); // Skrg LinearLayout
        LinearLayout btnAbout = findViewById(R.id.btnAbout); // Skrg LinearLayout
        TextView btnLogout = findViewById(R.id.btnLogoutSetting);

        btnBack.setOnClickListener(v -> finish());

        // 1. Ganti Password
        btnChangePass.setOnClickListener(v -> showChangePasswordDialog());

        // 2. Tentang Aplikasi
        btnAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Developer Info")
                    .setMessage("Aplikasi ini dibuat oleh:\n\nTim Champions\nUniversitas Internasional Batam\n\nUntuk Tugas Mata Kuliah Machine Learning dan Smart City.")
                    .setPositiveButton("Mantap", null)
                    .show();
        });

        // 3. Logout (Tambahan di menu setting)
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Keluar?")
                    .setMessage("Anda akan kembali ke halaman login.")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                        prefs.edit().clear().apply();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ganti Password");

        final EditText inputNewPass = new EditText(this);
        inputNewPass.setHint("Masukkan Password Baru");
        inputNewPass.setPadding(50, 30, 50, 30); // Biar inputnya lega
        builder.setView(inputNewPass);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newPass = inputNewPass.getText().toString();
            if (!newPass.isEmpty()) {
                updatePasswordDB(newPass);
            } else {
                Toast.makeText(this, "Password tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void updatePasswordDB(String newPass) {
        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                String sql = "UPDATE users SET password = ? WHERE username = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newPass);
                stmt.setString(2, username);
                int rows = stmt.executeUpdate();
                conn.close();

                runOnUiThread(() -> {
                    if(rows > 0) Toast.makeText(this, "Password Berhasil Diganti!", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(this, "Gagal Mengganti Password", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error Database", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}