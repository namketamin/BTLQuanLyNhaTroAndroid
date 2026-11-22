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
    private int maTK = -1;

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

        // Lấy MaTK và tên chủ nhà từ phiên đăng nhập
        SharedPreferences prefs = getSharedPreferences("SoTroPrefs", MODE_PRIVATE);
        maTK = prefs.getInt("MaTK", -1);
        if (maTK != -1) {
            String hoTen = dbHelper.getHoTenChuTro(maTK);
            txtGreeting.setText("Xin chào, " + hoTen + "!");
        }

        // Tải số liệu thống kê phòng theo chủ trọ
        loadStatistics();

        btnCreateBill.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ThemSuaHoaDonActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    private void loadStatistics() {
        int total = dbHelper.getTongSoPhongByChuTro(maTK);
        int rented = dbHelper.getPhongDangThueByChuTro(maTK);
        int vacant = dbHelper.getPhongTrongByChuTro(maTK);

        txtTotalRooms.setText(String.valueOf(total));
        txtRentedRooms.setText(String.valueOf(rented));
        txtVacantRooms.setText(String.valueOf(vacant));

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int thang = calendar.get(java.util.Calendar.MONTH) + 1;
        int nam = calendar.get(java.util.Calendar.YEAR);

        double doanhThu = dbHelper.getTongDoanhThuThangByChuTro(maTK, thang, nam);
        double congNo = dbHelper.getTongCongNoByChuTro(maTK);

        txtRevenue.setText(String.format("%,.0f đ", doanhThu));
        txtDebt.setText(String.format("%,.0f đ", congNo));
    }
}