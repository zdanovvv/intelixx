package com.example.intelixx;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // IP TERBARU DARI IPCONFIG ANDA
    private static final String BASE_URL = "http://0.0.0.0:8000/";

    private static Retrofit retrofit = null;

    public static ParkingApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ParkingApi.class);
    }
}