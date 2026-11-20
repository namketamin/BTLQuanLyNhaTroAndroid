package com.example.btlquanlynhatro;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ThongKeActivity extends AppCompatActivity {

    private TextView txtFilterThangTK, txtFilterNamTK, txtFilterNhaTroTK;
    private TextView txtTongDaThu, txtTongCongNo, txtTyLeLapDay;
    private ProgressBar pbLapDayPhong;
    private LinearLayout layoutCoCauKhoanThu;
    private DatabaseHelper dbHelper;

    private String selectedThang = "Tất cả";
    private String selectedNam = "Tất cả";
    private int selectedMaNhaTro = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);

        dbHelper = new DatabaseHelper(this);

        // Chỉnh index tab cho khớp với NavigationHelper của cậu
        NavigationHelper.setupNav(this, 4);

        ImageView btnBack = findViewById(R.id.btnBack);
        txtFilterThangTK = findViewById(R.id.txtFilterThangTK);
        txtFilterNamTK = findViewById(R.id.txtFilterNamTK);
        txtFilterNhaTroTK = findViewById(R.id.txtFilterNhaTroTK);

        txtTongDaThu = findViewById(R.id.txtTongDaThu);
        txtTongCongNo = findViewById(R.id.txtTongCongNo);
        txtTyLeLapDay = findViewById(R.id.txtTyLeLapDay);
        pbLapDayPhong = findViewById(R.id.pbLapDayPhong);
        layoutCoCauKhoanThu = findViewById(R.id.layoutCoCauKhoanThu);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        txtFilterThangTK.setText("Tất cả");
        txtFilterNamTK.setText("Tất cả");
        txtFilterNhaTroTK.setText("Tất cả");

        txtFilterThangTK.setOnClickListener(v -> hienDialogChonThang());
        txtFilterNamTK.setOnClickListener(v -> hienDialogChonNam());
        txtFilterNhaTroTK.setOnClickListener(v -> hienDialogChonNhaTro());
    }

    @Override
    protected void onResume() {
        super.onResume();
        capNhatThongKe();
    }

    private void hienDialogChonThang() {
        String[] options = {"Tất cả", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn tháng")
                .setItems(options, (dialog, which) -> {
                    selectedThang = options[which];
                    txtFilterThangTK.setText(selectedThang.equals("Tất cả") ? "Tất cả" : "Tháng " + selectedThang);
                    capNhatThongKe();
                })
                .show();
    }

    private void hienDialogChonNam() {
        List<String> namList = new ArrayList<>();
        namList.add("Tất cả");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT Nam FROM HOADON ORDER BY Nam DESC", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String nam = String.valueOf(cursor.getInt(0));
                if (!namList.contains(nam)) {
                    namList.add(nam);
                }
            }
            cursor.close();
        }

        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        if (!namList.contains(currentYear)) {
            namList.add(1, currentYear);
        }

        String[] options = namList.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Chọn năm")
                .setItems(options, (dialog, which) -> {
                    selectedNam = options[which];
                    txtFilterNamTK.setText(selectedNam);
                    capNhatThongKe();
                })
                .show();
    }

    private void hienDialogChonNhaTro() {
        List<Integer> idList = new ArrayList<>();
        List<String> tenList = new ArrayList<>();
        idList.add(-1);
        tenList.add("Tất cả");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MaNhaTro, TenNhaTro FROM NHATRO", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                idList.add(cursor.getInt(0));
                tenList.add(cursor.getString(1));
            }
            cursor.close();
        }

        String[] options = tenList.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Chọn nhà trọ")
                .setItems(options, (dialog, which) -> {
                    selectedMaNhaTro = idList.get(which);
                    txtFilterNhaTroTK.setText(tenList.get(which));
                    capNhatThongKe();
                })
                .show();
    }

    private void capNhatThongKe() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder whereHD = new StringBuilder(" WHERE 1=1 ");
        List<String> argsHD = new ArrayList<>();

        if (!selectedThang.equals("Tất cả")) {
            whereHD.append(" AND h.Thang = ? ");
            argsHD.add(selectedThang);
        }
        if (!selectedNam.equals("Tất cả")) {
            whereHD.append(" AND h.Nam = ? ");
            argsHD.add(selectedNam);
        }
        if (selectedMaNhaTro != -1) {
            whereHD.append(" AND p.MaNhaTro = ? ");
            argsHD.add(String.valueOf(selectedMaNhaTro));
        }

        String sqlTong = "SELECT h.TrangThai, COALESCE(SUM(h.TongTien), 0) " +
                "FROM HOADON h " +
                "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                whereHD +
                " GROUP BY h.TrangThai";

        double daThu = 0, congNo = 0;
        Cursor cTong = db.rawQuery(sqlTong, argsHD.toArray(new String[0]));
        if (cTong != null) {
            while (cTong.moveToNext()) {
                String tt = cTong.getString(0);
                double sum = cTong.getDouble(1);
                if ("Đã thanh toán".equals(tt)) {
                    daThu += sum;
                } else {
                    congNo += sum;
                }
            }
            cTong.close();
        }
        txtTongDaThu.setText(String.format(Locale.getDefault(), "%,.0f đ", daThu));
        txtTongCongNo.setText(String.format(Locale.getDefault(), "%,.0f đ", congNo));

        String sqlPhong = "SELECT COUNT(*), SUM(CASE WHEN TrangThai = 'Đã thuê' THEN 1 ELSE 0 END) FROM PHONG";
        String[] argsPhong = null;
        if (selectedMaNhaTro != -1) {
            sqlPhong += " WHERE MaNhaTro = ?";
            argsPhong = new String[]{String.valueOf(selectedMaNhaTro)};
        }
        Cursor cPhong = db.rawQuery(sqlPhong, argsPhong);
        if (cPhong != null && cPhong.moveToFirst()) {
            int tongPhong = cPhong.getInt(0);
            int daThue = cPhong.getInt(1);
            int trong = Math.max(0, tongPhong - daThue);
            int percent = tongPhong > 0 ? (daThue * 100 / tongPhong) : 0;

            txtTyLeLapDay.setText("Đã thuê: " + daThue + " / " + tongPhong + " phòng (" + percent + "%) · Trống: " + trong);
            pbLapDayPhong.setProgress(percent);
            cPhong.close();
        }

        loadCoCauKhoanThu(db, whereHD.toString(), argsHD);
    }

    private void loadCoCauKhoanThu(SQLiteDatabase db, String whereClause, List<String> args) {
        layoutCoCauKhoanThu.removeAllViews();
        String sql = "SELECT ct.LoaiPhi, COALESCE(SUM(ct.ThanhTien), 0) " +
                "FROM CHITIETHOADON ct " +
                "JOIN HOADON h ON ct.MaHD = h.MaHD " +
                "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                whereClause +
                " GROUP BY ct.LoaiPhi ORDER BY SUM(ct.ThanhTien) DESC";

        List<String> tenPhiList = new ArrayList<>();
        List<Double> tienPhiList = new ArrayList<>();
        double tongAll = 0;

        Cursor c = db.rawQuery(sql, args.toArray(new String[0]));
        if (c != null) {
            while (c.moveToNext()) {
                String loaiPhi = c.getString(0);
                double tien = c.getDouble(1);
                tenPhiList.add(loaiPhi);
                tienPhiList.add(tien);
                tongAll += tien;
            }
            c.close();
        }

        if (tenPhiList.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Chưa có dữ liệu khoản thu trong kỳ này");
            tvEmpty.setTextColor(Color.parseColor("#888888"));
            tvEmpty.setTextSize(13);
            layoutCoCauKhoanThu.addView(tvEmpty);
            return;
        }

        for (int i = 0; i < tenPhiList.size(); i++) {
            String ten = tenPhiList.get(i);
            double tien = tienPhiList.get(i);
            int pct = tongAll > 0 ? (int) Math.round(tien * 100.0 / tongAll) : 0;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, 8, 0, 8);

            LinearLayout topLine = new LinearLayout(this);
            topLine.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvTen = new TextView(this);
            tvTen.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvTen.setText(ten + " (" + pct + "%)");
            tvTen.setTextColor(Color.parseColor("#222222"));
            tvTen.setTextSize(13);

            TextView tvTien = new TextView(this);
            tvTien.setText(String.format(Locale.getDefault(), "%,.0f đ", tien));
            tvTien.setTextColor(Color.parseColor("#245C45"));
            tvTien.setTextSize(13);
            tvTien.setTypeface(null, Typeface.BOLD);

            topLine.addView(tvTen);
            topLine.addView(tvTien);

            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 14);
            barParams.setMargins(0, 6, 0, 0);
            bar.setLayoutParams(barParams);
            bar.setMax(100);
            bar.setProgress(pct);
            bar.getProgressDrawable().setColorFilter(Color.parseColor("#245C45"), PorterDuff.Mode.SRC_IN);

            row.addView(topLine);
            row.addView(bar);
            layoutCoCauKhoanThu.addView(row);
        }
    }
}