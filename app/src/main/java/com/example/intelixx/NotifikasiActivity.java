package com.example.intelixx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NotifikasiActivity extends AppCompatActivity {

    private LinearLayout navHome, navPeta, navNotifikasi, navProfil;
    private TextView tvMarkAllRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_notifikasi);

        initViews();
        setupNavigation();
        setupActions();
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navPeta = findViewById(R.id.navPeta);
        navNotifikasi = findViewById(R.id.navNotifikasi);
        navProfil = findViewById(R.id.navProfil);
        tvMarkAllRead = findViewById(R.id.tvMarkAllRead);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(NotifikasiActivity.this, MainActivity.class));
            finish();
        });

        navPeta.setOnClickListener(v -> {
            startActivity(new Intent(NotifikasiActivity.this, PetaActivity.class));
            finish();
        });

        navNotifikasi.setOnClickListener(v -> {
            // Already on notifikasi
        });

        navProfil.setOnClickListener(v -> {
            startActivity(new Intent(NotifikasiActivity.this, ProfilActivity.class));
            finish();
        });
    }

    private void setupActions() {
        tvMarkAllRead.setOnClickListener(v -> {
            Toast.makeText(this, "Semua notifikasi ditandai sudah dibaca", Toast.LENGTH_SHORT).show();
        });
    }
}