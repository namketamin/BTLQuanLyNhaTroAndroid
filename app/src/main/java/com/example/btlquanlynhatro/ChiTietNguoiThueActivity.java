package com.example.btlquanlynhatro;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ChiTietNguoiThueActivity extends AppCompatActivity {

    private EditText edtHoTen, edtSDT, edtCCCD, edtNgaySinh, edtQueQuan;
    private Button btnLuuNguoiThue, btnHoaDon, btnHopDong, btnXoaNguoiThue;
    private ImageView btnBack;
    private DatabaseHelper dbHelper;
    private int maNguoiThue = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_nguoi_thue);

        dbHelper = new DatabaseHelper(this);
        maNguoiThue = getIntent().getIntExtra("MaNguoiThue", -1);

        // Ánh xạ View
        btnBack = findViewById(R.id.btnBack);
        edtHoTen = findViewById(R.id.edtHoTen);
        edtSDT = findViewById(R.id.edtSDT);
        edtCCCD = findViewById(R.id.edtCCCD);
        edtNgaySinh = findViewById(R.id.edtNgaySinh);
        edtQueQuan = findViewById(R.id.edtQueQuan);

        btnLuuNguoiThue = findViewById(R.id.btnLuuNguoiThue);
        btnHoaDon = findViewById(R.id.btnHoaDon);
        btnHopDong = findViewById(R.id.btnHopDong);
        btnXoaNguoiThue = findViewById(R.id.btnXoaNguoiThue);

        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Load dữ liệu cũ của người thuê lên form
        loadThongTinNguoiThue();

        // Bật bảng lịch chọn ngày sinh
        edtNgaySinh.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                String dateStr = (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "/" +
                        ((month1 + 1) < 10 ? "0" + (month1 + 1) : (month1 + 1)) + "/" + year1;
                edtNgaySinh.setText(dateStr);
            }, year, month, day);
            datePickerDialog.show();
        });

        // Nút Lưu cập nhật thông tin
        btnLuuNguoiThue.setOnClickListener(v -> {
            String hoTen = edtHoTen.getText().toString().trim();
            String sdt = edtSDT.getText().toString().trim();
            String cccd = edtCCCD.getText().toString().trim();
            String ngaySinh = edtNgaySinh.getText().toString().trim();
            String queQuan = edtQueQuan.getText().toString().trim();

            // 1. Validate Họ tên
            if (hoTen.isEmpty()) {
                edtHoTen.setError("Vui lòng nhập họ tên người thuê!");
                edtHoTen.requestFocus();
                return;
            }
            if (hoTen.length() < 2) {
                edtHoTen.setError("Họ tên quá ngắn, vui lòng nhập đầy đủ!");
                edtHoTen.requestFocus();
                return;
            }

            // 2. Validate Số điện thoại (Bắt buộc 10 số, bắt đầu bằng 0)
            if (sdt.isEmpty()) {
                edtSDT.setError("Vui lòng nhập số điện thoại!");
                edtSDT.requestFocus();
                return;
            }
            if (!sdt.matches("^0\\d{9}$")) {
                edtSDT.setError("Số điện thoại không hợp lệ (phải có 10 chữ số và bắt đầu bằng 0)!");
                edtSDT.requestFocus();
                return;
            }

            // 3. Validate CCCD (Bắt buộc đúng 12 chữ số)
            if (cccd.isEmpty()) {
                edtCCCD.setError("Vui lòng nhập số CCCD!");
                edtCCCD.requestFocus();
                return;
            }
            if (!cccd.matches("^\\d{12}$")) {
                edtCCCD.setError("Số CCCD phải đúng 12 chữ số!");
                edtCCCD.requestFocus();
                return;
            }
            // 4. Validate Ngày sinh (Không được để trống và không được lớn hơn ngày hiện tại)
            if (!ngaySinh.isEmpty()) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                    sdf.setLenient(false);
                    java.util.Date dateNgaySinh = sdf.parse(ngaySinh);
                    java.util.Date ngayHienTai = new java.util.Date();

                    if (dateNgaySinh != null && dateNgaySinh.after(ngayHienTai)) {
                        // Thay vì dùng setError trên ô không focus, ta dùng Toast thông báo rõ chữ cho người dùng
                        Toast.makeText(this, "Ngày sinh không được lớn hơn ngày hiện tại!", Toast.LENGTH_SHORT).show();
                        edtNgaySinh.requestFocus();
                        return;
                    }
                } catch (java.text.ParseException e) {
                    Toast.makeText(this, "Định dạng ngày sinh không hợp lệ!", Toast.LENGTH_SHORT).show();
                    edtNgaySinh.requestFocus();
                    return;
                }
            }

            // 4. Tiến hành gọi DatabaseHelper để cập nhật vào SQLite
            boolean success = dbHelper.updateNguoiThue(maNguoiThue, hoTen, cccd, sdt, ngaySinh, queQuan);
            if (success) {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(0, 0);
            } else {
                // Nếu update thất bại (ví dụ trùng CCCD với người khác)
                edtCCCD.setError("Cập nhật thất bại! Số CCCD này có thể đã tồn tại.");
                edtCCCD.requestFocus();
            }
        });

        // Nút Hóa đơn người thuê
        btnHoaDon.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng xem hóa đơn của người thuê", Toast.LENGTH_SHORT).show();
        });

        // Ví dụ nút xem hợp đồng trong ChiTietNguoiThueActivity
        btnHopDong.setOnClickListener(v -> {
            Intent intent = new Intent(ChiTietNguoiThueActivity.this, HopDongActivity.class);
            intent.putExtra("TYPE", "NGUOITHUE");
            intent.putExtra("TARGET_ID", maNguoiThue); // Truyền mã của người thuê này sang
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Nút Xóa người thuê
        btnXoaNguoiThue.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa người thuê này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        boolean deleted = dbHelper.deleteNguoiThue(maNguoiThue);
                        if (deleted) {
                            Toast.makeText(this, "Đã xóa người thuê!", Toast.LENGTH_SHORT).show();
                            finish();
                            overridePendingTransition(0, 0);
                        } else {
                            Toast.makeText(this, "Xóa thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void loadThongTinNguoiThue() {
        if (maNguoiThue == -1) return;
        Cursor cursor = dbHelper.getNguoiThueById(maNguoiThue);
        if (cursor != null && cursor.moveToFirst()) {
            edtHoTen.setText(cursor.getString(0));
            edtCCCD.setText(cursor.getString(1));
            edtSDT.setText(cursor.getString(2));
            edtNgaySinh.setText(cursor.getString(3));
            edtQueQuan.setText(cursor.getString(4));
            cursor.close();
        }
    }
}