CREATE DATABASE DB_DatLichLau_17;
GO
USE DB_DatLichLau_17;
GO

-- 1. Bảng Vai trò nhân viên
CREATE TABLE vai_tro (
    id_vai_tro INT IDENTITY(1,1) PRIMARY KEY,
    ma_vai_tro AS ISNULL(CAST(('VT' + RIGHT('000' + CAST(id_vai_tro AS VARCHAR(10)), 3)) AS VARCHAR(10)), '') PERSISTED,
    ten_vai_tro NVARCHAR(100) NOT NULL,
    mo_ta_vai_tro NVARCHAR(255),
    trang_thai INT DEFAULT 1
);

-- 2. Bảng Khu vực
CREATE TABLE khu_vuc (
    id_khu_vuc INT IDENTITY(1,1) PRIMARY KEY,
    ma_khu_vuc AS ISNULL(CAST(('KV' + RIGHT('000' + CAST(id_khu_vuc AS VARCHAR(10)), 3)) AS VARCHAR(10)), '') PERSISTED,
    ten_khu_vuc NVARCHAR(100) NOT NULL,
    tang INT CHECK (tang > 0),
    mo_ta NVARCHAR(255),
    trang_thai INT DEFAULT 1
);

-- 3. Bảng Danh mục món ăn
CREATE TABLE danh_muc (
    id_danh_muc INT IDENTITY(1,1) PRIMARY KEY,
    ma_danh_muc AS ISNULL(CAST(('DM' + RIGHT('000' + CAST(id_danh_muc AS VARCHAR(10)), 3)) AS VARCHAR(10)), '') PERSISTED,
    ten_danh_muc NVARCHAR(100) NOT NULL,
    mo_ta NVARCHAR(255),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_sua DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_sua NVARCHAR(100),
    trang_thai INT DEFAULT 1
);

-- 4. Bảng Loại Set lẩu
CREATE TABLE loai_set_lau (
    id_loai_set INT IDENTITY(1,1) PRIMARY KEY,
    ma_loai_set AS ISNULL(CAST(('SL' + RIGHT('000' + CAST(id_loai_set AS VARCHAR(10)), 3)) AS VARCHAR(10)), '') PERSISTED,
    ten_loai_set NVARCHAR(100),
    mo_ta NVARCHAR(255),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_sua DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_sua NVARCHAR(100),
    trang_thai INT DEFAULT 1
);

-- 5. Bảng Phương thức thanh toán
CREATE TABLE phuong_thuc_thanh_toan (
    id_phuong_thuc INT IDENTITY(1,1) PRIMARY KEY,
    ma_phuong_thuc AS ISNULL(CAST(('PT' + RIGHT('00' + CAST(id_phuong_thuc AS VARCHAR(10)), 2)) AS VARCHAR(10)), '') PERSISTED,
    ten_phuong_thuc NVARCHAR(100),
    url_anh_phuong_thuc NVARCHAR(MAX),
    trang_thai INT DEFAULT 1
);

-- 6. Bảng Đợt khuyến mãi
CREATE TABLE dot_khuyen_mai (
    id_dot_khuyen_mai INT IDENTITY(1,1) PRIMARY KEY,
    ma_dot_khuyen_mai AS ISNULL(CAST(('DKM' + RIGHT('000' + CAST(id_dot_khuyen_mai AS VARCHAR(10)), 3)) AS VARCHAR(10)), '') PERSISTED,
    ten_dot_khuyen_mai NVARCHAR(200),
    mo_ta NVARCHAR(MAX),
    phan_tram_giam INT, 
	ngay_bat_dau date,
	ngay_ket_thuc date,
    trang_thai INT DEFAULT 1,
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_sua DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_sua NVARCHAR(100)
);

