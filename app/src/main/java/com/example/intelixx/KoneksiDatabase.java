package com.example.intelixx;

import android.os.StrictMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KoneksiDatabase {

    // Ganti dengan data PostgreSQL kamu
    // 10.0.2.2 adalah IP localhost khusus untuk Emulator Android Studio
    private static final String IP = "172.25.0.60"; // 172.25.0.60
    private static final String PORT = "5432";
    private static final String DB_NAME = "smartparking"; // Ganti ini!
    private static final String USER = "postgres"; // Default user postgres
    private static final String PASS = "123456"; // Password postgres kamu

    public static Connection connect() {
        Connection conn = null;
        String connURL = "jdbc:postgresql://" + IP + ":" + PORT + "/" + DB_NAME;

        try {
            // Izin untuk akses network di Main Thread (Khusus Prototype/Belajar)
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Load Driver PostgreSQL
            Class.forName("org.postgresql.Driver");

            // Buka Koneksi
            conn = DriverManager.getConnection(connURL, USER, PASS);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }
}