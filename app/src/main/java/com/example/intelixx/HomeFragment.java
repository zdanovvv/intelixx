package com.example.intelixx;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        setupData();
        setupRecyclerView();
        startRealtimeUpdate();

        return view;
    }

    private void initViews(View view) {
        tvAvailableSlots = view.findViewById(R.id.tvAvailableSlots);
        tvTotalCapacity = view.findViewById(R.id.tvTotalCapacity);
        recyclerParkingAreas = view.findViewById(R.id.recyclerParkingAreas);
    }

    private void setupData() {
        parkingAreas = new ArrayList<>();
        // Default data
        parkingAreas.add(new ParkingArea("Area Parkir Lantai 1", "50m", 0, 10)); // Kapasitas disesuaikan
        parkingAreas.add(new ParkingArea("Area Parkir Lantai 2", "100m", 0, 10));
    }

    private void setupRecyclerView() {
        adapter = new ParkingAreaAdapter(parkingAreas);
        recyclerParkingAreas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerParkingAreas.setAdapter(adapter);
    }

    private void startRealtimeUpdate() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                fetchDataFromApi(); // AMBIL DATA DARI PYTHON
                handler.postDelayed(this, 2000); // Update tiap 2 detik
            }
        };
        handler.post(updateRunnable);
    }

    private void fetchDataFromApi() {
        ApiClient.getApi().getSlots().enqueue(new Callback<List<Slot>>() {
            @Override
            public void onResponse(Call<List<Slot>> call, Response<List<Slot>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Slot> slots = response.body();

                    int countLantai1 = 0;
                    int countLantai2 = 0;
                    int capacity1 = 0;
                    int capacity2 = 0;

                    for (Slot s : slots) {
                        // Hitung kapasitas per lantai
                        if (s.floor == 1) capacity1++;
                        if (s.floor == 2) capacity2++;

                        // Hitung slot kosong (free)
                        if (s.status.equals("free")) {
                            if (s.floor == 1) countLantai1++;
                            else if (s.floor == 2) countLantai2++;
                        }
                    }

                    // Update UI List
                    if (parkingAreas.size() >= 2) {
                        ParkingArea p1 = parkingAreas.get(0);
                        p1.setAvailableSlots(countLantai1);
                        p1.setTotalCapacity(capacity1 > 0 ? capacity1 : 10); // Update kapasitas otomatis

                        ParkingArea p2 = parkingAreas.get(1);
                        p2.setAvailableSlots(countLantai2);
                        p2.setTotalCapacity(capacity2 > 0 ? capacity2 : 10);

                        adapter.notifyDataSetChanged();

                        // Update Header Total
                        tvAvailableSlots.setText(String.valueOf(countLantai1 + countLantai2));
                        tvTotalCapacity.setText(String.valueOf(capacity1 + capacity2));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Slot>> call, Throwable t) {
                Log.e("API_ERROR", "Gagal konek: " + t.getMessage());
                // Jangan tampilkan Toast terus menerus agar tidak mengganggu
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateRunnable);
    }

    // --- ADAPTER (Tidak Berubah) ---
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
            holder.progressBar.setMax(area.getTotalCapacity()); // Pastikan Max sesuai kapasitas
            holder.progressBar.setProgress(area.getAvailableSlots());

            // Ubah warna progress bar
            int color = area.getAvailableSlots() > 0 ? 0xFF4CAF50 : 0xFFFF0000; // Hijau jika ada, Merah jika habis
            holder.progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DetailAreaActivity.class);
                intent.putExtra("areaName", area.getName());
                // Kirim ID lantai agar DetailActivity tahu load slot lantai berapa
                intent.putExtra("floorId", position + 1);
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