-- 7. Bảng Tham số hệ thống
CREATE TABLE tham_so_he_thong (
    id_tham_so INT IDENTITY(1,1) PRIMARY KEY,
    ma_tham_so VARCHAR(50) NOT NULL UNIQUE, 
    ten_tham_so NVARCHAR(100) NOT NULL,
    gia_tri NVARCHAR(255) NOT NULL,
    kieu_du_lieu VARCHAR(50),
    mo_ta NVARCHAR(255),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- 8. Bảng Dữ liệu cửa hàng
CREATE TABLE du_lieu_cua_hang (
    id_du_lieu INT IDENTITY(1,1) PRIMARY KEY,
    ma_du_lieu AS ISNULL(CAST(('DL' + RIGHT('000' + CAST(id_du_lieu AS VARCHAR(10)), 3)) AS VARCHAR(10)), '') PERSISTED,
    nhom_chu_de NVARCHAR(200),
    cau_hoi_mau NVARCHAR(MAX),
    cau_tra_loi NVARCHAR(MAX) NOT NULL,
    tu_khoa NVARCHAR(200),
    do_uu_tien INT DEFAULT 1,
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- 9. Bảng Nhân viên
CREATE TABLE nhan_vien (
    id_nhan_vien INT IDENTITY(1,1) PRIMARY KEY,
    id_vai_tro INT,
    ma_nhan_vien AS ISNULL(CAST(('NV' + RIGHT('0000' + CAST(id_nhan_vien AS VARCHAR(10)), 4)) AS VARCHAR(10)), '') PERSISTED,
    ho_ten_nhan_vien NVARCHAR(100),
    sdt_nhan_vien VARCHAR(20),
    ten_dang_nhap VARCHAR(50) UNIQUE,
    mat_khau_dang_nhap VARCHAR(255),
    trang_thai_lam_viec INT DEFAULT 1,
    ngay_sinh DATE,
    ngay_vao_lam DATE,
    dia_chi NVARCHAR(255),
    email VARCHAR(100),
    gioi_tinh BIT,
    FOREIGN KEY (id_vai_tro) REFERENCES vai_tro(id_vai_tro) ON DELETE SET NULL
);

-- 10. Bảng Bàn ăn
CREATE TABLE ban_an (
    id_ban_an INT IDENTITY(1,1) PRIMARY KEY,
    id_khu_vuc INT,
    ma_ban AS ISNULL(CAST(('BA' + RIGHT('000' + CAST(id_ban_an AS VARCHAR(10)), 3)) AS VARCHAR(10)), '') PERSISTED,
    ten_ban NVARCHAR(100),
    so_nguoi_toi_da INT CHECK (so_nguoi_toi_da > 0),
    trang_thai INT DEFAULT 0, -- 0: Trống, 1: Đang dùng, 2: Đã đặt
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_sua DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_sua NVARCHAR(100),
    FOREIGN KEY (id_khu_vuc) REFERENCES khu_vuc(id_khu_vuc) ON DELETE SET NULL
);

-- 11. Bảng Danh mục chi tiết
CREATE TABLE danh_muc_chi_tiet (
    id_danh_muc_chi_tiet INT IDENTITY(1,1) PRIMARY KEY,
    id_danh_muc INT,
    ma_danh_muc_chi_tiet AS ISNULL(CAST(('DCT' + RIGHT('000' + CAST(id_danh_muc_chi_tiet AS VARCHAR(10)), 3)) AS VARCHAR(10)), '') PERSISTED,
    ten_danh_muc_chi_tiet NVARCHAR(100),
    mo_ta NVARCHAR(255),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_sua DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_sua NVARCHAR(100),
    trang_thai INT,
    FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id_danh_muc) ON DELETE CASCADE
);



-- 12. Bảng Phiếu giảm giá
CREATE TABLE phieu_giam_gia (
    id_phieu_giam_gia INT IDENTITY(1,1) PRIMARY KEY,
    id_dot_khuyen_mai INT,
    ma_phieu_giam_gia AS ISNULL(CAST(('VOU' + RIGHT('000' + CAST(id_phieu_giam_gia AS VARCHAR(10)), 3)) AS VARCHAR(10)), '') PERSISTED,
    code_giam_gia VARCHAR(50) UNIQUE,
    ten_phieu_giam_gia NVARCHAR(200),
	loai_giam_gia INT,    
	gia_tri_giam DECIMAL(18,0) CHECK (gia_tri_giam >= 0),
    gia_tri_giam_toi_da DECIMAL(18,0) DEFAULT 0,
    don_hang_toi_thieu DECIMAL(18,0) DEFAULT 0,
	doi_tuong INT,
    ngay_bat_dau DATETIME,
    ngay_ket_thuc DATETIME,
    so_luong INT CHECK (so_luong >= 0),
    trang_thai INT,
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_sua DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_sua NVARCHAR(100),
    FOREIGN KEY (id_dot_khuyen_mai) REFERENCES dot_khuyen_mai(id_dot_khuyen_mai) ON DELETE SET NULL
);

-- 13. Bảng Set lẩu
CREATE TABLE set_lau (
    id_set_lau INT IDENTITY(1,1) PRIMARY KEY,
    id_loai_set INT,
    ma_set_lau AS ISNULL(CAST(('SET' + RIGHT('0000' + CAST(id_set_lau AS VARCHAR(10)), 4)) AS VARCHAR(10)), '') PERSISTED,
    ten_set_lau NVARCHAR(200),
    mo_ta NVARCHAR(MAX),
    gia_ban DECIMAL(18,0) CHECK (gia_ban >= 0),
    hinh_anh NVARCHAR(MAX),
    mo_ta_chi_tiet NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_sua DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_sua NVARCHAR(100),
    trang_thai INT,
    FOREIGN KEY (id_loai_set) REFERENCES loai_set_lau(id_loai_set) ON DELETE SET NULL
);


-- Tạo lại bảng trung gian
CREATE TABLE chi_tiet_khuyen_mai_set (
    id_dot_khuyen_mai INT NOT NULL,
    id_set_lau INT NOT NULL,
    PRIMARY KEY (id_dot_khuyen_mai, id_set_lau),
    FOREIGN KEY (id_dot_khuyen_mai) REFERENCES dot_khuyen_mai(id_dot_khuyen_mai) ON DELETE CASCADE,
    FOREIGN KEY (id_set_lau) REFERENCES set_lau(id_set_lau) ON DELETE CASCADE
);

-- 14. Bảng Món ăn đi kèm
CREATE TABLE mon_an_di_kem (
    id_mon_an_di_kem INT IDENTITY(1,1) PRIMARY KEY,
    id_danh_muc_chi_tiet INT,
    ma_mon_an AS ISNULL(CAST(('MA' + RIGHT('0000' + CAST(id_mon_an_di_kem AS VARCHAR(10)), 4)) AS VARCHAR(10)), '') PERSISTED,
    ten_mon_an NVARCHAR(200),
    mo_ta NVARCHAR(MAX),
    trang_thai_kinh_doanh INT,
    gia_ban DECIMAL(18,0) CHECK (gia_ban >= 0),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_sua DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_sua NVARCHAR(100),
    trang_thai INT,
    FOREIGN KEY (id_danh_muc_chi_tiet) REFERENCES danh_muc_chi_tiet(id_danh_muc_chi_tiet) ON DELETE CASCADE
);

CREATE TABLE chi_tiet_khuyen_mai_mon (
    id_dot_khuyen_mai INT NOT NULL,
    id_mon_an_di_kem INT NOT NULL,
    PRIMARY KEY (id_dot_khuyen_mai, id_mon_an_di_kem),
    FOREIGN KEY (id_dot_khuyen_mai) REFERENCES dot_khuyen_mai(id_dot_khuyen_mai) ON DELETE CASCADE,
    FOREIGN KEY (id_mon_an_di_kem) REFERENCES mon_an_di_kem(id_mon_an_di_kem) ON DELETE CASCADE
);

-- 15. Bảng Hình ảnh món ăn
CREATE TABLE hinh_anh_mon_an (
    id_hinh_anh_mon_an INT IDENTITY(1,1) PRIMARY KEY,
    id_mon_an_di_kem INT, 
    url_anh NVARCHAR(MAX),
    trang_thai INT,
    FOREIGN KEY (id_mon_an_di_kem) REFERENCES mon_an_di_kem(id_mon_an_di_kem) ON DELETE CASCADE
);

-- 16. Bảng Chi tiết món ăn (Kích cỡ món)
CREATE TABLE chi_tiet_mon_an (
    id_chi_tiet_mon_an INT IDENTITY(1,1) PRIMARY KEY,
    id_mon_an_di_kem INT,
    ma_chi_tiet_mon_an AS ISNULL(CAST(('CTMA' + RIGHT('0000' + CAST(id_chi_tiet_mon_an AS VARCHAR(10)), 4)) AS VARCHAR(10)), '') PERSISTED,
    ten_chi_tiet_mon_an NVARCHAR(200),
    gia_ban DECIMAL(18,0) CHECK (gia_ban >= 0),
    gia_von DECIMAL(18,0) CHECK (gia_von >= 0),
    kich_co NVARCHAR(50),
    don_vi NVARCHAR(50),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_sua DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_sua NVARCHAR(100),
    trang_thai INT,
    FOREIGN KEY (id_mon_an_di_kem) REFERENCES mon_an_di_kem(id_mon_an_di_kem) ON DELETE CASCADE
);

-- 17. Bảng Khách hàng
CREATE TABLE khach_hang (
    id_khach_hang INT IDENTITY(1,1) PRIMARY KEY,
    ma_khach_hang AS ISNULL(CAST(('KH' + RIGHT('0000' + CAST(id_khach_hang AS VARCHAR(10)), 4)) AS VARCHAR(10)), '') PERSISTED,
    ten_khach_hang NVARCHAR(100),
    so_dien_thoai VARCHAR(20) UNIQUE,
    email VARCHAR(100),
    ngay_sinh DATE,
    diem_tich_luy INT DEFAULT 0 CHECK (diem_tich_luy >= 0),
    ngay_tao_tai_khoan DATETIME DEFAULT GETDATE(),
    gioi_tinh BIT,
    ten_dang_nhap VARCHAR(50) UNIQUE,
    mat_khau_dang_nhap VARCHAR(255),
    trang_thai INT,
    dia_chi NVARCHAR(255)
);

-- 18. Bảng Phiếu đặt bàn
CREATE TABLE phieu_dat_ban (
    id_phieu_dat_ban INT IDENTITY(1,1) PRIMARY KEY,
    id_ban_an INT,
    id_khach_hang INT,
    ma_dat_ban AS ISNULL(CAST(('PDB' + RIGHT('0000' + CAST(id_phieu_dat_ban AS VARCHAR(10)), 4)) AS VARCHAR(10)), '') PERSISTED,
    thoi_gian_dat DATETIME,
    hinh_thuc_dat INT,
    so_luong_khach INT CHECK (so_luong_khach > 0),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_sua DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_sua NVARCHAR(100),
    trang_thai INT,
    FOREIGN KEY (id_ban_an) REFERENCES ban_an(id_ban_an) ON DELETE SET NULL,
    FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id_khach_hang) ON DELETE SET NULL
);

