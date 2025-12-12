package com.example.intelixx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class NotifikasiFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifikasi, container, false);

        TextView tvMarkAllRead = view.findViewById(R.id.tvMarkAllRead);
        tvMarkAllRead.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Semua notifikasi ditandai sudah dibaca", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}