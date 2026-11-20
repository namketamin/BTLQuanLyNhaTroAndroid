package com.example.btlquanlynhatro;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.view.View;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class ChiTietNhaTroActivity extends AppCompatActivity {

    private TextView txtTenNhaTro, txtDiaChiNhaTro;
    private EditText edtSearchPhong;
    private LinearLayout layoutDanhSachPhong;
    private Button btnFilterTatCa, btnFilterTrong, btnFilterThue, btnFilterBaoTri;
    private DatabaseHelper dbHelper;

    private int maNhaTro = -1;
    private String trangThaiHienTai = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_nha_tro);

        dbHelper = new DatabaseHelper(this);

        // Thiết lập thanh điều hướng (Tab 2 tương ứng với mục Trọ)
        NavigationHelper.setupNav(this, 2);

        // Nhận MaNhaTro được truyền từ NhaTroActivity sang
        maNhaTro = getIntent().getIntExtra("MaNhaTro", -1);

        // Ánh xạ view
        txtTenNhaTro = findViewById(R.id.txtTenNhaTro);
        txtDiaChiNhaTro = findViewById(R.id.txtDiaChiNhaTro);
        edtSearchPhong = findViewById(R.id.edtSearchPhong);
        edtSearchPhong.setHint("Tìm theo tên phòng");
        layoutDanhSachPhong = findViewById(R.id.layoutDanhSachPhong);

        btnFilterTatCa = findViewById(R.id.btnFilterTatCa);
        btnFilterTrong = findViewById(R.id.btnFilterTrong);
        btnFilterThue = findViewById(R.id.btnFilterThue);
        btnFilterBaoTri = findViewById(R.id.btnFilterBaoTri);

        // Nút Back
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Load thông tin nhà trọ lên header
        loadThongTinNhaTro();

        // Load danh sách phòng ban đầu
        loadDanhSachPhong();

        // Xử lý tìm kiếm phòng theo thời gian thực
        edtSearchPhong.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadDanhSachPhong();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý các nút lọc trạng thái
        btnFilterTatCa.setOnClickListener(v -> chonBoLoc("Tất cả", btnFilterTatCa));
        btnFilterTrong.setOnClickListener(v -> chonBoLoc("Còn trống", btnFilterTrong));
        btnFilterThue.setOnClickListener(v -> chonBoLoc("Đã thuê", btnFilterThue));
        btnFilterBaoTri.setOnClickListener(v -> chonBoLoc("Bảo trì", btnFilterBaoTri));

        // Nút Thêm phòng -> Chuyển sang Activity thêm phòng mới
        findViewById(R.id.btnThemPhong).setOnClickListener(v -> {
            Intent intent = new Intent(ChiTietNhaTroActivity.this, ThemSuaPhongActivity.class);
            intent.putExtra("MaNhaTro", maNhaTro); // Truyền mã nhà trọ sang để biết phòng thuộc nhà trọ nào
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Nút Cài đơn giá
        findViewById(R.id.btnCaiChiSo).setOnClickListener(v -> {
            Intent intent = new Intent(ChiTietNhaTroActivity.this, CaiDonGiaActivity.class);
            intent.putExtra("MaNhaTro", maNhaTro);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Thiết lập thanh điều hướng
        NavigationHelper.setupNav(this, 2);
    }

    // Đổi màu nút lọc khi được chọn
    private void chonBoLoc(String trangThaiDB, Button btnChon) {
        trangThaiHienTai = trangThaiDB;

        // Reset màu tất cả nút lọc về trắng
        btnFilterTatCa.setBackgroundColor(0xFFFFFFFF);
        btnFilterTatCa.setTextColor(0xFF333333);
        btnFilterTrong.setBackgroundColor(0xFFFFFFFF);
        btnFilterTrong.setTextColor(0xFF333333);
        btnFilterThue.setBackgroundColor(0xFFFFFFFF);
        btnFilterThue.setTextColor(0xFF333333);
        btnFilterBaoTri.setBackgroundColor(0xFFFFFFFF);
        btnFilterBaoTri.setTextColor(0xFF333333);

        // Sáng nút được chọn với màu chủ đạo forest
        btnChon.setBackgroundColor(0xFF245C45);
        btnChon.setTextColor(0xFFFFFFFF);

        loadDanhSachPhong();
    }

    // Lấy thông tin nhà trọ từ DB
    private void loadThongTinNhaTro() {
        if (maNhaTro == -1) return;
        Cursor cursor = dbHelper.getNhaTroById(maNhaTro);
        if (cursor != null && cursor.moveToFirst()) {
            txtTenNhaTro.setText(cursor.getString(0));
            txtDiaChiNhaTro.setText(cursor.getString(1));
            cursor.close();
        }
    }

    // Hiển thị danh sách phòng từ SQLite
    private void loadDanhSachPhong() {
        layoutDanhSachPhong.removeAllViews();
        String keyword = edtSearchPhong.getText().toString();

        Cursor cursor = dbHelper.getRoomsByNhaTro(maNhaTro, trangThaiHienTai, keyword);
        int soKetQua = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                soKetQua++;
                int maPhong = cursor.getInt(0);
                String tenPhong = cursor.getString(1);
                double dienTich = cursor.getDouble(2);
                double giaPhong = cursor.getDouble(3);
                String trangThai = cursor.getString(4);

                // Format tiền tệ VNĐ
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                String giaFormatted = formatter.format(giaPhong) + " đ/tháng";

                // 1. Inflate cái card XML vừa tạo ra thành một View
                View cardView = LayoutInflater.from(this).inflate(R.layout.item_phong_card, layoutDanhSachPhong, false);

                // 2. Ánh xạ các TextView bên trong card
                TextView txtTenVaTrangThai = cardView.findViewById(R.id.txtTenVaTrangThai);
                TextView txtChiTietPhong = cardView.findViewById(R.id.txtChiTietPhong);

                // 3. Đổ dữ liệu lên
                txtTenVaTrangThai.setText(tenPhong + "  ·  " + trangThai);
                txtChiTietPhong.setText("Diện tích: " + (int)dienTich + " m²\nGiá thuê: " + giaFormatted);

                // 4. Sự kiện khi bấm vào card phòng
                cardView.setOnClickListener(v -> {
                    Intent intent = new Intent(ChiTietNhaTroActivity.this, ChiTietPhongActivity.class);
                    intent.putExtra("MaPhong", maPhong); // Truyền đúng biến ID của phòng qua
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                });

                // 5. Thêm vào container chính
                layoutDanhSachPhong.addView(cardView);
            }
            cursor.close();
        }

        // Nếu không có phòng nào
        if (soKetQua == 0) {
            TextView txtEmpty = new TextView(this);
            txtEmpty.setText("Không tìm thấy phòng trọ nào");
            txtEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            txtEmpty.setTextColor(0xFF888888);
            txtEmpty.setGravity(Gravity.CENTER);
            txtEmpty.setPadding(0, dp(30), 0, dp(30));
            layoutDanhSachPhong.addView(txtEmpty);
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDanhSachPhong(); // Tự động load lại danh sách phòng mỗi khi quay lại màn hình này
    }
}