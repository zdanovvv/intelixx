package com.example.intelixx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfilActivity extends AppCompatActivity {

    private LinearLayout navHome, navPeta, navNotifikasi, navProfil;
    private TextView btnUpdateVehicle, btnHistory, btnSettings, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        initViews();
        setupNavigation();
        setupActions();
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navPeta = findViewById(R.id.navPeta);
        navNotifikasi = findViewById(R.id.navNotifikasi);
        navProfil = findViewById(R.id.navProfil);
        btnUpdateVehicle = findViewById(R.id.btnUpdateVehicle);
        btnHistory = findViewById(R.id.btnHistory);
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(ProfilActivity.this, MainActivity.class));
            finish();
        });

        navPeta.setOnClickListener(v -> {
            startActivity(new Intent(ProfilActivity.this, PetaActivity.class));
            finish();
        });

        navNotifikasi.setOnClickListener(v -> {
            startActivity(new Intent(ProfilActivity.this, NotifikasiActivity.class));
            finish();
        });

        navProfil.setOnClickListener(v -> {
            // Already on profil
        });
    }

    private void setupActions() {
        btnUpdateVehicle.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur ubah data kendaraan akan segera hadir", Toast.LENGTH_SHORT).show();
        });

        btnHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur riwayat parkir akan segera hadir", Toast.LENGTH_SHORT).show();
        });

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur pengaturan akan segera hadir", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show();
            // Add logout logic here
        });
    }
}