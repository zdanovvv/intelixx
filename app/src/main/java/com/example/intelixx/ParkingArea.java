package com.example.intelixx;

public class ParkingArea {
    private String name;
    private String distance;
    private int availableSlots;
    private int totalCapacity;

    public ParkingArea(String name, String distance, int availableSlots, int totalCapacity) {
        this.name = name;
        this.distance = distance;
        this.availableSlots = availableSlots;
        this.totalCapacity = totalCapacity;
    }

    public String getName() { return name; }
    public String getDistance() { return distance; }
    public int getAvailableSlots() { return availableSlots; }
    public int getTotalCapacity() { return totalCapacity; }

    // --- TAMBAHAN BARU (Supaya tidak merah) ---
    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }
    // ------------------------------------------

    public float getPercentage() {
        if (totalCapacity == 0) return 0;
        return ((float) availableSlots / totalCapacity) * 100;
    }

    public int getProgressColor() {
        if (availableSlots == 0) return 0xFFFF0000; // Merah
        if (availableSlots < 3) return 0xFFFFA500;  // Oranye
        return 0xFF4CAF50;                          // Hijau
    }
}