-- 19. Bảng Hóa đơn thanh toán
CREATE TABLE hoa_don_thanh_toan (
    id_hoa_don INT IDENTITY(1,1) PRIMARY KEY,
    id_nhan_vien INT,
    id_khach_hang INT,
    id_ban_an INT, 
    id_phieu_dat_ban INT,
    ma_hoa_don AS ISNULL(CAST(('HD' + RIGHT('00000' + CAST(id_hoa_don AS VARCHAR(10)), 5)) AS VARCHAR(10)), '') PERSISTED,
    thoi_gian_tao DATETIME DEFAULT GETDATE(),
    thoi_gian_thanh_toan DATETIME,
    tong_tien_chua_giam DECIMAL(18,0) DEFAULT 0 CHECK (tong_tien_chua_giam >= 0),
    so_tien_da_giam DECIMAL(18,0) DEFAULT 0 CHECK (so_tien_da_giam >= 0),
    tong_tien_thanh_toan DECIMAL(18,0) DEFAULT 0 CHECK (tong_tien_thanh_toan >= 0),
    diem_su_dung INT DEFAULT 0,
    diem_cong_them INT DEFAULT 0,
    trang_thai_hoa_don INT DEFAULT 0,
    ghi_chu NVARCHAR(MAX),
    tien_khach_dua DECIMAL(18,0) DEFAULT 0,
    tien_thua DECIMAL(18,0) DEFAULT 0,
    FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id_nhan_vien),
    FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id_khach_hang),
    FOREIGN KEY (id_ban_an) REFERENCES ban_an(id_ban_an),
    FOREIGN KEY (id_phieu_dat_ban) REFERENCES phieu_dat_ban(id_phieu_dat_ban)
);

-- 20. Bảng Chi tiết hóa đơn
CREATE TABLE chi_tiet_hoa_don (
    id_chi_tiet_hd INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don INT,
    id_set_lau INT,
    id_chi_tiet_mon_an INT,
    so_luong INT CHECK (so_luong > 0),
    don_gia_tai_thoi_diem_ban DECIMAL(18,0) CHECK (don_gia_tai_thoi_diem_ban >= 0),
    thanh_tien DECIMAL(18,0) CHECK (thanh_tien >= 0),
    ghi_chu_mon NVARCHAR(255),
    trang_thai_mon INT DEFAULT 1,
    ngay_gio_tao DATETIME DEFAULT GETDATE(),
    ngay_gio_sua DATETIME,
    FOREIGN KEY (id_hoa_don) REFERENCES hoa_don_thanh_toan(id_hoa_don) ON DELETE CASCADE,
    FOREIGN KEY (id_set_lau) REFERENCES set_lau(id_set_lau),
    FOREIGN KEY (id_chi_tiet_mon_an) REFERENCES chi_tiet_mon_an(id_chi_tiet_mon_an)
);

-- 21. Bảng Phiếu giảm giá cá nhân
CREATE TABLE phieu_giam_gia_ca_nhan (
    id_phieu_giam_gia_ca_nhan INT IDENTITY(1,1) PRIMARY KEY,
    ma_giam_gia_ca_nhan AS ISNULL(CAST(('PGGCN' + RIGHT('0000' + CAST(id_phieu_giam_gia_ca_nhan AS VARCHAR(10)), 4)) AS VARCHAR(10)), '') PERSISTED,
    id_khach_hang INT,
    id_phieu_giam_gia INT,
    id_hoa_don_thanh_toan INT,
    ngay_nhan DATETIME DEFAULT GETDATE(),
    ngay_su_dung DATETIME,
    trang_thai_su_dung INT DEFAULT 0,
    nguon_goc NVARCHAR(100),
    ghi_chu NVARCHAR(255),
    ngay_het_han DATETIME,
    FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id_khach_hang),
    FOREIGN KEY (id_phieu_giam_gia) REFERENCES phieu_giam_gia(id_phieu_giam_gia),
    FOREIGN KEY (id_hoa_don_thanh_toan) REFERENCES hoa_don_thanh_toan(id_hoa_don)
);

