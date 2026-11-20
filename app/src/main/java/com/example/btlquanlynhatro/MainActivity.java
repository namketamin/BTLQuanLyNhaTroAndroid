package com.example.btlquanlynhatro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView txtGreeting, txtTotalRooms, txtRentedRooms, txtVacantRooms, txtRevenue, txtDebt;
    private Button btnCreateBill;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // Kích hoạt menu đáy ở vị trí Tab 1 (Tổng quan)
        NavigationHelper.setupNav(this, 1);

        txtGreeting = findViewById(R.id.txtGreeting);
        txtTotalRooms = findViewById(R.id.txtTotalRooms);
        txtRentedRooms = findViewById(R.id.txtRentedRooms);
        txtVacantRooms = findViewById(R.id.txtVacantRooms);
        txtRevenue = findViewById(R.id.txtRevenue);
        txtDebt = findViewById(R.id.txtDebt);
        btnCreateBill = findViewById(R.id.btnCreateBill);

        // Lấy tên chủ nhà từ phiên đăng nhập
        SharedPreferences prefs = getSharedPreferences("SoTroPrefs", MODE_PRIVATE);
        int maTK = prefs.getInt("MaTK", -1);
        if (maTK != -1) {
            String hoTen = dbHelper.getHoTenChuTro(maTK);
            txtGreeting.setText("Xin chào, " + hoTen + "!");
        }

        // Tải số liệu thống kê phòng
        loadStatistics();


        btnCreateBill.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ThemSuaHoaDonActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

    }

    //Load thống kê
    private void loadStatistics() {
        // 1. Thống kê phòng
        int total = dbHelper.getTongSoPhong();
        int rented = dbHelper.getPhongDangThuê();
        int vacant = dbHelper.getPhongTrong();

        txtTotalRooms.setText(String.valueOf(total));
        txtRentedRooms.setText(String.valueOf(rented));
        txtVacantRooms.setText(String.valueOf(vacant));

        // 2. Lấy tháng và năm hiện tại
        java.util.Calendar calendar = java.util.Calendar.getInstance();

        int thang = calendar.get(java.util.Calendar.MONTH) + 1;
        int nam = calendar.get(java.util.Calendar.YEAR);

        // 3. Lấy doanh thu và công nợ từ database
        double doanhThu = dbHelper.getTongDoanhThuThang(thang, nam);
        double congNo = dbHelper.getTongCongNo();

        // 4. Hiển thị lên giao diện
        txtRevenue.setText(String.format("%,.0f đ", doanhThu));
        txtDebt.setText(String.format("%,.0f đ", congNo));
    }
}