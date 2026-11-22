package com.example.btlquanlynhatro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HoaDonActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtSearchHoaDon;
    private TextView txtTieuDeTrang, txtFilterThang, txtFilterNam, txtFilterTrangThai;
    private LinearLayout layoutContainerHoaDon, layoutBottomThemHD;
    private MaterialButton btnThemHoaDon;
    private DatabaseHelper dbHelper;

    private int maTK = -1;
    private String type = "ALL";
    private int maPhong = -1;
    private String maHopDongHieuLuc = null;

    private String selectedThang = "Tất cả";
    private String selectedNam = "Tất cả";
    private String selectedTrangThai = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoa_don);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("SoTroPrefs", MODE_PRIVATE);
        maTK = prefs.getInt("MaTK", -1);

        NavigationHelper.setupNav(this, 3);

        type = getIntent().getStringExtra("TYPE");
        if (type == null) type = "ALL";
        maPhong = getIntent().getIntExtra("MaPhong", -1);

        btnBack = findViewById(R.id.btnBack);
        txtTieuDeTrang = findViewById(R.id.txtTieuDeTrang);
        if ("PHONG".equals(type)) {
            btnBack.setVisibility(View.VISIBLE);
            String tenPhongQuery = getTenPhongById(maPhong);
            if (tenPhongQuery != null && !tenPhongQuery.isEmpty()) {
                txtTieuDeTrang.setText("Hóa đơn " + tenPhongQuery);
            } else {
                txtTieuDeTrang.setText("Hóa đơn phòng");
            }
        } else {
            btnBack.setVisibility(View.GONE);
        }

        edtSearchHoaDon = findViewById(R.id.edtSearchHoaDon);
        edtSearchHoaDon.setHint("Tìm theo mã, người thuê...");

        txtFilterThang = findViewById(R.id.txtFilterThang);
        txtFilterNam = findViewById(R.id.txtFilterNam);
        txtFilterTrangThai = findViewById(R.id.txtFilterTrangThai);

        layoutContainerHoaDon = findViewById(R.id.layoutContainerHoaDon);
        layoutBottomThemHD = findViewById(R.id.layoutBottomThemHD);
        btnThemHoaDon = findViewById(R.id.btnThemHoaDon);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        if ("PHONG".equals(type)) {
            maHopDongHieuLuc = dbHelper.getMaHopDongDangHoatDongByPhong(maPhong);
            if (maHopDongHieuLuc == null || maHopDongHieuLuc.isEmpty()) {
                Toast.makeText(this, "Phòng này hiện chưa có hợp đồng thuê, không thể lập hóa đơn!", Toast.LENGTH_LONG).show();
                if (layoutBottomThemHD != null) {
                    layoutBottomThemHD.setVisibility(View.GONE);
                }
            }
        }

        if (btnThemHoaDon != null) {
            btnThemHoaDon.setOnClickListener(v -> {
                Intent intent = new Intent(HoaDonActivity.this, ThemSuaHoaDonActivity.class);
                if ("PHONG".equals(type)) {
                    intent.putExtra("MaHopDong", maHopDongHieuLuc);
                }
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        txtFilterThang.setOnClickListener(v -> hienDialogChonThang());
        txtFilterNam.setOnClickListener(v -> hienDialogChonNam());
        txtFilterTrangThai.setOnClickListener(v -> hienDialogChonTrangThai());

        loadDanhSachHoaDon("");

        if (edtSearchHoaDon != null) {
            edtSearchHoaDon.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    loadDanhSachHoaDon(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void hienDialogChonThang() {
        String[] options = {"Tất cả", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn tháng")
                .setItems(options, (dialog, which) -> {
                    selectedThang = options[which];
                    txtFilterThang.setText(selectedThang.equals("Tất cả") ? "Tất cả" : "Tháng " + selectedThang);
                    loadDanhSachHoaDon(edtSearchHoaDon.getText().toString().trim());
                })
                .show();
    }

    private void hienDialogChonNam() {
        List<String> namList = new ArrayList<>();
        namList.add("Tất cả");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT h.Nam FROM HOADON h " +
                        "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                        "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                        "JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro " +
                        "WHERE nt.MaTK = ? ORDER BY h.Nam DESC",
                new String[]{String.valueOf(maTK)}
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String nam = String.valueOf(cursor.getInt(0));
                if (!namList.contains(nam)) {
                    namList.add(nam);
                }
            }
            cursor.close();
        }

        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        if (!namList.contains(currentYear)) {
            namList.add(1, currentYear);
        }

        String[] options = namList.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Chọn năm")
                .setItems(options, (dialog, which) -> {
                    selectedNam = options[which];
                    txtFilterNam.setText(selectedNam);
                    loadDanhSachHoaDon(edtSearchHoaDon.getText().toString().trim());
                })
                .show();
    }

    private void hienDialogChonTrangThai() {
        String[] options = {"Tất cả", "Chưa thanh toán", "Đã thanh toán", "Quá hạn"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn trạng thái")
                .setItems(options, (dialog, which) -> {
                    selectedTrangThai = options[which];
                    txtFilterTrangThai.setText(selectedTrangThai);
                    loadDanhSachHoaDon(edtSearchHoaDon.getText().toString().trim());
                })
                .show();
    }

    private void loadDanhSachHoaDon(String keyword) {
        layoutContainerHoaDon.removeAllViews();
        Cursor cursor = null;

        if ("PHONG".equals(type)) {
            cursor = dbHelper.getHoaDonByPhong(maPhong, keyword);
        } else {
            cursor = dbHelper.getHoaDonByChuTro(maTK, keyword);
        }

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String maHD = cursor.getString(0);
                    String tenPhong = cursor.getString(1);
                    String tenNhaTro = cursor.getString(2);
                    String hoTenNguoiThue = cursor.getString(3);
                    int thang = cursor.getInt(4);
                    int nam = cursor.getInt(5);
                    double tongTien = cursor.getDouble(6);
                    String trangThaiHD = cursor.getString(7);

                    if (!selectedThang.equals("Tất cả") && thang != Integer.parseInt(selectedThang)) {
                        continue;
                    }
                    if (!selectedNam.equals("Tất cả") && nam != Integer.parseInt(selectedNam)) {
                        continue;
                    }
                    if (!selectedTrangThai.equals("Tất cả") && !trangThaiHD.equalsIgnoreCase(selectedTrangThai)) {
                        continue;
                    }

                    String kyThangNam = "Kỳ " + thang + "/" + nam;

                    View cardView = LayoutInflater.from(this).inflate(R.layout.item_hoa_don_card, layoutContainerHoaDon, false);

                    TextView txtTieuDe = cardView.findViewById(R.id.txtTieuDeHDItem);
                    TextView txtSub = cardView.findViewById(R.id.txtSubHDItem);
                    TextView txtGia = cardView.findViewById(R.id.txtGiaHDItem);

                    txtTieuDe.setText(tenPhong + " – " + tenNhaTro);
                    txtSub.setText(hoTenNguoiThue + " · " + kyThangNam + " · " + trangThaiHD);
                    txtGia.setText(String.format("%,.0f đ", tongTien));

                    cardView.setOnClickListener(v -> {
                        Intent intent = new Intent(HoaDonActivity.this, ChiTietHoaDonActivity.class);
                        intent.putExtra("MaHD", maHD);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    });

                    layoutContainerHoaDon.addView(cardView);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi tải hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                cursor.close();
            }
        }
    }

    private String getTenPhongById(int maPhongId) {
        String tenPhong = "";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT TenPhong FROM PHONG WHERE MaPhong = ?", new String[]{String.valueOf(maPhongId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                tenPhong = cursor.getString(0);
            }
            cursor.close();
        }
        return tenPhong;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentKeyword = (edtSearchHoaDon != null) ? edtSearchHoaDon.getText().toString().trim() : "";
        loadDanhSachHoaDon(currentKeyword);
    }
}