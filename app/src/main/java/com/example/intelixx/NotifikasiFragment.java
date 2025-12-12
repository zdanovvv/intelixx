package com.example.intelixx;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NotifikasiFragment extends Fragment {

    private ListView lvNotifikasi;
    private TextView tvEmpty, tvMarkAllRead;

    class NotifItem {
        String pesan, waktu;
        // Kita gabung Judul+Pesan jadi satu biar simpel kayak desainmu
        public NotifItem(String p, String w) { pesan=p; waktu=w; }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifikasi, container, false);

        lvNotifikasi = view.findViewById(R.id.lvNotifikasi);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvMarkAllRead = view.findViewById(R.id.tvMarkAllRead);

        // Load Data
        loadNotifications();

        // Tombol Tandai Baca (Dummy Action)
        tvMarkAllRead.setOnClickListener(v ->
                Toast.makeText(getContext(), "Semua notifikasi ditandai sudah dibaca", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void loadNotifications() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username == null) return;

        new Thread(() -> {
            List<NotifItem> dataList = new ArrayList<>();
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn == null) return;

                // Ambil data (Judul + Pesan digabung biar mirip desain)
                String sql = "SELECT judul, pesan, to_char(waktu, 'HH24:MI - DD Mon') as tgl FROM notifikasi WHERE username = ? ORDER BY id DESC";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String fullPesan = rs.getString("judul") + ": " + rs.getString("pesan");
                    String waktu = "⏱ " + rs.getString("tgl");
                    dataList.add(new NotifItem(fullPesan, waktu));
                }
                conn.close();

                getActivity().runOnUiThread(() -> {
                    if (dataList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        lvNotifikasi.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        lvNotifikasi.setVisibility(View.VISIBLE);
                        NotifAdapter adapter = new NotifAdapter(getContext(), dataList);
                        lvNotifikasi.setAdapter(adapter);
                    }
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // Fungsi Static untuk kirim notif dari Activity lain
    public static void createNotification(String username, String judul, String pesan) {
        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn != null) {
                    String sql = "INSERT INTO notifikasi (username, judul, pesan) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, username);
                    stmt.setString(2, judul);
                    stmt.setString(3, pesan);
                    stmt.executeUpdate();
                    conn.close();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // Adapter Custom
    class NotifAdapter extends ArrayAdapter<NotifItem> {
        public NotifAdapter(@NonNull Context context, List<NotifItem> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notifikasi, parent, false);
            }

            NotifItem item = getItem(position);

            TextView tvPesan = convertView.findViewById(R.id.tvPesan);
            TextView tvWaktu = convertView.findViewById(R.id.tvWaktu);

            tvPesan.setText(item.pesan);
            tvWaktu.setText(item.waktu);

            return convertView;
        }
    }
}