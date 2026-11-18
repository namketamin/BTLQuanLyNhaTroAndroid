package com.example.btlquanlynhatro;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ChiTietPhongActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtTenPhongTrangThai, txtTenNhaTro, txtGiaThue, txtDienTich;
    private TextView txtNguoiThueHD, txtSoHopDong, txtThoiHan;
    private Button btnXemHopDong, btnLichSuHopDong, btnNhanHoaDon, btnSuaThongTin, btnXoaPhong;

    private DatabaseHelper dbHelper;
    private int maPhong = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_phong);

        dbHelper = new DatabaseHelper(this);
        maPhong = getIntent().getIntExtra("MaPhong", -1);

        // Ánh xạ View
        btnBack = findViewById(R.id.btnBack);
        txtTenPhongTrangThai = findViewById(R.id.txtTenPhongTrangThai);
        txtTenNhaTro = findViewById(R.id.txtTenNhaTro);
        txtGiaThue = findViewById(R.id.txtGiaThue);
        txtDienTich = findViewById(R.id.txtDienTich);

        txtNguoiThueHD = findViewById(R.id.txtNguoiThue_HD);
        txtSoHopDong = findViewById(R.id.txtSoHopDong);
        txtThoiHan = findViewById(R.id.txtThoiHan);

        btnXemHopDong = findViewById(R.id.btnXemHopDong);
        btnLichSuHopDong = findViewById(R.id.btnLichSuHopDong);
        btnNhanHoaDon = findViewById(R.id.btnNhanHoaDon);
        btnSuaThongTin = findViewById(R.id.btnSuaThongTin);
        btnXoaPhong = findViewById(R.id.btnXoaPhong);

        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Load thông tin phòng từ SQLite lên màn hình
        loadThongTinPhong();

        // Lịch sử hợp đồng của phòng
        btnLichSuHopDong.setOnClickListener(v -> {
            Intent intent = new Intent(ChiTietPhongActivity.this, HopDongActivity.class);
            intent.putExtra("TYPE", "PHONG");
            intent.putExtra("TARGET_ID", maPhong); // Truyền MaPhong hiện tại của phòng đang xem
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        btnNhanHoaDon.setOnClickListener(v -> {
            Intent intent = new Intent(ChiTietPhongActivity.this, HoaDonActivity.class);
            intent.putExtra("TYPE", "PHONG");
            intent.putExtra("MaPhong", maPhong); // Truyền đúng biến mã phòng hiện tại của trang
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        btnSuaThongTin.setOnClickListener(v -> {
            Intent intent = new Intent(ChiTietPhongActivity.this, ThemSuaPhongActivity.class);
            intent.putExtra("MaPhong", maPhong); // Truyền mã phòng qua để form nhận biết đây là chế độ Sửa
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Nút xóa phòng
        btnXoaPhong.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa phòng này không? (Chỉ xóa được khi không có dữ liệu ràng buộc)")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        boolean deleted = dbHelper.deletePhong(maPhong);
                        if (deleted) {
                            Toast.makeText(this, "Đã xóa phòng thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                            overridePendingTransition(0, 0);
                        } else {
                            Toast.makeText(this, "Xóa thất bại! Phòng đang có dữ liệu liên quan.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void loadThongTinPhong() {
        if (maPhong == -1) return;

        Cursor cursor = dbHelper.getPhongById(maPhong);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String tenPhong = cursor.getString(cursor.getColumnIndexOrThrow("TenPhong"));
                    String trangThai = cursor.getString(cursor.getColumnIndexOrThrow("TrangThai"));
                    String tenNhaTro = cursor.getString(cursor.getColumnIndexOrThrow("TenNhaTro"));
                    double giaPhong = cursor.getDouble(cursor.getColumnIndexOrThrow("GiaPhong"));
                    double dienTich = cursor.getDouble(cursor.getColumnIndexOrThrow("DienTich"));

                    // Lấy an toàn bằng cách kiểm tra cột có tồn tại không
                    String hoTenNguoiThue = "";
                    String maHopDong = "";
                    String ngayBatDau = "";
                    String ngayKetThuc = "";

                    int idxHoTen = cursor.getColumnIndex("HoTen");
                    if (idxHoTen != -1 && !cursor.isNull(idxHoTen)) hoTenNguoiThue = cursor.getString(idxHoTen);

                    int idxMaHD = cursor.getColumnIndex("MaHopDong");
                    if (idxMaHD != -1 && !cursor.isNull(idxMaHD)) maHopDong = cursor.getString(idxMaHD);

                    int idxBD = cursor.getColumnIndex("NgayBatDau");
                    if (idxBD != -1 && !cursor.isNull(idxBD)) ngayBatDau = cursor.getString(idxBD);

                    int idxKT = cursor.getColumnIndex("NgayKetThuc");
                    if (idxKT != -1 && !cursor.isNull(idxKT)) ngayKetThuc = cursor.getString(idxKT);

                    // Đổ thông tin chung lên giao diện
                    txtTenNhaTro.setText(tenNhaTro != null ? tenNhaTro : "Chưa xác định");
                    txtGiaThue.setText(String.format("%,.0f đ/tháng", giaPhong));
                    txtDienTich.setText((int) dienTich + " m²");

                    // KIỂM TRA ĐIỀU KIỆN: ĐÃ CÓ HỢP ĐỒNG HAY CHƯA?
                    if (maHopDong != null && !maHopDong.trim().isEmpty()) {
                        // TRƯỜNG HỢP: ĐÃ CÓ HỢP ĐỒNG (Đang thuê)
                        txtTenPhongTrangThai.setText(tenPhong + " · Đang thuê");
                        txtNguoiThueHD.setText(hoTenNguoiThue);
                        txtSoHopDong.setText(maHopDong);
                        txtThoiHan.setText(ngayBatDau + " – " + ngayKetThuc);

                        btnXemHopDong.setText("Xem hợp đồng");
                        btnXemHopDong.setOnClickListener(v -> {
                            Intent intent = new Intent(ChiTietPhongActivity.this, ChiTietHopDongActivity.class);
                            intent.putExtra("MaPhong", maPhong);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        });

                    } else {
                        // TRƯỜNG HỢP: CHƯA CÓ HỢP ĐỒNG (Phòng trống)
                        txtTenPhongTrangThai.setText(tenPhong + " · Còn trống");
                        txtNguoiThueHD.setText("Chưa có người thuê");
                        txtSoHopDong.setText("---");
                        txtThoiHan.setText("---");

                        btnXemHopDong.setText("Thêm hợp đồng");
                        btnXemHopDong.setOnClickListener(v -> {
                            Intent intent = new Intent(ChiTietPhongActivity.this, ThemSuaHopDongActivity.class);
                            intent.putExtra("MaPhong", maPhong);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        });
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi load phòng: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                cursor.close();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadThongTinPhong(); // Tự động load lại danh sách phòng mỗi khi quay lại màn hình này
    }
}