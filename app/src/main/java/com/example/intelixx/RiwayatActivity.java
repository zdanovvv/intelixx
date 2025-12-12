package com.example.intelixx;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class RiwayatActivity extends AppCompatActivity {

    private ListView lvRiwayat;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        lvRiwayat = findViewById(R.id.lvRiwayat);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        loadHistoryData();
    }

    private void loadHistoryData() {
        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        new Thread(() -> {
            ArrayList<String> dataList = new ArrayList<>();
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn == null) return;

                // Ambil data riwayat user, urutkan dari yang terbaru
                String sql = "SELECT lokasi, to_char(waktu, 'DD Mon YYYY HH24:MI') as tgl, status FROM riwayat WHERE username = ? ORDER BY id DESC";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String lokasi = rs.getString("lokasi");
                    String tgl = rs.getString("tgl");
                    String status = rs.getString("status");

                    // Format tampilan list:
                    // 12 Dec 2025 14:00 | Lantai 1 - Slot 5 [Selesai]
                    dataList.add(tgl + "\n" + lokasi + "  •  " + status);
                }
                conn.close();

                runOnUiThread(() -> {
                    if (dataList.isEmpty()) {
                        dataList.add("Belum ada riwayat parkir.");
                    }
                    // Tampilkan ke ListView sederhana
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
                    lvRiwayat.setAdapter(adapter);
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}