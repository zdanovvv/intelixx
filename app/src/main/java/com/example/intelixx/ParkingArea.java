package com.example.intelixx;

import android.graphics.Color;

public class ParkingArea {
    private String name;
    private String distance;
    private int availableSlots;
    private int totalCapacity;

    // Constructor
    public ParkingArea(String name, String distance, int availableSlots, int totalCapacity) {
        this.name = name;
        this.distance = distance;
        this.availableSlots = availableSlots;
        this.totalCapacity = totalCapacity;
    }

    // Tambahkan di dalam class ParkingArea

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    // Menghitung persentase ketersediaan untuk ProgressBar
    public float getPercentage() {
        if (totalCapacity == 0) return 0;
        return ((float) availableSlots / totalCapacity) * 100;
    }

    // Menentukan warna berdasarkan persentase (Sesuai logika di DetailAreaActivity)
    public int getProgressColor() {
        float percentage = getPercentage();
        if (percentage > 40) {
            return Color.parseColor("#10B981"); // Hijau (Banyak)
        } else if (percentage >= 15) {
            return Color.parseColor("#F59E0B"); // Kuning (Sedang)
        } else {
            return Color.parseColor("#EF4444"); // Merah (Sedikit/Penuh)
        }
    }
}