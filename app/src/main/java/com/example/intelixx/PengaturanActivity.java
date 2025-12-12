package com.example.intelixx;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
        TextView btnChangePass = findViewById(R.id.btnChangePass);
        TextView btnAbout = findViewById(R.id.btnAbout);

        btnBack.setOnClickListener(v -> finish());

        // 1. Ganti Password
        btnChangePass.setOnClickListener(v -> showChangePasswordDialog());

        // 2. Tentang Aplikasi
        btnAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Tentang Intelixx")
                    .setMessage("Aplikasi Smart Parking IoT\nVersi 1.0.0\n\nDeveloper:\nMuhammad Taufik (2332043)\nUniversitas Internasional Batam")
                    .setPositiveButton("Keren!", null)
                    .show();
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ganti Password");

        final EditText inputNewPass = new EditText(this);
        inputNewPass.setHint("Password Baru");
        builder.setView(inputNewPass);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newPass = inputNewPass.getText().toString();
            if (!newPass.isEmpty()) updatePasswordDB(newPass);
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
                stmt.executeUpdate();
                conn.close();
                runOnUiThread(() -> Toast.makeText(this, "Password Berhasil Diganti!", Toast.LENGTH_SHORT).show());
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}