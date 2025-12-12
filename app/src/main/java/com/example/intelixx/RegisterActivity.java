package com.example.intelixx;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etNPM, etUsername, etPassword;
    private EditText etVehicleModel, etVehiclePlate;
    private Spinner spVehicleType;
    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Init Views
        etName = findViewById(R.id.etRegName);
        etNPM = findViewById(R.id.etRegNPM);
        etUsername = findViewById(R.id.etRegUsername);
        etPassword = findViewById(R.id.etRegPassword);

        spVehicleType = findViewById(R.id.spVehicleType);
        etVehicleModel = findViewById(R.id.etVehicleModel);
        etVehiclePlate = findViewById(R.id.etVehiclePlate);

        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Setup Spinner (Pilihan Mobil/Motor)
        String[] vehicleTypes = {"Mobil", "Motor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicleTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVehicleType.setAdapter(adapter);

        // LOGIKA TOMBOL REGISTER
        btnRegister.setOnClickListener(v -> {
            // Ambil semua input
            String name = etName.getText().toString().trim();
            String npm = etNPM.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            String vehicleType = spVehicleType.getSelectedItem().toString();
            String vehicleModel = etVehicleModel.getText().toString().trim();
            String vehiclePlate = etVehiclePlate.getText().toString().trim();

            // Validasi: Pastikan tidak ada yang kosong
            if (name.isEmpty() || npm.isEmpty() || username.isEmpty() || password.isEmpty() ||
                    vehicleModel.isEmpty() || vehiclePlate.isEmpty()) {
                Toast.makeText(this, "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show();
            } else {
                // SIMPAN SEMUA DATA KE MEMORI (SharedPreferences)
                SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                // Data User
                editor.putString("saved_name", name);
                editor.putString("saved_npm", npm);
                editor.putString("saved_username", username);
                editor.putString("saved_password", password);

                // Data Kendaraan
                editor.putString("saved_vehicle_type", vehicleType);
                editor.putString("saved_vehicle_model", vehicleModel);
                editor.putString("saved_vehicle_plate", vehiclePlate);

                editor.apply(); // Simpan perubahan

                Toast.makeText(this, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show();
                finish(); // Tutup halaman register, balik ke Login
            }
        });

        // Klik link "Login disini"
        tvLoginLink.setOnClickListener(v -> finish());
    }
}