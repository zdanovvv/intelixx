package com.example.intelixx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class PetaActivity extends AppCompatActivity {

    private LinearLayout navHome, navPeta, navNotifikasi, navProfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_peta);

        initViews();
        setupNavigation();
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navPeta = findViewById(R.id.navPeta);
        navNotifikasi = findViewById(R.id.navNotifikasi);
        navProfil = findViewById(R.id.navProfil);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(PetaActivity.this, MainActivity.class));
            finish();
        });

        navPeta.setOnClickListener(v -> {
            // Already on peta
        });

        navNotifikasi.setOnClickListener(v -> {
            startActivity(new Intent(PetaActivity.this, NotifikasiActivity.class));
            finish();
        });

        navProfil.setOnClickListener(v -> {
            startActivity(new Intent(PetaActivity.this, ProfilActivity.class));
            finish();
        });
    }
}