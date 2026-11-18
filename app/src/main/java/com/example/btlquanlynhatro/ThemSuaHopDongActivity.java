package com.example.btlquanlynhatro;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ThemSuaHopDongActivity extends AppCompatActivity {

    private TextView txtTieuDeHopDong, txtTenNhaTroHD, txtDiaChiNhaTroHD, txtTenPhongHD;
    private EditText edtSearchNguoiThueHD, edtNgayBatDau, edtNgayKetThuc;
    private EditText edtGiaPhong, edtDonGiaDien, edtDonGiaNuoc, edtPhiInternet, edtPhiDichVu, edtTienCoc;
    private LinearLayout layoutDanhSachNguoiThueHD;
    private Button btnLuuHopDong;
    private ImageView btnBack;
    private DatabaseHelper dbHelper;

    private int maPhong = -1;
    private String maHopDongCu = null; // Biến nhận diện Sửa hay Tạo mới
    private int selectedMaNguoiThue = -1; // Lưu mã người thuê được chọn làm đại diện

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_sua_hop_dong);

        dbHelper = new DatabaseHelper(this);

        // Nhận dữ liệu truyền qua Intent
        maPhong = getIntent().getIntExtra("MaPhong", -1);
        maHopDongCu = getIntent().getStringExtra("MaHopDong"); // Nếu có mã này -> Chế độ Sửa

        // Ánh xạ View
        btnBack = findViewById(R.id.btnBack);

        txtTieuDeHopDong = findViewById(R.id.txtTieuDeHopDong);
        txtTenNhaTroHD = findViewById(R.id.txtTenNhaTroHD);
        txtDiaChiNhaTroHD = findViewById(R.id.txtDiaChiNhaTroHD);
        txtTenPhongHD = findViewById(R.id.txtTenPhongHD);

        edtSearchNguoiThueHD = findViewById(R.id.edtSearchNguoiThueHD);
        layoutDanhSachNguoiThueHD = findViewById(R.id.layoutDanhSachNguoiThueHD);

        edtNgayBatDau = findViewById(R.id.edtNgayBatDau);
        edtNgayKetThuc = findViewById(R.id.edtNgayKetThuc);

        // Gắn sự kiện mở lịch cho 2 ô ngày tháng
        edtNgayBatDau.setOnClickListener(v -> showDatePicker(edtNgayBatDau));
        edtNgayKetThuc.setOnClickListener(v -> showDatePicker(edtNgayKetThuc));

        edtGiaPhong = findViewById(R.id.edtGiaPhong);
        edtDonGiaDien = findViewById(R.id.edtDonGiaDien);
        edtDonGiaNuoc = findViewById(R.id.edtDonGiaNuoc);
        edtPhiInternet = findViewById(R.id.edtPhiInternet);
        edtPhiDichVu = findViewById(R.id.edtPhiDichVu);
        edtTienCoc = findViewById(R.id.edtTienCoc);
        btnLuuHopDong = findViewById(R.id.btnLuuHopDong);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // PHÂN BIỆT CHẾ ĐỘ TẠO MỚI HAY SỬA
        if (maHopDongCu != null && !maHopDongCu.isEmpty()) {
            // --- CHẾ ĐỘ SỬA HỢP ĐỒNG ---
            txtTieuDeHopDong.setText("Sửa hợp đồng");

            // Ở chế độ sửa, ẩn phần tìm kiếm người thuê đi vì giữ nguyên hợp đồng cũ
            edtSearchNguoiThueHD.setVisibility(View.GONE);
            layoutDanhSachNguoiThueHD.setVisibility(View.GONE);

            loadDuLieuHopDongCu(maHopDongCu);
        } else {
            // --- CHẾ ĐỘ TẠO MỚI ---
            txtTieuDeHopDong.setText("Tạo hợp đồng");
            loadThongTinPhongMacDinh(maPhong);

            // Load toàn bộ danh sách người thuê ban đầu
            loadDanhSachNguoiThue("");

            // Xử lý tìm kiếm realtime khi gõ vào ô tìm kiếm người thuê
            edtSearchNguoiThueHD.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    loadDanhSachNguoiThue(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // NÚT LƯU HỢP ĐỒNG
        btnLuuHopDong.setOnClickListener(v -> {
            String ngayBatDau = edtNgayBatDau.getText().toString().trim();
            String ngayKetThuc = edtNgayKetThuc.getText().toString().trim();
            String giaPhongStr = edtGiaPhong.getText().toString().trim();
            String dienStr = edtDonGiaDien.getText().toString().trim();
            String nuocStr = edtDonGiaNuoc.getText().toString().trim();
            String internetStr = edtPhiInternet.getText().toString().trim();
            String dvStr = edtPhiDichVu.getText().toString().trim();
            String cocStr = edtTienCoc.getText().toString().trim();

            if (ngayBatDau.isEmpty() || ngayKetThuc.isEmpty() || giaPhongStr.isEmpty() ||
                    dienStr.isEmpty() || nuocStr.isEmpty() || internetStr.isEmpty() || dvStr.isEmpty() || cocStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if ((maHopDongCu == null || maHopDongCu.isEmpty()) && selectedMaNguoiThue == -1) {
                Toast.makeText(this, "Vui lòng chọn người đại diện thuê phòng!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double giaPhong = Double.parseDouble(giaPhongStr);
                double donGiaDien = Double.parseDouble(dienStr);
                double donGiaNuoc = Double.parseDouble(nuocStr);
                double phiInternet = Double.parseDouble(internetStr);
                double phiDichVu = Double.parseDouble(dvStr);
                double tienCoc = Double.parseDouble(cocStr);

                if (maHopDongCu != null && !maHopDongCu.isEmpty()) {
                    // --- CẬP NHẬT HỢP ĐỒNG ---
                    boolean success = dbHelper.updateHopDong(maHopDongCu, ngayBatDau, ngayKetThuc,
                            giaPhong, donGiaDien, donGiaNuoc, phiInternet, phiDichVu, tienCoc);

                    if (success) {
                        if (maPhong != -1) {
                            dbHelper.updateTrangThaiPhong(maPhong, "Đã thuê");
                        }
                        Toast.makeText(this, "Cập nhật hợp đồng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(0, 0);
                    } else {
                        Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // --- TẠO MỚI HỢP ĐỒNG ---
                    String maHopDongMoi = "HD" + System.currentTimeMillis();

                    boolean success = dbHelper.insertHopDongDayDu(
                            maHopDongMoi, maPhong, selectedMaNguoiThue,
                            ngayBatDau, ngayKetThuc, giaPhong,
                            donGiaDien, donGiaNuoc, phiInternet, phiDichVu, tienCoc
                    );

                    if (success) {
                        if (maPhong != -1) {
                            dbHelper.updateTrangThaiPhong(maPhong, "Đã thuê");
                        }
                        Toast.makeText(this, "Tạo hợp đồng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(0, 0);
                    } else {
                        Toast.makeText(this, "Tạo hợp đồng thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Định dạng số không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==========================================
    // HÀM ĐỔ DỮ LIỆU NGƯỜI THUÊ DÙNG CARD ITEM RIÊNG
    // ==========================================
    private void loadDanhSachNguoiThue(String keyword) {
        layoutDanhSachNguoiThueHD.removeAllViews();
        Cursor cursor = dbHelper.searchNguoiThue(keyword);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int maNT = cursor.getInt(0);
                String hoTen = cursor.getString(1);
                String sdt = cursor.getString(2);
                String cccd = cursor.getString(3);
                // Nếu bảng NGUOITHUE có thêm cột Quê quán thì lấy thêm, không thì bỏ qua
                String queQuan = "";
                try {
                    queQuan = cursor.getString(4);
                } catch (Exception e) {}

                // Nạp file giao diện item_nguoi_thue_card.xml vào danh sách
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_nguoi_thue_card, layoutDanhSachNguoiThueHD, false);
                TextView txtTen = itemView.findViewById(R.id.txtTenNguoiThue);
                TextView txtInfo = itemView.findViewById(R.id.txtInfoNguoiThue);
                TextView txtTrangThai = itemView.findViewById(R.id.txtTrangThaiChon);

                txtTen.setText(hoTen);
                txtInfo.setText("SĐT: " + sdt + " · CCCD: " + cccd + (queQuan != null && !queQuan.isEmpty() ? " · " + queQuan : ""));

                // Nếu người này đang được chọn -> Đổi màu nổi bật hoặc đánh dấu tick
                if (maNT == selectedMaNguoiThue) {
                    txtTrangThai.setText("✓");
                    txtTrangThai.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    itemView.setBackgroundResource(R.drawable.bg_edittext); // Có thể tùy chỉnh màu viền đậm hơn nếu muốn
                } else {
                    txtTrangThai.setText("›");
                    txtTrangThai.setTextColor(parseColor("#245C45"));
                }

                // Sự kiện bấm chọn người thuê làm đại diện
                itemView.setOnClickListener(v -> {
                    selectedMaNguoiThue = maNT;
                    edtSearchNguoiThueHD.setText(hoTen); // Điền tên lên ô tìm kiếm để dễ theo dõi
                    Toast.makeText(this, "Đã chọn đại diện: " + hoTen, Toast.LENGTH_SHORT).show();
                    loadDanhSachNguoiThue(keyword); // Load lại để cập nhật giao diện chọn
                });

                layoutDanhSachNguoiThueHD.addView(itemView);
            }
            cursor.close();
        }
    }

    private int parseColor(String colorString) {
        return android.graphics.Color.parseColor(colorString);
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

    private void loadThongTinPhongMacDinh(int pId) {
        Cursor cursor = dbHelper.getInfoPhongVaNhaTroForHopDong(pId);
        if (cursor != null && cursor.moveToFirst()) {
            txtTenNhaTroHD.setText(cursor.getString(0));
            txtDiaChiNhaTroHD.setText(cursor.getString(1));
            txtTenPhongHD.setText("Phòng " + cursor.getString(2));

            edtGiaPhong.setText(String.valueOf((int)cursor.getDouble(3)));
            edtDonGiaDien.setText(String.valueOf((int)cursor.getDouble(4)));
            edtDonGiaNuoc.setText(String.valueOf((int)cursor.getDouble(5)));
            edtPhiInternet.setText(String.valueOf((int)cursor.getDouble(6)));
            edtPhiDichVu.setText(String.valueOf((int)cursor.getDouble(7)));
            cursor.close();
        }
    }

    private void loadDuLieuHopDongCu(String maHD) {
        Cursor cursor = dbHelper.getHopDongChiTietByMaHD(maHD);
        if (cursor != null && cursor.moveToFirst()) {
            maPhong = cursor.getInt(0);
            txtTenNhaTroHD.setText(cursor.getString(1));
            txtDiaChiNhaTroHD.setText(cursor.getString(2));
            txtTenPhongHD.setText("Phòng " + cursor.getString(3));

            edtNgayBatDau.setText(cursor.getString(8));
            edtNgayKetThuc.setText(cursor.getString(9));

            edtGiaPhong.setText(String.valueOf((int)cursor.getDouble(10)));
            edtDonGiaDien.setText(String.valueOf((int)cursor.getDouble(11)));
            edtDonGiaNuoc.setText(String.valueOf((int)cursor.getDouble(12)));
            edtPhiInternet.setText(String.valueOf((int)cursor.getDouble(13)));
            edtPhiDichVu.setText(String.valueOf((int)cursor.getDouble(14)));
            edtTienCoc.setText(String.valueOf((int)cursor.getDouble(15)));

            cursor.close();
        }
    }
}