package com.example.btlquanlynhatro;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ChiTietHoaDonActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtTieuDeHeader, txtChiTietPhong, txtChiTietNguoiThue, txtChiTietKy, txtChiTietNgayLap;
    private TextView txtChiTietTienPhong, txtChiTietTienDien, txtChiTietSubDien;
    private TextView txtChiTietTienNuoc, txtChiTietSubNuoc, txtChiTietInternet, txtChiTietDichVu, txtChiTietHanThanhToan;;
    private TextView txtChiTietTongTien, txtChiTietTrangThai;
    private MaterialButton btnGhiNhanThanhToan, btnXuatHoaDon, btnSuaHoaDon, btnXoaHoaDon;
    private DatabaseHelper dbHelper;

    private String maHD = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_hoa_don);

        dbHelper = new DatabaseHelper(this);

        maHD = getIntent().getStringExtra("MaHD");
        if (maHD == null) maHD = "";

        btnBack = findViewById(R.id.btnBack);
        txtTieuDeHeader = findViewById(R.id.txtTieuDeHeader);
        txtChiTietPhong = findViewById(R.id.txtChiTietPhong);
        txtChiTietNguoiThue = findViewById(R.id.txtChiTietNguoiThue);
        txtChiTietKy = findViewById(R.id.txtChiTietKy);
        txtChiTietNgayLap = findViewById(R.id.txtChiTietNgayLap);
        txtChiTietHanThanhToan = findViewById(R.id.txtChiTietHanThanhToan);
        txtChiTietTienPhong = findViewById(R.id.txtChiTietTienPhong);
        txtChiTietTienDien = findViewById(R.id.txtChiTietTienDien);
        txtChiTietSubDien = findViewById(R.id.txtChiTietSubDien);
        txtChiTietTienNuoc = findViewById(R.id.txtChiTietTienNuoc);
        txtChiTietSubNuoc = findViewById(R.id.txtChiTietSubNuoc);
        txtChiTietInternet = findViewById(R.id.txtChiTietInternet);
        txtChiTietDichVu = findViewById(R.id.txtChiTietDichVu);
        txtChiTietTongTien = findViewById(R.id.txtChiTietTongTien);
        txtChiTietTrangThai = findViewById(R.id.txtChiTietTrangThai);
        btnGhiNhanThanhToan = findViewById(R.id.btnGhiNhanThanhToan);
        btnXuatHoaDon = findViewById(R.id.btnXuatHoaDon);
        btnSuaHoaDon = findViewById(R.id.btnSuaHoaDon);
        btnXoaHoaDon = findViewById(R.id.btnXoaHoaDon);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        loadThongTinHoaDon();

        btnGhiNhanThanhToan.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận thanh toán")
                    .setMessage("Xác nhận đã thu đủ tiền và ghi nhận hóa đơn này là Đã thanh toán? Sau khi ghi nhận sẽ không thể sửa hoặc xóa hóa đơn.")
                    .setPositiveButton("Ghi nhận", (dialog, which) -> {
                        boolean success = updateTrangThaiThanhToan("Đã thanh toán");
                        if (success) {
                            Toast.makeText(this, "Đã ghi nhận thanh toán thành công!", Toast.LENGTH_SHORT).show();
                            loadThongTinHoaDon();
                        } else {
                            Toast.makeText(this, "Lỗi cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        btnSuaHoaDon.setOnClickListener(v -> {
            Intent intent = new Intent(ChiTietHoaDonActivity.this, ThemSuaHoaDonActivity.class);
            intent.putExtra("MaHD", maHD);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        btnXoaHoaDon.setOnClickListener(v -> hienDialogXacNhanXoa());

        btnXuatHoaDon.setOnClickListener(v -> {
            File pdfFile = taoFilePdfHoaDon();
            if (pdfFile == null) {
                Toast.makeText(this, "Không thể tạo file PDF hóa đơn!", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] options = {"In hóa đơn (PDF / Máy in)", "Chia sẻ file PDF (Zalo, Gmail...)"};
            new AlertDialog.Builder(this)
                    .setTitle("Xuất / Chia sẻ hóa đơn")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            inFilePdf(pdfFile);
                        } else {
                            chiaSeFilePdf(pdfFile);
                        }
                    })
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadThongTinHoaDon();
    }

    private void loadThongTinHoaDon() {
        if (maHD.isEmpty()) return;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT h.MaHD, p.TenPhong, nt.HoTen, h.Thang, h.Nam, h.NgayLap, h.HanThanhToan, " +
                "h.DienDau, h.DienCuoi, h.NuocDau, h.NuocCuoi, h.TongTien, h.TrangThai " +
                "FROM HOADON h " +
                "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                "JOIN NGUOITHUE nt ON hd.MaNguoiThue = nt.MaNguoiThue " +
                "WHERE h.MaHD = ?";

        double dienDau = 0, dienCuoi = 0, nuocDau = 0, nuocCuoi = 0;

        Cursor cursor = db.rawQuery(query, new String[]{maHD});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String idHD = cursor.getString(0);
                String tenPhong = cursor.getString(1);
                String tenNguoiThue = cursor.getString(2);
                int thang = cursor.getInt(3);
                int nam = cursor.getInt(4);
                String ngayLap = cursor.getString(5);
                String hanThanhToan = cursor.getString(6);
                dienDau = cursor.getDouble(7);
                dienCuoi = cursor.getDouble(8);
                nuocDau = cursor.getDouble(9);
                nuocCuoi = cursor.getDouble(10);
                double tongTien = cursor.getDouble(11);
                String trangThai = cursor.getString(12);

                txtTieuDeHeader.setText("HĐ-" + idHD + " · Chi tiết hóa đơn");
                txtChiTietPhong.setText(tenPhong != null ? tenPhong : "—");
                txtChiTietNguoiThue.setText(tenNguoiThue != null ? tenNguoiThue : "—");
                txtChiTietKy.setText(thang + "/" + nam);
                txtChiTietNgayLap.setText(ngayLap != null && !ngayLap.isEmpty() ? ngayLap : "—");
                if (txtChiTietHanThanhToan != null) {
                    txtChiTietHanThanhToan.setText(hanThanhToan != null && !hanThanhToan.isEmpty() ? hanThanhToan : "—");
                }
                txtChiTietTongTien.setText(String.format("%,.0f đ", tongTien));
                txtChiTietTrangThai.setText(trangThai != null ? trangThai : "");

                if ("Đã thanh toán".equalsIgnoreCase(trangThai)) {
                    txtChiTietTrangThai.setTextColor(0xFF245C45);
                    btnGhiNhanThanhToan.setText("Đã thanh toán");
                    btnGhiNhanThanhToan.setEnabled(false);
                    btnGhiNhanThanhToan.setAlpha(0.6f);

                    if (btnSuaHoaDon != null) btnSuaHoaDon.setVisibility(View.GONE);
                    if (btnXoaHoaDon != null) btnXoaHoaDon.setVisibility(View.GONE);
                } else {
                    txtChiTietTrangThai.setTextColor(0xFFD97706);
                    btnGhiNhanThanhToan.setText("Ghi nhận thanh toán");
                    btnGhiNhanThanhToan.setEnabled(true);
                    btnGhiNhanThanhToan.setAlpha(1.0f);

                    if (btnSuaHoaDon != null) btnSuaHoaDon.setVisibility(View.VISIBLE);
                    if (btnXoaHoaDon != null) btnXoaHoaDon.setVisibility(View.VISIBLE);
                }
            }
            cursor.close();
        }

        // Đặt mặc định trước khi đọc chi tiết
        txtChiTietTienPhong.setText("0 đ");
        txtChiTietTienDien.setText("0 đ");
        txtChiTietTienNuoc.setText("0 đ");
        txtChiTietInternet.setText("0 đ");
        txtChiTietDichVu.setText("0 đ");

        Cursor cursorCT = db.rawQuery("SELECT LoaiPhi, SoLuong, DonGia, ThanhTien FROM CHITIETHOADON WHERE MaHD = ?", new String[]{maHD});
        if (cursorCT != null) {
            while (cursorCT.moveToNext()) {
                String loaiPhi = cursorCT.getString(0);
                double donGia = cursorCT.getDouble(2);
                double thanhTien = cursorCT.getDouble(3);

                if ("Tiền phòng".equals(loaiPhi)) {
                    txtChiTietTienPhong.setText(String.format("%,.0f đ", thanhTien));
                } else if ("Tiền điện".equals(loaiPhi)) {
                    txtChiTietTienDien.setText(String.format("%,.0f đ", thanhTien));
                    txtChiTietSubDien.setText(String.format(
                            "Chỉ số %.0f → %.0f · %.0f kWh (Đơn giá: %,.0f đ)",
                            dienDau, dienCuoi, (dienCuoi - dienDau), donGia
                    ));
                } else if ("Tiền nước".equals(loaiPhi)) {
                    txtChiTietTienNuoc.setText(String.format("%,.0f đ", thanhTien));
                    txtChiTietSubNuoc.setText(String.format(
                            "Chỉ số %.0f → %.0f · %.0f m³ (Đơn giá: %,.0f đ)",
                            nuocDau, nuocCuoi, (nuocCuoi - nuocDau), donGia
                    ));
                } else if ("Tiền Internet".equals(loaiPhi)) {
                    txtChiTietInternet.setText(String.format("%,.0f đ", thanhTien));
                } else if ("Tiền dịch vụ".equals(loaiPhi)) {
                    txtChiTietDichVu.setText(String.format("%,.0f đ", thanhTien));
                }
            }
            cursorCT.close();
        }
    }

    private void hienDialogXacNhanXoa() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa hóa đơn")
                .setMessage("Bạn có chắc chắn muốn xóa hóa đơn này không? Thao tác này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean success = xoaHoaDonKhoiDB();
                    if (success) {
                        Toast.makeText(this, "Đã xóa hóa đơn thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(0, 0);
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa hóa đơn!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private boolean xoaHoaDonKhoiDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete("HOADON", "MaHD = ?", new String[]{maHD});
        return rows > 0;
    }

    private boolean updateTrangThaiThanhToan(String trangThaiMoi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TrangThai", trangThaiMoi);
        int rows = db.update("HOADON", values, "MaHD = ?", new String[]{maHD});
        return rows > 0;
    }
    // Xử lý xuất hóa đơn
    private File taoFilePdfHoaDon() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(420, 595, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#245C45"));
        titlePaint.setTextSize(18f);
        titlePaint.setFakeBoldText(true);

        Paint boldPaint = new Paint();
        boldPaint.setColor(Color.BLACK);
        boldPaint.setTextSize(12f);
        boldPaint.setFakeBoldText(true);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(12f);

        Paint subPaint = new Paint();
        subPaint.setColor(Color.GRAY);
        subPaint.setTextSize(10f);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(1f);

        int xLeft = 30;
        int xRight = 390;
        int y = 45;

        canvas.drawText("HÓA ĐƠN TIỀN PHÒNG TRỌ", xLeft, y, titlePaint);
        y += 22;
        canvas.drawText(txtTieuDeHeader.getText().toString(), xLeft, y, boldPaint);
        y += 16;
        canvas.drawLine(xLeft, y, xRight, y, linePaint);
        y += 24;

        y = veDongThongTin(canvas, "Phòng:", txtChiTietPhong.getText().toString(), xLeft, xRight, y, textPaint, boldPaint);
        y = veDongThongTin(canvas, "Người thuê:", txtChiTietNguoiThue.getText().toString(), xLeft, xRight, y, textPaint, boldPaint);
        y = veDongThongTin(canvas, "Kỳ thanh toán:", txtChiTietKy.getText().toString(), xLeft, xRight, y, textPaint, boldPaint);
        y = veDongThongTin(canvas, "Ngày lập:", txtChiTietNgayLap.getText().toString(), xLeft, xRight, y, textPaint, boldPaint);

        y += 6;
        canvas.drawLine(xLeft, y, xRight, y, linePaint);
        y += 24;

        y = veDongThongTin(canvas, "Tiền phòng:", txtChiTietTienPhong.getText().toString(), xLeft, xRight, y, textPaint, textPaint);

        y = veDongThongTin(canvas, "Tiền điện:", txtChiTietTienDien.getText().toString(), xLeft, xRight, y, textPaint, textPaint);
        canvas.drawText(txtChiTietSubDien.getText().toString(), xLeft, y - 6, subPaint);
        y += 14;

        y = veDongThongTin(canvas, "Tiền nước:", txtChiTietTienNuoc.getText().toString(), xLeft, xRight, y, textPaint, textPaint);
        canvas.drawText(txtChiTietSubNuoc.getText().toString(), xLeft, y - 6, subPaint);
        y += 14;

        y = veDongThongTin(canvas, "Tiền Internet:", txtChiTietInternet.getText().toString(), xLeft, xRight, y, textPaint, textPaint);
        y = veDongThongTin(canvas, "Tiền dịch vụ:", txtChiTietDichVu.getText().toString(), xLeft, xRight, y, textPaint, textPaint);

        y += 6;
        canvas.drawLine(xLeft, y, xRight, y, linePaint);
        y += 26;

        titlePaint.setTextSize(15f);
        y = veDongThongTin(canvas, "TỔNG CỘNG:", txtChiTietTongTien.getText().toString(), xLeft, xRight, y, boldPaint, titlePaint);
        y = veDongThongTin(canvas, "Trạng thái:", txtChiTietTrangThai.getText().toString(), xLeft, xRight, y, textPaint, boldPaint);

        document.finishPage(page);

        try {
            File dir = new File(getCacheDir(), "hoadon");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "HoaDon_" + maHD + ".pdf");
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            fos.close();
            document.close();
            return file;
        } catch (IOException e) {
            document.close();
            return null;
        }
    }

    private int veDongThongTin(Canvas canvas, String label, String value, int xLeft, int xRight, int y, Paint leftPaint, Paint rightPaint) {
        canvas.drawText(label, xLeft, y, leftPaint);
        float valueWidth = rightPaint.measureText(value);
        canvas.drawText(value, xRight - valueWidth, y, rightPaint);
        return y + 22;
    }

    private void inFilePdf(File pdfFile) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = "HoaDon_" + maHD;

        PrintDocumentAdapter printAdapter = new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                 CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }
                PrintDocumentInfo info = new PrintDocumentInfo.Builder("HoaDon_" + maHD + ".pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build();
                callback.onLayoutFinished(info, true);
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                                CancellationSignal cancellationSignal, WriteResultCallback callback) {
                try (FileInputStream in = new FileInputStream(pdfFile);
                     FileOutputStream out = new FileOutputStream(destination.getFileDescriptor())) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                } catch (Exception e) {
                    callback.onWriteFailed(e.getMessage());
                }
            }
        };

        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
    }

    private void chiaSeFilePdf(File pdfFile) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hóa đơn tiền phòng HĐ-" + maHD);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Gửi hóa đơn qua"));
    }
}