-- 22. Lịch sử thanh toán
CREATE TABLE lich_su_thanh_toan (
    id_lich_su_thanh_toan INT IDENTITY(1,1) PRIMARY KEY,
    ma_lich_su_tt AS ISNULL(CAST(('LSTT' + RIGHT('00000' + CAST(id_lich_su_thanh_toan AS VARCHAR(10)), 5)) AS VARCHAR(10)), '') PERSISTED,
    id_phuong_thuc_thanh_toan INT,
    id_hoa_don INT,
    ten_phuong_thuc NVARCHAR(100),
    ma_giao_dich VARCHAR(100),
    so_tien_thanh_toan DECIMAL(18,0) CHECK (so_tien_thanh_toan >= 0),
    ngay_thanh_toan DATETIME DEFAULT GETDATE(),
    trang_thai INT,
    FOREIGN KEY (id_phuong_thuc_thanh_toan) REFERENCES phuong_thuc_thanh_toan(id_phuong_thuc),
    FOREIGN KEY (id_hoa_don) REFERENCES hoa_don_thanh_toan(id_hoa_don)
);

-- 23. Bảng Lịch sử hóa đơn (Log)
CREATE TABLE lich_su_hoa_don (
    id_lich_su_hoa_don INT IDENTITY(1,1) PRIMARY KEY,
    id_nhan_vien INT,
    id_hoa_don INT,
    hanh_dong NVARCHAR(255),
    thoi_gian_thuc_hien DATETIME DEFAULT GETDATE(),
    ly_do_thuc_hien NVARCHAR(MAX),
    trang_thai_truoc_do INT,
    trang_thai_moi INT,
    FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id_nhan_vien),
    FOREIGN KEY (id_hoa_don) REFERENCES hoa_don_thanh_toan(id_hoa_don)
);

-- 24. Bảng Đánh giá
CREATE TABLE danh_gia (
    id_danh_gia INT IDENTITY(1,1) PRIMARY KEY,
    ma_danh_gia AS ISNULL(CAST(('DG' + RIGHT('0000' + CAST(id_danh_gia AS VARCHAR(10)), 4)) AS VARCHAR(10)), '') PERSISTED,
    id_khach_hang INT,
    id_hoa_don INT,
    so_sao INT CHECK (so_sao BETWEEN 1 AND 5),
    noi_dung NVARCHAR(MAX),
    thoi_gian_danh_gia DATETIME DEFAULT GETDATE(),
    trang_thai INT,
    FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id_khach_hang),
    FOREIGN KEY (id_hoa_don) REFERENCES hoa_don_thanh_toan(id_hoa_don)
);

-- 25. Bảng Hình ảnh đánh giá
CREATE TABLE hinh_anh_danh_gia (
    id_hinh_anh_danh_gia INT IDENTITY(1,1) PRIMARY KEY, 
    id_danh_gia INT,
    url_anh NVARCHAR(MAX),
    trang_thai INT,
    FOREIGN KEY (id_danh_gia) REFERENCES danh_gia(id_danh_gia) ON DELETE CASCADE
);

-- 26. Bảng Giao ca
CREATE TABLE giao_ca (
    id_giao_ca INT IDENTITY(1,1) PRIMARY KEY,
    ma_ca AS ISNULL(CAST(('CA' + RIGHT('000' + CAST(id_giao_ca AS VARCHAR(10)), 3)) AS VARCHAR(10)), '') PERSISTED,
    id_nhan_vien INT,
    tien_mat_ca_truoc DECIMAL(18,0) DEFAULT 0,
    tien_mat_trong_ca DECIMAL(18,0) DEFAULT 0,
    tien_ck_trong_ca DECIMAL(18,0) DEFAULT 0,
    tong_tien_trong_ca DECIMAL(18,0) DEFAULT 0,
    tien_mat_thuc_te DECIMAL(18,0) DEFAULT 0,
    tong_tien_phat_sinh DECIMAL(18,0) DEFAULT 0,
    ghi_chu NVARCHAR(MAX),
    trang_thai INT,
    FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id_nhan_vien)
);

-- 27. Bảng Lịch làm việc
CREATE TABLE lich_lam_viec (
    id_lich_lam_viec INT IDENTITY(1,1) PRIMARY KEY,
    ma_lich_lam AS ISNULL(CAST(('LLV' + RIGHT('0000' + CAST(id_lich_lam_viec AS VARCHAR(10)), 4)) AS VARCHAR(10)), '') PERSISTED,
    id_nhan_vien INT,
    ngay DATE,
    vi_tri NVARCHAR(100),  
    gio_bat_dau TIME,
    gio_ket_thuc TIME,
    ghi_chu NVARCHAR(255),
    trang_thai INT,
    FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id_nhan_vien) ON DELETE CASCADE
);
GO


ALTER TABLE chi_tiet_hoa_don
ADD CONSTRAINT CK_ChiTietHoaDon_SetOrMon
CHECK (
   (id_set_lau IS NOT NULL AND id_chi_tiet_mon_an IS NULL)
OR (id_set_lau IS NULL AND id_chi_tiet_mon_an IS NOT NULL)
);


-- Xem các Set lẩu được giảm giá
SELECT dkm.ten_dot_khuyen_mai, sl.ten_set_lau, dkm.phan_tram_giam
FROM dot_khuyen_mai dkm
JOIN chi_tiet_khuyen_mai_set ct ON dkm.id_dot_khuyen_mai = ct.id_dot_khuyen_mai
JOIN set_lau sl ON ct.id_set_lau = sl.id_set_lau;

-- Xem các món lẻ được giảm giá
SELECT dkm.ten_dot_khuyen_mai, ma.ten_mon_an, dkm.phan_tram_giam
FROM dot_khuyen_mai dkm
JOIN chi_tiet_khuyen_mai_mon ct ON dkm.id_dot_khuyen_mai = ct.id_dot_khuyen_mai
JOIN mon_an_di_kem ma ON ct.id_mon_an_di_kem = ma.id_mon_an_di_kem;
-----------------------------------------------------------------------------------
-- CHÈN DỮ LIỆU MẪU 
-----------------------------------------------------------------------------------


