package com.example.intelixx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences; // PENTING
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ProfilFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        TextView btnUpdateVehicle = view.findViewById(R.id.btnUpdateVehicle);
        TextView btnHistory = view.findViewById(R.id.btnHistory);
        TextView btnSettings = view.findViewById(R.id.btnSettings);
        TextView btnLogout = view.findViewById(R.id.btnLogout);

        btnUpdateVehicle.setOnClickListener(v -> showToast("Fitur ubah data akan segera hadir"));
        btnHistory.setOnClickListener(v -> showToast("Fitur riwayat parkir akan segera hadir"));
        btnSettings.setOnClickListener(v -> showToast("Fitur pengaturan akan segera hadir"));

        btnLogout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });

        return view;
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin keluar dari akun?")
                .setPositiveButton("Ya, Keluar", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Batal", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void performLogout() {
        showToast("Logout Berhasil!");

        // --- HAPUS SESSION LOGIN ---
        if (getActivity() != null) {
            // Gunakan nama "UserSession" yang SAMA PERSIS dengan di LoginActivity
            SharedPreferences preferences = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear(); // Hapus semua data login
            editor.apply();
        }

        // Pindah ke LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}