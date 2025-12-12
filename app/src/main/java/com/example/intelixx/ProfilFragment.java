package com.example.intelixx;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProfilFragment extends Fragment {

    private ImageView imgProfile;
    private TextView tvNama, tvNPM, tvPlat, tvTipe, tvModel;
    private TextView btnLogout, btnUpdateVehicle, btnHistory, btnSettings;

    // Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<Intent> cropLauncher;

    private Uri tempCameraUri; // Simpan lokasi foto sementara kamera
    private static final int CAMERA_PERMISSION_CODE = 100; // Kode Izin

    // Class Helper untuk List
    class VehicleItem {
        int id;
        String label;
        public VehicleItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        initViews(view);
        setupLaunchers(); // Siapkan fitur kamera/galeri
        loadUserData();   // Ambil data dari DB
        setupActions();   // Siapkan tombol

        return view;
    }

    private void initViews(View view) {
        imgProfile = view.findViewById(R.id.imgProfile);
        tvNama = view.findViewById(R.id.tvNama);
        tvNPM = view.findViewById(R.id.tvNPM);
        tvPlat = view.findViewById(R.id.tvPlat);
        tvTipe = view.findViewById(R.id.tvTipe);
        tvModel = view.findViewById(R.id.tvModel);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnUpdateVehicle = view.findViewById(R.id.btnUpdateVehicle);
        btnHistory = view.findViewById(R.id.btnHistory);
        btnSettings = view.findViewById(R.id.btnSettings);
    }

    private void setupActions() {
        // Klik Foto: Munculkan Dialog Pilihan
        imgProfile.setOnClickListener(v -> showImageSourceDialog());

        btnUpdateVehicle.setOnClickListener(v -> showSwitchVehicleDialog());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Pindah ke Halaman Lain
        btnHistory.setOnClickListener(v -> startActivity(new Intent(getActivity(), RiwayatActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(getActivity(), PengaturanActivity.class)));
    }

    // === 1. FITUR KAMERA & GALERI ===

    private void setupLaunchers() {
        // A. HASIL DARI GALERI
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        startCrop(result.getData().getData());
                    }
                }
        );

        // B. HASIL DARI KAMERA
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess && tempCameraUri != null) {
                        startCrop(tempCameraUri);
                    }
                }
        );

        // C. HASIL DARI CROP
        cropLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri resultUri = UCrop.getOutput(result.getData());
                        if (resultUri != null) processAndUploadImage(resultUri);
                    } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                        Toast.makeText(getContext(), "Gagal Crop Gambar", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showImageSourceDialog() {
        String[] options = {"Ambil Foto (Kamera)", "Pilih dari Galeri"};
        new AlertDialog.Builder(getContext())
                .setTitle("Ganti Foto Profil")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermission(); // Cek izin dulu
                    else openGallery();
                })
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Izin kamera wajib diberikan!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        try {
            File photoFile = File.createTempFile("camera_img", ".jpg", getActivity().getExternalCacheDir());
            tempCameraUri = FileProvider.getUriForFile(getContext(), getActivity().getPackageName() + ".provider", photoFile);
            cameraLauncher.launch(tempCameraUri);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Gagal membuka kamera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void startCrop(Uri sourceUri) {
        String destName = "cropped_" + System.currentTimeMillis() + ".jpg";
        Uri destinationUri = Uri.fromFile(new File(getActivity().getCacheDir(), destName));

        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropGrid(false);
        options.setCompressionQuality(80);
        options.setToolbarTitle("Potong Gambar");

        Intent intent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withOptions(options)
                .getIntent(getContext());

        cropLauncher.launch(intent);
    }

    private void processAndUploadImage(Uri imageUri) {
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            imgProfile.setImageBitmap(bitmap);
            imgProfile.setImageTintList(null);
            imgProfile.setPadding(0,0,0,0);

            saveImageToDB(encodedImage);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveImageToDB(String encodedImage) {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);
        if (username == null) return;

        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                String sql = "UPDATE users SET foto_profil = ? WHERE username = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, encodedImage);
                stmt.setString(2, username);
                stmt.executeUpdate();
                conn.close();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Foto Profil Diperbarui!", Toast.LENGTH_SHORT).show());
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // === 2. LOAD DATA DARI DATABASE ===

    private void loadUserData() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);
        if (username == null) return;

        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                if (conn == null) return;

                // Ambil Data User
                String sqlUser = "SELECT * FROM users WHERE username = ?";
                PreparedStatement stmt = conn.prepareStatement(sqlUser);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String nama = rs.getString("nama");
                    String npm = rs.getString("npm");
                    String fotoBase64 = rs.getString("foto_profil");

                    // Ambil Kendaraan Aktif
                    String sqlVeh = "SELECT * FROM kendaraan WHERE username = ? AND is_active = 1";
                    PreparedStatement stmtVeh = conn.prepareStatement(sqlVeh);
                    stmtVeh.setString(1, username);
                    ResultSet rsVeh = stmtVeh.executeQuery();

                    String vTipe = "-", vModel = "-", vPlat = "-";
                    if (rsVeh.next()) {
                        vTipe = rsVeh.getString("tipe");
                        vModel = rsVeh.getString("model");
                        vPlat = rsVeh.getString("plat");
                    }

                    String finalVTipe = vTipe;
                    String finalVModel = vModel;
                    String finalVPlat = vPlat;

                    getActivity().runOnUiThread(() -> {
                        tvNama.setText(nama);
                        tvNPM.setText("Mahasiswa Aktif | NPM: " + npm);
                        tvTipe.setText(finalVTipe);
                        tvModel.setText(finalVModel);
                        tvPlat.setText(finalVPlat);

                        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                imgProfile.setImageBitmap(decodedByte);
                                imgProfile.setImageTintList(null);
                                imgProfile.setPadding(0,0,0,0);
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                    });
                }
                conn.close();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // === 3. FITUR KENDARAAN (GANTI & TAMBAH) ===

    private void showSwitchVehicleDialog() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        new Thread(() -> {
            List<VehicleItem> myVehicles = new ArrayList<>();
            try {
                Connection conn = KoneksiDatabase.connect();
                String sql = "SELECT id, tipe, model, plat, is_active FROM kendaraan WHERE username = ? ORDER BY id DESC";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                while(rs.next()) {
                    int id = rs.getInt("id");
                    String label = rs.getString("tipe") + " " + rs.getString("model") + " (" + rs.getString("plat") + ")";
                    if (rs.getInt("is_active") == 1) label += " [AKTIF]";
                    myVehicles.add(new VehicleItem(id, label));
                }
                conn.close();

                getActivity().runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Pilih Kendaraan");
                    ArrayAdapter<VehicleItem> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, myVehicles);

                    builder.setAdapter(adapter, (dialog, which) -> {
                        setVehicleActive(myVehicles.get(which).id);
                    });

                    builder.setPositiveButton("+ Tambah Baru", (dialog, which) -> showAddVehicleDialog());
                    builder.setNegativeButton("Batal", null);
                    builder.show();
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void setVehicleActive(int vehicleId) {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                conn.setAutoCommit(false);
                String sqlReset = "UPDATE kendaraan SET is_active = 0 WHERE username = ?";
                PreparedStatement stmtReset = conn.prepareStatement(sqlReset);
                stmtReset.setString(1, username);
                stmtReset.executeUpdate();
                String sqlSet = "UPDATE kendaraan SET is_active = 1 WHERE id = ?";
                PreparedStatement stmtSet = conn.prepareStatement(sqlSet);
                stmtSet.setInt(1, vehicleId);
                stmtSet.executeUpdate();
                conn.commit();
                conn.close();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Kendaraan diganti!", Toast.LENGTH_SHORT).show();
                    loadUserData();
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void showAddVehicleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Tambah Kendaraan Baru");
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        final Spinner spType = new Spinner(getContext());
        String[] types = {"Mobil", "Motor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(adapter);
        layout.addView(spType);
        final EditText etModel = new EditText(getContext());
        etModel.setHint("Model (Contoh: Yamaha NMAX)");
        layout.addView(etModel);
        final EditText etPlat = new EditText(getContext());
        etPlat.setHint("Plat Nomor (Contoh: D 4545 JK)");
        layout.addView(etPlat);
        builder.setView(layout);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            addNewVehicleToDB(spType.getSelectedItem().toString(), etModel.getText().toString(), etPlat.getText().toString());
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void addNewVehicleToDB(String tipe, String model, String plat) {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);
        new Thread(() -> {
            try {
                Connection conn = KoneksiDatabase.connect();
                String sql = "INSERT INTO kendaraan (username, tipe, model, plat, is_active) VALUES (?, ?, ?, ?, 0)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, tipe);
                stmt.setString(3, model);
                stmt.setString(4, plat);
                stmt.executeUpdate();
                conn.close();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Kendaraan ditambahkan!", Toast.LENGTH_LONG).show());
            } catch(Exception e) { e.printStackTrace(); }
        }).start();
    }

    // === 4. LOGOUT ===

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Konfirmasi Keluar")
                .setMessage("Apakah Anda yakin ingin keluar dari akun?")
                .setPositiveButton("Ya, Keluar", (dialog, which) -> {
                    SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}