-----------------------------------------------------------------------------------
-- 1. BẢNG VAI TRÒ (Chỉ Quản lý và Nhân viên)
-----------------------------------------------------------------------------------
INSERT INTO vai_tro (ten_vai_tro, mo_ta_vai_tro, trang_thai) VALUES 
(N'Quản lý', N'Toàn quyền quản trị hệ thống và nhân sự', 1),
(N'Nhân viên', N'Thực hiện nghiệp vụ bán hàng và phục vụ', 1);

-----------------------------------------------------------------------------------
-- 2. BẢNG KHU VỰC
-----------------------------------------------------------------------------------
INSERT INTO khu_vuc (ten_khu_vuc, tang, mo_ta, trang_thai) VALUES 
(N'Sảnh tầng 1', 1, N'Khu vực không gian mở', 1),
(N'Phòng VIP 1', 2, N'Máy lạnh, riêng tư 10 người', 1),
(N'Ban công', 2, N'Gió trời, ngắm cảnh phố', 1),
(N'Sân vườn', 1, N'Tiểu cảnh nước ngoài trời', 1),
(N'Phòng VIP 2', 2, N'Phòng lớn cho tiệc 20 người', 1);

-----------------------------------------------------------------------------------
-- 3. BẢNG DANH MỤC
-----------------------------------------------------------------------------------
INSERT INTO danh_muc (ten_danh_muc, mo_ta, ngay_tao, nguoi_tao, trang_thai) VALUES 
(N'Nước lẩu', N'Các loại cốt lẩu đặc sắc', GETDATE(), N'Quản lý', 1),
(N'Thịt nhúng', N'Bò Mỹ, bắp bò, sườn heo', GETDATE(), N'Quản lý', 1),
(N'Hải sản', N'Tôm sú, mực ống, cá hồi', GETDATE(), N'Quản lý', 1),
(N'Rau Nấm', N'Rau củ sạch và nấm tươi', GETDATE(), N'Quản lý', 1),
(N'Đồ uống', N'Rượu, bia và nước ngọt', GETDATE(), N'Quản lý', 1);

-----------------------------------------------------------------------------------
-- 4. BẢNG LOẠI SET LẨU
-----------------------------------------------------------------------------------
INSERT INTO loai_set_lau (ten_loai_set, mo_ta, ngay_tao, nguoi_tao, trang_thai) VALUES 
(N'Buffet', N'Ăn không giới hạn theo gói', GETDATE(), N'Quản lý', 1),
(N'Alacarte', N'Gọi món lẻ theo đĩa', GETDATE(), N'Quản lý', 1),
(N'Combo Cặp đôi', N'Set tiết kiệm cho 2 người', GETDATE(), N'Quản lý', 1),
(N'Combo Gia đình', N'Dành cho nhóm 4-6 người', GETDATE(), N'Quản lý', 1),
(N'Set Luxury', N'Hải sản thượng hạng VIP', GETDATE(), N'Quản lý', 1);

-----------------------------------------------------------------------------------
-- 5. BẢNG PHƯƠNG THỨC THANH TOÁN
-----------------------------------------------------------------------------------
INSERT INTO phuong_thuc_thanh_toan (ten_phuong_thuc, url_anh_phuong_thuc, trang_thai) VALUES 
(N'Tiền mặt', 'cash.png', 1),
(N'Chuyển khoản', 'banking.png', 1),
(N'Ví MoMo', 'momo.png', 1),
(N'Thẻ ATM/Visa', 'card.png', 1),
(N'ZaloPay', 'zalopay.png', 1);

-----------------------------------------------------------------------------------
-- 6. BẢNG ĐỢT KHUYẾN MÃI
-----------------------------------------------------------------------------------
INSERT INTO dot_khuyen_mai (ten_dot_khuyen_mai, mo_ta, phan_tram_giam, ngay_bat_dau, ngay_ket_thuc, trang_thai, ngay_tao, nguoi_tao) VALUES 
(N'Khai trương 2026', N'Giảm giá 20% tổng bill', 10, '2026-01-01', '2026-02-02', 1, GETDATE(), N'Quản lý'),
(N'Giờ vàng buổi trưa', N'Giảm 50k cho buffet', 10, '2026-01-01', '2026-02-02', 1, GETDATE(), N'Quản lý'),
(N'Tri ân hội viên', N'Tích x2 điểm thưởng', 50, '2026-01-01', '2026-02-02', 1, GETDATE(), N'Quản lý'),
(N'Mừng Xuân 2026', N'Tặng đĩa bò Mỹ', 90, '2026-01-01', '2026-02-02', 1, GETDATE(), N'Quản lý'),
(N'Ngày hội gia đình', N'Miễn phí đồ uống', 60, '2026-01-01', '2026-02-02', 1, GETDATE(), N'Quản lý');

-----------------------------------------------------------------------------------
-- 7. BẢNG THAM SỐ HỆ THỐNG
-----------------------------------------------------------------------------------
INSERT INTO tham_so_he_thong (ma_tham_so, ten_tham_so, gia_tri, kieu_du_lieu, mo_ta, trang_thai) VALUES 
('VAT', N'Thuế VAT', '10', 'INT', N'Phần trăm thuế hóa đơn', 1),
('MIN_RESERVE', N'Thời gian đặt trước', '30', 'INT', N'Phút tối thiểu', 1),
('POINT_RATE', N'Tỷ lệ tích điểm', '0.01', 'DECIMAL', N'1% giá trị bill', 1),
('CURRENCY', N'Tiền tệ', 'VND', 'STRING', N'Đơn vị mặc định', 1),
('HOTLINE', N'Tổng đài', '19001234', 'STRING', N'Số hỗ trợ khách hàng', 1);

-----------------------------------------------------------------------------------
-- 8. BẢNG DỮ LIỆU CỬA HÀNG (FAQ)
-----------------------------------------------------------------------------------
INSERT INTO du_lieu_cua_hang (nhom_chu_de, cau_hoi_mau, cau_tra_loi, tu_khoa, do_uu_tien, trang_thai) VALUES 
(N'Vị trí', N'Quán ở đâu?', N'123 Trịnh Văn Bô, Hà Nội', N'diachi, dau', 1, 1),
(N'Wifi', N'Mật khẩu wifi?', N'laungon2026', N'wifi, mang', 2, 1),
(N'Gửi xe', N'Chỗ để xe ở đâu?', N'Bãi xe miễn phí đối diện quán', N'xe, oto', 1, 1),
(N'Đặt bàn', N'Có cần đặt bàn trước không?', N'Nên đặt trước 30p vào cuối tuần', N'datban', 2, 1),
(N'Thanh toán', N'Có nhận thẻ không?', N'Có nhận quẹt thẻ và chuyển khoản', N'the, chuyenkhoan', 3, 1);

