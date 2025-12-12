package com.example.intelixx;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailAreaActivity extends AppCompatActivity {

    private LinearLayout btnBack;
    private TextView tvAreaName, tvDistance, tvAvailableSlots, tvTotalCapacity;
    private TextView tvPercentage, btnNavigate, btnBooking;
    private ProgressBar progressBar;
    private GridLayout gridSlots;

    private int currentAvailable = 0;
    private int totalCapacity = 0;

    // ARRAY STATUS SLOT: true = kosong (hijau), false = terisi (merah)
    private boolean[] slotStatus;
    // MENYIMPAN SLOT YANG DIPILIH USER (-1 artinya belum ada yang dipilih)
    private int selectedSlotIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_area);

        initViews();
        loadData();
        setupActions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvAreaName = findViewById(R.id.tvAreaName);
        tvDistance = findViewById(R.id.tvDistance);
        tvAvailableSlots = findViewById(R.id.tvAvailableSlots);
        tvTotalCapacity = findViewById(R.id.tvTotalCapacity);
        tvPercentage = findViewById(R.id.tvPercentage);
        progressBar = findViewById(R.id.progressBar);
        btnNavigate = findViewById(R.id.btnNavigate);
        btnBooking = findViewById(R.id.btnBooking);
        gridSlots = findViewById(R.id.gridSlots);
    }

    private void loadData() {
        String areaName = getIntent().getStringExtra("areaName");
        String distance = getIntent().getStringExtra("distance");
        currentAvailable = getIntent().getIntExtra("available", 0);
        totalCapacity = getIntent().getIntExtra("capacity", 0);

        tvAreaName.setText(areaName);
        tvDistance.setText(distance + " dari lokasi Anda");

        // --- INISIALISASI SLOT SECARA ACAK ---
        // Kita buat daftar posisi slot, lalu acak mana yang kosong
        slotStatus = new boolean[totalCapacity];
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < totalCapacity; i++) {
            positions.add(i);
            slotStatus[i] = false; // Default semua terisi dulu
        }
        // Acak posisi
        Collections.shuffle(positions);

        // Ambil sejumlah 'currentAvailable' untuk dijadikan Kosong (true)
        for (int i = 0; i < currentAvailable; i++) {
            int slotIndex = positions.get(i);
            slotStatus[slotIndex] = true;
        }

        updateUIStats(); // Update angka & progress bar
        drawParkingSlots(); // Gambar kotak-kotak
    }

    private void updateUIStats() {
        tvAvailableSlots.setText(String.valueOf(currentAvailable));
        tvTotalCapacity.setText(String.valueOf(totalCapacity));

        float percentage = 0;
        if (totalCapacity > 0) {
            percentage = (float) currentAvailable / totalCapacity * 100;
        }
        tvPercentage.setText(String.format("%.0f%%", percentage));
        progressBar.setProgress((int) percentage);

        int color;
        if (percentage > 40) {
            color = Color.parseColor("#10B981");
        } else if (percentage >= 15) {
            color = Color.parseColor("#F59E0B");
        } else {
            color = Color.parseColor("#EF4444");
        }

        progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        tvPercentage.setTextColor(color);
    }

    private void drawParkingSlots() {
        gridSlots.removeAllViews();

        int columnCount = (int) Math.ceil(totalCapacity / 2.0);
        gridSlots.setColumnCount(columnCount);

        for (int i = 0; i < totalCapacity; i++) {
            TextView slotView = new TextView(this);
            int finalIndex = i; // Perlu final variable untuk akses di dalam onClick

            int size = (int) (40 * getResources().getDisplayMetrics().density);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(8, 8, 8, 8);
            slotView.setLayoutParams(params);

            slotView.setGravity(Gravity.CENTER);
            slotView.setText(String.valueOf(i + 1));
            slotView.setTextColor(Color.WHITE);
            slotView.setTextSize(12);
            slotView.setTypeface(null, android.graphics.Typeface.BOLD);

            // --- LOGIKA WARNA KOTAK ---
            if (i == selectedSlotIndex) {
                // Kalo lagi dipilih -> BIRU
                slotView.setBackgroundResource(R.drawable.bg_button);
                slotView.getBackground().setColorFilter(Color.parseColor("#3B82F6"), PorterDuff.Mode.SRC_IN);
            } else if (slotStatus[i]) {
                // Kalo kosong -> HIJAU
                slotView.setBackgroundResource(R.drawable.bg_button);
                slotView.getBackground().setColorFilter(Color.parseColor("#10B981"), PorterDuff.Mode.SRC_IN);
            } else {
                // Kalo terisi -> MERAH
                slotView.setBackgroundResource(R.drawable.bg_button);
                slotView.getBackground().setColorFilter(Color.parseColor("#EF4444"), PorterDuff.Mode.SRC_IN);
            }

            // --- KLIK SLOT ---
            slotView.setOnClickListener(v -> {
                if (slotStatus[finalIndex]) {
                    // Hanya bisa pilih slot yang KOSONG (true)
                    selectedSlotIndex = finalIndex; // Simpan index yang dipilih
                    drawParkingSlots(); // Gambar ulang biar warnanya berubah jadi Biru
                } else {
                    Toast.makeText(this, "Slot no " + (finalIndex + 1) + " sudah terisi!", Toast.LENGTH_SHORT).show();
                }
            });

            gridSlots.addView(slotView);
        }
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        btnNavigate.setOnClickListener(v -> {
            Toast.makeText(this, "Membuka navigasi ke " + tvAreaName.getText(), Toast.LENGTH_SHORT).show();
        });

        btnBooking.setOnClickListener(v -> {
            if (currentAvailable > 0) {
                // Cek apakah user sudah pilih slot atau belum
                if (selectedSlotIndex != -1) {
                    showBookingDialog();
                } else {
                    Toast.makeText(this, "Silakan pilih kotak slot parkir (Hijau) terlebih dahulu!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Maaf, semua slot parkir penuh!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBookingDialog() {
        // Tampilkan nomor slot di dialog (+1 karena index mulai dari 0)
        int slotNumber = selectedSlotIndex + 1;

        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Booking")
                .setMessage("Booking Slot Nomor " + slotNumber + " di " + tvAreaName.getText() + "?\n\nSlot akan ditahan selama 15 menit.")
                .setPositiveButton("Ya, Booking", (dialog, which) -> {
                    Toast.makeText(this, "Berhasil Booking Slot No " + slotNumber, Toast.LENGTH_LONG).show();

                    // Ubah status slot jadi TERISI (false)
                    slotStatus[selectedSlotIndex] = false;
                    selectedSlotIndex = -1; // Reset pilihan

                    // Update Data
                    currentAvailable--;
                    updateUIStats();
                    drawParkingSlots(); // Gambar ulang (Slot tadi akan jadi merah)
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}