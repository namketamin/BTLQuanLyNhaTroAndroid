package com.example.btlquanlynhatro;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class ThemSuaPhongActivity extends AppCompatActivity {

    private TextView txtTieuDe;
    private EditText edtTenPhong, edtDienTich, edtGiaPhong;
    private SwitchCompat switchBaoTri;
    private Button btnLuuPhong;
    private ImageView btnBack;
    private DatabaseHelper dbHelper;

    private int maNhaTro = -1;
    private int maPhong = -1; // Biến xác định Thêm hay Sửa (-1 là thêm, khác -1 là sửa)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_sua_phong);

        dbHelper = new DatabaseHelper(this);

        // Nhận dữ liệu truyền sang
        maNhaTro = getIntent().getIntExtra("MaNhaTro", -1);
        maPhong = getIntent().getIntExtra("MaPhong", -1);

        // Ánh xạ View
        txtTieuDe = findViewById(R.id.txtTieuDe);
        btnBack = findViewById(R.id.btnBack);
        edtTenPhong = findViewById(R.id.edtTenPhong);
        edtDienTich = findViewById(R.id.edtDienTich);
        edtGiaPhong = findViewById(R.id.edtGiaPhong);
        switchBaoTri = findViewById(R.id.switchBaoTri);
        btnLuuPhong = findViewById(R.id.btnLuuPhong);

        // Phân biệt chế độ Thêm hay Sửa
        if (maPhong != -1) {
            txtTieuDe.setText("Sửa thông tin phòng");
            loadDuLieuCu(maPhong); // Đổ dữ liệu cũ lên form
        } else {
            txtTieuDe.setText("Thêm phòng");
        }

        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Nút Lưu phòng
        btnLuuPhong.setOnClickListener(v -> {
            String tenPhong = edtTenPhong.getText().toString().trim();
            String dienTichStr = edtDienTich.getText().toString().trim();
            String giaPhongStr = edtGiaPhong.getText().toString().trim();

            if (tenPhong.isEmpty() || dienTichStr.isEmpty() || giaPhongStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin phòng!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double dienTich = Double.parseDouble(dienTichStr);
                double giaPhong = Double.parseDouble(giaPhongStr);
                String trangThai = switchBaoTri.isChecked() ? "Bảo trì" : "Còn trống";

                if (maPhong != -1) {
                    // --- CHẾ ĐỘ SỬA ---
                    boolean success = dbHelper.updatePhong(maPhong, tenPhong, dienTich, giaPhong, trangThai);
                    if (success) {
                        Toast.makeText(this, "Cập nhật phòng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(0, 0);
                    } else {
                        Toast.makeText(this, "Cập nhật phòng thất bại!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // --- CHẾ ĐỘ THÊM ---
                    long result = dbHelper.insertPhong(maNhaTro, tenPhong, dienTich, giaPhong, trangThai);
                    if (result != -1) {
                        Toast.makeText(this, "Thêm phòng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(0, 0);
                    } else {
                        Toast.makeText(this, "Thêm phòng thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Diện tích hoặc giá phòng không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm load dữ liệu cũ lên form khi ở chế độ Sửa
    private void loadDuLieuCu(int idPhong) {
        Cursor cursor = dbHelper.getPhongById(idPhong);
        if (cursor != null && cursor.moveToFirst()) {
            // Cấu trúc câu lệnh getPhongById:
            // 0: TenPhong, 1: TrangThai, 2: TenNhaTro, 3: GiaPhong, 4: DienTich
            String ten = cursor.getString(0);
            String trangThai = cursor.getString(1);
            double gia = cursor.getDouble(3);
            double dienTich = cursor.getDouble(4);

            edtTenPhong.setText(ten);
            edtDienTich.setText(String.valueOf(dienTich));
            edtGiaPhong.setText(String.valueOf((int) gia)); // Ép kiểu int cho gọn số tiền

            // Nếu trạng thái là 'Bảo trì' thì bật công tắc
            switchBaoTri.setChecked("Bảo trì".equals(trangThai));

            cursor.close();
        }
    }
}