package com.example.intelixx;

import com.google.gson.annotations.SerializedName;

public class Slot {
    public int id;
    public String name;
    public String status; // "free", "booked", "busy"

    @SerializedName("floor")
    public int floor; // 1 atau 2

    @SerializedName("booked_by")
    public String bookedBy;
}