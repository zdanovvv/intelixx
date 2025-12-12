package com.example.intelixx;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navPeta, navNotifikasi, navProfil;
    private ImageView icHome, icPeta, icNotif, icProfil;
    private TextView tvHome, tvPeta, tvNotif, tvProfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();

        // Load Home Fragment pertama kali buka
        loadFragment(new HomeFragment());
        updateNavUI(navHome, icHome, tvHome);
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navPeta = findViewById(R.id.navPeta);
        navNotifikasi = findViewById(R.id.navNotifikasi);
        navProfil = findViewById(R.id.navProfil);

        icHome = findViewById(R.id.icHome);
        icPeta = findViewById(R.id.icPeta);
        icNotif = findViewById(R.id.icNotif);
        icProfil = findViewById(R.id.icProfil);

        tvHome = findViewById(R.id.tvHome);
        tvPeta = findViewById(R.id.tvPeta);
        tvNotif = findViewById(R.id.tvNotif);
        tvProfil = findViewById(R.id.tvProfil);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            updateNavUI(navHome, icHome, tvHome);
        });

        navPeta.setOnClickListener(v -> {
            loadFragment(new PetaFragment());
            updateNavUI(navPeta, icPeta, tvPeta);
        });

        navNotifikasi.setOnClickListener(v -> {
            loadFragment(new NotifikasiFragment());
            updateNavUI(navNotifikasi, icNotif, tvNotif);
        });

        navProfil.setOnClickListener(v -> {
            loadFragment(new ProfilFragment());
            updateNavUI(navProfil, icProfil, tvProfil);
        });
    }

    private void loadFragment(Fragment fragment) {
        // Ganti isi fragment_container dengan fragment baru
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out) // Animasi smooth
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void updateNavUI(LinearLayout selectedNav, ImageView selectedIcon, TextView selectedText) {
        // 1. Reset semua ke warna Gray (Tidak Aktif)
        int colorGray = ContextCompat.getColor(this, R.color.gray);
        int colorBlue = ContextCompat.getColor(this, R.color.primary_blue);

        icHome.setColorFilter(colorGray); tvHome.setTextColor(colorGray);
        icPeta.setColorFilter(colorGray); tvPeta.setTextColor(colorGray);
        icNotif.setColorFilter(colorGray); tvNotif.setTextColor(colorGray);
        icProfil.setColorFilter(colorGray); tvProfil.setTextColor(colorGray);

        // 2. Set yang dipilih ke warna Biru (Aktif)
        selectedIcon.setColorFilter(colorBlue);
        selectedText.setTextColor(colorBlue);
    }
}