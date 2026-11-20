
package com.example.btlquanlynhatro;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.app.Activity;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class NhaTroActivity extends AppCompatActivity {

    private EditText edtSearchNhaTro;
    private LinearLayout layoutDanhSachNhaTro;
    private DatabaseHelper dbHelper;

    private final List<NhaTro> danhSachNhaTro = new ArrayList<>();

    // Lưu thông tin nhà trọ
    private static class NhaTro {
        int maNhaTro;
        String tenNhaTro;
        String diaChi;

        NhaTro(int ma, String ten, String diaChi) {
            this.maNhaTro = ma;
            this.tenNhaTro = ten;
            this.diaChi = diaChi;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nha_tro);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ giao diện
        edtSearchNhaTro = findViewById(R.id.layoutSearchNhaTro);
        edtSearchNhaTro.setHint("Tìm theo tên, địa chỉ nhà trọ");
        layoutDanhSachNhaTro = findViewById(R.id.layoutDanhSachNhaTro);

        // Thiết lập thanh điều hướng
        NavigationHelper.setupNav(this, 2);


        // Tìm kiếm nhà trọ
        edtSearchNhaTro.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                hienThiDanhSach(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        // Nút thêm nhà trọ -> Chuyển sang màn hình ThemNhaTroActivity
        findViewById(R.id.btnThemNhaTro).setOnClickListener(v -> {
            Intent intent = new Intent(NhaTroActivity.this, ThemNhaTroActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });


        // Nút người thuê
        findViewById(R.id.btnNguoiThue).setOnClickListener(v -> {
            Intent intent = new Intent(NhaTroActivity.this, NguoiThueActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

    // Nút hợp đồng - Lấy tất cả hợp đồng của chủ trọ hiện tại
        findViewById(R.id.btnHopDong).setOnClickListener(v -> {
            // 1. Lấy MaTK hiện tại đang lưu trong Session (SharedPreferences)
            SharedPreferences prefs = getSharedPreferences("SoTroPrefs", MODE_PRIVATE);
            int currentMaTK = prefs.getInt("MaTK", -1);

            // 2. Bắn sang DanhSachHopDongActivity kèm theo MaTK thật
            Intent intent = new Intent(NhaTroActivity.this,HopDongActivity.class);
            intent.putExtra("TYPE", "CHUTRO");
            intent.putExtra("MA_TK", currentMaTK); // Truyền đúng mã tài khoản đang đăng nhập
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    // Chuyển dp sang pixel
    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    // Lấy danh sách nhà trọ từ SQLite
    private void loadDanhSachNhaTro() {

        danhSachNhaTro.clear();

        SharedPreferences prefs =
                getSharedPreferences("SoTroPrefs", MODE_PRIVATE);

        int maTK = prefs.getInt("MaTK", -1);

        if (maTK == -1) {
            Toast.makeText(this,
                    "Không tìm thấy tài khoản",
                    Toast.LENGTH_SHORT).show();

            hienThiDanhSach("");
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT MaNhaTro, TenNhaTro, DiaChi " +
                        "FROM NHATRO WHERE MaTK = ?",
                new String[]{String.valueOf(maTK)}
        )) {

            while (cursor.moveToNext()) {

                int ma = cursor.getInt(0);

                String ten = cursor.getString(1);
                String diaChi = cursor.getString(2);

                if (ten == null) ten = "";
                if (diaChi == null) diaChi = "";

                danhSachNhaTro.add(
                        new NhaTro(ma, ten, diaChi)
                );
            }
        }

        // Giữ từ khóa đang tìm kiếm khi tải lại danh sách
        String tuKhoa = edtSearchNhaTro.getText().toString();

        hienThiDanhSach(tuKhoa);
    }

    // Hiển thị danh sách nhà trọ
    private void hienThiDanhSach(String tuKhoa) {

        layoutDanhSachNhaTro.removeAllViews();

        String keyword = tuKhoa.toLowerCase().trim();

        int soKetQua = 0;

        for (NhaTro nhaTro : danhSachNhaTro) {

            // Kiểm tra từ khóa
            if (!nhaTro.tenNhaTro.toLowerCase().contains(keyword)
                    && !nhaTro.diaChi.toLowerCase().contains(keyword)) {
                continue;
            }

            soKetQua++;

            // 1. INFLATE CARD TỪ FILE XML
            View card = LayoutInflater.from(this).inflate(R.layout.item_nha_tro_card, layoutDanhSachNhaTro, false);

            // 2. ÁNH XẠ VIEW BÊN TRONG CARD
            TextView txtTen = card.findViewById(R.id.txtTenNhaTroItem);
            TextView txtDiaChi = card.findViewById(R.id.txtDiaChiItem);

            // 3. ĐỔ DỮ LIỆU LÊN CARD
            txtTen.setText(nhaTro.tenNhaTro);
            txtDiaChi.setText(nhaTro.diaChi);

            // 4. SỰ KIỆN NHẤN VÀO THẺ (CHUYỂN TRANG CHI TIẾT)
            card.setOnClickListener(v -> {
                Intent intent = new Intent(NhaTroActivity.this, ChiTietNhaTroActivity.class);
                intent.putExtra("MaNhaTro", nhaTro.maNhaTro); // Truyền đúng mã nhà trọ qua để trang kia load phòng
                startActivity(intent);
                overridePendingTransition(0, 0); // Bỏ hiệu ứng lướt trang cho mượt
            });

            // Thêm thẻ vào danh sách container
            layoutDanhSachNhaTro.addView(card);
        }

        // Không có kết quả
        if (soKetQua == 0) {

            TextView txtEmpty = new TextView(this);

            if (danhSachNhaTro.isEmpty()) {
                txtEmpty.setText("Bạn chưa có nhà trọ nào");
            } else {
                txtEmpty.setText("Không tìm thấy nhà trọ");
            }

            txtEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            txtEmpty.setTextColor(0xFF888888);
            txtEmpty.setGravity(Gravity.CENTER);

            txtEmpty.setPadding(
                    0, dp(24),
                    0, dp(24)
            );

            layoutDanhSachNhaTro.addView(txtEmpty);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null && layoutDanhSachNhaTro != null) {
            loadDanhSachNhaTro();
        }
    }
}
