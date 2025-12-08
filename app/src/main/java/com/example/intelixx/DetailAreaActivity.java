package com.example.intelixx;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailAreaActivity extends AppCompatActivity {

    private LinearLayout btnBack;
    private TextView tvAreaName, tvDistance, tvAvailableSlots, tvTotalCapacity;
    private TextView tvPercentage, btnNavigate;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_area);

        initViews();
        loadData();
        setupActions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvAreaName = findViewById(R.id.tvAreaName);
        tvDistance = findViewById(R.id.tvDistance);
        tvAvailableSlots = findViewById(R.id.tvAvailableSlots);
        tvTotalCapacity = findViewById(R.id.tvTotalCapacity);
        tvPercentage = findViewById(R.id.tvPercentage);
        progressBar = findViewById(R.id.progressBar);
        btnNavigate = findViewById(R.id.btnNavigate);
    }

    private void loadData() {
        String areaName = getIntent().getStringExtra("areaName");
        String distance = getIntent().getStringExtra("distance");
        int available = getIntent().getIntExtra("available", 0);
        int capacity = getIntent().getIntExtra("capacity", 0);

        tvAreaName.setText(areaName);
        tvDistance.setText(distance + " dari lokasi Anda");
        tvAvailableSlots.setText(String.valueOf(available));
        tvTotalCapacity.setText(String.valueOf(capacity));

        float percentage = (float) available / capacity * 100;
        tvPercentage.setText(String.format("%.0f%%", percentage));
        progressBar.setProgress((int) percentage);

        int color;
        if (percentage > 40) {
            color = android.graphics.Color.parseColor("#10B981"); // Green
        } else if (percentage >= 15) {
            color = android.graphics.Color.parseColor("#F59E0B"); // Yellow
        } else {
            color = android.graphics.Color.parseColor("#EF4444"); // Red
        }

        progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        tvPercentage.setTextColor(color);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        btnNavigate.setOnClickListener(v -> {
            Toast.makeText(this, "Membuka navigasi ke " + tvAreaName.getText(), Toast.LENGTH_SHORT).show();
            // Add navigation logic here
        });
    }
}