package com.example.btlquanlynhatro;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ThemSuaHoaDonActivity extends AppCompatActivity {

    private EditText edtChonNhaTro, edtChonPhong, edtKyThang, edtTienPhong;
    private EditText edtDienDau, edtDienCuoi, edtNuocDau, edtNuocCuoi, edtTienInternet, edtTienDichVu, edtHanThanhToan;
    private TextView txtTieuDe;
    private MaterialButton btnLuuNhap, btnXacNhanHoaDon;
    private DatabaseHelper dbHelper;
    private int maTK = -1;

    private String maHopDong = null;
    private String maHoaDonEdit = null;
    private boolean isEditMode = false;
    private String trangThaiHienTai = "Chưa thanh toán";

    private int selectedMaPhong = -1;
    private double donGiaDienHD = 3500;
    private double donGiaNuocHD = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_sua_hoa_don);

        dbHelper = new DatabaseHelper(this);
        dbHelper.capNhatHoaDonQuaHan();

        SharedPreferences prefs = getSharedPreferences("SoTroPrefs", MODE_PRIVATE);
        maTK = prefs.getInt("MaTK", -1);

        maHoaDonEdit = getIntent().getStringExtra("MaHD");
        isEditMode = (maHoaDonEdit != null && !maHoaDonEdit.isEmpty());

        maHopDong = getIntent().getStringExtra("MaHopDong");
        selectedMaPhong = getIntent().getIntExtra("MaPhong", -1);

        ImageView btnBack = findViewById(R.id.btnBack);
        txtTieuDe = findViewById(R.id.txtTieuDe);
        edtChonNhaTro = findViewById(R.id.edtChonNhaTro);
        edtChonPhong = findViewById(R.id.edtChonPhong);
        edtKyThang = findViewById(R.id.edtKyThang);
        edtTienPhong = findViewById(R.id.edtTienPhong);
        edtDienDau = findViewById(R.id.edtDienDau);
        edtDienCuoi = findViewById(R.id.edtDienCuoi);
        edtNuocDau = findViewById(R.id.edtNuocDau);
        edtNuocCuoi = findViewById(R.id.edtNuocCuoi);
        edtTienInternet = findViewById(R.id.edtTienInternet);
        edtTienDichVu = findViewById(R.id.edtTienDichVu);
        edtHanThanhToan = findViewById(R.id.edtHanThanhToan);
        btnLuuNhap = findViewById(R.id.btnLuuNhap);
        btnXacNhanHoaDon = findViewById(R.id.btnXacNhanHoaDon);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        String currentMonthYear = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        edtKyThang.setText(currentMonthYear);
        edtHanThanhToan.setOnClickListener(v -> showDatePicker(edtHanThanhToan));

        if (isEditMode) {
            txtTieuDe.setText("Sửa hóa đơn");
            edtChonNhaTro.setEnabled(false);
            edtChonPhong.setEnabled(false);
            edtKyThang.setEnabled(false);
            if (btnLuuNhap != null) btnLuuNhap.setVisibility(View.GONE);
            btnXacNhanHoaDon.setText("Cập nhật hóa đơn");
            loadThongTinHoaDonDeSua(maHoaDonEdit);
            btnXacNhanHoaDon.setOnClickListener(v -> capNhatHoaDonTrongDB());
        } else if (maHopDong != null && !maHopDong.isEmpty()) {
            edtChonNhaTro.setEnabled(false);
            edtChonPhong.setEnabled(false);
            loadThongTinHopDong(maHopDong);
            btnXacNhanHoaDon.setOnClickListener(v -> luuHoaDonVaoDB("Chưa thanh toán"));
        } else {
            edtChonNhaTro.setOnClickListener(v -> hienDialogChonNhaTro());
            edtChonPhong.setOnClickListener(v -> {
                String nhaTro = edtChonNhaTro.getText().toString().trim();
                if (nhaTro.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn nhà trọ trước!", Toast.LENGTH_SHORT).show();
                    hienDialogChonNhaTro();
                } else {
                    Toast.makeText(this, "Vui lòng bấm vào ô Nhà trọ để đổi nhà/phòng khác!", Toast.LENGTH_SHORT).show();
                }
            });
            btnXacNhanHoaDon.setOnClickListener(v -> luuHoaDonVaoDB("Chưa thanh toán"));
        }
    }

    private void loadThongTinHoaDonDeSua(String idHD) {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT h.MaHopDong, h.Thang, h.Nam, h.DienDau, h.DienCuoi, h.NuocDau, h.NuocCuoi, " +
                    "h.TrangThai, h.HanThanhToan, p.MaPhong, p.TenPhong, nt.TenNhaTro, " +
                    "hd.GiaPhong, hd.PhiInternet, hd.PhiDichVu, hd.DonGiaDien, hd.DonGiaNuoc " +
                    "FROM HOADON h " +
                    "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                    "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                    "JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro " +
                    "WHERE h.MaHD = ?";

            Cursor c = db.rawQuery(sql, new String[]{idHD});
            if (c != null) {
                if (c.moveToFirst()) {
                    maHopDong = c.getString(0);
                    int thang = c.getInt(1);
                    int nam = c.getInt(2);
                    double dienDau = c.getDouble(3);
                    double dienCuoi = c.getDouble(4);
                    double nuocDau = c.getDouble(5);
                    double nuocCuoi = c.getDouble(6);
                    trangThaiHienTai = c.getString(7) != null ? c.getString(7) : "Chưa thanh toán";
                    String hanThanhToan = c.getString(8);
                    selectedMaPhong = c.getInt(9);
                    String tenPhong = c.getString(10);
                    String tenNhaTro = c.getString(11);
                    double giaPhong = c.getDouble(12);
                    double phiInternet = c.getDouble(13);
                    double phiDichVu = c.getDouble(14);
                    donGiaDienHD = c.getDouble(15) > 0 ? c.getDouble(15) : 3500;
                    donGiaNuocHD = c.getDouble(16) > 0 ? c.getDouble(16) : 15000;

                    edtChonNhaTro.setText(tenNhaTro != null ? tenNhaTro : "");
                    edtChonPhong.setText(tenPhong != null ? tenPhong : "");
                    edtKyThang.setText(String.format(Locale.getDefault(), "%02d/%d", thang, nam));
                    edtTienPhong.setText(String.valueOf((int) giaPhong));
                    edtDienDau.setText(String.valueOf((int) dienDau));
                    edtDienCuoi.setText(String.valueOf((int) dienCuoi));
                    edtNuocDau.setText(String.valueOf((int) nuocDau));
                    edtNuocCuoi.setText(String.valueOf((int) nuocCuoi));
                    edtTienInternet.setText(String.valueOf((int) phiInternet));
                    edtTienDichVu.setText(String.valueOf((int) phiDichVu));
                    if (hanThanhToan != null && !hanThanhToan.isEmpty()) {
                        edtHanThanhToan.setText(hanThanhToan);
                    }
                }
                c.close();
            }

            Cursor cCT = db.rawQuery("SELECT LoaiPhi, DonGia, ThanhTien FROM CHITIETHOADON WHERE MaHD = ?", new String[]{idHD});
            if (cCT != null) {
                while (cCT.moveToNext()) {
                    String loaiPhi = cCT.getString(0);
                    double donGia = cCT.getDouble(1);
                    double thanhTien = cCT.getDouble(2);
                    if ("Tiền phòng".equals(loaiPhi)) {
                        edtTienPhong.setText(String.valueOf((int) thanhTien));
                    } else if ("Tiền điện".equals(loaiPhi)) {
                        if (donGia > 0) donGiaDienHD = donGia;
                    } else if ("Tiền nước".equals(loaiPhi)) {
                        if (donGia > 0) donGiaNuocHD = donGia;
                    } else if ("Tiền Internet".equals(loaiPhi)) {
                        edtTienInternet.setText(String.valueOf((int) thanhTien));
                    } else if ("Tiền dịch vụ".equals(loaiPhi)) {
                        edtTienDichVu.setText(String.valueOf((int) thanhTien));
                    }
                }
                cCT.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tải dữ liệu sửa hóa đơn: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void capNhatHoaDonTrongDB() {
        try {
            double dienDau = parseDoubleSafe(edtDienDau.getText().toString());
            double dienCuoi = parseDoubleSafe(edtDienCuoi.getText().toString());
            double nuocDau = parseDoubleSafe(edtNuocDau.getText().toString());
            double nuocCuoi = parseDoubleSafe(edtNuocCuoi.getText().toString());

            if (dienCuoi < dienDau) {
                Toast.makeText(this, "Lỗi: Chỉ số điện cuối không được nhỏ hơn chỉ số đầu!", Toast.LENGTH_SHORT).show();
                edtDienCuoi.requestFocus();
                return;
            }
            if (nuocCuoi < nuocDau) {
                Toast.makeText(this, "Lỗi: Chỉ số nước cuối không được nhỏ hơn chỉ số đầu!", Toast.LENGTH_SHORT).show();
                edtNuocCuoi.requestFocus();
                return;
            }

            String hanThanhToan = edtHanThanhToan.getText().toString().trim();
            if (!hanThanhToan.isEmpty() && isNgayQuaKhu(hanThanhToan)) {
                Toast.makeText(this, "Hạn thanh toán không được là ngày trong quá khứ!", Toast.LENGTH_SHORT).show();
                edtHanThanhToan.requestFocus();
                return;
            }

            double tienPhong = parseDoubleSafe(edtTienPhong.getText().toString());
            double tienInternet = parseDoubleSafe(edtTienInternet.getText().toString());
            double tienDichVu = parseDoubleSafe(edtTienDichVu.getText().toString());

            double soDien = dienCuoi - dienDau;
            double tienDien = soDien * donGiaDienHD;

            double soNuoc = nuocCuoi - nuocDau;
            double tienNuoc = soNuoc * donGiaNuocHD;

            double tongTien = tienPhong + tienDien + tienNuoc + tienInternet + tienDichVu;

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues hdValues = new ContentValues();
                hdValues.put("DienDau", dienDau);
                hdValues.put("DienCuoi", dienCuoi);
                hdValues.put("NuocDau", nuocDau);
                hdValues.put("NuocCuoi", nuocCuoi);
                hdValues.put("TongTien", tongTien);
                hdValues.put("HanThanhToan", hanThanhToan);

                if ("Quá hạn".equals(trangThaiHienTai) && !hanThanhToan.isEmpty() && !isNgayQuaKhu(hanThanhToan)) {
                    hdValues.put("TrangThai", "Chưa thanh toán");
                }

                int updated = db.update("HOADON", hdValues, "MaHD = ?", new String[]{maHoaDonEdit});
                if (updated <= 0) {
                    Toast.makeText(this, "Không tìm thấy hóa đơn để cập nhật!", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.delete("CHITIETHOADON", "MaHD = ?", new String[]{maHoaDonEdit});
                insertChiTietPhi(db, maHoaDonEdit, "Tiền phòng", 1, tienPhong, tienPhong);
                insertChiTietPhi(db, maHoaDonEdit, "Tiền điện", soDien, donGiaDienHD, tienDien);
                insertChiTietPhi(db, maHoaDonEdit, "Tiền nước", soNuoc, donGiaNuocHD, tienNuoc);
                insertChiTietPhi(db, maHoaDonEdit, "Tiền Internet", 1, tienInternet, tienInternet);
                insertChiTietPhi(db, maHoaDonEdit, "Tiền dịch vụ", 1, tienDichVu, tienDichVu);

                db.setTransactionSuccessful();
                Toast.makeText(this, "Cập nhật hóa đơn thành công! Tổng: " + String.format("%,.0f đ", tongTien), Toast.LENGTH_LONG).show();
                finish();
                overridePendingTransition(0, 0);
            } finally {
                db.endTransaction();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi cập nhật hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void insertChiTietPhi(SQLiteDatabase db, String idHD, String loaiPhi, double soLuong, double donGia, double thanhTien) {
        ContentValues ct = new ContentValues();
        ct.put("MaHD", idHD);
        ct.put("LoaiPhi", loaiPhi);
        ct.put("SoLuong", soLuong);
        ct.put("DonGia", donGia);
        ct.put("ThanhTien", thanhTien);
        db.insert("CHITIETHOADON", null, ct);
    }

    private void hienDialogChonNhaTro() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_danh_sach, null);

        TextView txtTieuDeDialog = dialogView.findViewById(R.id.txtTieuDeDialog);
        EditText edtSearch = dialogView.findViewById(R.id.edtSearch);
        LinearLayout container = dialogView.findViewById(R.id.layoutContainerDialog);
        TextView btnHuy = dialogView.findViewById(R.id.btnHuyDialog);

        txtTieuDeDialog.setText("Chọn nhà trọ");
        edtSearch.setHint("Tìm tên hoặc địa chỉ nhà trọ...");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnHuy.setOnClickListener(v -> dialog.dismiss());

        loadDanhSachNhaTro(container, dialog, "");

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadDanhSachNhaTro(container, dialog, s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void loadDanhSachNhaTro(LinearLayout container, AlertDialog dialog, String keyword) {
        container.removeAllViews();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT nt.MaNhaTro, nt.TenNhaTro, nt.DiaChi FROM NHATRO nt " +
                        "JOIN PHONG p ON nt.MaNhaTro = p.MaNhaTro " +
                        "JOIN HOPDONG hd ON p.MaPhong = hd.MaPhong " +
                        "WHERE hd.TrangThai = 'Còn hiệu lực' AND nt.MaTK = ?"
        );
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(maTK));

        if (!keyword.isEmpty()) {
            sql.append(" AND (nt.TenNhaTro LIKE ? OR nt.DiaChi LIKE ?)");
            String pattern = "%" + keyword + "%";
            args.add(pattern);
            args.add(pattern);
        }

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int maNhaTro = cursor.getInt(0);
                String tenNhaTro = cursor.getString(1);
                String diaChi = cursor.getString(2);

                View itemView = LayoutInflater.from(this).inflate(R.layout.item_nha_tro_card, container, false);
                TextView txtTen = itemView.findViewById(R.id.txtTenNhaTroItem);
                TextView txtDiaChi = itemView.findViewById(R.id.txtDiaChiItem);

                if (txtTen != null) txtTen.setText(tenNhaTro);
                if (txtDiaChi != null) txtDiaChi.setText(diaChi);

                itemView.setOnClickListener(v -> {
                    dialog.dismiss();
                    edtChonNhaTro.setText(tenNhaTro);
                    edtChonPhong.setText("");
                    selectedMaPhong = -1;
                    maHopDong = null;

                    hienDialogChonPhong(maNhaTro, tenNhaTro);
                });

                container.addView(itemView);
            }
            cursor.close();
        }
    }

    private void hienDialogChonPhong(int maNhaTro, String tenNhaTro) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_danh_sach, null);

        TextView txtTieuDeDialog = dialogView.findViewById(R.id.txtTieuDeDialog);
        EditText edtSearch = dialogView.findViewById(R.id.edtSearch);
        LinearLayout container = dialogView.findViewById(R.id.layoutContainerDialog);
        TextView btnHuy = dialogView.findViewById(R.id.btnHuyDialog);

        txtTieuDeDialog.setText("Chọn phòng (" + tenNhaTro + ")");
        edtSearch.setHint("Tìm tên phòng...");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnHuy.setOnClickListener(v -> dialog.dismiss());

        loadDanhSachPhong(maNhaTro, container, dialog, "");

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadDanhSachPhong(maNhaTro, container, dialog, s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void loadDanhSachPhong(int maNhaTro, LinearLayout container, AlertDialog dialog, String keyword) {
        container.removeAllViews();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder sql = new StringBuilder(
                "SELECT p.MaPhong, p.TenPhong, hd.GiaPhong, hd.MaHopDong, hd.PhiInternet, hd.PhiDichVu, hd.DonGiaDien, hd.DonGiaNuoc " +
                        "FROM PHONG p " +
                        "JOIN HOPDONG hd ON p.MaPhong = hd.MaPhong " +
                        "WHERE p.MaNhaTro = ? AND hd.TrangThai = 'Còn hiệu lực'"
        );
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(maNhaTro));

        if (!keyword.isEmpty()) {
            sql.append(" AND p.TenPhong LIKE ?");
            args.add("%" + keyword + "%");
        }

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int maPhong = cursor.getInt(0);
                String tenPhong = cursor.getString(1);
                double giaThue = cursor.getDouble(2);
                String hdId = cursor.getString(3);
                double tienInternet = cursor.getDouble(4);
                double tienDichVu = cursor.getDouble(5);
                double donGiaD = cursor.getDouble(6);
                double donGiaN = cursor.getDouble(7);

                View itemView = LayoutInflater.from(this).inflate(R.layout.item_phong_card, container, false);
                TextView txtTenPhong = itemView.findViewById(R.id.txtTenVaTrangThai);
                TextView txtGia = itemView.findViewById(R.id.txtChiTietPhong);

                if (txtTenPhong != null) txtTenPhong.setText(tenPhong + " · Đang thuê");
                if (txtGia != null) txtGia.setText(String.format("Giá thuê: %,.0f đ/tháng", giaThue));

                itemView.setOnClickListener(v -> {
                    dialog.dismiss();
                    selectedMaPhong = maPhong;
                    maHopDong = hdId;
                    donGiaDienHD = donGiaD > 0 ? donGiaD : 3500;
                    donGiaNuocHD = donGiaN > 0 ? donGiaN : 15000;
                    edtChonPhong.setText(tenPhong);

                    edtTienPhong.setText(String.valueOf((int) giaThue));
                    edtTienInternet.setText(String.valueOf((int) tienInternet));
                    edtTienDichVu.setText(String.valueOf((int) tienDichVu));
                });

                container.addView(itemView);
            }
            cursor.close();
        }
    }

    private void loadThongTinHopDong(String maHD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT p.TenPhong, nt.TenNhaTro, hd.GiaPhong, hd.PhiInternet, hd.PhiDichVu, hd.DonGiaDien, hd.DonGiaNuoc FROM HOPDONG hd " +
                        "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                        "JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro " +
                        "WHERE hd.MaHopDong = ?",
                new String[]{maHD}
        );
        if (cursor != null && cursor.moveToFirst()) {
            edtChonNhaTro.setText(cursor.getString(1));
            edtChonPhong.setText(cursor.getString(0));
            edtTienPhong.setText(String.valueOf((int) cursor.getDouble(2)));
            edtTienInternet.setText(String.valueOf((int) cursor.getDouble(3)));
            edtTienDichVu.setText(String.valueOf((int) cursor.getDouble(4)));
            donGiaDienHD = cursor.getDouble(5) > 0 ? cursor.getDouble(5) : 3500;
            donGiaNuocHD = cursor.getDouble(6) > 0 ? cursor.getDouble(6) : 15000;
            cursor.close();
        }
    }

    private void luuHoaDonVaoDB(String trangThai) {
        if (maHopDong == null || maHopDong.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ Nhà trọ và Phòng đang có người thuê!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double dienDau = parseDoubleSafe(edtDienDau.getText().toString());
            double dienCuoi = parseDoubleSafe(edtDienCuoi.getText().toString());
            double nuocDau = parseDoubleSafe(edtNuocDau.getText().toString());
            double nuocCuoi = parseDoubleSafe(edtNuocCuoi.getText().toString());

            if (dienCuoi < dienDau) {
                Toast.makeText(this, "Lỗi: Chỉ số điện cuối không được nhỏ hơn chỉ số đầu!", Toast.LENGTH_SHORT).show();
                edtDienCuoi.requestFocus();
                return;
            }

            if (nuocCuoi < nuocDau) {
                Toast.makeText(this, "Lỗi: Chỉ số nước cuối không được nhỏ hơn chỉ số đầu!", Toast.LENGTH_SHORT).show();
                edtNuocCuoi.requestFocus();
                return;
            }

            String hanThanhToan = edtHanThanhToan.getText().toString().trim();
            if (!hanThanhToan.isEmpty() && isNgayQuaKhu(hanThanhToan)) {
                Toast.makeText(this, "Hạn thanh toán không được là ngày trong quá khứ!", Toast.LENGTH_SHORT).show();
                edtHanThanhToan.requestFocus();
                return;
            }

            String kyThangStr = edtKyThang.getText().toString().trim();
            double tienPhong = parseDoubleSafe(edtTienPhong.getText().toString());
            double tienInternet = parseDoubleSafe(edtTienInternet.getText().toString());
            double tienDichVu = parseDoubleSafe(edtTienDichVu.getText().toString());

            int thang = 1, nam = 2026;
            if (kyThangStr.contains("/")) {
                String[] parts = kyThangStr.split("/");
                thang = parseIntSafe(parts[0]);
                nam = parseIntSafe(parts[1]);
            }
            if (isTrungHoaDonCuaPhong(maHopDong, thang, nam)) {
                Toast.makeText(this, "Phòng này đã có hóa đơn kỳ " + thang + "/" + nam + " rồi!", Toast.LENGTH_LONG).show();
                return;
            }

            double soDienTieuThu = dienCuoi - dienDau;
            double tienDien = soDienTieuThu * donGiaDienHD;

            double soNuocTieuThu = nuocCuoi - nuocDau;
            double tienNuoc = soNuocTieuThu * donGiaNuocHD;

            double tongTien = tienPhong + tienDien + tienNuoc + tienInternet + tienDichVu;

            boolean success = dbHelper.themHoaDon(
                    maHopDong, thang, nam, tienPhong,
                    dienDau, dienCuoi, donGiaDienHD,
                    nuocDau, nuocCuoi, donGiaNuocHD,
                    tienInternet, tienDichVu,
                    tongTien, trangThai
            );

            if (success && !hanThanhToan.isEmpty()) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("HanThanhToan", hanThanhToan);
                db.update("HOADON", cv, "MaHopDong = ? AND Thang = ? AND Nam = ?",
                        new String[]{maHopDong, String.valueOf(thang), String.valueOf(nam)});
            }

            if (success) {
                Toast.makeText(this, "Lập hóa đơn thành công! Tổng: " + String.format("%,.0f đ", tongTien), Toast.LENGTH_LONG).show();
                finish();
                overridePendingTransition(0, 0);
            } else {
                Toast.makeText(this, "Lỗi: Có thể hóa đơn tháng này của phòng đã được lập trước đó!", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi nhập liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isTrungHoaDonCuaPhong(String idHopDong, int thang, int nam) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM HOADON h " +
                "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                "WHERE hd.MaPhong = (SELECT MaPhong FROM HOPDONG WHERE MaHopDong = ?) " +
                "AND h.Thang = ? AND h.Nam = ?";
        Cursor c = db.rawQuery(sql, new String[]{idHopDong, String.valueOf(thang), String.valueOf(nam)});
        boolean trung = false;
        if (c != null) {
            if (c.moveToFirst() && c.getInt(0) > 0) {
                trung = true;
            }
            c.close();
        }
        return trung;
    }

    private boolean isNgayQuaKhu(String ddMMyyyy) {
        if (ddMMyyyy == null || ddMMyyyy.trim().isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            Date target = sdf.parse(ddMMyyyy.trim());

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            return target != null && target.before(today.getTime());
        } catch (Exception e) {
            return false;
        }
    }

    private void showDatePicker(EditText edtTarget) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, day) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", day, (month + 1), year);
                    edtTarget.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        Calendar minDay = Calendar.getInstance();
        minDay.set(Calendar.HOUR_OF_DAY, 0);
        minDay.set(Calendar.MINUTE, 0);
        minDay.set(Calendar.SECOND, 0);
        minDay.set(Calendar.MILLISECOND, 0);
        dialog.getDatePicker().setMinDate(minDay.getTimeInMillis());

        dialog.show();
    }

    private double parseDoubleSafe(String val) {
        try { return val.isEmpty() ? 0 : Double.parseDouble(val); } catch (Exception e) { return 0; }
    }

    private int parseIntSafe(String val) {
        try { return val.isEmpty() ? 0 : Integer.parseInt(val); } catch (Exception e) { return 0; }
    }
}