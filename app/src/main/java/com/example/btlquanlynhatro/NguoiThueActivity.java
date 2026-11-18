package com.example.btlquanlynhatro;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class NguoiThueActivity extends AppCompatActivity {

    private EditText edtSearchNguoiThue;
    private LinearLayout layoutDanhSachNguoiThue;
    private Button btnThemNguoiThue;
    private ImageView btnBack;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nguoi_thue);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ View
        btnBack = findViewById(R.id.btnBack);
        edtSearchNguoiThue = findViewById(R.id.edtSearchNguoiThue);
        edtSearchNguoiThue.setHint("Tìm theo tên, số điện thoại, CCCD");
        layoutDanhSachNguoiThue = findViewById(R.id.layoutDanhSachNguoiThue);
        btnThemNguoiThue = findViewById(R.id.btnThemNguoiThue);

        // Nút quay lại
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Load danh sách ban đầu
        loadDanhSachNguoiThue("");

        // Tìm kiếm thời gian thực
        edtSearchNguoiThue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadDanhSachNguoiThue(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Nút chuyển sang màn hình Thêm người thuê (nếu cậu làm activity riêng, hoặc mở dialog)
        btnThemNguoiThue.setOnClickListener(v -> {
            Intent intent = new Intent(NguoiThueActivity.this, ThemNguoiThueActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load lại danh sách khi quay lại màn hình này
        loadDanhSachNguoiThue(edtSearchNguoiThue.getText().toString());
    }

    private void loadDanhSachNguoiThue(String keyword) {
        layoutDanhSachNguoiThue.removeAllViews();
        Cursor cursor = dbHelper.getNguoiThue(keyword);
        int count = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                count++;
                int maNguoiThue = cursor.getInt(0);
                String hoTen = cursor.getString(1);
                String cccd = cursor.getString(2);
                String sdt = cursor.getString(3);
                String ngaySinh = cursor.getString(4);
                String queQuan = cursor.getString(5);

                // Bơm (inflate) cái file XML item_nguoi_thue vào đây
                View card = getLayoutInflater().inflate(R.layout.item_nguoi_thue_card, layoutDanhSachNguoiThue, false);

                // Ánh xạ các TextView bên trong file XML item
                TextView txtTen = card.findViewById(R.id.txtTenNguoiThue);
                TextView txtInfo = card.findViewById(R.id.txtInfoNguoiThue);

                // Gán dữ liệu
                txtTen.setText(hoTen);
                txtInfo.setText("SĐT: " + sdt + "  ·  CCCD: " + cccd + "\nQuê quán: " + (queQuan != null && !queQuan.isEmpty() ? queQuan : "Chưa cập nhật"));

                // Sự kiện bấm vào card
                card.setOnClickListener(v -> {
                    Intent intent = new Intent(NguoiThueActivity.this, ChiTietNguoiThueActivity.class);
                    intent.putExtra("MaNguoiThue", maNguoiThue);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                });

                // Thêm card vào danh sách chính
                layoutDanhSachNguoiThue.addView(card);
            }
            cursor.close();
        }

        // Nếu không có kết quả nào (phần này giữ nguyên hoặc làm tương tự)
        if (count == 0) {
            TextView txtEmpty = new TextView(this);
            txtEmpty.setText("Không tìm thấy người thuê nào");
            txtEmpty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            txtEmpty.setTextColor(0xFF888888);
            txtEmpty.setGravity(Gravity.CENTER);
            txtEmpty.setPadding(0, dp(30), 0, dp(30));
            layoutDanhSachNguoiThue.addView(txtEmpty);
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}