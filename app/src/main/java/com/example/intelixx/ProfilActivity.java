package com.example.intelixx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog; // Pastikan ini di-import
import androidx.appcompat.app.AppCompatActivity;

public class ProfilActivity extends AppCompatActivity {

    private LinearLayout navHome, navPeta, navNotifikasi, navProfil;
    private TextView btnUpdateVehicle, btnHistory, btnSettings, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profil); // Pastikan nama layout XML benar

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
            overridePendingTransition(0, 0);
            finish();
        });

        navPeta.setOnClickListener(v -> {
            startActivity(new Intent(ProfilActivity.this, PetaActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navNotifikasi.setOnClickListener(v -> {
            startActivity(new Intent(ProfilActivity.this, NotifikasiActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navProfil.setOnClickListener(v -> {
            // Sedang di halaman profil
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

        // --- LOGIKA LOGOUT DENGAN POP-UP ---
        btnLogout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });
    }

    private void showLogoutConfirmationDialog() {
        // Membuat Dialog (Pop-up)
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin keluar dari akun?")
                .setPositiveButton("Ya, Keluar", (dialog, which) -> {
                    // Aksi jika user pilih YA
                    performLogout();
                })
                .setNegativeButton("Batal", (dialog, which) -> {
                    // Aksi jika user pilih BATAL (Tutup dialog saja)
                    dialog.dismiss();
                })
                .show();
    }

    private void performLogout() {
        Toast.makeText(this, "Logout Berhasil!", Toast.LENGTH_SHORT).show();

        // Pindah ke LoginActivity
        Intent intent = new Intent(ProfilActivity.this, LoginActivity.class);

        // Membersihkan tumpukan activity agar tidak bisa di-Back
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish(); // Tutup ProfilActivity
    }
}