package com.example.btlquanlynhatro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName, txtPhone, txtEmail;
    private Button btnUpdateProfile, btnChangePassword, btnLogout;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        // Kích hoạt thanh menu đáy và định vị đang ở Tab 5 (Cá nhân)
        NavigationHelper.setupNav(this, 5);

        // Ánh xạ view
        txtName = findViewById(R.id.txtName);
        txtPhone = findViewById(R.id.txtPhone);
        txtEmail = findViewById(R.id.txtEmail);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);

        // Lấy MaTK từ SharedPreferences để load dữ liệu thật từ DB
        SharedPreferences prefs = getSharedPreferences("SoTroPrefs", MODE_PRIVATE);
        int maTK = prefs.getInt("MaTK", -1);

        if (maTK != -1) {
            String hoTen = dbHelper.getHoTenChuTro(maTK);
            String email = dbHelper.getUserEmail(maTK);
            String sdt = dbHelper.getUserSDT(maTK);

            txtName.setText(hoTen);
            txtPhone.setText(sdt.isEmpty() ? "Chưa cập nhật SĐT" : sdt);
            txtEmail.setText(email.isEmpty() ? "Chưa cập nhật Email" : email);
        }

        // Xử lý sự kiện Cập nhật hồ sơ
        btnUpdateProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng cập nhật hồ sơ đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Xử lý sự kiện Đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Chuyển sang màn hình đổi mật khẩu...", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            // startActivity(intent);
        });

        // Xử lý sự kiện ĐĂNG XUẤT
        btnLogout.setOnClickListener(v -> {
            // Xóa phiên đăng nhập trong SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // Xóa sạch dữ liệu lưu (MaTK, Username)
            editor.apply();

            Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

            // Quay trở về màn hình Đăng nhập và xóa sạch lịch sử activity cũ
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}