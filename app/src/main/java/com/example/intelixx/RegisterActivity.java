package com.example.intelixx;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

        // LOGIKA TOMBOL REGISTER (DATABASE VERSION)
        btnRegister.setOnClickListener(v -> {
            // 1. Ambil semua input
            String name = etName.getText().toString().trim();
            String npm = etNPM.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            String vehicleType = spVehicleType.getSelectedItem().toString();
            String vehicleModel = etVehicleModel.getText().toString().trim();
            String vehiclePlate = etVehiclePlate.getText().toString().trim();

            // 2. Validasi Input
            if (name.isEmpty() || npm.isEmpty() || username.isEmpty() || password.isEmpty() ||
                    vehicleModel.isEmpty() || vehiclePlate.isEmpty()) {
                Toast.makeText(this, "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Proses Simpan ke Database (Background Thread)
            new Thread(() -> {
                Connection conn = null;
                try {
                    conn = KoneksiDatabase.connect();
                    if (conn == null) {
                        runOnUiThread(() -> Toast.makeText(this, "Gagal terhubung ke Database Server!", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    // --- MULAI TRANSAKSI ---
                    // Matikan auto-commit biar data user & kendaraan masuk barengan
                    conn.setAutoCommit(false);

                    // A. Insert Data User
                    // (Hapus kolom kendaraan dari sini karena sudah dipisah)
                    String sqlUser = "INSERT INTO users (nama, npm, username, password) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmtUser = conn.prepareStatement(sqlUser);
                    stmtUser.setString(1, name);
                    stmtUser.setString(2, npm);
                    stmtUser.setString(3, username);
                    stmtUser.setString(4, password);
                    stmtUser.executeUpdate();

                    // B. Insert Data Kendaraan (Langsung Set Active = 1)
                    String sqlVehicle = "INSERT INTO kendaraan (username, tipe, model, plat, is_active) VALUES (?, ?, ?, ?, 1)";
                    PreparedStatement stmtVeh = conn.prepareStatement(sqlVehicle);
                    stmtVeh.setString(1, username);
                    stmtVeh.setString(2, vehicleType);
                    stmtVeh.setString(3, vehicleModel);
                    stmtVeh.setString(4, vehiclePlate);
                    stmtVeh.executeUpdate();

                    // --- KOMIT TRANSAKSI ---
                    conn.commit();
                    conn.close();

                    // Jika Berhasil (Balik ke UI Thread)
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show();
                        finish(); // Tutup halaman register, balik ke Login
                    });

                } catch (SQLException e) {
                    e.printStackTrace();
                    // Rollback kalau ada error (Biar gak ada data setengah-setengah)
                    if (conn != null) {
                        try { conn.rollback(); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
                    }

                    // Tampilkan Error UI
                    runOnUiThread(() -> {
                        String errorMsg = e.getMessage();
                        if (errorMsg != null && errorMsg.contains("unique constraint")) {
                            Toast.makeText(this, "Username atau NPM sudah terdaftar!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Gagal Registrasi: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        });

        // Klik link "Login disini"
        tvLoginLink.setOnClickListener(v -> finish());
    }
}