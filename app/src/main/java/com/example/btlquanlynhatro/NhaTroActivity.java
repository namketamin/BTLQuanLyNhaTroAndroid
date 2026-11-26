
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
                loadDanhSachNhaTro(s.toString());
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
    private void loadDanhSachNhaTro(String tuKhoa) {
        layoutDanhSachNhaTro.removeAllViews();

        int maTK = getSharedPreferences("SoTroPrefs", MODE_PRIVATE).getInt("MaTK", -1);
        Cursor cursor = dbHelper.getNhaTroByMaTK(maTK);
        String keyword = tuKhoa == null ? "" : tuKhoa.toLowerCase().trim();
        int count = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int ma = cursor.getInt(0);
                String ten = cursor.getString(1);
                String diaChi = cursor.getString(2);

                if (!keyword.isEmpty()
                        && !(ten != null && ten.toLowerCase().contains(keyword))
                        && !(diaChi != null && diaChi.toLowerCase().contains(keyword))) {
                    continue;
                }

                count++;
                View card = getLayoutInflater().inflate(R.layout.item_nha_tro_card, layoutDanhSachNhaTro, false);
                TextView txtTen = card.findViewById(R.id.txtTenNhaTroItem);
                TextView txtDiaChi = card.findViewById(R.id.txtDiaChiItem);

                txtTen.setText(ten);
                txtDiaChi.setText(diaChi);

                card.setOnClickListener(v -> {
                    Intent intent = new Intent(NhaTroActivity.this, ChiTietNhaTroActivity.class);
                    intent.putExtra("MaNhaTro", ma);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                });

                layoutDanhSachNhaTro.addView(card);
            }
            cursor.close();
        }

        if (count == 0) {
            TextView txtEmpty = new TextView(this);
            txtEmpty.setText("Không tìm thấy nhà trọ nào");
            txtEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            txtEmpty.setTextColor(0xFF888888);
            txtEmpty.setGravity(Gravity.CENTER);
            txtEmpty.setPadding(0, dp(30), 0, dp(30));
            layoutDanhSachNhaTro.addView(txtEmpty);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null && layoutDanhSachNhaTro != null) {
            loadDanhSachNhaTro("");
        }
    }
}
