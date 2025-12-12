package com.example.intelixx;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PetaFragment extends Fragment {

    private GridLayout gridLantai2, gridLantai1;
    private TextView tvInfoLantai2, tvInfoLantai1;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_peta, container, false);

        gridLantai2 = view.findViewById(R.id.gridLantai2);
        gridLantai1 = view.findViewById(R.id.gridLantai1);
        tvInfoLantai2 = view.findViewById(R.id.tvInfoLantai2);
        tvInfoLantai1 = view.findViewById(R.id.tvInfoLantai1);

        startRealtimeUpdate();

        return view;
    }

    private void startRealtimeUpdate() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                // Update Lantai 1 (Sekarang di Posisi Atas Layout XML)
                drawFloorGrid(gridLantai1, ParkingData.slotsLantai1, tvInfoLantai1);

                // Update Lantai 2 (Sekarang di Posisi Bawah Layout XML)
                drawFloorGrid(gridLantai2, ParkingData.slotsLantai2, tvInfoLantai2);

                handler.postDelayed(this, 1000); // Update tiap 1 detik
            }
        };
        handler.post(updateRunnable);
    }

    private void drawFloorGrid(GridLayout grid, int[] slotStatus, TextView tvInfo) {
        grid.removeAllViews();

        int availableCount = ParkingData.getAvailableCount(slotStatus);
        tvInfo.setText(availableCount + " Slot Tersedia");

        for (int i = 0; i < slotStatus.length; i++) {
            // Container Slot
            LinearLayout slotContainer = new LinearLayout(getContext());

            // Ukuran Kotak (Clean UI)
            int size = (int) (42 * getResources().getDisplayMetrics().density);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = (int) (size * 1.3);
            params.setMargins(8, 8, 8, 8);
            slotContainer.setLayoutParams(params);
            slotContainer.setGravity(Gravity.CENTER);

            int status = slotStatus[i];

            if (status == 2) {
                // === BOOKING (KUNING) ===
                ImageView bookingIcon = new ImageView(getContext());
                bookingIcon.setImageResource(R.drawable.ic_car); // Ikon mobil
                bookingIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

                // Background Kuning
                slotContainer.setBackgroundResource(R.drawable.bg_card);
                slotContainer.getBackground().setColorFilter(Color.parseColor("#FBBF24"), PorterDuff.Mode.SRC_IN);

                slotContainer.addView(bookingIcon);

            } else if (status == 1) {
                // === TERISI (MERAH) ===
                ImageView carIcon = new ImageView(getContext());
                carIcon.setImageResource(R.drawable.ic_car);
                carIcon.setColorFilter(Color.parseColor("#EF4444"), PorterDuff.Mode.SRC_IN);

                // Background Merah Muda
                slotContainer.setBackgroundResource(R.drawable.bg_card);
                slotContainer.getBackground().setColorFilter(Color.parseColor("#FEE2E2"), PorterDuff.Mode.SRC_IN);

                slotContainer.addView(carIcon);

            } else {
                // === KOSONG (PUTIH/ABU) ===
                TextView textP = new TextView(getContext());
                textP.setText("P" + (i + 1));
                textP.setTextColor(Color.parseColor("#111827")); // Hitam
                textP.setTextSize(12);
                textP.setTypeface(null, android.graphics.Typeface.BOLD);

                // Background Abu-abu
                slotContainer.setBackgroundResource(R.drawable.bg_input_field);
                slotContainer.getBackground().setColorFilter(null);

                slotContainer.addView(textP);
            }

            grid.addView(slotContainer);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateRunnable);
    }
}