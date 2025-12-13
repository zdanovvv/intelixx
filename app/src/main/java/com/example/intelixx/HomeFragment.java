package com.example.intelixx;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvAvailableSlots, tvTotalCapacity;
    private RecyclerView recyclerParkingAreas;
    private ParkingAreaAdapter adapter;
    private List<ParkingArea> parkingAreas;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupData(); // Setup data awal
        setupRecyclerView();
        startRealtimeUpdate(); // Mulai update UI

        return view;
    }

    private void initViews(View view) {
        tvAvailableSlots = view.findViewById(R.id.tvAvailableSlots);
        tvTotalCapacity = view.findViewById(R.id.tvTotalCapacity);
        recyclerParkingAreas = view.findViewById(R.id.recyclerParkingAreas);
    }

    private void setupData() {
        parkingAreas = new ArrayList<>();
        // Inisialisasi list, data slot nanti di-update realtime
        parkingAreas.add(new ParkingArea("Area Parkir Lantai 1", "50m", 0, ParkingData.CAP_LANTAI_1));
        parkingAreas.add(new ParkingArea("Area Parkir Lantai 2", "100m", 0, ParkingData.CAP_LANTAI_2));
        updateTotalStats();
    }

    private void setupRecyclerView() {
        adapter = new ParkingAreaAdapter(parkingAreas);
        recyclerParkingAreas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerParkingAreas.setAdapter(adapter);
    }

    private void updateTotalStats() {
        int totalAvailable = 0;
        int totalCapacity = 0;
        for (ParkingArea area : parkingAreas) {
            totalAvailable += area.getAvailableSlots();
            totalCapacity += area.getTotalCapacity();
        }
        tvAvailableSlots.setText(String.valueOf(totalAvailable));
        tvTotalCapacity.setText(String.valueOf(totalCapacity));
    }

    private void startRealtimeUpdate() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (parkingAreas.size() >= 2) {
                    // Hitung dulu jumlah true di array
                    int count1 = ParkingData.getAvailableCount(ParkingData.slotsLantai1);
                    int count2 = ParkingData.getAvailableCount(ParkingData.slotsLantai2);

                    parkingAreas.get(0).setAvailableSlots(count1);
                    parkingAreas.get(1).setAvailableSlots(count2);

                    adapter.notifyDataSetChanged();
                    updateTotalStats();
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateRunnable);
    }

    // --- ADAPTER TETAP SAMA ---
    class ParkingAreaAdapter extends RecyclerView.Adapter<ParkingAreaAdapter.ViewHolder> {
        private List<ParkingArea> areas;

        ParkingAreaAdapter(List<ParkingArea> areas) { this.areas = areas; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking_area, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ParkingArea area = areas.get(position);
            holder.tvAreaName.setText(area.getName());
            holder.tvDistance.setText(area.getDistance());
            holder.tvAvailable.setText(String.valueOf(area.getAvailableSlots()));
            holder.tvCapacity.setText("/ " + area.getTotalCapacity() + " tersedia");
            holder.progressBar.setProgress((int) area.getPercentage());
            holder.progressBar.getProgressDrawable().setColorFilter(area.getProgressColor(), PorterDuff.Mode.SRC_IN);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DetailAreaActivity.class);
                intent.putExtra("areaName", area.getName());
                intent.putExtra("distance", area.getDistance());
                intent.putExtra("available", area.getAvailableSlots());
                intent.putExtra("capacity", area.getTotalCapacity());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return areas.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAreaName, tvDistance, tvAvailable, tvCapacity;
            ProgressBar progressBar;
            ViewHolder(View itemView) {
                super(itemView);
                tvAreaName = itemView.findViewById(R.id.tvAreaName);
                tvDistance = itemView.findViewById(R.id.tvDistance);
                tvAvailable = itemView.findViewById(R.id.tvAvailable);
                tvCapacity = itemView.findViewById(R.id.tvCapacity);
                progressBar = itemView.findViewById(R.id.progressBar);
            }
        }
    }
}