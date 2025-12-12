package com.example.intelixx;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

public class DetailAreaActivity extends AppCompatActivity {

    private LinearLayout btnBack;
    private TextView tvAreaName, tvDistance, tvAvailableSlots, tvTotalCapacity;
    private TextView tvPercentage, btnNavigate, btnBooking;
    private ProgressBar progressBar;
    private GridLayout gridSlots;

    private int[] currentSlotStatus;
    private int totalCapacity;
    private boolean isFloor1 = false;
    private int selectedSlotIndex = -1;

    // Penanda status tombol saat ini (0=Idle, 1=Mau Booking, 2=Mau Cancel)
    private int buttonActionState = 0;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    // Timeout Booking (Set 15 Menit = 900000, atau 10000 buat tes)
    private static final long BOOKING_TIMEOUT = 900000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_area);

        initViews();
        loadInitialData();
        setupActions();

        startRealtimeUpdate();
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

        // Default tombol mati dulu sebelum pilih slot
        updateButtonState(0, "Pilih Slot Dulu");
    }

    private void loadInitialData() {
        String areaName = getIntent().getStringExtra("areaName");
        String distance = getIntent().getStringExtra("distance");

        if (areaName != null && areaName.toLowerCase().contains("lantai 1")) {
            isFloor1 = true;
            totalCapacity = 11;
        } else {
            isFloor1 = false;
            totalCapacity = 10;
        }

        currentSlotStatus = new int[totalCapacity];
        Arrays.fill(currentSlotStatus, 0);

        tvAreaName.setText(areaName);
        tvDistance.setText(distance + " dari lokasi Anda");
        tvTotalCapacity.setText(String.valueOf(totalCapacity));
    }

    private void startRealtimeUpdate() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                new Thread(() -> {
                    try {
                        Connection conn = KoneksiDatabase.connect();
                        if (conn == null) return;

                        String sql;
                        if (isFloor1) {
                            sql = "SELECT status FROM slots WHERE id BETWEEN 1 AND 11 ORDER BY id ASC";
                        } else {
                            sql = "SELECT status FROM slots WHERE id BETWEEN 12 AND 21 ORDER BY id ASC";
                        }

                        PreparedStatement stmt = conn.prepareStatement(sql);
                        ResultSet rs = stmt.executeQuery();

                        int index = 0;
                        while (rs.next() && index < totalCapacity) {
                            currentSlotStatus[index] = rs.getInt("status");
                            index++;
                        }
                        conn.close();

                        runOnUiThread(() -> {
                            updateStats();
                            drawParkingSlots();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateRunnable);
    }

    private void updateStats() {
        int available = 0;
        for (int status : currentSlotStatus) {
            if (status == 0) available++;
        }

        tvAvailableSlots.setText(String.valueOf(available));
        float percentage = (float) available / totalCapacity * 100;
        tvPercentage.setText(String.format("%.0f%%", percentage));
        progressBar.setProgress((int) percentage);

        int color;
        if (percentage > 40) color = Color.parseColor("#10B981");
        else if (percentage >= 15) color = Color.parseColor("#F59E0B");
        else color = Color.parseColor("#EF4444");

        progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        tvPercentage.setTextColor(color);
    }

    // === UPDATE: Logika saat slot diklik ===
    private void drawParkingSlots() {
        gridSlots.removeAllViews();
        int columnCount = (int) Math.ceil(totalCapacity / 2.0);
        gridSlots.setColumnCount(columnCount);

        for (int i = 0; i < totalCapacity; i++) {
            TextView slotView = new TextView(this);
            int finalIndex = i;
            int size = (int) (40 * getResources().getDisplayMetrics().density);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size; params.height = size;
            params.setMargins(8, 8, 8, 8);
            slotView.setLayoutParams(params);
            slotView.setGravity(Gravity.CENTER);
            slotView.setText(String.valueOf(i + 1));
            slotView.setTextColor(Color.WHITE);
            slotView.setTypeface(null, Typeface.BOLD);

            int status = currentSlotStatus[i];

            // Warna Slot
            if (i == selectedSlotIndex) {
                // Sedang dipilih (Biru)
                slotView.setBackgroundResource(R.drawable.bg_button);
                slotView.getBackground().setColorFilter(Color.parseColor("#3B82F6"), PorterDuff.Mode.SRC_IN);
            } else if (status == 2) {
                slotView.setBackgroundResource(R.drawable.bg_button);
                slotView.getBackground().setColorFilter(Color.parseColor("#FBBF24"), PorterDuff.Mode.SRC_IN);
            } else if (status == 1) {
                slotView.setBackgroundResource(R.drawable.bg_button);
                slotView.getBackground().setColorFilter(Color.parseColor("#EF4444"), PorterDuff.Mode.SRC_IN);
            } else {
                slotView.setBackgroundResource(R.drawable.bg_button);
                slotView.getBackground().setColorFilter(Color.parseColor("#10B981"), PorterDuff.Mode.SRC_IN);
            }

            // Saat Slot Diklik
            slotView.setOnClickListener(v -> {
                selectedSlotIndex = finalIndex;
                drawParkingSlots(); // Refresh warna seleksi (Biru)

                // Cek Status Slot untuk update Tombol
                checkSlotOwnership(finalIndex, currentSlotStatus[finalIndex]);
            });

            gridSlots.addView(slotView);
        }
    }

    // === FITUR BARU: CEK KEPEMILIKAN SLOT ===
    private void checkSlotOwnership(int slotIndex, int status) {
        if (status == 0) {
            // Slot Kosong -> Bisa Booking
            updateButtonState(1, "Booking Slot Ini");
            return;
        }

        if (status == 1) {
            // Slot Terisi -> Gak bisa apa-apa
            updateButtonState(0, "Slot Sudah Terisi");
            return;
        }

        // Kalau Status 2 (Kuning), Cek ke Database: Punya siapa?
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String myUsername = prefs.getString("username", "User");
        int dbSlotId = isFloor1 ? (slotIndex + 1) : (slotIndex + 1 + 11);

        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn == null) return;

                String sql = "SELECT booked_by FROM slots WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, dbSlotId);
                ResultSet rs = stmt.executeQuery();

                boolean isMine = false;
                if (rs.next()) {
                    String bookedBy = rs.getString("booked_by");
                    if (bookedBy != null && bookedBy.equals(myUsername)) {
                        isMine = true;
                    }
                }
                conn.close();

                final boolean finalIsMine = isMine;
                runOnUiThread(() -> {
                    if (finalIsMine) {
                        updateButtonState(2, "Batalkan Booking"); // Tombol Merah
                    } else {
                        updateButtonState(0, "Dibooking Orang Lain"); // Tombol Mati
                    }
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // Fungsi Ganti Tampilan Tombol
    private void updateButtonState(int state, String text) {
        buttonActionState = state;
        btnBooking.setText(text);

        if (state == 0) { // Disabled
            btnBooking.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.GRAY));
            btnBooking.setEnabled(false);
        } else if (state == 1) { // Booking (Hijau)
            btnBooking.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));
            btnBooking.setEnabled(true);
        } else if (state == 2) { // Cancel (Merah)
            btnBooking.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444")));
            btnBooking.setEnabled(true);
        }
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        btnBooking.setOnClickListener(v -> {
            if (buttonActionState == 1) {
                // Mode Booking
                checkAndShowBookingDialog();
            } else if (buttonActionState == 2) {
                // Mode Cancel
                showCancelConfirmation();
            }
        });

        btnNavigate.setOnClickListener(v ->
                Toast.makeText(this, "Navigasi dimulai...", Toast.LENGTH_SHORT).show());
    }

    // === LOGIKA BOOKING (Sama seperti sebelumnya) ===
    private void checkAndShowBookingDialog() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = prefs.getString("username", "User");

        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn == null) return;

                String sqlCheck = "SELECT COUNT(*) FROM slots WHERE booked_by = ? AND status = 2";
                PreparedStatement stmt = conn.prepareStatement(sqlCheck);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                boolean hasBooking = false;
                if (rs.next() && rs.getInt(1) > 0) hasBooking = true;
                conn.close();

                final boolean finalHasBooking = hasBooking;
                runOnUiThread(() -> {
                    if (finalHasBooking) {
                        new AlertDialog.Builder(this)
                                .setTitle("Gagal Booking")
                                .setMessage("Anda sudah punya bookingan aktif. Batalkan yang lama dulu!")
                                .setPositiveButton("OK", null).show();
                    } else {
                        showBookingConfirmation();
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void showBookingConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Booking")
                .setMessage("Booking slot ini selama 15 menit?")
                .setPositiveButton("Ya", (dialog, which) -> performFullBookingProcess())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void performFullBookingProcess() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        int dbSlotId = isFloor1 ? (selectedSlotIndex + 1) : (selectedSlotIndex + 1 + 11);
        String lokasiStr = "Lantai " + (isFloor1 ? "1" : "2") + " - Slot " + (selectedSlotIndex + 1);

        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn == null) return;
                conn.setAutoCommit(false);

                // Update Slot
                String sqlUpdate = "UPDATE slots SET status = 2, booked_by = ? WHERE id = ?";
                PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
                stmtUpdate.setString(1, username);
                stmtUpdate.setInt(2, dbSlotId);
                stmtUpdate.executeUpdate();

                // Insert Riwayat & Notifikasi
                String sqlRiwayat = "INSERT INTO riwayat (username, lokasi, status) VALUES (?, ?, 'Booking Aktif')";
                PreparedStatement stmtRiwayat = conn.prepareStatement(sqlRiwayat);
                stmtRiwayat.setString(1, username);
                stmtRiwayat.setString(2, lokasiStr);
                stmtRiwayat.executeUpdate();

                String sqlNotif = "INSERT INTO notifikasi (username, judul, pesan) VALUES (?, ?, ?)";
                PreparedStatement stmtNotif = conn.prepareStatement(sqlNotif);
                stmtNotif.setString(1, username);
                stmtNotif.setString(2, "Booking Berhasil");
                stmtNotif.setString(3, "Anda berhasil booking " + lokasiStr);
                stmtNotif.executeUpdate();

                conn.commit();
                conn.close();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Booking Berhasil!", Toast.LENGTH_SHORT).show();
                    // Update UI manual biar cepet
                    currentSlotStatus[selectedSlotIndex] = 2;
                    drawParkingSlots();
                    checkSlotOwnership(selectedSlotIndex, 2); // Refresh tombol jadi Cancel

                    startBookingTimeout(dbSlotId, selectedSlotIndex);
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // === FITUR BARU: BATALKAN BOOKING ===
    private void showCancelConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Batalkan Booking?")
                .setMessage("Apakah Anda yakin ingin membatalkan booking ini?")
                .setPositiveButton("Ya, Batalkan", (dialog, which) -> performCancelBooking())
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void performCancelBooking() {
        int dbSlotId = isFloor1 ? (selectedSlotIndex + 1) : (selectedSlotIndex + 1 + 11);
        String lokasiStr = "Lantai " + (isFloor1 ? "1" : "2") + " - Slot " + (selectedSlotIndex + 1);
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = prefs.getString("username", "User");

        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn == null) return;
                conn.setAutoCommit(false);

                // 1. Reset Slot jadi 0 (Kosong)
                String sqlUpdate = "UPDATE slots SET status = 0, booked_by = NULL WHERE id = ?";
                PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
                stmtUpdate.setInt(1, dbSlotId);
                stmtUpdate.executeUpdate();

                // 2. Catat di Riwayat (Dibatalkan)
                String sqlRiwayat = "INSERT INTO riwayat (username, lokasi, status) VALUES (?, ?, 'Dibatalkan User')";
                PreparedStatement stmtRiwayat = conn.prepareStatement(sqlRiwayat);
                stmtRiwayat.setString(1, username);
                stmtRiwayat.setString(2, lokasiStr);
                stmtRiwayat.executeUpdate();

                conn.commit();
                conn.close();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Booking Dibatalkan.", Toast.LENGTH_SHORT).show();
                    // Update UI Manual
                    currentSlotStatus[selectedSlotIndex] = 0;
                    drawParkingSlots();
                    updateButtonState(1, "Booking Slot Ini"); // Balik jadi tombol booking
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // === AUTO TIMEOUT ===
    private void startBookingTimeout(int dbId, int slotIdx) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            new Thread(() -> {
                try {
                    Connection conn = KoneksiDatabase.connect();
                    if (conn != null) {
                        String sqlCheck = "SELECT status FROM slots WHERE id = ?";
                        PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck);
                        stmtCheck.setInt(1, dbId);
                        ResultSet rs = stmtCheck.executeQuery();

                        if (rs.next() && rs.getInt("status") == 2) {
                            String sqlReset = "UPDATE slots SET status = 0, booked_by = NULL WHERE id = ?";
                            PreparedStatement stmtReset = conn.prepareStatement(sqlReset);
                            stmtReset.setInt(1, dbId);
                            stmtReset.executeUpdate();

                            runOnUiThread(() -> {
                                Toast.makeText(this, "Waktu Habis! Slot dilepas.", Toast.LENGTH_LONG).show();
                                if (slotIdx >= 0 && slotIdx < currentSlotStatus.length) {
                                    currentSlotStatus[slotIdx] = 0;
                                    drawParkingSlots();
                                    updateStats();
                                }
                            });
                        }
                        conn.close();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }, BOOKING_TIMEOUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
}