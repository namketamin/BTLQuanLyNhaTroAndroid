package com.example.btlquanlynhatro;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class CaiDonGiaActivity extends AppCompatActivity {

    private EditText edtGiaDien, edtGiaNuoc, edtPhiInternet, edtPhiDichVu;
    private Button btnLuuThietLap;
    private ImageView btnBack;
    private DatabaseHelper dbHelper;
    private int maNhaTro = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cai_don_gia);

        dbHelper = new DatabaseHelper(this);
        maNhaTro = getIntent().getIntExtra("MaNhaTro", -1);

        // Ánh xạ View
        btnBack = findViewById(R.id.btnBack);
        edtGiaDien = findViewById(R.id.edtGiaDien);
        edtGiaNuoc = findViewById(R.id.edtGiaNuoc);
        edtPhiInternet = findViewById(R.id.edtPhiInternet);
        edtPhiDichVu = findViewById(R.id.edtPhiDichVu);
        btnLuuThietLap = findViewById(R.id.btnLuuThietLap);

        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Load dữ liệu cũ lên form
        loadDonGiaHienTai();

        // Sự kiện nút Lưu thiết lập
        btnLuuThietLap.setOnClickListener(v -> {
            String dienStr = edtGiaDien.getText().toString().trim();
            String nuocStr = edtGiaNuoc.getText().toString().trim();
            String internetStr = edtPhiInternet.getText().toString().trim();
            String dvStr = edtPhiDichVu.getText().toString().trim();

            if (dienStr.isEmpty() || nuocStr.isEmpty() || internetStr.isEmpty() || dvStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ các khoản giá!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double giaDien = Double.parseDouble(dienStr);
                double giaNuoc = Double.parseDouble(nuocStr);
                double phiInternet = Double.parseDouble(internetStr);
                double phiDichVu = Double.parseDouble(dvStr);

                boolean success = dbHelper.updateDonGia(maNhaTro, giaDien, giaNuoc, phiInternet, phiDichVu);
                if (success) {
                    Toast.makeText(this, "Lưu thiết lập đơn giá thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                    overridePendingTransition(0, 0);
                } else {
                    Toast.makeText(this, "Lưu thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Định dạng số không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDonGiaHienTai() {
        if (maNhaTro == -1) return;
        Cursor cursor = dbHelper.getDonGia(maNhaTro);
        if (cursor != null && cursor.moveToFirst()) {
            // Ép kiểu (int) để loại bỏ số .0 phía sau
            edtGiaDien.setText(String.valueOf((int) cursor.getDouble(0)));
            edtGiaNuoc.setText(String.valueOf((int) cursor.getDouble(1)));
            edtPhiInternet.setText(String.valueOf((int) cursor.getDouble(2)));
            edtPhiDichVu.setText(String.valueOf((int) cursor.getDouble(3)));
            cursor.close();
        }
    }
}