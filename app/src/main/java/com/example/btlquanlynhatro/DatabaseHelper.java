package com.example.btlquanlynhatro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.List;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "quan_ly_nha_tro.db";
    private static final int DB_VERSION =5;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Bật kiểm tra khóa ngoại để bảo vệ toàn vẹn dữ liệu DB
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. TAIKHOAN — Quản lý tài khoản
        db.execSQL("CREATE TABLE TAIKHOAN (" +
                "MaTK INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TenDangNhap TEXT UNIQUE NOT NULL, " +
                "MatKhau TEXT NOT NULL, " +
                "VaiTro TEXT NOT NULL, " + // 'Admin' hoặc 'Chủ trọ'
                "HoTen TEXT NOT NULL, " +
                "Email TEXT, " +
                "SDT TEXT, " +
                "TrangThai TEXT DEFAULT 'Hoạt động')"); // 'Hoạt động' hoặc 'Bị khóa'

        // 2. NHATRO — Quản lý nhà trọ
        db.execSQL("CREATE TABLE NHATRO (" +
                "MaNhaTro INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MaTK INTEGER NOT NULL, " +
                "TenNhaTro TEXT NOT NULL, " +
                "DiaChi TEXT NOT NULL, " +
                "MoTa TEXT, " +
                "DonGiaDien REAL DEFAULT 0, " +
                "DonGiaNuoc REAL DEFAULT 0, " +
                "PhiInternet REAL DEFAULT 0, " +
                "PhiDichVu REAL DEFAULT 0, " +
                "FOREIGN KEY(MaTK) REFERENCES TAIKHOAN(MaTK) ON DELETE CASCADE)");


        // 3. PHONG — Quản lý phòng
        db.execSQL("CREATE TABLE PHONG (" +
                "MaPhong INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MaNhaTro INTEGER NOT NULL, " +
                "TenPhong TEXT NOT NULL, " +
                "DienTich REAL, " +
                "GiaPhong REAL NOT NULL, " +
                "TrangThai TEXT DEFAULT 'Còn trống', " + // 'Còn trống' hoặc 'Đã thuê'
                "FOREIGN KEY(MaNhaTro) REFERENCES NHATRO(MaNhaTro) ON DELETE CASCADE)");

        // 4. NGUOITHUE — Quản lý người thuê
        db.execSQL("CREATE TABLE NGUOITHUE (" +
                "MaNguoiThue INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "HoTen TEXT NOT NULL, " +
                "CCCD TEXT UNIQUE NOT NULL, " +
                "SDT TEXT NOT NULL, " +
                "NgaySinh TEXT, " +
                "QueQuan TEXT)");

        // 5. HOPDONG — Quản lý hợp đồng
        db.execSQL("CREATE TABLE HOPDONG (" +
                "MaHopDong TEXT PRIMARY KEY, " +
                "MaPhong INTEGER NOT NULL, " +
                "MaNguoiThue INTEGER NOT NULL, " +
                "NgayBatDau TEXT NOT NULL, " +
                "NgayKetThuc TEXT NOT NULL, " +
                "GiaPhong REAL NOT NULL DEFAULT 0, " +
                "DonGiaDien REAL NOT NULL DEFAULT 0, " +
                "DonGiaNuoc REAL NOT NULL DEFAULT 0, " +
                "PhiInternet REAL NOT NULL DEFAULT 0, " +
                "PhiDichVu REAL NOT NULL DEFAULT 0, " +
                "TienCoc REAL NOT NULL DEFAULT 0, " +
                "TrangThai TEXT DEFAULT 'Còn hiệu lực', " +
                "FOREIGN KEY(MaPhong) REFERENCES PHONG(MaPhong), " +
                "FOREIGN KEY(MaNguoiThue) REFERENCES NGUOITHUE(MaNguoiThue))");

        // 6. HOADON — Quản lý hóa đơn (Gộp luôn chỉ số điện nước vào đây cho tiện)
        db.execSQL("CREATE TABLE HOADON (" +
                "MaHD INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MaHopDong TEXT NOT NULL, " + // Sửa thành TEXT cho khớp với bảng HOPDONG
                "Thang INTEGER NOT NULL, " +
                "Nam INTEGER NOT NULL, " +
                "NgayLap TEXT NOT NULL, " +
                "HanThanhToan TEXT, " +

                "DienDau REAL NOT NULL, " +
                "DienCuoi REAL NOT NULL, " +
                "NuocDau REAL NOT NULL, " +
                "NuocCuoi REAL NOT NULL, " +

                "TongTien REAL NOT NULL, " +
                "TrangThai TEXT DEFAULT 'Chưa thanh toán', " +

                "FOREIGN KEY(MaHopDong) REFERENCES HOPDONG(MaHopDong) ON DELETE CASCADE, " +
                "UNIQUE(MaHopDong, Thang, Nam))");

        // 7. CHITIETHOADON — Chi tiết các khoản phí trong hóa đơn
        db.execSQL("CREATE TABLE CHITIETHOADON (" +
                "MaCTHD INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MaHD INTEGER NOT NULL, " +
                "LoaiPhi TEXT NOT NULL, " + // 'Tiền phòng', 'Tiền điện', 'Tiền nước', 'Phụ phí'...
                "SoLuong REAL NOT NULL, " +
                "DonGia REAL NOT NULL, " +
                "ThanhTien REAL NOT NULL, " +

                "FOREIGN KEY(MaHD) REFERENCES HOADON(MaHD) ON DELETE CASCADE, " +
                "UNIQUE(MaHD, LoaiPhi))");

        // 8. DATLICHXEMTRO — Yêu cầu xem phòng từ khách
        db.execSQL("CREATE TABLE DATLICHXEMTRO (" +
                "MaDatLich INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MaPhong INTEGER NOT NULL, " +
                "HoTen TEXT NOT NULL, " +
                "SDT TEXT NOT NULL, " +
                "NgayGioXem TEXT NOT NULL, " +
                "TrangThai TEXT DEFAULT 'Chờ xác nhận', " + // 'Chờ xác nhận', 'Đã xác nhận', 'Từ chối'
                "FOREIGN KEY(MaPhong) REFERENCES PHONG(MaPhong) ON DELETE CASCADE)");

        // --- CHÈN DỮ LIỆU MẪU ĐỂ TEST HỆ THỐNG ---
        // Tài khoản Admin và Chủ trọ (Mật khẩu mặc định: 123456)
        db.execSQL("INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, VaiTro, HoTen, Email, SDT) " +
                "VALUES ('admin', '123456', 'Admin', 'Quản Trị Viên', 'admin@sotro.com', '0900000000')");
        db.execSQL("INSERT INTO TAIKHOAN (TenDangNhap, MatKhau, VaiTro, HoTen, Email, SDT) " +
                "VALUES ('chutro', '123456', 'Chủ trọ', 'Nguyễn Văn An', 'chutro@sotro.com', '0911111111')");

        // Nhà trọ mẫu cho tài khoản chủ trọ (MaTK = 2)
        db.execSQL("INSERT INTO NHATRO (MaTK, TenNhaTro, DiaChi, MoTa) " +
                "VALUES (2, 'Nhà trọ Hoa Mai', '18 Nguyễn Văn Cừ, Hà Nội', 'Nhà trọ sạch sẽ, an ninh tốt')");

        // Phòng trọ mẫu
        db.execSQL("INSERT INTO PHONG (MaNhaTro, TenPhong, DienTich, GiaPhong, TrangThai) " +
                "VALUES (1, 'P101', 20.0, 2500000.0, 'Còn trống')");
        db.execSQL("INSERT INTO PHONG (MaNhaTro, TenPhong, DienTich, GiaPhong, TrangThai) " +
                "VALUES (1, 'P102', 22.0, 2800000.0, 'Còn trống')");

        // === BỔ SUNG: DỮ LIỆU MẪU NGƯỜI THUÊ ĐỂ TEST NHANH ===
        db.execSQL("INSERT INTO NGUOITHUE (HoTen, CCCD, SDT, NgaySinh, QueQuan) " +
                "VALUES ('Lê Hữu Dũng', '032444488492', '0869471214', '15/05/2004', 'Hà Nội')");
        db.execSQL("INSERT INTO NGUOITHUE (HoTen, CCCD, SDT, NgaySinh, QueQuan) " +
                "VALUES ('Đỗ Thế Bách', '039876543212', '0987654321', '20/10/2004', 'Bắc Ninh')");
        db.execSQL("INSERT INTO NGUOITHUE (HoTen, CCCD, SDT, NgaySinh, QueQuan) " +
                "VALUES ('Nguyễn Văn Nam', '031234567890', '0912345678', '18/12/2006', 'Hải Phòng')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            try { db.execSQL("ALTER TABLE NHATRO ADD COLUMN GiaDien REAL DEFAULT 3500;"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE NHATRO ADD COLUMN GiaNuoc REAL DEFAULT 15000;"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE NHATRO ADD COLUMN PhiInternet REAL DEFAULT 70000;"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE NHATRO ADD COLUMN PhiDichVu REAL DEFAULT 50000;"); } catch (Exception e) {}
        }
        if (oldVersion < 5) {
            // Bọc try-catch để tránh crash nếu cột đã tồn tại từ trước
            try { db.execSQL("ALTER TABLE HOPDONG ADD COLUMN GiaPhong REAL NOT NULL DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE HOPDONG ADD COLUMN DonGiaDien REAL NOT NULL DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE HOPDONG ADD COLUMN DonGiaNuoc REAL NOT NULL DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE HOPDONG ADD COLUMN PhiInternet REAL NOT NULL DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE HOPDONG ADD COLUMN PhiDichVu REAL NOT NULL DEFAULT 0"); } catch (Exception e) {}
        }
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ NGHIỆP VỤ CƠ BẢN
    // ==========================================

    // Kiểm tra đăng nhập (Trả về MaTK nếu thành công, -1 nếu thất bại)
    public int checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap = ? AND MatKhau = ? AND TrangThai = 'Hoạt động'",
                new String[]{username, password}
        );
        int maTK = -1;
        if (cursor.moveToFirst()) {
            maTK = cursor.getInt(0);
        }
        cursor.close();
        return maTK;
    }

    // Lấy thông tin vai trò của tài khoản
    public String getVaiTro(int maTK) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT VaiTro FROM TAIKHOAN WHERE MaTK = ?", new String[]{String.valueOf(maTK)});
        String vaiTro = "";
        if (cursor.moveToFirst()) {
            vaiTro = cursor.getString(0);
        }
        cursor.close();
        return vaiTro;
    }

    // Thêm người thuê mới
    public long insertNguoiThue(String hoTen, String cccd, String sdt, String ngaySinh, String queQuan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("HoTen", hoTen);
        values.put("CCCD", cccd);
        values.put("SDT", sdt);
        values.put("NgaySinh", ngaySinh);
        values.put("QueQuan", queQuan);
        return db.insert("NGUOITHUE", null, values);
    }

    // Đếm tổng số phòng
    public int getTongSoPhong() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM PHONG", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // Đếm số phòng đang thuê
    public int getPhongDangThuê() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM PHONG WHERE TrangThai = 'Đã thuê'", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // Đếm số phòng trống
    public int getPhongTrong() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM PHONG WHERE TrangThai = 'Còn trống'", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // Tính doanh thu đã thanh toán trong tháng
    public double getTongDoanhThuThang(int thang, int nam) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(TongTien), 0) " +
                        "FROM HOADON " +
                        "WHERE TrangThai = ? AND Thang = ? AND Nam = ?",
                new String[]{
                        "Đã thanh toán",
                        String.valueOf(thang),
                        String.valueOf(nam)
                }
        );

        double total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        return total;
    }

    // Tính tổng công nợ chưa thanh toán
    public double getTongCongNo() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(TongTien), 0) " +
                        "FROM HOADON " +
                        "WHERE TrangThai = ?",
                new String[]{"Chưa thanh toán"}
        );

        double total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        return total;
    }

    // Lấy tên chủ trọ dựa vào MaTK
    public String getHoTenChuTro(int maTK) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT HoTen FROM TAIKHOAN WHERE MaTK = ?", new String[]{String.valueOf(maTK)});
        String name = "Chủ nhà";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    // Lấy Email của tài khoản
    public String getUserEmail(int maTK) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Email FROM TAIKHOAN WHERE MaTK = ?", new String[]{String.valueOf(maTK)});
        String email = "";
        if (cursor.moveToFirst()) {
            email = cursor.getString(0);
        }
        cursor.close();
        return email;
    }

    // Lấy số điện thoại của tài khoản
    public String getUserSDT(int maTK) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SDT FROM TAIKHOAN WHERE MaTK = ?", new String[]{String.valueOf(maTK)});
        String sdt = "";
        if (cursor.moveToFirst()) {
            sdt = cursor.getString(0);
        }
        cursor.close();
        return sdt;
    }

    // TRANG NHÀ TRỌ
    // Thêm nhà trọ mới
    public long insertNhaTro(int maTK, String tenNhaTro, String diaChi, String moTa) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaTK", maTK);
        values.put("TenNhaTro", tenNhaTro);
        values.put("DiaChi", diaChi);
        values.put("MoTa", moTa);
        return db.insert("NHATRO", null, values);
    }

    // Lấy thông tin 1 nhà trọ theo MaNhaTro
    public Cursor getNhaTroById(int maNhaTro) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT TenNhaTro, DiaChi FROM NHATRO WHERE MaNhaTro = ?", new String[]{String.valueOf(maNhaTro)});
    }

    // Lấy danh sách phòng theo nhà trọ (hỗ trợ lọc trạng thái và tìm kiếm tên phòng)
    public Cursor getRoomsByNhaTro(int maNhaTro, String trangThai, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder query = new StringBuilder("SELECT MaPhong, TenPhong, DienTich, GiaPhong, TrangThai FROM PHONG WHERE MaNhaTro = ?");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(maNhaTro));

        if (trangThai != null && !trangThai.equals("Tất cả")) {
            query.append(" AND TrangThai = ?");
            args.add(trangThai);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND TenPhong LIKE ?");
            args.add("%" + keyword.trim() + "%");
        }

        return db.rawQuery(query.toString(), args.toArray(new String[0]));
    }

    // Thêm phòng mới vào database
    public long insertPhong(int maNhaTro, String tenPhong, double dienTich, double giaPhong, String trangThai) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaNhaTro", maNhaTro);
        values.put("TenPhong", tenPhong);
        values.put("DienTich", dienTich);
        values.put("GiaPhong", giaPhong);
        values.put("TrangThai", trangThai);
        return db.insert("PHONG", null, values);
    }
    //update phòng
    public boolean updatePhong(int maPhong, String tenPhong, double dienTich, double giaPhong, String trangThai) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenPhong", tenPhong);
        values.put("DienTich", dienTich);
        values.put("GiaPhong", giaPhong);
        values.put("TrangThai", trangThai);
        int rows = db.update("PHONG", values, "MaPhong = ?", new String[]{String.valueOf(maPhong)});
        return rows > 0;
    }
    // Lấy danh sách phòng và mã hợp đồng đang có hiệu lực trong nhà trọ
    public Cursor getRoomsWithContract(int maNhaTro) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT p.MaPhong, p.TenPhong, h.MaHopDong " +
                "FROM PHONG p " +
                "JOIN HOPDONG h ON p.MaPhong = h.MaPhong " +
                "WHERE p.MaNhaTro = ? AND h.TrangThai = 'Còn hiệu lực'";
        return db.rawQuery(query, new String[]{String.valueOf(maNhaTro)});
    }

    // Lấy thông tin đơn giá của nhà trọ
    public Cursor getDonGia(int maNhaTro) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT DonGiaDien, DonGiaNuoc, PhiInternet, PhiDichVu FROM NHATRO WHERE MaNhaTro = ?",
                new String[]{String.valueOf(maNhaTro)}
        );
    }

    // Cập nhật đơn giá cho nhà trọ
    public boolean updateDonGia(int maNhaTro, double giaDien, double giaNuoc, double phiInternet, double phiDichVu) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("DonGiaDien", giaDien);
        values.put("DonGiaNuoc", giaNuoc);
        values.put("PhiInternet", phiInternet);
        values.put("PhiDichVu", phiDichVu);
        int rows = db.update("NHATRO", values, "MaNhaTro = ?", new String[]{String.valueOf(maNhaTro)});
        return rows > 0;
    }
    // NGƯỜI THUÊ
    // Lấy danh sách người thuê (có hỗ trợ tìm kiếm theo từ khóa)
    public Cursor getNguoiThue(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder query = new StringBuilder("SELECT MaNguoiThue, HoTen, CCCD, SDT, NgaySinh, QueQuan FROM NGUOITHUE");
        List<String> args = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" WHERE HoTen LIKE ? OR SDT LIKE ? OR CCCD LIKE ?");
            String pattern = "%" + keyword.trim() + "%";
            args.add(pattern);
            args.add(pattern);
            args.add(pattern);
        }
        query.append(" ORDER BY MaNguoiThue DESC");
        return db.rawQuery(query.toString(), args.toArray(new String[0]));
    }

    // Lấy thông tin 1 người thuê theo MaNguoiThue
    public Cursor getNguoiThueById(int maNguoiThue) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT HoTen, CCCD, SDT, NgaySinh, QueQuan FROM NGUOITHUE WHERE MaNguoiThue = ?", new String[]{String.valueOf(maNguoiThue)});
    }

    // Cập nhật thông tin người thuê
    public boolean updateNguoiThue(int maNguoiThue, String hoTen, String cccd, String sdt, String ngaySinh, String queQuan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("HoTen", hoTen);
        values.put("CCCD", cccd);
        values.put("SDT", sdt);
        values.put("NgaySinh", ngaySinh);
        values.put("QueQuan", queQuan);
        int rows = db.update("NGUOITHUE", values, "MaNguoiThue = ?", new String[]{String.valueOf(maNguoiThue)});
        return rows > 0;
    }

    // Xóa người thuê
    public boolean deleteNguoiThue(int maNguoiThue) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("NGUOITHUE", "MaNguoiThue = ?", new String[]{String.valueOf(maNguoiThue)});
        return rows > 0;
    }

    // Tìm kiếm người thuê theo SĐT, CCCD hoặc Họ tên để chọn làm đại diện hợp đồng
    public Cursor searchNguoiThue(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MaNguoiThue, HoTen, SDT, CCCD FROM NGUOITHUE";
        if (keyword != null && !keyword.trim().isEmpty()) {
            query += " WHERE SDT LIKE ? OR CCCD LIKE ? OR HoTen LIKE ?";
            String k = "%" + keyword.trim() + "%";
            return db.rawQuery(query, new String[]{k, k, k});
        }
        return db.rawQuery(query, null);
    }

    // PHÒNG TRỌ
    public Cursor getPhongById(int maPhong) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT p.TenPhong, p.TrangThai, n.TenNhaTro, p.GiaPhong, p.DienTich, " +
                "nt.HoTen, h.MaHopDong, h.NgayBatDau, h.NgayKetThuc " +
                "FROM PHONG p " +
                "JOIN NHATRO n ON p.MaNhaTro = n.MaNhaTro " +
                "LEFT JOIN HOPDONG h ON p.MaPhong = h.MaPhong AND h.TrangThai = 'Còn hiệu lực' " +
                "LEFT JOIN NGUOITHUE nt ON h.MaNguoiThue = nt.MaNguoiThue " +
                "WHERE p.MaPhong = ?";
        return db.rawQuery(query, new String[]{String.valueOf(maPhong)});
    }

    // Xóa phòng trọ
    public boolean deletePhong(int maPhong) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rows = db.delete("PHONG", "MaPhong = ?", new String[]{String.valueOf(maPhong)});
            return rows > 0;
        } catch (Exception e) {
            return false; // Tránh crash nếu vướng khóa ngoại ràng buộc dữ liệu
        }
    }

    // Cập nhật trạng thái phòng (Ví dụ: "Còn trống" hoặc "Đã thuê")
    public boolean updateTrangThaiPhong(int maPhong, String trangThai) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TrangThai", trangThai);
        int rows = db.update("PHONG", values, "MaPhong = ?", new String[]{String.valueOf(maPhong)});
        return rows > 0;
    }


    // HỢP ĐỒNG
    // Lấy thông tin phòng, nhà trọ và các đơn giá mặc định để điền form hợp đồng
    public Cursor getInfoPhongVaNhaTroForHopDong(int maPhong) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT n.TenNhaTro, n.DiaChi, p.TenPhong, p.GiaPhong, " +
                "COALESCE(n.DonGiaDien, 3500), COALESCE(n.DonGiaNuoc, 15000), " +
                "COALESCE(n.PhiInternet, 70000), COALESCE(n.PhiDichVu, 50000) " +
                "FROM PHONG p " +
                "JOIN NHATRO n ON p.MaNhaTro = n.MaNhaTro " +
                "WHERE p.MaPhong = ?";
        return db.rawQuery(query, new String[]{String.valueOf(maPhong)});
    }

    // Lưu hợp đồng với đầy đủ các trường đơn giá và cọc
    public boolean insertHopDongDayDu(String maHopDong, int maPhong, int maNguoiThue,
                                      String ngayBatDau, String ngayKetThuc,
                                      double giaPhong, double donGiaDien, double donGiaNuoc,
                                      double phiInternet, double phiDichVu, double tienCoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MaHopDong", maHopDong);
        values.put("MaPhong", maPhong);
        values.put("MaNguoiThue", maNguoiThue);
        values.put("NgayBatDau", ngayBatDau);
        values.put("NgayKetThuc", ngayKetThuc);
        values.put("GiaPhong", giaPhong);
        values.put("DonGiaDien", donGiaDien);
        values.put("DonGiaNuoc", donGiaNuoc);
        values.put("PhiInternet", phiInternet);
        values.put("PhiDichVu", phiDichVu);
        values.put("TienCoc", tienCoc);
        values.put("TrangThai", "Còn hiệu lực");

        long result = db.insert("HOPDONG", null, values);
        return result != -1;
    }
    // lấy thông tin hợp đồng theo phòng
    public Cursor getHopDongChiTietByPhong(int maPhong) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT h.MaHopDong, h.TrangThai, p.TenPhong, nt.MaNguoiThue, nt.HoTen, nt.SDT, nt.CCCD, " +
                "h.NgayBatDau, h.NgayKetThuc, h.GiaPhong, h.DonGiaDien, h.DonGiaNuoc, h.PhiInternet, h.PhiDichVu, h.TienCoc " +
                "FROM HOPDONG h " +
                "JOIN PHONG p ON h.MaPhong = p.MaPhong " +
                "JOIN NGUOITHUE nt ON h.MaNguoiThue = nt.MaNguoiThue " +
                "WHERE h.MaPhong = ? AND h.TrangThai = 'Còn hiệu lực'";
        return db.rawQuery(query, new String[]{String.valueOf(maPhong)});
    }
    // lấy thông tin hợp đồng theo mã hđ
    public Cursor getHopDongChiTietByMaHD(String maHopDong) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT h.MaHopDong, h.TrangThai, p.TenPhong, h.MaNguoiThue, nt.HoTen, nt.SDT, nt.CCCD, " +
                "h.NgayBatDau, h.NgayKetThuc, h.GiaPhong, h.DonGiaDien, h.DonGiaNuoc, h.PhiInternet, h.PhiDichVu, h.TienCoc, p.MaPhong " +
                "FROM HOPDONG h " +
                "JOIN PHONG p ON h.MaPhong = p.MaPhong " +
                "JOIN NGUOITHUE nt ON h.MaNguoiThue = nt.MaNguoiThue " +
                "WHERE h.MaHopDong = ?";
        return db.rawQuery(query, new String[]{maHopDong});
    }
    // update hợp đồng
    public boolean updateHopDong(String maHopDong, String ngayBatDau, String ngayKetThuc,
                                 double giaPhong, double donGiaDien, double donGiaNuoc,
                                 double phiInternet, double phiDichVu, double tienCoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NgayBatDau", ngayBatDau);
        values.put("NgayKetThuc", ngayKetThuc);
        values.put("GiaPhong", giaPhong);
        values.put("DonGiaDien", donGiaDien);
        values.put("DonGiaNuoc", donGiaNuoc);
        values.put("PhiInternet", phiInternet);
        values.put("PhiDichVu", phiDichVu);
        values.put("TienCoc", tienCoc);

        int rows = db.update("HOPDONG", values, "MaHopDong = ?", new String[]{maHopDong});
        return rows > 0;
    }
    // update ngày kết thúc
    public boolean updateNgayKetThucHopDong(String maHopDong, String ngayKetThucMoi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NgayKetThuc", ngayKetThucMoi);
        int rows = db.update("HOPDONG", values, "MaHopDong = ?", new String[]{maHopDong});
        return rows > 0;
    }
    // update trạng thái hợp đồng
    public boolean updateTrangThaiHopDong(String maHopDong, String trangThaiMoi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TrangThai", trangThaiMoi);
        int rows = db.update("HOPDONG", values, "MaHopDong = ?", new String[]{maHopDong});
        return rows > 0;
    }

    // 1. Lấy tất cả hợp đồng thuộc quyền quản lý của chủ trọ hiện tại (theo MaTK)
    public Cursor getHopDongByChuTro(int maTK, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT h.MaHopDong, h.TrangThai, p.TenPhong, nt.HoTen, nt.SDT, nt.CCCD, nt.QueQuan " +
                "FROM HOPDONG h " +
                "JOIN PHONG p ON h.MaPhong = p.MaPhong " +
                "JOIN NHATRO n ON p.MaNhaTro = n.MaNhaTro " +
                "JOIN NGUOITHUE nt ON h.MaNguoiThue = nt.MaNguoiThue " +
                "WHERE n.MaTK = ?";
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(maTK));

        if (keyword != null && !keyword.trim().isEmpty()) {
            query += " AND (nt.HoTen LIKE ? OR nt.SDT LIKE ? OR nt.CCCD LIKE ? OR h.MaHopDong LIKE ?)";
            String k = "%" + keyword.trim() + "%";
            args.add(k); args.add(k); args.add(k); args.add(k);
        }
        query += " ORDER BY h.MaHopDong DESC";
        return db.rawQuery(query, args.toArray(new String[0]));
    }

    // 2. Lấy toàn bộ lịch sử hợp đồng của riêng phòng hiện tại
    public Cursor getHopDongByPhong(int maPhong) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT h.MaHopDong, h.TrangThai, p.TenPhong, nt.HoTen, nt.SDT, nt.CCCD, nt.QueQuan " +
                "FROM HOPDONG h " +
                "JOIN PHONG p ON h.MaPhong = p.MaPhong " +
                "JOIN NGUOITHUE nt ON h.MaNguoiThue = nt.MaNguoiThue " +
                "WHERE h.MaPhong = ? " +
                "ORDER BY CASE WHEN h.TrangThai = 'Còn hiệu lực' THEN 0 ELSE 1 END, h.MaHopDong DESC";
        return db.rawQuery(query, new String[]{String.valueOf(maPhong)});
    }

    // 3. Lấy toàn bộ lịch sử hợp đồng của người thuê hiện tại
    public Cursor getHopDongByNguoiThue(int maNguoiThue) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT h.MaHopDong, h.TrangThai, p.TenPhong, nt.HoTen, nt.SDT, nt.CCCD, nt.QueQuan " +
                "FROM HOPDONG h " +
                "JOIN PHONG p ON h.MaPhong = p.MaPhong " +
                "JOIN NGUOITHUE nt ON h.MaNguoiThue = nt.MaNguoiThue " +
                "WHERE h.MaNguoiThue = ? " +
                "ORDER BY h.MaHopDong DESC";
        return db.rawQuery(query, new String[]{String.valueOf(maNguoiThue)});
    }
    // HÓA ĐƠN
    // Lấy tất cả hóa đơn (dùng cho trang danh sách hóa đơn tổng) kèm tìm kiếm
    public Cursor getAllHoaDon(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT h.MaHD, p.TenPhong, nt.TenNhaTro, n.HoTen, h.Thang, h.Nam, h.TongTien, h.TrangThai " +
                "FROM HOADON h " +
                "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                "JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro " +
                "JOIN NGUOITHUE n ON hd.MaNguoiThue = n.MaNguoiThue";

        List<String> args = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            query += " WHERE h.MaHD LIKE ? OR n.HoTen LIKE ? OR p.TenPhong LIKE ?";
            String k = "%" + keyword.trim() + "%";
            args.add(k); args.add(k); args.add(k);
        }
        query += " ORDER BY h.MaHD DESC";
        return db.rawQuery(query, args.toArray(new String[0]));
    }

    // Lấy danh sách hóa đơn của riêng 1 phòng cụ thể kèm tìm kiếm
    public Cursor getHoaDonByPhong(int maPhong, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT h.MaHD, p.TenPhong, nt.TenNhaTro, n.HoTen, h.Thang, h.Nam, h.TongTien, h.TrangThai " +
                "FROM HOADON h " +
                "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                "JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro " +
                "JOIN NGUOITHUE n ON hd.MaNguoiThue = n.MaNguoiThue " +
                "WHERE p.MaPhong = ?";

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(maPhong));

        if (keyword != null && !keyword.trim().isEmpty()) {
            query += " AND (h.MaHD LIKE ? OR n.HoTen LIKE ?)";
            String k = "%" + keyword.trim() + "%";
            args.add(k); args.add(k);
        }
        query += " ORDER BY h.MaHD DESC";
        return db.rawQuery(query, args.toArray(new String[0]));
    }
    // Lấy mã hợp đồng đang còn hiệu lực của phòng (Dùng để kiểm tra phòng trống/đang thuê)
    public String getMaHopDongDangHoatDongByPhong(int maPhong) {
        SQLiteDatabase db = this.getReadableDatabase();
        String maHD = null;
        Cursor cursor = db.rawQuery(
                "SELECT MaHopDong FROM HOPDONG WHERE MaPhong = ? AND TrangThai = 'Còn hiệu lực'",
                new String[]{String.valueOf(maPhong)}
        );
        if (cursor != null && cursor.moveToFirst()) {
            maHD = cursor.getString(0);
            cursor.close();
        }
        return maHD;
    }
    // ==========================================
    // HÀM XỬ LÝ LẬP HÓA ĐƠN TRỌN GÓI (HOADON + CHITIETHOADON)
    // ==========================================
    public boolean themHoaDon(String maHopDong, int thang, int nam, double tienPhong,
                              double dienDau, double dienCuoi, double donGiaDien,
                              double nuocDau, double nuocCuoi, double donGiaNuoc,
                              double tienInternet, double tienDichVu,
                              double tongTien, String trangThai) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction(); // Dùng transaction để bảo đảm tính toàn vẹn dữ liệu qua 2 bảng
        try {
            String ngayLap = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());

            // 1. Insert thông tin tổng vào bảng HOADON
            ContentValues valuesHD = new ContentValues();
            valuesHD.put("MaHopDong", maHopDong);
            valuesHD.put("Thang", thang);
            valuesHD.put("Nam", nam);
            valuesHD.put("NgayLap", ngayLap);
            valuesHD.put("DienDau", dienDau);
            valuesHD.put("DienCuoi", dienCuoi);
            valuesHD.put("NuocDau", nuocDau);
            valuesHD.put("NuocCuoi", nuocCuoi);
            valuesHD.put("TongTien", tongTien);
            valuesHD.put("TrangThai", trangThai);

            long maHD = db.insert("HOADON", null, valuesHD);
            if (maHD == -1) {
                db.endTransaction();
                return false;
            }

            // 2. Insert các khoản phí chi tiết vào bảng CHITIETHOADON
            if (tienPhong > 0) {
                insertChiTietHD(db, maHD, "Tiền phòng", 1, tienPhong);
            }

            double soDienTieuThu = Math.max(0, dienCuoi - dienDau);
            if (soDienTieuThu > 0 || donGiaDien > 0) {
                double tienDien = soDienTieuThu * donGiaDien;
                insertChiTietHD(db, maHD, "Tiền điện", soDienTieuThu, donGiaDien);
            }

            double soNuocTieuThu = Math.max(0, nuocCuoi - nuocDau);
            if (soNuocTieuThu > 0 || donGiaNuoc > 0) {
                double tienNuoc = soNuocTieuThu * donGiaNuoc;
                insertChiTietHD(db, maHD, "Tiền nước", soNuocTieuThu, donGiaNuoc);
            }

            if (tienInternet > 0) {
                insertChiTietHD(db, maHD, "Tiền Internet", 1, tienInternet);
            }

            if (tienDichVu > 0) {
                insertChiTietHD(db, maHD, "Tiền dịch vụ", 1, tienDichVu);
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // Hàm phụ trợ ghi chi tiết hóa đơn
    private void insertChiTietHD(SQLiteDatabase db, long maHD, String loaiPhi, double soLuong, double donGia) {
        ContentValues values = new ContentValues();
        values.put("MaHD", maHD);
        values.put("LoaiPhi", loaiPhi);
        values.put("SoLuong", soLuong);
        values.put("DonGia", donGia);
        values.put("ThanhTien", soLuong * donGia);
        db.insert("CHITIETHOADON", null, values);
    }

    // Xử lý cho Preferences
    public int getTongSoPhongByChuTro(int maTK) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM PHONG p JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro WHERE nt.MaTK = ?",
                new String[]{String.valueOf(maTK)}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getPhongDangThueByChuTro(int maTK) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM PHONG p JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro WHERE nt.MaTK = ? AND p.TrangThai = 'Đã thuê'",
                new String[]{String.valueOf(maTK)}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getPhongTrongByChuTro(int maTK) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM PHONG p JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro WHERE nt.MaTK = ? AND p.TrangThai = 'Còn trống'",
                new String[]{String.valueOf(maTK)}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public double getTongDoanhThuThangByChuTro(int maTK, int thang, int nam) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(h.TongTien), 0) " +
                        "FROM HOADON h " +
                        "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                        "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                        "JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro " +
                        "WHERE nt.MaTK = ? AND h.TrangThai = 'Đã thanh toán' AND h.Thang = ? AND h.Nam = ?",
                new String[]{String.valueOf(maTK), String.valueOf(thang), String.valueOf(nam)}
        );
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    public double getTongCongNoByChuTro(int maTK) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(h.TongTien), 0) " +
                        "FROM HOADON h " +
                        "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                        "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                        "JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro " +
                        "WHERE nt.MaTK = ? AND h.TrangThai = 'Chưa thanh toán'",
                new String[]{String.valueOf(maTK)}
        );
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    public Cursor getHoaDonByChuTro(int maTK, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder query = new StringBuilder(
                "SELECT h.MaHD, p.TenPhong, nt.TenNhaTro, n.HoTen, h.Thang, h.Nam, h.TongTien, h.TrangThai " +
                        "FROM HOADON h " +
                        "JOIN HOPDONG hd ON h.MaHopDong = hd.MaHopDong " +
                        "JOIN PHONG p ON hd.MaPhong = p.MaPhong " +
                        "JOIN NHATRO nt ON p.MaNhaTro = nt.MaNhaTro " +
                        "JOIN NGUOITHUE n ON hd.MaNguoiThue = n.MaNguoiThue " +
                        "WHERE nt.MaTK = ?"
        );
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(maTK));

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND (h.MaHD LIKE ? OR n.HoTen LIKE ? OR p.TenPhong LIKE ?)");
            String k = "%" + keyword.trim() + "%";
            args.add(k);
            args.add(k);
            args.add(k);
        }
        query.append(" ORDER BY h.MaHD DESC");
        return db.rawQuery(query.toString(), args.toArray(new String[0]));
    }

    // PROFILE
    // Cập nhật thông tin hồ sơ tài khoản (HoTen, SDT, Email)
    public boolean updateProfile(int maTK, String hoTen, String sdt, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("HoTen", hoTen);
        values.put("SDT", sdt);
        values.put("Email", email);
        int rows = db.update("TAIKHOAN", values, "MaTK = ?", new String[]{String.valueOf(maTK)});
        return rows > 0;
    }

    // Đổi mật khẩu (Kiểm tra mật khẩu cũ khớp mới cho đổi)
    public boolean changePassword(int maTK, String oldPass, String newPass) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT MaTK FROM TAIKHOAN WHERE MaTK = ? AND MatKhau = ?",
                new String[]{String.valueOf(maTK), oldPass}
        );
        boolean isMatch = false;
        if (cursor != null) {
            isMatch = cursor.moveToFirst();
            cursor.close();
        }
        if (!isMatch) {
            return false; // Sai mật khẩu cũ
        }

        ContentValues values = new ContentValues();
        values.put("MatKhau", newPass);
        int rows = db.update("TAIKHOAN", values, "MaTK = ?", new String[]{String.valueOf(maTK)});
        return rows > 0;
    }

    // ==========================================
    // QUẢN LÝ TÀI KHOẢN CHỦ TRỌ (DÀNH CHO ADMIN)
    // ==========================================

    // Lấy danh sách tài khoản chủ trọ (hỗ trợ tìm kiếm theo Họ tên, Tên đăng nhập, SĐT)
    public Cursor getDanhSachChuTro(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder sql = new StringBuilder(
                "SELECT MaTK, TenDangNhap, HoTen, SDT, Email, TrangThai FROM TAIKHOAN WHERE VaiTro = 'Chủ trọ'"
        );
        List<String> args = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (HoTen LIKE ? OR TenDangNhap LIKE ? OR SDT LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            args.add(pattern);
            args.add(pattern);
            args.add(pattern);
        }
        sql.append(" ORDER BY MaTK DESC");
        return db.rawQuery(sql.toString(), args.toArray(new String[0]));
    }

    // Kiểm tra trùng tên đăng nhập
    public boolean isTenDangNhapTonTai(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MaTK FROM TAIKHOAN WHERE TenDangNhap = ?", new String[]{username});
        boolean exists = false;
        if (c != null) {
            exists = c.moveToFirst();
            c.close();
        }
        return exists;
    }

    // Thêm tài khoản chủ trọ mới
    public long themTaiKhoanChuTro(String tenDangNhap, String matKhau, String hoTen, String sdt, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenDangNhap", tenDangNhap);
        values.put("MatKhau", matKhau);
        values.put("VaiTro", "Chủ trọ");
        values.put("HoTen", hoTen);
        values.put("SDT", sdt);
        values.put("Email", email);
        values.put("TrangThai", "Hoạt động");
        return db.insert("TAIKHOAN", null, values);
    }

    // Khóa / Mở khóa tài khoản
    public boolean updateTrangThaiTaiKhoan(int maTK, String trangThaiMoi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TrangThai", trangThaiMoi);
        return db.update("TAIKHOAN", values, "MaTK = ?", new String[]{String.valueOf(maTK)}) > 0;
    }

    // Đặt lại mật khẩu cho chủ trọ
    public boolean resetMatKhauTaiKhoan(int maTK, String matKhauMoi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MatKhau", matKhauMoi);
        return db.update("TAIKHOAN", values, "MaTK = ?", new String[]{String.valueOf(maTK)}) > 0;
    }
    // Cập nhật thông tin tài khoản chủ trọ (nếu matKhauMoi không rỗng thì cập nhật luôn mật khẩu)
    public boolean updateThongTinChuTro(int maTK, String hoTen, String sdt, String email, String trangThai, String matKhauMoi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("HoTen", hoTen);
        values.put("SDT", sdt);
        values.put("Email", email);
        values.put("TrangThai", trangThai);
        if (matKhauMoi != null && !matKhauMoi.isEmpty()) {
            values.put("MatKhau", matKhauMoi);
        }
        return db.update("TAIKHOAN", values, "MaTK = ?", new String[]{String.valueOf(maTK)}) > 0;
    }
}