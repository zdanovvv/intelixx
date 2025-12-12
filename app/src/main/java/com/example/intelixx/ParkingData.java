package com.example.intelixx;

import android.os.Handler;
import android.os.Looper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ParkingData {
    // Array penampung data untuk UI
    public static int[] slotsLantai1 = new int[11];
    public static int[] slotsLantai2 = new int[10];

    public static final int CAP_LANTAI_1 = 11;
    public static final int CAP_LANTAI_2 = 10;
    public static final long BOOKING_TIMEOUT = 900000; // 15 Menit

    private static boolean isRunning = false;
    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void startSimulation() {
        if (isRunning) return;
        isRunning = true;

        // Jalankan sinkronisasi database terus-menerus
        syncDatabaseLoop();
    }

    // FUNGSI 1: BACA DATA DARI POSTGRESQL
    private static void syncDatabaseLoop() {
        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn != null) {
                    Statement stmt = conn.createStatement();

                    // Ambil ID dan STATUS, urutkan biar Lantai 1 selalu duluan
                    String sql = "SELECT id, status FROM slots ORDER BY id ASC";
                    ResultSet rs = stmt.executeQuery(sql);

                    int indexL1 = 0;
                    int indexL2 = 0;

                    while (rs.next()) {
                        // ID tidak perlu disimpan, kita pakai urutannya saja
                        int status = rs.getInt("status");

                        // 11 Data pertama masuk Lantai 1
                        if (indexL1 < CAP_LANTAI_1) {
                            slotsLantai1[indexL1] = status;
                            indexL1++;
                        }
                        // Sisanya masuk Lantai 2
                        else if (indexL2 < CAP_LANTAI_2) {
                            slotsLantai2[indexL2] = status;
                            indexL2++;
                        }
                    }
                    conn.close();
                } else {
                    System.out.println("Gagal konek ke database!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Ulangi cek database setiap 1 detik
            handler.postDelayed(ParkingData::syncDatabaseLoop, 1000);

        }).start();
    }

    // FUNGSI 2: UPDATE DATA KE POSTGRESQL (SAAT BOOKING)
    public static void updateSlotToDB(int floor, int slotIndex, int newStatus) {
        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn != null) {
                    // HITUNG ID DATABASE:
                    // Jika Lantai 1: ID = Index + 1 (Contoh: Index 0 -> ID 1)
                    // Jika Lantai 2: ID = Index + 1 + 11 (Contoh: Index 0 -> ID 12)
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

    // FUNGSI BOOKING DENGAN TIMEOUT
    public static void bookSlot(int floor, int slotIndex) {
        // 1. Update ke Database (Jadi Kuning/2)
        updateSlotToDB(floor, slotIndex, 2);

        // 2. Pasang Timer 15 Menit
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Cek status terakhir di array lokal (karena array ini sinkron sama DB)
            int[] targetFloor = (floor == 1) ? slotsLantai1 : slotsLantai2;

            // Kalau masih 'Booking' (belum check-in jadi Merah), batalkan otomatis
            if (targetFloor[slotIndex] == 2) {
                updateSlotToDB(floor, slotIndex, 0); // Balikin ke Hijau di DB
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