package com.example.btlquanlynhatro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName, txtPhone, txtEmail;
    private Button btnUpdateProfile, btnChangePassword, btnQuanLyChuTro, btnLogout;
    private View layoutBottomNav;
    private DatabaseHelper dbHelper;
    private int maTK = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ view
        txtName = findViewById(R.id.txtName);
        txtPhone = findViewById(R.id.txtPhone);
        txtEmail = findViewById(R.id.txtEmail);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnQuanLyChuTro = findViewById(R.id.btnQuanLyChuTro);
        btnLogout = findViewById(R.id.btnLogout);
        layoutBottomNav = findViewById(R.id.layoutBottomNav);

        // Lấy MaTK từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("SoTroPrefs", MODE_PRIVATE);
        maTK = prefs.getInt("MaTK", -1);

        // Phân quyền hiển thị theo VaiTro
        String vaiTro = dbHelper.getVaiTro(maTK);
        if ("Admin".equalsIgnoreCase(vaiTro)) {
            btnQuanLyChuTro.setVisibility(View.VISIBLE);
            if (layoutBottomNav != null) {
                layoutBottomNav.setVisibility(View.GONE);
            }
        } else {
            btnQuanLyChuTro.setVisibility(View.GONE);
            if (layoutBottomNav != null) {
                layoutBottomNav.setVisibility(View.VISIBLE);
            }
            NavigationHelper.setupNav(this, 5);
        }

        loadThongTinHoSo();

        // 1. Dialog Cập nhật hồ sơ
        btnUpdateProfile.setOnClickListener(v -> showDialogCapNhatHoSo());

        // 2. Dialog Đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> showDialogDoiMatKhau());

        // 3. Mở trang Quản lý tài khoản chủ trọ (Admin)
        btnQuanLyChuTro.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, QuanLyChuTroActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // 4. Đăng xuất
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Các hàm loadThongTinHoSo(), showDialogCapNhatHoSo(), showDialogDoiMatKhau() giữ nguyên như cũ

    private void loadThongTinHoSo() {
        if (maTK != -1) {
            String hoTen = dbHelper.getHoTenChuTro(maTK);
            String email = dbHelper.getUserEmail(maTK);
            String sdt = dbHelper.getUserSDT(maTK);

            txtName.setText(hoTen);
            txtPhone.setText(sdt.isEmpty() ? "Chưa cập nhật SĐT" : sdt);
            txtEmail.setText(email.isEmpty() ? "Chưa cập nhật Email" : email);
        }
    }

    private void showDialogCapNhatHoSo() {
        if (maTK == -1) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật hồ sơ");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 20);

        EditText edtHoTen = new EditText(this);
        edtHoTen.setHint("Họ và tên");
        edtHoTen.setBackgroundResource(R.drawable.bg_edittext);
        edtHoTen.setPadding(28, 24, 28, 24);
        edtHoTen.setText(dbHelper.getHoTenChuTro(maTK));
        edtHoTen.setLayoutParams(params);

        EditText edtSDT = new EditText(this);
        edtSDT.setHint("Số điện thoại");
        edtSDT.setInputType(InputType.TYPE_CLASS_PHONE);
        edtSDT.setBackgroundResource(R.drawable.bg_edittext);
        edtSDT.setPadding(28, 24, 28, 24);
        edtSDT.setText(dbHelper.getUserSDT(maTK));
        edtSDT.setLayoutParams(params);

        EditText edtEmail = new EditText(this);
        edtEmail.setHint("Email");
        edtEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        edtEmail.setBackgroundResource(R.drawable.bg_edittext);
        edtEmail.setPadding(28, 24, 28, 24);
        edtEmail.setText(dbHelper.getUserEmail(maTK));
        edtEmail.setLayoutParams(params);

        layout.addView(edtHoTen);
        layout.addView(edtSDT);
        layout.addView(edtEmail);
        builder.setView(layout);

        builder.setNegativeButton("Hủy", null);
        builder.setPositiveButton("Lưu", null); // Ghi đè onClick bên dưới để validate không bị đóng dialog

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String hoTenMoi = edtHoTen.getText().toString().trim();
            String sdtMoi = edtSDT.getText().toString().trim();
            String emailMoi = edtEmail.getText().toString().trim();

            if (hoTenMoi.isEmpty()) {
                edtHoTen.setError("Vui lòng nhập họ tên!");
                edtHoTen.requestFocus();
                return;
            }

            if (dbHelper.updateProfile(maTK, hoTenMoi, sdtMoi, emailMoi)) {
                Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                loadThongTinHoSo();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialogDoiMatKhau() {
        if (maTK == -1) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi mật khẩu");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 20);

        EditText edtOldPass = new EditText(this);
        edtOldPass.setHint("Mật khẩu hiện tại");
        edtOldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edtOldPass.setBackgroundResource(R.drawable.bg_edittext);
        edtOldPass.setPadding(28, 24, 28, 24);
        edtOldPass.setLayoutParams(params);

        EditText edtNewPass = new EditText(this);
        edtNewPass.setHint("Mật khẩu mới (tối thiểu 6 ký tự)");
        edtNewPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edtNewPass.setBackgroundResource(R.drawable.bg_edittext);
        edtNewPass.setPadding(28, 24, 28, 24);
        edtNewPass.setLayoutParams(params);

        EditText edtConfirmPass = new EditText(this);
        edtConfirmPass.setHint("Nhập lại mật khẩu mới");
        edtConfirmPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edtConfirmPass.setBackgroundResource(R.drawable.bg_edittext);
        edtConfirmPass.setPadding(28, 24, 28, 24);
        edtConfirmPass.setLayoutParams(params);

        layout.addView(edtOldPass);
        layout.addView(edtNewPass);
        layout.addView(edtConfirmPass);
        builder.setView(layout);

        builder.setNegativeButton("Hủy", null);
        builder.setPositiveButton("Đổi mật khẩu", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPass = edtOldPass.getText().toString().trim();
            String newPass = edtNewPass.getText().toString().trim();
            String confirmPass = edtConfirmPass.getText().toString().trim();

            if (oldPass.isEmpty()) {
                edtOldPass.setError("Vui lòng nhập mật khẩu hiện tại!");
                edtOldPass.requestFocus();
                return;
            }
            if (newPass.length() < 6) {
                edtNewPass.setError("Mật khẩu mới phải từ 6 ký tự trở lên!");
                edtNewPass.requestFocus();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                edtConfirmPass.setError("Mật khẩu xác nhận không khớp!");
                edtConfirmPass.requestFocus();
                return;
            }
            if (oldPass.equals(newPass)) {
                edtNewPass.setError("Mật khẩu mới không được trùng mật khẩu cũ!");
                edtNewPass.requestFocus();
                return;
            }

            if (dbHelper.changePassword(maTK, oldPass, newPass)) {
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                edtOldPass.setError("Mật khẩu hiện tại không đúng!");
                edtOldPass.requestFocus();
            }
        });
    }
}