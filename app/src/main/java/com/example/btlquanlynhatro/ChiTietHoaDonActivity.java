package com.example.btlquanlynhatro;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ChiTietHoaDonActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtTieuDeHeader, txtChiTietPhong, txtChiTietNguoiThue, txtChiTietKy;
    private TextView txtChiTietTienPhong, txtChiTietTienDien, txtChiTietSubDien;
    private TextView txtChiTietTienNuoc, txtChiTietSubNuoc, txtChiTietInternet, txtChiTietDichVu;
    private TextView txtChiTietTongTien, txtChiTietTrangThai;
    private MaterialButton btnGhiNhanThanhToan, btnXuatHoaDon;
    private DatabaseHelper dbHelper;

    private String maHD = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_hoa_don);

        dbHelper = new DatabaseHelper(this);

        // Nhận MaHD truyền từ HoaDonActivity
        maHD = getIntent().getStringExtra("MaHD");
        if (maHD == null) maHD = "";

        // Ánh xạ View
        btnBack = findViewById(R.id.btnBack);
        txtTieuDeHeader = findViewById(R.id.txtTieuDeHeader);
        txtChiTietPhong = findViewById(R.id.txtChiTietPhong);
        txtChiTietNguoiThue = findViewById(R.id.txtChiTietNguoiThue);
        txtChiTietKy = findViewById(R.id.txtChiTietKy);
        txtChiTietTienPhong = findViewById(R.id.txtChiTietTienPhong);
        txtChiTietTienDien = findViewById(R.id.txtChiTietTienDien);
        txtChiTietSubDien = findViewById(R.id.txtChiTietSubDien);
        txtChiTietTienNuoc = findViewById(R.id.txtChiTietTienNuoc);
        txtChiTietSubNuoc = findViewById(R.id.txtChiTietSubNuoc);
        txtChiTietInternet = findViewById(R.id.txtChiTietInternet);
        txtChiTietDichVu = findViewById(R.id.txtChiTietDichVu);
        txtChiTietTongTien = findViewById(R.id.txtChiTietTongTien);
        txtChiTietTrangThai = findViewById(R.id.txtChiTietTrangThai);
        btnGhiNhanThanhToan = findViewById(R.id.btnGhiNhanThanhToan);
        btnXuatHoaDon = findViewById(R.id.btnXuatHoaDon);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Tải dữ liệu hóa đơn lên màn hình
        loadThongTinHoaDon();

        // Sự kiện bấm nút Ghi nhận thanh toán
        btnGhiNhanThanhToan.setOnClickListener(v -> {
            boolean success = updateTrangThaiThanhToan("Đã thanh toán");
            if (success) {
                Toast.makeText(this, "Đã ghi nhận thanh toán thành công!", Toast.LENGTH_SHORT).show();
                loadThongTinHoaDon(); // Tải lại giao diện
            } else {
                Toast.makeText(this, "Lỗi cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
            }
        });

        btnXuatHoaDon.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng xuất/chia sẻ hóa đơn đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadThongTinHoaDon() {
        if (maHD.isEmpty()) return;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Truy vấn thông tin tổng hợp của hóa đơn, phòng, người thuê
        String query = "SELECT h.MaHD, p.TenPhong, nt.HoTen, h.Thang, h.Nam, " +
                "h.DienDau, h.DienCuoi, h.NuocDau, h.NuocCuoi, h.TongTien, h.TrangThai " +
                "FROM HOADON h " +
                "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                "JOIN NGUOITHUE nt ON hd.MaNguoiThue = nt.MaNguoiThue " +
                "WHERE h.MaHD = ?";

        Cursor cursor = db.rawQuery(query, new String[]{maHD});
        if (cursor != null && cursor.moveToFirst()) {
            String idHD = cursor.getString(0);
            String tenPhong = cursor.getString(1);
            String tenNguoiThue = cursor.getString(2);
            int thang = cursor.getInt(3);
            int nam = cursor.getInt(4);
            double dienDau = cursor.getDouble(5);
            double dienCuoi = cursor.getDouble(6);
            double nuocDau = cursor.getDouble(7);
            double nuocCuoi = cursor.getDouble(8);
            double tongTien = cursor.getDouble(9);
            String trangThai = cursor.getString(10);

            // Đặt tiêu đề Header theo dạng HĐ-<Mã> · Chi tiết hóa đơn
            txtTieuDeHeader.setText("HĐ-" + idHD + " · Chi tiết hóa đơn");
            txtChiTietPhong.setText(tenPhong);
            txtChiTietNguoiThue.setText(tenNguoiThue);
            txtChiTietKy.setText(thang + "/" + nam);
            txtChiTietTongTien.setText(String.format("%,.0f đ", tongTien));
            txtChiTietTrangThai.setText(trangThai);

            if ("Đã thanh toán".equalsIgnoreCase(trangThai)) {
                txtChiTietTrangThai.setTextColor(0xFF245C45); // Màu xanh
                btnGhiNhanThanhToan.setText("Đã thanh toán");
                btnGhiNhanThanhToan.setEnabled(false);
                btnGhiNhanThanhToan.setAlpha(0.6f);
            } else {
                txtChiTietTrangThai.setTextColor(0xFFD97706); // Màu vàng cam / đỏ
                btnGhiNhanThanhToan.setText("Ghi nhận thanh toán");
                btnGhiNhanThanhToan.setEnabled(true);
                btnGhiNhanThanhToan.setAlpha(1.0f);
            }

            cursor.close();
        }

        // Truy vấn chi tiết các khoản phí từ bảng CHITIETHOADON để hiển thị đúng số tiền từng mục
        Cursor cursorCT = db.rawQuery("SELECT LoaiPhi, SoLuong, DonGia, ThanhTien FROM CHITIETHOADON WHERE MaHD = ?", new String[]{maHD});
        if (cursorCT != null) {
            while (cursorCT.moveToNext()) {
                String loaiPhi = cursorCT.getString(0);
                double soLuong = cursorCT.getDouble(1);
                double thanhTien = cursorCT.getDouble(2); // hoặc cột ThanhTien

                if (loaiPhi.equals("Tiền phòng")) {
                    txtChiTietTienPhong.setText(String.format("%,.0f đ", thanhTien));
                } else if (loaiPhi.equals("Tiền điện")) {
                    txtChiTietTienDien.setText(String.format("%,.0f đ", thanhTien));
                    // Lấy lại chỉ số điện từ HOADON để viết dòng chú thích
                    Cursor cDien = db.rawQuery("SELECT DienDau, DienCuoi FROM HOADON WHERE MaHD = ?", new String[]{maHD});
                    if (cDien != null && cDien.moveToFirst()) {
                        double dDau = cDien.getDouble(0);
                        double dCuoi = cDien.getDouble(1);
                        txtChiTietSubDien.setText(String.format("Chỉ số %.0f → %.0f · %.0f kWh", dDau, dCuoi, (dCuoi - dDau)));
                        cDien.close();
                    }
                } else if (loaiPhi.equals("Tiền nước")) {
                    txtChiTietTienNuoc.setText(String.format("%,.0f đ", thanhTien));
                    Cursor cNuoc = db.rawQuery("SELECT NuocDau, NuocCuoi FROM HOADON WHERE MaHD = ?", new String[]{maHD});
                    if (cNuoc != null && cNuoc.moveToFirst()) {
                        double nDau = cNuoc.getDouble(0);
                        double nCuoi = cNuoc.getDouble(1);
                        txtChiTietSubNuoc.setText(String.format("Chỉ số %.0f → %.0f · %.0f m³", nDau, nCuoi, (nCuoi - nDau)));
                        cNuoc.close();
                    }
                } else if (loaiPhi.equals("Tiền Internet")) {
                    txtChiTietInternet.setText(String.format("%,.0f đ", thanhTien));
                } else if (loaiPhi.equals("Tiền dịch vụ")) {
                    txtChiTietDichVu.setText(String.format("%,.0f đ", thanhTien));
                }
            }
            cursorCT.close();
        }
    }

    private boolean updateTrangThaiThanhToan(String trangThaiMoi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("TrangThai", trangThaiMoi);
        int rows = db.update("HOADON", values, "MaHD = ?", new String[]{maHD});
        return rows > 0;
    }
}