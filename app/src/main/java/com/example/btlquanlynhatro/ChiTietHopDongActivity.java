package com.example.btlquanlynhatro;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.content.Intent;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class ChiTietHopDongActivity extends AppCompatActivity {

    private TextView txtMaVaTrangThai, txtPhongHD, txtNguoiDaiDienHD, txtNgayBatDauHD, txtNgayKetThucHD;
    private TextView txtGiaThueHD, txtDonGiaDienHD, txtDonGiaNuocHD, txtPhiInternetHD, txtPhiDichVuHD, txtTienCocHD;
    private TextView txtTenNguoiThueCard, txtSubNguoiThueCard;
    private View layoutNguoiThueItem;
    private Button btnSuaHopDong, btnGiaHanHopDong, btnKetThucHopDong;
    private ImageView btnBack;

    private DatabaseHelper dbHelper;
    private int maPhong = -1;
    private int maNguoiThue = -1;
    private String maHopDongIntent = null;

    private String maHopDongHienTai = null; // Khai báo biến toàn cục

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_hop_dong);

        dbHelper = new DatabaseHelper(this);
        maPhong = getIntent().getIntExtra("MaPhong", -1);
        maHopDongIntent = getIntent().getStringExtra("MaHopDong");

        // Ánh xạ toàn bộ View
        btnBack = findViewById(R.id.btnBack);
        txtMaVaTrangThai = findViewById(R.id.txtMaVaTrangThai);
        txtPhongHD = findViewById(R.id.txtPhongHD);
        txtNguoiDaiDienHD = findViewById(R.id.txtNguoiDaiDienHD);
        txtNgayBatDauHD = findViewById(R.id.txtNgayBatDauHD);
        txtNgayKetThucHD = findViewById(R.id.txtNgayKetThucHD);

        txtGiaThueHD = findViewById(R.id.txtGiaThueHD);
        txtDonGiaDienHD = findViewById(R.id.txtDonGiaDienHD);
        txtDonGiaNuocHD = findViewById(R.id.txtDonGiaNuocHD);
        txtPhiInternetHD = findViewById(R.id.txtPhiInternetHD);
        txtPhiDichVuHD = findViewById(R.id.txtPhiDichVuHD);
        txtTienCocHD = findViewById(R.id.txtTienCocHD);

        // Ánh xạ phần card include người thuê
        layoutNguoiThueItem = findViewById(R.id.layoutNguoiThueItem);
        txtTenNguoiThueCard = findViewById(R.id.txtTenNguoiThue);
        txtSubNguoiThueCard = findViewById(R.id.txtInfoNguoiThue);

        btnSuaHopDong = findViewById(R.id.btnSuaHopDong);
        btnGiaHanHopDong = findViewById(R.id.btnGiaHanHopDong);
        btnKetThucHopDong = findViewById(R.id.btnKetThucHopDong);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        loadThongTinHopDong();

        layoutNguoiThueItem.setOnClickListener(v -> {
            if (maNguoiThue != -1) {
                Intent intent = new Intent(ChiTietHopDongActivity.this, ChiTietNguoiThueActivity.class);
                intent.putExtra("MaNguoiThue", maNguoiThue);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        btnSuaHopDong.setOnClickListener(v -> {
            Intent intent = new Intent(ChiTietHopDongActivity.this, ThemSuaHopDongActivity.class);
            intent.putExtra("MaHopDong", maHopDongHienTai); // Truyền mã hợp đồng sang
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        btnGiaHanHopDong.setOnClickListener(v -> {
            // Inflate file XML dialog_gia_han.xml
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_gia_han, null);

            // Ánh xạ các thành phần bên trong file XML của dialog
            EditText edtNgayMoi = dialogView.findViewById(R.id.edtNgayKetThucMoi);
            Button btnHuyGiaHan = dialogView.findViewById(R.id.btnHuyGiaHan);
            Button btnXacNhanGiaHan = dialogView.findViewById(R.id.btnXacNhanGiaHan);

            // Khởi tạo Dialog
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            // Bấm vào ô input là bật lịch lên chọn
            edtNgayMoi.setOnClickListener(view -> showDatePicker(edtNgayMoi));

            // Xử lý sự kiện nút Hủy trong XML
            if (btnHuyGiaHan != null) {
                btnHuyGiaHan.setOnClickListener(view -> dialog.dismiss());
            }

            // Xử lý sự kiện nút Xác nhận trong XML
            if (btnXacNhanGiaHan != null) {
                btnXacNhanGiaHan.setOnClickListener(view -> {
                    String ngayMoi = edtNgayMoi.getText().toString().trim();
                    if (ngayMoi.isEmpty()) {
                        Toast.makeText(this, "Vui lòng chọn ngày kết thúc mới!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!kiemtraNgayHopLe(ngayMoi)) {
                        Toast.makeText(this, "Ngày kết thúc mới phải lớn hơn ngày hiện tại!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    boolean success = dbHelper.updateNgayKetThucHopDong(maHopDongHienTai, ngayMoi);
                    if (success) {
                        Toast.makeText(this, "Gia hạn hợp đồng thành công!", Toast.LENGTH_SHORT).show();
                        loadThongTinHopDong();
                        dialog.dismiss(); // Tắt dialog sau khi thành công
                    } else {
                        Toast.makeText(this, "Gia hạn thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            dialog.show();
        });

        btnKetThucHopDong.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận kết thúc hợp đồng")
                    .setMessage("Bạn có chắc chắn muốn kết thúc hợp đồng này? Phòng sẽ chuyển về trạng thái 'Còn trống'.")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        // 1. Cập nhật trạng thái hợp đồng thành "Đã kết thúc" (hoặc hủy hiệu lực tùy logic bảng của cậu)
                        // 2. Đồng thời cập nhật trạng thái phòng trọ về lại "Còn trống"

                        // (Ví dụ gọi hàm update trong DatabaseHelper):
                        boolean updateHD = dbHelper.updateTrangThaiHopDong(maHopDongHienTai, "Đã kết thúc");
                        boolean updatePhong = dbHelper.updateTrangThaiPhong(maPhong, "Còn trống");

                        if (updateHD || updatePhong) {
                            Toast.makeText(this, "Đã kết thúc hợp đồng thành công!", Toast.LENGTH_SHORT).show();
                            finish(); // Đóng màn hình chi tiết, quay lại danh sách phòng
                        } else {
                            Toast.makeText(this, "Kết thúc hợp đồng thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void showDatePicker(EditText edtTarget) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(
                this,
                (datePicker, year, month, day) -> {
                    String formattedDate = String.format(java.util.Locale.getDefault(), "%02d/%02d/%d", day, (month + 1), year);
                    edtTarget.setText(formattedDate);
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
        ).show();
    }

    private boolean kiemtraNgayHopLe(String ngayKetThucStr) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            sdf.setLenient(false);
            java.util.Date ngayKetThuc = sdf.parse(ngayKetThucStr);
            java.util.Date ngayHienTai = new java.util.Date(); // Ngày giờ hiện tại của hệ thống

            // Trả về true nếu ngày kết thúc sau ngày hiện tại
            return ngayKetThuc != null && ngayKetThuc.after(ngayHienTai);
        } catch (Exception e) {
            return false;
        }
    }

    private void loadThongTinHopDong() {
        Cursor cursor = null;

        // PHÂN NHÁNH THÔNG MINH:
        // 1. Nếu có truyền MaHopDong (bấm từ danh sách hợp đồng chung/lịch sử) -> Lấy chính xác hợp đồng đó
        if (maHopDongIntent != null && !maHopDongIntent.isEmpty()) {
            cursor = dbHelper.getHopDongChiTietByMaHD(maHopDongIntent);
        }
        // 2. Nếu chỉ có MaPhong (bấm từ nút Xem hợp đồng ở trang chi tiết phòng) -> Lấy hợp đồng của phòng
        else if (maPhong != -1) {
            cursor = dbHelper.getHopDongChiTietByPhong(maPhong);
        }

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    maHopDongHienTai = cursor.getString(0);
                    String trangThai = cursor.getString(1);
                    String tenPhong = cursor.getString(2);
                    maNguoiThue = cursor.getInt(3);
                    String hoTenNguoiThue = cursor.getString(4);
                    String sdt = cursor.getString(5);
                    String cccd = cursor.getString(6);
                    String ngayBatDau = cursor.getString(7);
                    String ngayKetThuc = cursor.getString(8);
                    double giaPhong = cursor.getDouble(9);
                    double donGiaDien = cursor.getDouble(10);
                    double donGiaNuoc = cursor.getDouble(11);
                    double phiInternet = cursor.getDouble(12);
                    double phiDichVu = cursor.getDouble(13);
                    double tienCoc = cursor.getDouble(14);

                    NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

                    // Đổ dữ liệu lên giao diện
                    txtMaVaTrangThai.setText(maHopDongHienTai + " · " + trangThai);
                    txtPhongHD.setText(tenPhong);
                    txtNguoiDaiDienHD.setText(hoTenNguoiThue);
                    txtNgayBatDauHD.setText(ngayBatDau);
                    txtNgayKetThucHD.setText(ngayKetThuc);

                    txtGiaThueHD.setText(formatter.format(giaPhong) + " đ/tháng");
                    txtDonGiaDienHD.setText(formatter.format(donGiaDien) + " đ/kWh");
                    txtDonGiaNuocHD.setText(formatter.format(donGiaNuoc) + " đ/m³");
                    txtPhiInternetHD.setText(formatter.format(phiInternet) + " đ/tháng");
                    txtPhiDichVuHD.setText(formatter.format(phiDichVu) + " đ/tháng");
                    txtTienCocHD.setText(formatter.format(tienCoc) + " đ");

                    // Card thông tin người thuê
                    txtTenNguoiThueCard.setText(hoTenNguoiThue);
                    txtSubNguoiThueCard.setText("SĐT: " + sdt + "  ·  CCCD: " + cccd);

                    // --- KIỂM TRA TRẠNG THÁI ĐỂ ẨN/HIỆN 3 NÚT CHỨC NĂNG ---
                    if ("Còn hiệu lực".equals(trangThai)) {
                        btnSuaHopDong.setVisibility(View.VISIBLE);
                        btnGiaHanHopDong.setVisibility(View.VISIBLE);
                        btnKetThucHopDong.setVisibility(View.VISIBLE);
                    } else {
                        // Nếu đã kết thúc hoặc trạng thái khác thì ẩn toàn bộ nút chỉnh sửa đi
                        btnSuaHopDong.setVisibility(View.GONE);
                        btnGiaHanHopDong.setVisibility(View.GONE);
                        btnKetThucHopDong.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi load hợp đồng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                cursor.close();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadThongTinHopDong();
    }
}