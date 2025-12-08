package com.example.intelixx;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparking.models.ParkingArea;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvAvailableSlots, tvTotalCapacity;
    private RecyclerView recyclerParkingAreas;
    private LinearLayout navHome, navPeta, navNotifikasi, navProfil;
    private List<ParkingArea> parkingAreas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupData();
        setupRecyclerView();
        setupNavigation();
    }

    private void initViews() {
        tvAvailableSlots = findViewById(R.id.tvAvailableSlots);
        tvTotalCapacity = findViewById(R.id.tvTotalCapacity);
        recyclerParkingAreas = findViewById(R.id.recyclerParkingAreas);
        navHome = findViewById(R.id.navHome);
        navPeta = findViewById(R.id.navPeta);
        navNotifikasi = findViewById(R.id.navNotifikasi);
        navProfil = findViewById(R.id.navProfil);
    }

    private void setupData() {
        parkingAreas = new ArrayList<>();
        parkingAreas.add(new ParkingArea("Area Parkir Gedung A", "200m", 12, 50));
        parkingAreas.add(new ParkingArea("Area Parkir Gedung B", "350m", 8, 40));
        parkingAreas.add(new ParkingArea("Area Parkir Sporthall", "180m", 18, 30));
        parkingAreas.add(new ParkingArea("Area Parkir Belakang", "450m", 3, 25));

        int totalAvailable = 0;
        int totalCapacity = 0;
        for (ParkingArea area : parkingAreas) {
            totalAvailable += area.getAvailableSlots();
            totalCapacity += area.getTotalCapacity();
        }

        tvAvailableSlots.setText(String.valueOf(totalAvailable));
        tvTotalCapacity.setText(String.valueOf(totalCapacity));
    }

    private void setupRecyclerView() {
        ParkingAreaAdapter adapter = new ParkingAreaAdapter(parkingAreas);
        recyclerParkingAreas.setLayoutManager(new LinearLayoutManager(this));
        recyclerParkingAreas.setAdapter(adapter);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            // Already on home
        });

        navPeta.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PetaActivity.class));
        });

        navNotifikasi.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NotifikasiActivity.class));
        });

        navProfil.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfilActivity.class));
        });
    }

    class ParkingAreaAdapter extends RecyclerView.Adapter<ParkingAreaAdapter.ViewHolder> {
        private List<ParkingArea> areas;

        ParkingAreaAdapter(List<ParkingArea> areas) {
            this.areas = areas;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_parking_area, parent, false);
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
            holder.progressBar.getProgressDrawable().setColorFilter(
                    area.getProgressColor(), PorterDuff.Mode.SRC_IN);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DetailAreaActivity.class);
                intent.putExtra("areaName", area.getName());
                intent.putExtra("distance", area.getDistance());
                intent.putExtra("available", area.getAvailableSlots());
                intent.putExtra("capacity", area.getTotalCapacity());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return areas.size();
        }

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