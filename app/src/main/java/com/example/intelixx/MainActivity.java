package com.example.intelixx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu; // Import Wajib
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navPeta, navNotifikasi, navProfil;
    private ImageView icHome, icPeta, icNotif, icProfil, btnMenu; // Tambah btnMenu
    private TextView tvHome, tvPeta, tvNotif, tvProfil;

    private TextView tvNotifBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParkingData.startSimulation();

        initViews();
        setupNavigation();
        setupTopMenu(); // Setup Menu Titik 3

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            updateNavUI(navHome, icHome, tvHome);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
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

        btnMenu = findViewById(R.id.btnMenu); // Init Titik 3

        tvHome = findViewById(R.id.tvHome);
        tvPeta = findViewById(R.id.tvPeta);
        tvNotif = findViewById(R.id.tvNotif);
        tvProfil = findViewById(R.id.tvProfil);

        tvNotifBadge = findViewById(R.id.tvNotifBadge);
    }

    // === LOGIKA TITIK 3 (POP-UP MENU) ===
    private void setupTopMenu() {
        btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, btnMenu);
            popup.getMenuInflater().inflate(R.menu.top_options_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_refresh) {
                    // 1. REFRESH: Reload fragment yang sedang aktif & Badge
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (currentFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .detach(currentFragment)
                                .attach(currentFragment)
                                .commit();
                    }
                    updateNotificationBadge();
                    Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show();
                    return true;

                } else if (id == R.id.action_help) {
                    // 2. BANTUAN: Muncul Dialog
                    new AlertDialog.Builder(this)
                            .setTitle("Bantuan Singkat")
                            .setMessage("1. Pilih menu 'Peta' untuk cari parkir.\n2. Klik slot Hijau untuk Booking.\n3. Anda punya waktu 15 menit untuk Check-in.")
                            .setPositiveButton("Paham", null)
                            .show();
                    return true;

                } else if (id == R.id.action_logout) {
                    // 3. LOGOUT: Keluar aplikasi
                    logoutUser();
                    return true;
                }

                return false;
            });
            popup.show();
        });
    }

    private void logoutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Keluar Akun?")
                .setMessage("Anda yakin ingin keluar?")
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
    }

    // ... (Sisa kode Navigasi & Badge TETAP SAMA seperti sebelumnya) ...

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
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void updateNavUI(LinearLayout selectedNav, ImageView selectedIcon, TextView selectedText) {
        int colorGray = ContextCompat.getColor(this, R.color.gray);
        int colorBlue = ContextCompat.getColor(this, R.color.primary_blue);

        icHome.setColorFilter(colorGray); tvHome.setTextColor(colorGray);
        icPeta.setColorFilter(colorGray); tvPeta.setTextColor(colorGray);
        icNotif.setColorFilter(colorGray); tvNotif.setTextColor(colorGray);
        icProfil.setColorFilter(colorGray); tvProfil.setTextColor(colorGray);

        selectedIcon.setColorFilter(colorBlue);
        selectedText.setTextColor(colorBlue);
    }

    public void updateNotificationBadge() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = prefs.getString("username", null);
        if (username == null) return;

        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn == null) return;

                String sql = "SELECT COUNT(*) FROM notifikasi WHERE username = ? AND is_read = 0";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                int count = 0;
                if (rs.next()) count = rs.getInt(1);
                conn.close();

                int finalCount = count;
                runOnUiThread(() -> {
                    if (tvNotifBadge != null) {
                        if (finalCount > 0) {
                            tvNotifBadge.setText(String.valueOf(finalCount));
                            tvNotifBadge.setVisibility(View.VISIBLE);
                        } else {
                            tvNotifBadge.setVisibility(View.GONE);
                        }
                    }
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}