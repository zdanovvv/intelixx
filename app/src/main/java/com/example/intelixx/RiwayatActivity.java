package com.example.intelixx;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RiwayatActivity extends AppCompatActivity {

    private ListView lvRiwayat;
    private ImageView btnBack;
    private TextView tvEmpty;

    // Model Data
    class RiwayatItem {
        String lokasi, waktu, status;
        public RiwayatItem(String l, String w, String s) { lokasi=l; waktu=w; status=s; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        lvRiwayat = findViewById(R.id.lvRiwayat);
        btnBack = findViewById(R.id.btnBack);
        // Tambahkan ID tvEmpty di XML activity_riwayat.xml kalau belum ada
        // Kalau males ubah XML, bisa pakai footer view

        btnBack.setOnClickListener(v -> finish());

        loadHistoryData();
    }

    private void loadHistoryData() {
        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        new Thread(() -> {
            List<RiwayatItem> dataList = new ArrayList<>();
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn == null) return;

                String sql = "SELECT lokasi, to_char(waktu, 'DD Mon YYYY • HH24:MI') as tgl, status FROM riwayat WHERE username = ? ORDER BY id DESC";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    dataList.add(new RiwayatItem(
                            rs.getString("lokasi"),
                            rs.getString("tgl"),
                            rs.getString("status")
                    ));
                }
                conn.close();

                runOnUiThread(() -> {
                    // Pakai Custom Adapter Biar Cantik
                    RiwayatAdapter adapter = new RiwayatAdapter(this, dataList);
                    lvRiwayat.setAdapter(adapter);
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // === ADAPTER KHUSUS BIAR STATUSNYA WARNA-WARNI ===
    class RiwayatAdapter extends ArrayAdapter<RiwayatItem> {
        public RiwayatAdapter(@NonNull Context context, List<RiwayatItem> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_riwayat_card, parent, false);
            }

            RiwayatItem item = getItem(position);

            TextView tvLokasi = convertView.findViewById(R.id.tvLokasi);
            TextView tvWaktu = convertView.findViewById(R.id.tvWaktu);
            TextView tvStatus = convertView.findViewById(R.id.tvStatus);

            tvLokasi.setText(item.lokasi);
            tvWaktu.setText(item.waktu);
            tvStatus.setText(item.status);

            // LOGIKA WARNA STATUS
            String st = item.status.toLowerCase();
            if (st.contains("aktif") || st.contains("booking")) {
                tvStatus.getBackground().setColorFilter(Color.parseColor("#F59E0B"), PorterDuff.Mode.SRC_IN); // Kuning
                tvStatus.setText("Sedang Berjalan");
            } else if (st.contains("batal")) {
                tvStatus.getBackground().setColorFilter(Color.parseColor("#EF4444"), PorterDuff.Mode.SRC_IN); // Merah
                tvStatus.setText("Dibatalkan");
            } else {
                tvStatus.getBackground().setColorFilter(Color.parseColor("#10B981"), PorterDuff.Mode.SRC_IN); // Hijau
                tvStatus.setText("Selesai");
            }

            return convertView;
        }
    }
}