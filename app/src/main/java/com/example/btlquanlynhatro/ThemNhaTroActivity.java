package com.example.btlquanlynhatro;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ThemNhaTroActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtTenNhaTro, edtDiaChi;
    private Button btnLuuNhaTro;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_nha_tro);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ view
        btnBack = findViewById(R.id.btnBack);
        edtTenNhaTro = findViewById(R.id.edtTenNhaTro);
        edtDiaChi = findViewById(R.id.edtDiaChi);
        btnLuuNhaTro = findViewById(R.id.btnLuuNhaTro);

        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Nút Lưu nhà trọ
        btnLuuNhaTro.setOnClickListener(v -> {
            String tenNhaTro = edtTenNhaTro.getText().toString().trim();
            String diaChi = edtDiaChi.getText().toString().trim();

            if (tenNhaTro.isEmpty() || diaChi.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ tên nhà trọ và địa chỉ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Kiểm tra địa chỉ phải chứa ký tự chữ (tránh nhập toàn số như "123456", hỗ trợ cả tiếng Việt có dấu)
            if (!diaChi.matches(".*[a-zA-Zà-ỹÀ-Ỹ].*")) {
                edtDiaChi.setError("Địa chỉ phải bao gồm ký tự chữ cái hợp lệ!");
                edtDiaChi.requestFocus();
                return;
            }

            // Lấy MaTK của chủ trọ đang đăng nhập từ SharedPreferences
            SharedPreferences prefs = getSharedPreferences("SoTroPrefs", MODE_PRIVATE);
            int maTK = prefs.getInt("MaTK", -1);

            if (maTK != -1) {
                // Gọi hàm insert vào DatabaseHelper (đã có sẵn từ các bước trước)
                long result = dbHelper.insertNhaTro(maTK, tenNhaTro, diaChi, "Nhà trọ mới thêm");

                if (result != -1) {
                    Toast.makeText(this, "Thêm nhà trọ thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng màn hình này, tự động quay về màn hình danh sách
                } else {
                    Toast.makeText(this, "Thêm nhà trọ thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Lỗi phiên đăng nhập, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}