package com.example.btlquanlynhatro;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HopDongActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText edtSearchHopDong;
    private LinearLayout layoutContainerHopDong;
    private DatabaseHelper dbHelper;

    private String type = ""; // Nhận diện: "CHUTRO", "PHONG", hoặc "NGUOITHUE"
    private int targetId = -1;
    private int currentMaTK = 2; // Mã tài khoản chủ trọ đang đăng nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hop_dong); // Dùng chung giao diện XML danh sách đã có

        dbHelper = new DatabaseHelper(this);

        // Nhận dữ liệu phân loại nguồn gọi từ Intent
        type = getIntent().getStringExtra("TYPE");
        targetId = getIntent().getIntExtra("TARGET_ID", -1);
        currentMaTK = getIntent().getIntExtra("MA_TK", 2);

        btnBack = findViewById(R.id.btnBack);
        edtSearchHopDong = findViewById(R.id.edtSearchHopDong);
        edtSearchHopDong.setHint("Tìm theo CCCD, số điện thoại, tên...");
        layoutContainerHopDong = findViewById(R.id.layoutContainerHopDong);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        loadDanhSach("");

        // Tìm kiếm realtime theo từ khóa
        edtSearchHopDong.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadDanhSach(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadDanhSach(String keyword) {
        layoutContainerHopDong.removeAllViews();
        Cursor cursor = null;

        // Phân rã luồng dữ liệu theo đúng 3 yêu cầu của cậu
        if ("CHUTRO".equals(type)) {
            cursor = dbHelper.getHopDongByChuTro(currentMaTK, keyword);
        } else if ("PHONG".equals(type)) {
            cursor = dbHelper.getHopDongByPhong(targetId);
        } else if ("NGUOITHUE".equals(type)) {
            cursor = dbHelper.getHopDongByNguoiThue(targetId);
        }

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String maHopDong = cursor.getString(0);
                    String trangThai = cursor.getString(1);
                    String tenPhong = cursor.getString(2);
                    String hoTen = cursor.getString(3);
                    String sdt = cursor.getString(4);
                    String cccd = cursor.getString(5);

                    // Inflate card item theo đúng mẫu XML cậu vừa gửi
                    View cardView = LayoutInflater.from(this).inflate(R.layout.item_hop_dong_card, layoutContainerHopDong, false);

                    // Ánh xạ đúng ID từ file XML card sang
                    TextView txtTen = cardView.findViewById(R.id.txtMaHopDongItem);
                    TextView txtSub = cardView.findViewById(R.id.txtSubInfoItem);

                    // Đổ dữ liệu lên card
                    txtTen.setText(maHopDong + " · " + tenPhong + " · " + trangThai);
                    txtSub.setText(hoTen + " · " + sdt + " · " + cccd);

                    // Click vào card để chuyển hướng sang trang chi tiết phòng / hợp đồng tương ứng
                    cardView.setOnClickListener(v -> {
                        Intent intent = new Intent(HopDongActivity.this, ChiTietHopDongActivity.class);
                        intent.putExtra("MaHopDong", maHopDong); // Truyền chính xác mã hợp đồng của dòng được bấm
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    });

                    layoutContainerHopDong.addView(cardView);
                }
            } catch (Exception e) {
                // Bắt lỗi ngoại lệ nếu có
            } finally {
                cursor.close();
            }
        }
    }

    private int getMaPhongByMaHD(String maHopDong) {
        Cursor c = dbHelper.getHopDongChiTietByMaHD(maHopDong);
        int pId = -1;
        if (c != null && c.moveToFirst()) {
            pId = c.getInt(0); // Cột đầu tiên là MaPhong
            c.close();
        }
        return pId;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDanhSach("");
    }
}