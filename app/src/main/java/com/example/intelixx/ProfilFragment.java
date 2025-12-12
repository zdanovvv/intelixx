package com.example.intelixx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class ProfilFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        view.findViewById(R.id.btnUpdateVehicle).setOnClickListener(v -> showToast("Fitur ubah data akan segera hadir"));
        view.findViewById(R.id.btnHistory).setOnClickListener(v -> showToast("Fitur riwayat parkir akan segera hadir"));
        view.findViewById(R.id.btnSettings).setOnClickListener(v -> showToast("Fitur pengaturan akan segera hadir"));
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> showToast("Logout berhasil"));

        return view;
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}