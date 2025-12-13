package com.example.intelixx;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
        boolean isUnread;
        public NotifItem(String p, String w, boolean u) { pesan=p; waktu=w; isUnread=u; }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifikasi, container, false);

        lvNotifikasi = view.findViewById(R.id.lvNotifikasi);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        // === PASANG FOOTER (TOMBOL & TIPS) AGAR NEMPEL LIST ===
        View footerView = inflater.inflate(R.layout.layout_footer_notifikasi, lvNotifikasi, false);
        lvNotifikasi.addFooterView(footerView, null, false);

        // Ambil tombol dari Footer
        tvMarkAllRead = footerView.findViewById(R.id.tvMarkAllRead);

        loadNotifications();

        tvMarkAllRead.setOnClickListener(v -> markAllAsRead());

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

                String sql = "SELECT judul, pesan, is_read, to_char(waktu, 'HH24:MI - DD Mon') as tgl " +
                        "FROM notifikasi " +
                        "WHERE username = ? " +
                        "ORDER BY id DESC " +
                        "LIMIT 5"; // <-- INI BATASNYA (Cuma ambil 5 terbaru)
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String fullPesan = rs.getString("judul") + ": " + rs.getString("pesan");
                    String waktu = "⏱ " + rs.getString("tgl");
                    boolean unread = rs.getInt("is_read") == 0;
                    dataList.add(new NotifItem(fullPesan, waktu, unread));
                }
                conn.close();

                getActivity().runOnUiThread(() -> {
                    if (dataList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        // Kalau kosong, sembunyikan list tapi footer tetep mau dilihat?
                        // Biasanya footer juga dihide, tapi kita hide listnya aja isinya.
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                    // Set Adapter
                    NotifAdapter adapter = new NotifAdapter(getContext(), dataList);
                    lvNotifikasi.setAdapter(adapter);

                    // Update Badge di Menu Bawah
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateNotificationBadge();
                    }
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void markAllAsRead() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn == null) return;

                String sql = "UPDATE notifikasi SET is_read = 1 WHERE username = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                int rows = stmt.executeUpdate();
                conn.close();

                getActivity().runOnUiThread(() -> {
                    if (rows > 0) {
                        Toast.makeText(getContext(), "Semua ditandai sudah dibaca", Toast.LENGTH_SHORT).show();
                        loadNotifications(); // Refresh list (hilangkan titik biru)
                    } else {
                        Toast.makeText(getContext(), "Tidak ada notifikasi baru", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // Fungsi Static untuk membuat notif
    public static void createNotification(String username, String judul, String pesan) {
        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn != null) {
                    String sql = "INSERT INTO notifikasi (username, judul, pesan, is_read) VALUES (?, ?, ?, 0)";
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
            View indicator = convertView.findViewById(R.id.indicatorUnread);

            tvPesan.setText(item.pesan);
            tvWaktu.setText(item.waktu);

            if (item.isUnread) {
                indicator.setVisibility(View.VISIBLE);
                tvPesan.setTextColor(Color.parseColor("#111827"));
                tvPesan.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                indicator.setVisibility(View.INVISIBLE);
                tvPesan.setTextColor(Color.parseColor("#6B7280"));
                tvPesan.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            return convertView;
        }
    }
}