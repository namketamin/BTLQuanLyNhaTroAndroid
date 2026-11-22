package com.example.btlquanlynhatro;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class QuanLyChuTroActivity extends AppCompatActivity {

    private EditText edtSearchChuTro;
    private LinearLayout layoutDanhSachChuTro;
    private MaterialButton btnThemChuTro;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_chu_tro);

        dbHelper = new DatabaseHelper(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        View searchInclude = findViewById(R.id.edtSearchChuTro);
        if (searchInclude instanceof EditText) {
            edtSearchChuTro = (EditText) searchInclude;
        } else if (searchInclude != null) {
            edtSearchChuTro = searchInclude.findViewById(R.id.edtSearch);
        }
        layoutDanhSachChuTro = findViewById(R.id.layoutDanhSachChuTro);
        btnThemChuTro = findViewById(R.id.btnThemChuTro);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        btnThemChuTro.setOnClickListener(v -> showDialogThemChuTro());

        if (edtSearchChuTro != null) {
            edtSearchChuTro.setHint("Tìm theo tên, tài khoản, SĐT...");
            edtSearchChuTro.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    loadDanhSachChuTro(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        loadDanhSachChuTro("");
    }

    private void loadDanhSachChuTro(String keyword) {
        layoutDanhSachChuTro.removeAllViews();
        Cursor cursor = dbHelper.getDanhSachChuTro(keyword);
        boolean hasData = false;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                hasData = true;
                int maTK = cursor.getInt(0);
                String username = cursor.getString(1);
                String hoTen = cursor.getString(2);
                String sdt = cursor.getString(3);
                String email = cursor.getString(4);
                String trangThai = cursor.getString(5) != null ? cursor.getString(5) : "Hoạt động";

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackgroundResource(R.drawable.bg_edittext);
                card.setPadding(36, 28, 36, 28);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 24);
                card.setLayoutParams(lp);

                LinearLayout line1 = new LinearLayout(this);
                line1.setOrientation(LinearLayout.HORIZONTAL);

                TextView tvTen = new TextView(this);
                tvTen.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                tvTen.setText(hoTen);
                tvTen.setTextColor(Color.parseColor("#222222"));
                tvTen.setTextSize(15);
                tvTen.setTypeface(null, Typeface.BOLD);

                TextView tvTrangThai = new TextView(this);
                tvTrangThai.setText(trangThai);
                tvTrangThai.setTextSize(12);
                tvTrangThai.setTypeface(null, Typeface.BOLD);
                tvTrangThai.setTextColor(Color.parseColor("Hoạt động".equalsIgnoreCase(trangThai) ? "#245C45" : "#D9534F"));

                line1.addView(tvTen);
                line1.addView(tvTrangThai);

                TextView tvSub = new TextView(this);
                String sdtStr = (sdt != null && !sdt.isEmpty()) ? sdt : "Chưa có SĐT";
                tvSub.setText("TK: " + username + " · SĐT: " + sdtStr);
                tvSub.setTextColor(Color.parseColor("#666666"));
                tvSub.setTextSize(13);
                tvSub.setPadding(0, 8, 0, 0);

                card.addView(line1);
                card.addView(tvSub);

                if (email != null && !email.trim().isEmpty()) {
                    TextView tvEmail = new TextView(this);
                    tvEmail.setText("Email: " + email);
                    tvEmail.setTextColor(Color.parseColor("#888888"));
                    tvEmail.setTextSize(12);
                    tvEmail.setPadding(0, 4, 0, 0);
                    card.addView(tvEmail);
                }

                card.setOnClickListener(v -> showDialogThaoTacChuTro(maTK, hoTen, username, sdt, email, trangThai));
                layoutDanhSachChuTro.addView(card);
            }
            cursor.close();
        }

        if (!hasData) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có tài khoản chủ trọ nào.");
            tvEmpty.setTextColor(Color.parseColor("#888888"));
            tvEmpty.setTextSize(14);
            layoutDanhSachChuTro.addView(tvEmpty);
        }
    }

    private void showDialogThemChuTro() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm tài khoản chủ trọ");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 18);

        EditText edtHoTen = new EditText(this);
        edtHoTen.setHint("Họ và tên chủ trọ *");
        edtHoTen.setBackgroundResource(R.drawable.bg_edittext);
        edtHoTen.setPadding(28, 22, 28, 22);
        edtHoTen.setLayoutParams(params);

        EditText edtUsername = new EditText(this);
        edtUsername.setHint("Tên đăng nhập *");
        edtUsername.setInputType(InputType.TYPE_CLASS_TEXT);
        edtUsername.setBackgroundResource(R.drawable.bg_edittext);
        edtUsername.setPadding(28, 22, 28, 22);
        edtUsername.setLayoutParams(params);

        EditText edtPassword = new EditText(this);
        edtPassword.setHint("Mật khẩu (mặc định 123456) *");
        edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edtPassword.setText("123456");
        edtPassword.setBackgroundResource(R.drawable.bg_edittext);
        edtPassword.setPadding(28, 22, 28, 22);
        edtPassword.setLayoutParams(params);

        EditText edtSDT = new EditText(this);
        edtSDT.setHint("Số điện thoại");
        edtSDT.setInputType(InputType.TYPE_CLASS_PHONE);
        edtSDT.setBackgroundResource(R.drawable.bg_edittext);
        edtSDT.setPadding(28, 22, 28, 22);
        edtSDT.setLayoutParams(params);

        EditText edtEmail = new EditText(this);
        edtEmail.setHint("Email");
        edtEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        edtEmail.setBackgroundResource(R.drawable.bg_edittext);
        edtEmail.setPadding(28, 22, 28, 22);
        edtEmail.setLayoutParams(params);

        layout.addView(edtHoTen);
        layout.addView(edtUsername);
        layout.addView(edtPassword);
        layout.addView(edtSDT);
        layout.addView(edtEmail);
        builder.setView(layout);

        builder.setNegativeButton("Hủy", null);
        builder.setPositiveButton("Tạo tài khoản", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String hoTen = edtHoTen.getText().toString().trim();
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String sdt = edtSDT.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();

            if (hoTen.isEmpty()) {
                edtHoTen.setError("Vui lòng nhập họ tên!");
                edtHoTen.requestFocus();
                return;
            }
            if (username.isEmpty()) {
                edtUsername.setError("Vui lòng nhập tên đăng nhập!");
                edtUsername.requestFocus();
                return;
            }
            if (dbHelper.isTenDangNhapTonTai(username)) {
                edtUsername.setError("Tên đăng nhập đã tồn tại!");
                edtUsername.requestFocus();
                return;
            }
            if (password.length() < 6) {
                edtPassword.setError("Mật khẩu phải từ 6 ký tự trở lên!");
                edtPassword.requestFocus();
                return;
            }

            long res = dbHelper.themTaiKhoanChuTro(username, password, hoTen, sdt, email);
            if (res != -1) {
                Toast.makeText(this, "Tạo tài khoản chủ trọ thành công!", Toast.LENGTH_SHORT).show();
                String kw = edtSearchChuTro != null ? edtSearchChuTro.getText().toString().trim() : "";
                loadDanhSachChuTro(kw);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Lỗi khi tạo tài khoản!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialogThaoTacChuTro(int maTK, String hoTen, String username, String sdt, String email, String trangThaiHienTai) {
        boolean dangHoatDong = "Hoạt động".equalsIgnoreCase(trangThaiHienTai);
        String actionKhoa = dangHoatDong ? "Khóa tài khoản" : "Mở khóa tài khoản";
        String[] options = {"Sửa thông tin", actionKhoa, "Đặt lại mật khẩu về 123456"};

        new AlertDialog.Builder(this)
                .setTitle(hoTen + " (" + username + ")")
                .setItems(options, (dialog, which) -> {
                    String kw = edtSearchChuTro != null ? edtSearchChuTro.getText().toString().trim() : "";
                    if (which == 0) {
                        showDialogSuaChuTro(maTK, hoTen, username, sdt, email, trangThaiHienTai);
                    } else if (which == 1) {
                        String trangThaiMoi = dangHoatDong ? "Bị khóa" : "Hoạt động";
                        if (dbHelper.updateTrangThaiTaiKhoan(maTK, trangThaiMoi)) {
                            Toast.makeText(this, "Đã cập nhật trạng thái: " + trangThaiMoi, Toast.LENGTH_SHORT).show();
                            loadDanhSachChuTro(kw);
                        }
                    } else if (which == 2) {
                        if (dbHelper.resetMatKhauTaiKhoan(maTK, "123456")) {
                            Toast.makeText(this, "Đã đặt lại mật khẩu của " + username + " về 123456", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void showDialogSuaChuTro(int maTK, String hoTenCu, String username, String sdtCu, String emailCu, String trangThaiHienTai) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sửa thông tin · " + username);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 18);

        EditText edtHoTen = new EditText(this);
        edtHoTen.setHint("Họ và tên chủ trọ *");
        edtHoTen.setBackgroundResource(R.drawable.bg_edittext);
        edtHoTen.setPadding(28, 22, 28, 22);
        edtHoTen.setText(hoTenCu);
        edtHoTen.setLayoutParams(params);

        EditText edtSDT = new EditText(this);
        edtSDT.setHint("Số điện thoại");
        edtSDT.setInputType(InputType.TYPE_CLASS_PHONE);
        edtSDT.setBackgroundResource(R.drawable.bg_edittext);
        edtSDT.setPadding(28, 22, 28, 22);
        edtSDT.setText(sdtCu != null ? sdtCu : "");
        edtSDT.setLayoutParams(params);

        EditText edtEmail = new EditText(this);
        edtEmail.setHint("Email");
        edtEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        edtEmail.setBackgroundResource(R.drawable.bg_edittext);
        edtEmail.setPadding(28, 22, 28, 22);
        edtEmail.setText(emailCu != null ? emailCu : "");
        edtEmail.setLayoutParams(params);

        EditText edtMatKhauMoi = new EditText(this);
        edtMatKhauMoi.setHint("Mật khẩu mới (để trống nếu giữ nguyên)");
        edtMatKhauMoi.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edtMatKhauMoi.setBackgroundResource(R.drawable.bg_edittext);
        edtMatKhauMoi.setPadding(28, 22, 28, 22);
        edtMatKhauMoi.setLayoutParams(params);

        layout.addView(edtHoTen);
        layout.addView(edtSDT);
        layout.addView(edtEmail);
        layout.addView(edtMatKhauMoi);
        builder.setView(layout);

        builder.setNegativeButton("Hủy", null);
        builder.setPositiveButton("Lưu", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String hoTenMoi = edtHoTen.getText().toString().trim();
            String sdtMoi = edtSDT.getText().toString().trim();
            String emailMoi = edtEmail.getText().toString().trim();
            String matKhauMoi = edtMatKhauMoi.getText().toString().trim();

            if (hoTenMoi.isEmpty()) {
                edtHoTen.setError("Vui lòng nhập họ tên!");
                edtHoTen.requestFocus();
                return;
            }
            if (!matKhauMoi.isEmpty() && matKhauMoi.length() < 6) {
                edtMatKhauMoi.setError("Mật khẩu mới phải từ 6 ký tự trở lên!");
                edtMatKhauMoi.requestFocus();
                return;
            }

            if (dbHelper.updateThongTinChuTro(maTK, hoTenMoi, sdtMoi, emailMoi, trangThaiHienTai, matKhauMoi)) {
                String msg = matKhauMoi.isEmpty()
                        ? "Cập nhật thông tin chủ trọ thành công!"
                        : "Cập nhật thông tin và mật khẩu thành công!";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                String kw = edtSearchChuTro != null ? edtSearchChuTro.getText().toString().trim() : "";
                loadDanhSachChuTro(kw);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}