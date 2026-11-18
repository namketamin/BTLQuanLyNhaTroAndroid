package com.example.btlquanlynhatro;

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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ThemHoaDonActivity extends AppCompatActivity {

    private EditText edtChonNhaTro, edtChonPhong, edtKyThang, edtTienPhong;
    private EditText edtDienDau, edtDienCuoi, edtNuocDau, edtNuocCuoi, edtTienInternet, edtTienDichVu, edtHanThanhToan;
    private MaterialButton btnLuuNhap, btnXacNhanHoaDon;
    private DatabaseHelper dbHelper;

    private String maHopDong = null;
    private int selectedMaPhong = -1;
    private double donGiaDienHD = 3500; // Giá mặc định nếu không đọc được từ hợp đồng
    private double donGiaNuocHD = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_hoa_don);

        dbHelper = new DatabaseHelper(this);

        maHopDong = getIntent().getStringExtra("MaHopDong");
        selectedMaPhong = getIntent().getIntExtra("MaPhong", -1);

        ImageView btnBack = findViewById(R.id.btnBack);
        edtChonNhaTro = findViewById(R.id.edtChonNhaTro);
        edtChonPhong = findViewById(R.id.edtChonPhong);
        edtKyThang = findViewById(R.id.edtKyThang);
        edtTienPhong = findViewById(R.id.edtTienPhong);
        edtDienDau = findViewById(R.id.edtDienDau);
        edtDienCuoi = findViewById(R.id.edtDienCuoi);
        edtNuocDau = findViewById(R.id.edtNuocDau);
        edtNuocCuoi = findViewById(R.id.edtNuocCuoi);
        edtTienInternet = findViewById(R.id.edtTienInternet);
        edtTienDichVu = findViewById(R.id.edtTienDichVu);
        edtHanThanhToan = findViewById(R.id.edtHanThanhToan);
        btnLuuNhap = findViewById(R.id.btnLuuNháp);
        btnXacNhanHoaDon = findViewById(R.id.btnXacNhanHoaDon);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        String currentMonthYear = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        edtKyThang.setText(currentMonthYear);
        edtHanThanhToan.setText("25/" + currentMonthYear);

        if (maHopDong != null && !maHopDong.isEmpty()) {
            edtChonNhaTro.setEnabled(false);
            edtChonPhong.setEnabled(false);
            loadThongTinHopDong(maHopDong);
        } else {
            edtChonNhaTro.setOnClickListener(v -> hienDialogChonNhaTro());
            edtChonPhong.setOnClickListener(v -> {
                String nhaTro = edtChonNhaTro.getText().toString().trim();
                if (nhaTro.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn nhà trọ trước!", Toast.LENGTH_SHORT).show();
                    hienDialogChonNhaTro();
                } else {
                    Toast.makeText(this, "Vui lòng bấm vào ô Nhà trọ để đổi nhà/phòng khác!", Toast.LENGTH_SHORT).show();
                }
            });
        }

