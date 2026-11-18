package com.example.btlquanlynhatro;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ThemNguoiThueActivity extends AppCompatActivity {

    private EditText edtHoTen, edtSDT, edtCCCD, edtNgaySinh, edtQueQuan;
    private Button btnLuuNguoiThue;
    private ImageView btnBack;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_nguoi_thue);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ View
        btnBack = findViewById(R.id.btnBack);
        edtHoTen = findViewById(R.id.edtHoTen);
        edtSDT = findViewById(R.id.edtSDT);
        edtCCCD = findViewById(R.id.edtCCCD);
        edtNgaySinh = findViewById(R.id.edtNgaySinh);
        edtQueQuan = findViewById(R.id.edtQueQuan);
        btnLuuNguoiThue = findViewById(R.id.btnLuuNguoiThue);

        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Xử lý khi bấm vào ô Ngày sinh -> Bật lịch chọn ngày
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

        // Nút Lưu người thuê
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

            // 4. Tiến hành gọi DatabaseHelper để lưu vào SQLite
            long result = dbHelper.insertNguoiThue(hoTen, cccd, sdt, ngaySinh, queQuan);
            if (result != -1) {
                Toast.makeText(this, "Thêm người thuê thành công!", Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(0, 0);
            } else {
                // Nếu insert thất bại (do vướng ràng buộc UNIQUE của cột CCCD trong SQLite)
                edtCCCD.setError("Số CCCD này đã tồn tại trong hệ thống!");
                edtCCCD.requestFocus();
            }
        });
    }
}