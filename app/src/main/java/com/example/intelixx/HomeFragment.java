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
import com.example.intelixx.ParkingArea;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    private TextView tvAvailableSlots, tvTotalCapacity;
    private RecyclerView recyclerParkingAreas;
    private ParkingAreaAdapter adapter;
    private List<ParkingArea> parkingAreas;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    private Runnable updateRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupData();
        setupRecyclerView();
        startRealtimeSimulation();

        return view;
    }

    private void initViews(View view) {
        tvAvailableSlots = view.findViewById(R.id.tvAvailableSlots);
        tvTotalCapacity = view.findViewById(R.id.tvTotalCapacity);
        recyclerParkingAreas = view.findViewById(R.id.recyclerParkingAreas);
    }

    private void setupData() {
        parkingAreas = new ArrayList<>();

        // --- DATA DIUBAH SESUAI REQUEST ---
        // Lantai 1: Kapasitas 12, Jarak 50m
        parkingAreas.add(new ParkingArea("Area Parkir Lantai 1", "50m", 5, 11));

        // Lantai 2: Kapasitas 10, Jarak 100m
        parkingAreas.add(new ParkingArea("Area Parkir Lantai 2", "100m", 3, 10));

        updateTotalStats();
    }

    private void setupRecyclerView() {
        adapter = new ParkingAreaAdapter(parkingAreas);
        recyclerParkingAreas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerParkingAreas.setAdapter(adapter);
        if (recyclerParkingAreas.getItemAnimator() != null) {
            recyclerParkingAreas.getItemAnimator().setChangeDuration(0);
        }
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

    private void startRealtimeSimulation() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                int randomIndex = random.nextInt(parkingAreas.size());
                ParkingArea selectedArea = parkingAreas.get(randomIndex);

                // Ubah jumlah slot acak sesuai kapasitas baru
                int newAvailable = random.nextInt(selectedArea.getTotalCapacity() + 1);
                selectedArea.setAvailableSlots(newAvailable);

                adapter.notifyItemChanged(randomIndex);
                updateTotalStats();

                int delay = 2000 + random.nextInt(3000);
                handler.postDelayed(this, delay);
            }
        };
        handler.post(updateRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateRunnable);
    }

    class ParkingAreaAdapter extends RecyclerView.Adapter<ParkingAreaAdapter.ViewHolder> {
        private List<ParkingArea> areas;

        ParkingAreaAdapter(List<ParkingArea> areas) {
            this.areas = areas;
        }

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
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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