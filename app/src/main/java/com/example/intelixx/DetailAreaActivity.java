package com.example.intelixx;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailAreaActivity extends AppCompatActivity {

    private LinearLayout btnBack;
    private TextView tvAreaName, tvDistance, tvAvailableSlots, tvTotalCapacity;
    private TextView tvPercentage, btnNavigate, btnBooking;
    private ProgressBar progressBar;
    private GridLayout gridSlots;

    // Data Referensi ke Global Variable
    private int[] currentSlotStatus;
    private int totalCapacity;
    private boolean isFloor1 = false;

    // Index slot yang sedang dipilih user (belum dibooking)
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

        // Tentukan ini Lantai 1 atau 2 berdasarkan Nama Area
        if (areaName != null && areaName.toLowerCase().contains("lantai 1")) {
            isFloor1 = true;
            currentSlotStatus = ParkingData.slotsLantai1;
            totalCapacity = ParkingData.CAP_LANTAI_1;
        } else {
            isFloor1 = false;
            currentSlotStatus = ParkingData.slotsLantai2;
            totalCapacity = ParkingData.CAP_LANTAI_2;
        }

        tvAreaName.setText(areaName);
        tvDistance.setText(distance + " dari lokasi Anda");
        tvTotalCapacity.setText(String.valueOf(totalCapacity));

        updateStats();     // Hitung statistik angka
        drawParkingSlots(); // Gambar kotak-kotak
    }

    private void updateStats() {
        int available = ParkingData.getAvailableCount(currentSlotStatus);
        tvAvailableSlots.setText(String.valueOf(available));

        float percentage = (float) available / totalCapacity * 100;
        tvPercentage.setText(String.format("%.0f%%", percentage));
        progressBar.setProgress((int) percentage);

        int color;
        if (percentage > 40) {
            color = Color.parseColor("#10B981"); // Hijau
        } else if (percentage >= 15) {
            color = Color.parseColor("#F59E0B"); // Kuning
        } else {
            color = Color.parseColor("#EF4444"); // Merah
        }

        progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        tvPercentage.setTextColor(color);
    }

    private void drawParkingSlots() {
        gridSlots.removeAllViews();

        // Atur jumlah kolom otomatis (Bagi 2 baris)
        int columnCount = (int) Math.ceil(totalCapacity / 2.0);
        gridSlots.setColumnCount(columnCount);

        for (int i = 0; i < totalCapacity; i++) {
            TextView slotView = new TextView(this);
            int finalIndex = i;

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
            slotView.setTypeface(null, Typeface.BOLD);

            // LOGIKA WARNA KOTAK
            int status = currentSlotStatus[i];

            if (i == selectedSlotIndex) {
                // SEDANG DIPILIH (BIRU)
                slotView.setBackgroundResource(R.drawable.bg_button);
                slotView.getBackground().setColorFilter(Color.parseColor("#3B82F6"), PorterDuff.Mode.SRC_IN);
            } else if (status == 2) {
                // BOOKING (KUNING)
                slotView.setBackgroundResource(R.drawable.bg_button);
                slotView.getBackground().setColorFilter(Color.parseColor("#FBBF24"), PorterDuff.Mode.SRC_IN);
            } else if (status == 1) {
                // TERISI (MERAH)
                slotView.setBackgroundResource(R.drawable.bg_button);
                slotView.getBackground().setColorFilter(Color.parseColor("#EF4444"), PorterDuff.Mode.SRC_IN);
            } else {
                // KOSONG (HIJAU)
                slotView.setBackgroundResource(R.drawable.bg_button);
                slotView.getBackground().setColorFilter(Color.parseColor("#10B981"), PorterDuff.Mode.SRC_IN);
            }

            // KLIK SLOT
            slotView.setOnClickListener(v -> {
                // Cuma bisa pilih slot yang KOSONG (0)
                if (currentSlotStatus[finalIndex] == 0) {
                    selectedSlotIndex = finalIndex;
                    drawParkingSlots(); // Refresh warna jadi Biru
                } else if (currentSlotStatus[finalIndex] == 2) {
                    Toast.makeText(this, "Slot ini sedang dibooking orang lain!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Slot sudah terisi!", Toast.LENGTH_SHORT).show();
                }
            });

            gridSlots.addView(slotView);
        }
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnNavigate.setOnClickListener(v -> {
            Toast.makeText(this, "Membuka navigasi ke " + tvAreaName.getText(), Toast.LENGTH_SHORT).show();
        });

        btnBooking.setOnClickListener(v -> {
            if (selectedSlotIndex != -1) {
                showBookingDialog();
            } else {
                Toast.makeText(this, "Silakan pilih slot berwarna HIJAU terlebih dahulu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBookingDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Booking")
                // Ubah teks "10 detik" jadi "15 menit"
                .setMessage("Anda akan membooking Slot No " + (selectedSlotIndex + 1) + ".\n\nSlot akan ditahan (KUNING) selama 15 menit. Jika Anda tidak check-in, slot akan kembali Hijau.")
                .setPositiveButton("Booking Sekarang", (dialog, which) -> {

                    int floorNum = isFloor1 ? 1 : 2;
                    ParkingData.bookSlot(floorNum, selectedSlotIndex);

                    // Ubah toast-nya juga
                    Toast.makeText(this, "Booking Berhasil! Slot ditahan 15 menit.", Toast.LENGTH_LONG).show();

                    selectedSlotIndex = -1;
                    updateStats();
                    drawParkingSlots();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}