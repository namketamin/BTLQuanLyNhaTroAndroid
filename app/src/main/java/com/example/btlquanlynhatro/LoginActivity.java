package com.example.btlquanlynhatro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private Button btnLogin;
    private TextView txtForgotPassword, txtGuest;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Khởi tạo và mở database trước
        dbHelper = new DatabaseHelper(this);
        dbHelper.getWritableDatabase();

        // 2. Kiểm tra người dùng đã đăng nhập trước đó chưa
        SharedPreferences prefs = getSharedPreferences("SoTroPrefs", MODE_PRIVATE);
        int savedMaTK = prefs.getInt("MaTK", -1);

        if (savedMaTK != -1) {
            navigateByRole(savedMaTK);
            return;
        }

        // 3. Hiển thị giao diện đăng nhập
        setContentView(R.layout.activity_login);

        // 4. Ánh xạ giao diện
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtGuest = findViewById(R.id.txtGuest);

        // 5. Xử lý đăng nhập
        btnLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int maTK = dbHelper.checkLogin(username, password);

            if (maTK != -1) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("MaTK", maTK);
                editor.putString("Username", username);
                editor.apply();

                Toast.makeText(this, "Đăng nhập thành công!",
                        Toast.LENGTH_SHORT).show();

                navigateByRole(maTK);
            } else {
                Toast.makeText(this,
                        "Tên đăng nhập hoặc mật khẩu không chính xác!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 6. Quên mật khẩu
        txtForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Vui lòng liên hệ Quản trị viên (Admin) để được cấp lại mật khẩu.",
                    Toast.LENGTH_LONG).show();
        });

        // 7. Xem phòng với tư cách khách
        txtGuest.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Chuyển sang chế độ khách xem phòng...",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void navigateByRole(int maTK) {
        String vaiTro = dbHelper.getVaiTro(maTK);
        Intent intent;
        if ("Admin".equalsIgnoreCase(vaiTro)) {
            intent = new Intent(LoginActivity.this, ProfileActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}