-----------------------------------------------------------------------------------
-- 9. BẢNG NHÂN VIÊN
-----------------------------------------------------------------------------------
INSERT INTO nhan_vien (id_vai_tro, ho_ten_nhan_vien, sdt_nhan_vien, ten_dang_nhap, mat_khau_dang_nhap, trang_thai_lam_viec, ngay_sinh, ngay_vao_lam, dia_chi, email, gioi_tinh) VALUES 
(1, N'Nguyễn Quản Lý', '0911111111', 'admin01', '123456', 1, '1990-05-15', '2023-01-10', N'Hà Nội', 'admin@gmail.com', 1),
(2, N'Trần Thu Ngân', '0922222222', 'cashier01', '123456', 1, '1995-08-20', '2024-02-15', N'Bắc Ninh', 'ngan@gmail.com', 0),
(2, N'Lê Phục Vụ', '0933333333', 'staff01', '123456', 1, '2000-10-05', '2024-03-01', N'Nam Định', 'staff1@gmail.com', 1),
(2, N'Phạm Bếp Trưởng', '0944444444', 'chef01', '123456', 1, '1988-12-12', '2023-05-20', N'Hà Nam', 'chef@gmail.com', 1),
(2, N'Hoàng Tiếp Tân', '0955555555', 'staff02', '123456', 1, '2001-01-25', '2024-01-10', N'Hưng Yên', 'staff2@gmail.com', 0);

-----------------------------------------------------------------------------------
-- 10. BẢNG BÀN ĂN
-----------------------------------------------------------------------------------
INSERT INTO ban_an (id_khu_vuc, ten_ban, so_nguoi_toi_da, trang_thai, nguoi_tao) VALUES 
(1, N'Bàn S01', 4, 0, N'Quản lý'),
(1, N'Bàn S02', 4, 0, N'Quản lý'),
(2, N'Bàn VIP01', 10, 0, N'Quản lý'),
(3, N'Bàn BC01', 2, 0, N'Quản lý'),
(4, N'Bàn SV05', 6, 0, N'Quản lý');

-----------------------------------------------------------------------------------
-- 11. BẢNG DANH MỤC CHI TIẾT
-----------------------------------------------------------------------------------
INSERT INTO danh_muc_chi_tiet (id_danh_muc, ten_danh_muc_chi_tiet, mo_ta, nguoi_tao, trang_thai) VALUES 
(2, N'Bò Mỹ', N'Thịt bò nhập khẩu ba chỉ, bắp bò', N'Quản lý', 1),
(2, N'Thịt heo', N'Sụn heo, nạc vai', N'Quản lý', 1),
(3, N'Hải sản tươi', N'Tôm sú, mực ống', N'Quản lý', 1),
(4, N'Nấm tươi', N'Nấm kim châm, nấm đùi gà', N'Quản lý', 1),
(5, N'Bia chai', N'Heineken, Tiger', N'Quản lý', 1);

-----------------------------------------------------------------------------------
-- 12. BẢNG PHIẾU GIẢM GIÁ
-----------------------------------------------------------------------------------
INSERT INTO phieu_giam_gia (id_dot_khuyen_mai, code_giam_gia, ten_phieu_giam_gia, loai_giam_gia, gia_tri_giam, gia_tri_giam_toi_da, don_hang_toi_thieu,doi_tuong, ngay_bat_dau, ngay_ket_thuc, so_luong, trang_thai, nguoi_tao) VALUES 
(1, 'WELCOME2026', N'Giảm 50k khai trương', 0, 50000, 50000,50000,0,  '2026-01-01', '2026-02-01', 100, 1, N'Quản lý'),
(2, 'LUNCH50', N'Giảm 10% trưa', 1, 10, 100000,50000,0, '2026-01-15', '2026-03-15', 50, 1, N'Quản lý'),
(3, 'VIP200', N'Tri ân VIP 200k', 0, 200000, 200000,50000,0, '2026-01-01', '2026-12-31', 20, 1, N'Quản lý'),
(4, 'TET2026', N'Lì xì Tết 15%', 1, 30, 300000,50000,1, '2026-01-20', '2026-02-10', 200, 1, N'Quản lý'),
(5, 'FREEWATER', N'Freeship đồ uống', 0, 30000,50000, 0,1, '2026-01-01', '2026-06-30', 500, 1, N'Quản lý');
-----------------------------------------------------------------------------------
-- 13. BẢNG SET LẨU
-----------------------------------------------------------------------------------
INSERT INTO set_lau (id_loai_set, ten_set_lau, mo_ta, gia_ban, hinh_anh, mo_ta_chi_tiet, nguoi_tao, trang_thai) VALUES 
(1, N'Buffet 199k', N'Gói cơ bản', 199000, 'buffet199.png', N'Bao gồm 10 món nhúng cơ bản', N'Quản lý', 1),
(1, N'Buffet 299k', N'Gói phổ thông', 299000, 'buffet299.png', N'Bao gồm 25 món nhúng + hải sản', N'Quản lý', 1),
(3, N'Combo Tình Nhân', N'Dành cho 2 người', 450000, 'combo_couple.png', N'1 nồi lẩu + đĩa thịt lớn + 2 nước', N'Quản lý', 1),
(4, N'Combo Family', N'Dành cho 4-6 người', 899000, 'combo_fam.png', N'2 nồi lẩu + hải sản tổng hợp', N'Quản lý', 1),
(5, N'Set Luxury VIP', N'Cao cấp nhất', 1500000, 'set_vip.png', N'Bào ngư, tôm hùm, bò Wagyu', N'Quản lý', 1);

INSERT INTO chi_tiet_khuyen_mai_set (id_dot_khuyen_mai,id_set_lau) values
(1,2),
(2,5),
(3,1),
(4,3),
(5,4);