//        if (btnLuuNhap != null) {
//            btnLuuNhap.setOnClickListener(v -> luuHoaDonVaoDB("Nháp"));
//        }
        btnXacNhanHoaDon.setOnClickListener(v -> luuHoaDonVaoDB("Chưa thanh toán"));
    }

    private void hienDialogChonNhaTro() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn nhà trọ");

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(30, 20, 30, 20);

        EditText edtSearch = new EditText(this);
        edtSearch.setHint("Tìm tên hoặc địa chỉ nhà trọ...");
        edtSearch.setPadding(24, 20, 24, 20);
        edtSearch.setBackgroundResource(R.drawable.bg_edittext);
        edtSearch.setTextSize(14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        dialogLayout.addView(edtSearch, params);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(container);
        dialogLayout.addView(scrollView);

        builder.setView(dialogLayout);
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();

        loadDanhSachNhaTro(container, dialog, "");

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadDanhSachNhaTro(container, dialog, s.toString().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void loadDanhSachNhaTro(LinearLayout container, AlertDialog dialog, String keyword) {
        container.removeAllViews();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT DISTINCT nt.MaNhaTro, nt.TenNhaTro, nt.DiaChi FROM NHATRO nt " +
                "JOIN PHONG p ON nt.MaNhaTro = p.MaNhaTro " +
                "JOIN HOPDONG hd ON p.MaPhong = hd.MaPhong " +
                "WHERE hd.TrangThai = 'Còn hiệu lực'";

        if (!keyword.isEmpty()) {
            sql += " AND (nt.TenNhaTro LIKE '%" + keyword + "%' OR nt.DiaChi LIKE '%" + keyword + "%')";
        }

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int maNhaTro = cursor.getInt(0);
                String tenNhaTro = cursor.getString(1);
                String diaChi = cursor.getString(2);

                View itemView = LayoutInflater.from(this).inflate(R.layout.item_nha_tro_card, container, false);
                TextView txtTen = itemView.findViewById(R.id.txtTenNhaTroItem);
                TextView txtDiaChi = itemView.findViewById(R.id.txtDiaChiItem);

                if (txtTen != null) txtTen.setText(tenNhaTro);
                if (txtDiaChi != null) txtDiaChi.setText(diaChi);

                itemView.setOnClickListener(v -> {
                    dialog.dismiss();
                    edtChonNhaTro.setText(tenNhaTro);
                    edtChonPhong.setText("");
                    selectedMaPhong = -1;
                    maHopDong = null;

                    hienDialogChonPhong(maNhaTro, tenNhaTro);
                });

                container.addView(itemView);
            }
            cursor.close();
        }
    }

    private void hienDialogChonPhong(int maNhaTro, String tenNhaTro) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn phòng (" + tenNhaTro + ")");

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(30, 20, 30, 20);

        EditText edtSearch = new EditText(this);
        edtSearch.setHint("Tìm tên phòng...");
        edtSearch.setPadding(24, 20, 24, 20);
        edtSearch.setBackgroundResource(R.drawable.bg_edittext);
        edtSearch.setTextSize(14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        dialogLayout.addView(edtSearch, params);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(container);
        dialogLayout.addView(scrollView);

        builder.setView(dialogLayout);
        builder.setNegativeButton("Hủy", null);
        AlertDialog dialog = builder.create();

        loadDanhSachPhong(maNhaTro, container, dialog, "");

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadDanhSachPhong(maNhaTro, container, dialog, s.toString().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void loadDanhSachPhong(int maNhaTro, LinearLayout container, AlertDialog dialog, String keyword) {
        container.removeAllViews();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT p.MaPhong, p.TenPhong, hd.GiaPhong, hd.MaHopDong, hd.PhiInternet, hd.PhiDichVu, hd.DonGiaDien, hd.DonGiaNuoc FROM PHONG p " +
                "JOIN HOPDONG hd ON p.MaPhong = hd.MaPhong " +
                "WHERE p.MaNhaTro = " + maNhaTro + " AND hd.TrangThai = 'Còn hiệu lực'";

        if (!keyword.isEmpty()) {
            sql += " AND p.TenPhong LIKE '%" + keyword + "%'";
        }

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int maPhong = cursor.getInt(0);
                String tenPhong = cursor.getString(1);
                double giaThue = cursor.getDouble(2);
                String hdId = cursor.getString(3);
                double tienInternet = cursor.getDouble(4);
                double tienDichVu = cursor.getDouble(5);
                double donGiaD = cursor.getDouble(6);
                double donGiaN = cursor.getDouble(7);

                View itemView = LayoutInflater.from(this).inflate(R.layout.item_phong_card, container, false);
                TextView txtTenPhong = itemView.findViewById(R.id.txtTenVaTrangThai);
                TextView txtGia = itemView.findViewById(R.id.txtChiTietPhong);

                if (txtTenPhong != null) txtTenPhong.setText(tenPhong + " · Đang thuê");
                if (txtGia != null) txtGia.setText(String.format("Giá thuê: %,.0f đ/tháng", giaThue));

                itemView.setOnClickListener(v -> {
                    dialog.dismiss();
                    selectedMaPhong = maPhong;
                    maHopDong = hdId;
                    donGiaDienHD = donGiaD > 0 ? donGiaD : 3500;
                    donGiaNuocHD = donGiaN > 0 ? donGiaN : 15000;
                    edtChonPhong.setText(tenPhong);

                    edtTienPhong.setText(String.valueOf((int) giaThue));
                    edtTienInternet.setText(String.valueOf((int) tienInternet));
                    edtTienDichVu.setText(String.valueOf((int) tienDichVu));
                });

                container.addView(itemView);
            }
            cursor.close();
        }
    }

    private void loadThongTinHopDong(String maHD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT p.TenPhong, nt.TenNhaTro, hd.GiaPhong, hd.PhiInternet, hd.PhiDichVu, hd.DonGiaDien, hd.DonGiaNuoc FROM HOPDONG hd " +
                        "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                        "JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro " +
                        "WHERE hd.MaHopDong = ?",
                new String[]{maHD}
        );
        if (cursor != null && cursor.moveToFirst()) {
            edtChonNhaTro.setText(cursor.getString(1));
            edtChonPhong.setText(cursor.getString(0));
            edtTienPhong.setText(String.valueOf((int) cursor.getDouble(2)));
            edtTienInternet.setText(String.valueOf((int) cursor.getDouble(3)));
            edtTienDichVu.setText(String.valueOf((int) cursor.getDouble(4)));
            donGiaDienHD = cursor.getDouble(5) > 0 ? cursor.getDouble(5) : 3500;
            donGiaNuocHD = cursor.getDouble(6) > 0 ? cursor.getDouble(6) : 15000;
            cursor.close();
        }
    }

    private void luuHoaDonVaoDB(String trangThai) {
        if (maHopDong == null || maHopDong.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ Nhà trọ và Phòng đang có người thuê!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double dienDau = parseDoubleSafe(edtDienDau.getText().toString());
            double dienCuoi = parseDoubleSafe(edtDienCuoi.getText().toString());
            double nuocDau = parseDoubleSafe(edtNuocDau.getText().toString());
            double nuocCuoi = parseDoubleSafe(edtNuocCuoi.getText().toString());

            // ===== BỔ SUNG CHECK LOGIC CHỈ SỐ =====
            if (dienCuoi < dienDau) {
                Toast.makeText(this, "Lỗi: Chỉ số điện cuối không được nhỏ hơn chỉ số đầu!", Toast.LENGTH_SHORT).show();
                edtDienCuoi.requestFocus();
                return;
            }

            if (nuocCuoi < nuocDau) {
                Toast.makeText(this, "Lỗi: Chỉ số nước cuối không được nhỏ hơn chỉ số đầu!", Toast.LENGTH_SHORT).show();
                edtNuocCuoi.requestFocus();
                return;
            }
            // ======================================

            String kyThangStr = edtKyThang.getText().toString().trim();
            double tienPhong = parseDoubleSafe(edtTienPhong.getText().toString());
            double tienInternet = parseDoubleSafe(edtTienInternet.getText().toString());
            double tienDichVu = parseDoubleSafe(edtTienDichVu.getText().toString());

            int thang = 1, nam = 2026;
            if (kyThangStr.contains("/")) {
                String[] parts = kyThangStr.split("/");
                thang = parseIntSafe(parts[0]);
                nam = parseIntSafe(parts[1]);
            }

            // Tính tiền điện nước
            double soDienTieuThu = dienCuoi - dienDau;
            double tienDien = soDienTieuThu * donGiaDienHD;

            double soNuocTieuThu = nuocCuoi - nuocDau;
            double tienNuoc = soNuocTieuThu * donGiaNuocHD;

            double tongTien = tienPhong + tienDien + tienNuoc + tienInternet + tienDichVu;

            // Gọi hàm lưu vào DatabaseHelper như cũ...
            boolean success = dbHelper.themHoaDon(
                    maHopDong, thang, nam, tienPhong,
                    dienDau, dienCuoi, donGiaDienHD,
                    nuocDau, nuocCuoi, donGiaNuocHD,
                    tienInternet, tienDichVu,
                    tongTien, trangThai
            );

            if (success) {
                Toast.makeText(this, "Lập hóa đơn thành công! Tổng: " + String.format("%,.0f đ", tongTien), Toast.LENGTH_LONG).show();
                finish();
                overridePendingTransition(0, 0);
            } else {
                Toast.makeText(this, "Lỗi: Có thể hóa đơn tháng này của phòng đã được lập trước đó!", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi nhập liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private double parseDoubleSafe(String val) {
        try { return val.isEmpty() ? 0 : Double.parseDouble(val); } catch (Exception e) { return 0; }
    }

    private int parseIntSafe(String val) {
        try { return val.isEmpty() ? 0 : Integer.parseInt(val); } catch (Exception e) { return 0; }
    }
}