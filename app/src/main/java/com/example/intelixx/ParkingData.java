package com.example.intelixx;

import android.os.Handler;
import android.os.Looper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ParkingData {
    public static int[] slotsLantai1 = new int[11];
    public static int[] slotsLantai2 = new int[10];

    public static final int CAP_LANTAI_1 = 11;
    public static final int CAP_LANTAI_2 = 10;

    // Waktu Booking 15 Menit (900.000 ms)
    public static final long BOOKING_TIMEOUT = 10;

    private static boolean isRunning = false;
    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void startSimulation() {
        if (isRunning) return;
        isRunning = true;
        syncDatabaseLoop(); // Mulai sinkronisasi DB
    }

    // Loop baca database tiap 1 detik
    private static void syncDatabaseLoop() {
        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn != null) {
                    Statement stmt = conn.createStatement();
                    String sql = "SELECT id, status FROM slots ORDER BY id ASC";
                    ResultSet rs = stmt.executeQuery(sql);

                    int indexL1 = 0;
                    int indexL2 = 0;

                    while (rs.next()) {
                        int status = rs.getInt("status");
                        // Masukkan ke array lokal
                        if (indexL1 < CAP_LANTAI_1) {
                            slotsLantai1[indexL1] = status;
                            indexL1++;
                        } else if (indexL2 < CAP_LANTAI_2) {
                            slotsLantai2[indexL2] = status;
                            indexL2++;
                        }
                    }
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Ulangi terus
            handler.postDelayed(ParkingData::syncDatabaseLoop, 1000);
        }).start();
    }

    // Update ke Database (Background)
    public static void updateSlotToDB(int floor, int slotIndex, int newStatus) {
        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn != null) {
                    int dbId = (floor == 1) ? (slotIndex + 1) : (slotIndex + 1 + CAP_LANTAI_1);
                    String sql = "UPDATE slots SET status = " + newStatus + " WHERE id = " + dbId;
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(sql);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Booking dengan Update Instan
    public static void bookSlot(int floor, int slotIndex) {
        // 1. Update Tampilan HP Dulu (Biar gak delay)
        if (floor == 1) slotsLantai1[slotIndex] = 2;
        else slotsLantai2[slotIndex] = 2;

        // 2. Update Database
        updateSlotToDB(floor, slotIndex, 2);

        // 3. Timer 15 Menit buat batalin otomatis
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            int[] targetFloor = (floor == 1) ? slotsLantai1 : slotsLantai2;
            // Kalau masih status booking (belum check-in), balikin hijau
            if (targetFloor[slotIndex] == 2) {
                if (floor == 1) slotsLantai1[slotIndex] = 0;
                else slotsLantai2[slotIndex] = 0;
                updateSlotToDB(floor, slotIndex, 0);
            }
        }, BOOKING_TIMEOUT);
    }

    public static int getAvailableCount(int[] slots) {
        int count = 0;
        for (int status : slots) {
            if (status == 0) count++;
        }
        return count;
    }
}