-----------------------------------------------------------------------------------
-- 14. BẢNG MÓN ĂN ĐI KÈM
-----------------------------------------------------------------------------------
INSERT INTO mon_an_di_kem (id_danh_muc_chi_tiet, ten_mon_an, mo_ta, trang_thai_kinh_doanh, gia_ban, nguoi_tao, trang_thai) VALUES 
(1, N'Ba chỉ bò Mỹ', N'Thịt tươi mềm', 1, 120000, N'Quản lý', 1),
(3, N'Tôm sú tươi', N'Tôm loại 1 còn sống', 1, 150000, N'Quản lý', 1),
(4, N'Nấm kim châm', N'Nấm sạch giòn', 1, 35000, N'Quản lý', 1),
(5, N'Bia Heineken', N'Bia chai lạnh', 1, 28000, N'Quản lý', 1),
(2, N'Sụn heo giòn', N'Tẩm ướp gia vị', 1, 95000, N'Quản lý', 1);


INSERT INTO chi_tiet_khuyen_mai_mon(id_dot_khuyen_mai,id_mon_an_di_kem) values
(1,2),
(2,5),
(3,1),
(4,3),
(5,4);
-----------------------------------------------------------------------------------
-- 15. BẢNG HÌNH ẢNH MÓN ĂN
-----------------------------------------------------------------------------------
INSERT INTO hinh_anh_mon_an (id_mon_an_di_kem, url_anh, trang_thai) VALUES 
(1, 'bo_my_1.png', 1), (2, 'tom_su_1.png', 1), (3, 'nam_kim_1.png', 1), (4, 'ken_1.png', 1), (5, 'sun_heo_1.png', 1);

-----------------------------------------------------------------------------------
-- 16. BẢNG CHI TIẾT MÓN ĂN
-----------------------------------------------------------------------------------
INSERT INTO chi_tiet_mon_an (id_mon_an_di_kem, ten_chi_tiet_mon_an, gia_ban, gia_von, kich_co, don_vi, nguoi_tao, trang_thai) VALUES 
(1, N'Đĩa bò Mỹ lớn', 120000, 60000, 'L', N'Đĩa', N'Quản lý', 1),
(1, N'Đĩa bò Mỹ nhỏ', 70000, 35000, 'M', N'Đĩa', N'Quản lý', 1),
(4, N'Bia Heineken thùng', 650000, 580000, '24 chai', N'Thùng', N'Quản lý', 1),
(2, N'Tôm sú đĩa 10 con', 150000, 90000, 'Standard', N'Đĩa', N'Quản lý', 1),
(5, N'Sụn heo 200g', 95000, 45000, 'Small', N'Đĩa', N'Quản lý', 1);

-----------------------------------------------------------------------------------
-- 17. BẢNG KHÁCH HÀNG
-----------------------------------------------------------------------------------
INSERT INTO khach_hang (ten_khach_hang, so_dien_thoai, email, ngay_sinh, diem_tich_luy, ngay_tao_tai_khoan, gioi_tinh, ten_dang_nhap, mat_khau_dang_nhap, trang_thai, dia_chi) VALUES 
(N'Lê Anh Tuấn', '0912345001', 'tuan@gmail.com', '1992-10-10', 100, GETDATE(), 1, 'tuanle', '123', 1, N'Hà Nội'),
(N'Trần Thị Hoa', '0912345002', 'hoa@gmail.com', '1995-05-05', 50, GETDATE(), 0, 'hoatran', '123', 1, N'Hà Nam'),
(N'Nguyễn Mạnh Hùng', '0912345003', 'hung@gmail.com', '1988-02-28', 500, GETDATE(), 1, 'hunghp', '123', 1, N'Hải Phòng'),
(N'Hoàng Bảo Ngọc', '0912345004', 'ngoc@gmail.com', '2001-11-20', 0, GETDATE(), 0, 'ngochb', '123', 1, N'Thái Bình'),
(N'Vũ Đình Nam', '0912345005', 'nam@gmail.com', '1997-09-15', 10, GETDATE(), 1, 'namvu', '123', 1, N'Hưng Yên');

-----------------------------------------------------------------------------------
-- 18. BẢNG PHIẾU ĐẶT BÀN
-----------------------------------------------------------------------------------
INSERT INTO phieu_dat_ban (id_ban_an, id_khach_hang, thoi_gian_dat, hinh_thuc_dat, so_luong_khach, nguoi_tao, trang_thai) VALUES 
(1, 1, '2026-01-20 18:30', 1, 4, N'Khách hàng', 1),
(3, 3, '2026-01-20 19:00', 2, 8, N'Nhân viên', 1),
(5, 5, '2026-01-21 11:30', 1, 6, N'Khách hàng', 1),
(2, 2, '2026-01-21 18:00', 1, 2, N'Khách hàng', 1),
(4, 4, '2026-01-22 20:00', 2, 4, N'Nhân viên', 1);

-----------------------------------------------------------------------------------
-- 19. BẢNG HÓA ĐƠN
-----------------------------------------------------------------------------------
INSERT INTO hoa_don_thanh_toan (id_nhan_vien, id_khach_hang, id_ban_an, id_phieu_dat_ban, thoi_gian_tao, thoi_gian_thanh_toan, tong_tien_chua_giam, so_tien_da_giam, tong_tien_thanh_toan, trang_thai_hoa_don, ghi_chu, tien_khach_dua, tien_thua) VALUES 
(2, 1, 1, 1, GETDATE(), GETDATE(), 450000, 50000, 400000, 1, N'Khách hài lòng', 500000, 100000),
(2, 3, 3, 2, GETDATE(), GETDATE(), 3000000, 200000, 2800000, 1, N'Tiệc công ty', 3000000, 200000),
(2, 2, 2, 4, GETDATE(), GETDATE(), 199000, 0, 199000, 1, NULL, 200000, 1000),
(2, 5, 5, 3, GETDATE(), GETDATE(), 899000, 100000, 799000, 1, N'Khách VIP', 800000, 1000),
(2, 4, 4, 5, GETDATE(), NULL, 1500000, 0, 1500000, 0, N'Đang chờ', 0, 0);

-----------------------------------------------------------------------------------
-- 20. BẢNG CHI TIẾT HÓA ĐƠN
-----------------------------------------------------------------------------------
INSERT INTO chi_tiet_hoa_don (id_hoa_don, id_set_lau, id_chi_tiet_mon_an, so_luong, don_gia_tai_thoi_diem_ban, thanh_tien, ghi_chu_mon, trang_thai_mon) VALUES 
(1, 3, NULL, 1, 450000, 450000, N'Cay vừa', 1),
(2, 5, NULL, 2, 1500000, 3000000, N'Nhiều hải sản', 1),
(3, 1, NULL, 1, 199000, 199000, NULL, 1),
(4, 4, NULL, 1, 899000, 899000, NULL, 1),
(5, 5, NULL, 1, 1500000, 1500000, N'Phục vụ nhanh', 1);

