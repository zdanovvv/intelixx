package com.example.intelixx;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ParkingApi {
    @GET("/api/slots")
    Call<List<Slot>> getSlots();
}