package com.example.intelixx;
import com.google.gson.annotations.SerializedName;

public class Slot {
    public int id;
    public String name;
    public String status;
    @SerializedName("floor") public int floor;
    @SerializedName("booked_by") public String bookedBy;
}