-----------------------------------------------------------------------------------
-- 21. BẢNG PHIẾU GIẢM GIÁ CÁ NHÂN
-----------------------------------------------------------------------------------
INSERT INTO phieu_giam_gia_ca_nhan (id_khach_hang, id_phieu_giam_gia, id_hoa_don_thanh_toan, ngay_nhan, ngay_su_dung, trang_thai_su_dung, nguon_goc, ngay_het_han) VALUES 
(1, 1, 1, GETDATE(), GETDATE(), 1, N'App', '2026-12-31'),
(3, 3, 2, GETDATE(), GETDATE(), 1, N'Quầy', '2026-12-31'),
(2, 2, NULL, GETDATE(), NULL, 0, N'Zalo', '2026-12-31'),
(4, 4, NULL, GETDATE(), NULL, 0, N'Email', '2026-12-31'),
(5, 5, NULL, GETDATE(), NULL, 0, N'Sự kiện', '2026-12-31');

-----------------------------------------------------------------------------------
-- 22. BẢNG LỊCH SỬ THANH TOÁN
-----------------------------------------------------------------------------------
INSERT INTO lich_su_thanh_toan (id_phuong_thuc_thanh_toan, id_hoa_don, ten_phuong_thuc, ma_giao_dich, so_tien_thanh_toan, trang_thai) VALUES 
(1, 1, N'Tiền mặt', 'CASH001', 400000, 1),
(2, 2, N'Chuyển khoản', 'BANK999', 2800000, 1),
(1, 3, N'Tiền mặt', 'CASH002', 199000, 1),
(3, 4, N'Ví MoMo', 'MOMO777', 799000, 1),
(4, 5, N'Thẻ ATM', 'CARD555', 0, 0);

-----------------------------------------------------------------------------------
-- 23. BẢNG LOG HÓA ĐƠN
-----------------------------------------------------------------------------------
INSERT INTO lich_su_hoa_don (id_nhan_vien, id_hoa_don, hanh_dong, thoi_gian_thuc_hien, ly_do_thuc_hien, trang_thai_truoc_do, trang_thai_moi) VALUES 
(2, 1, N'Thanh toán', GETDATE(), N'Hoàn tất bill', 0, 1),
(1, 2, N'Duyệt giảm giá', GETDATE(), N'Khách VIP đặc biệt', 0, 0),
(2, 3, N'In lại hóa đơn', GETDATE(), N'Khách xin thêm bản copy', 1, 1),
(2, 5, N'Tạo hóa đơn', GETDATE(), N'Khách bắt đầu ăn', 0, 0),
(2, 4, N'Thanh toán', GETDATE(), N'App MoMo xác nhận', 0, 1);

-----------------------------------------------------------------------------------
-- 24. BẢNG ĐÁNH GIÁ
-----------------------------------------------------------------------------------
INSERT INTO danh_gia (id_khach_hang, id_hoa_don, so_sao, noi_dung, trang_thai) VALUES 
(1, 1, 5, N'Nước lẩu rất ngon, phục vụ nhanh', 1),
(3, 2, 5, N'Phòng VIP sang trọng, hải sản tươi', 1),
(2, 3, 4, N'Hơi ồn một chút nhưng đồ ăn ổn', 1),
(5, 4, 5, N'Giá hợp lý, sẽ quay lại', 1),
(4, 5, 3, N'Đang chờ món hơi lâu', 0);

-----------------------------------------------------------------------------------
-- 25. BẢNG HÌNH ẢNH ĐÁNH GIÁ
-----------------------------------------------------------------------------------
INSERT INTO hinh_anh_danh_gia (id_danh_gia, url_anh, trang_thai) VALUES 
(1, 'review1_1.jpg', 1), (2, 'review2_1.jpg', 1), (3, 'review3_1.jpg', 1), (4, 'review4_1.jpg', 1), (5, 'review5_1.jpg', 1);

-----------------------------------------------------------------------------------
-- 26. BẢNG GIAO CA
-----------------------------------------------------------------------------------
INSERT INTO giao_ca (id_nhan_vien, tien_mat_ca_truoc, tien_mat_trong_ca, tien_ck_trong_ca, tong_tien_trong_ca, tien_mat_thuc_te, tong_tien_phat_sinh, ghi_chu, trang_thai) VALUES 
(2, 2000000, 5000000, 3000000, 8000000, 7000000, 0, N'Bàn giao đủ', 1),
(2, 7000000, 4500000, 2000000, 6500000, 11500000, 0, N'Ca tối đông khách', 1),
(1, 11500000, 10000000, 5000000, 15000000, 21500000, 0, N'Ca sáng cuối tuần', 1),
(2, 21500000, 3000000, 1000000, 4000000, 24500000, 0, N'Bình thường', 1),
(2, 24500000, 8000000, 5000000, 13000000, 32500000, 0, N'Hết ca', 1);

-----------------------------------------------------------------------------------
-- 27. BẢNG LỊCH LÀM VIỆC
-----------------------------------------------------------------------------------
INSERT INTO lich_lam_viec (id_nhan_vien, ngay, vi_tri, gio_bat_dau, gio_ket_thuc, ghi_chu, trang_thai) VALUES 
(2, '2026-01-20', N'Thu ngân', '08:00', '16:00', N'Ca sáng', 1),
(3, '2026-01-20', N'Phục vụ sảnh', '08:00', '16:00', N'Ca sáng', 1),
(4, '2026-01-20', N'Bếp chính', '08:00', '22:00', N'Full ca', 1),
(5, '2026-01-20', N'Tiếp tân', '16:00', '23:00', N'Ca tối', 1),
(1, '2026-01-21', N'Văn phòng', '09:00', '18:00', N'Hành chính', 1);

SELECT * phieu_giam_gia , 
CASE 
    WHEN ngay_ket_thuc < NOW() THEN 2 
    ELSE trang_thai 
END as status_effective
FROM phieu_giam_gia
WHERE (target_status IS NULL OR status_effective = target_status)