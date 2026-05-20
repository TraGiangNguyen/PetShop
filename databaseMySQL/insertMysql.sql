
USE `petstore`;

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO CUA_HANG(MACUAHANG, TENCUAHANG, DIACHI, SODIENTHOAI)
VALUES
(1, 'Pet Store Thủ Đức', 'Thủ Đức, TP.HCM', '0909000001'),
(2, 'Pet Store Dĩ An', 'Dĩ An, Bình Dương', '0909000002');

INSERT INTO DOI_TAC(MADOITAC, TENDOITAC, SODIENTHOAI, DIACHI, EMAIL, LOAIDOITAC)
VALUES
(1, 'Nguyễn Văn A', '0911111111', 'TP.HCM', 'a@gmail.com', 'KHACH_HANG'),
(2, 'Trần Thị B', '0922222222', 'Bình Dương', 'b@gmail.com', 'KHACH_HANG'),
(3, 'Royal Canin', '0933333333', 'Hà Nội', 'royal@gmail.com', 'NHA_CUNG_CAP');

INSERT INTO KHACH_HANG(MADOITAC, DIEMTICHLUY, NGAYTHAMGIA, LOAIKHACHHANG)
VALUES
(1, 200, '2025-01-10', 'Bạc'),
(2, 500, '2025-02-15', 'Vàng');

INSERT INTO NHA_CUNG_CAP
(MADOITAC, MASOTHUE, DIEUKHOANTHANHTOAN, GHICHU)
VALUES
(3, '0312345678', 'Thanh toán 30 ngày', 'Nhà cung cấp thức ăn');

INSERT INTO SAN_PHAM
(MASANPHAM, TENSANPHAM, DONVITINH, GIANIEMYET, THUE, MAVACH,
COTHEMUA, COTHEBAN, POS, THUONGHIEU, XUATXU,
PHUHOP, THANHPHAN, HUONGDAN, MOTA)
VALUES
(1, 'Royal Canin Mini Adult', 'Gói', 250000, 5, '8931111111111',
TRUE, TRUE, TRUE, 'Royal Canin', 'Pháp',
'Chó nhỏ', 'Thịt gà', 'Cho ăn 2 lần/ngày', 'Thức ăn cho chó'),

(2, 'Pedigree Puppy', 'Gói', 180000, 5, '8932222222222',
TRUE, TRUE, TRUE, 'Pedigree', 'Mỹ',
'Chó con', 'Ngũ cốc', 'Cho ăn theo cân nặng', 'Thức ăn cho chó con');

INSERT INTO NHAN_VIEN
(MANHANVIEN, MACUAHANG, HOTEN, SDT, EMAIL, NGAYVAOLAM, TRANGTHAI)
VALUES
(1, 1, 'Lê Minh Quân', '0901111111', 'quan@gmail.com', '2024-01-10', 'Đang làm việc'),
(2, 1, 'Nguyễn Hải Nam', '0902222222', 'nam@gmail.com', '2024-03-20', 'Đang làm việc'),
(3, 1, 'Trần Minh Đức', '0903333333', 'duc@gmail.com', '2024-04-01', 'Đang làm việc'),
(4, 1, 'Phạm Quốc Huy', '0904444444', 'huy@gmail.com', '2024-04-05', 'Đang làm việc'),
(5, 1, 'Nguyễn Hoàng Long', '0905555555', 'long@gmail.com', '2024-04-10', 'Đang làm việc'),
(6, 2, 'Lê Thanh Tùng', '0906666666', 'tung@gmail.com', '2024-04-15', 'Đang làm việc'),
(7, 2, 'Đỗ Gia Bảo', '0907777777', 'bao@gmail.com', '2024-04-20', 'Đang làm việc'),
(8, 2, 'Võ Nhật Nam', '0908888888', 'nhatnam@gmail.com', '2024-04-25', 'Đang làm việc'),
(9, 1, 'Bùi Anh Khoa', '0909999999', 'khoa@gmail.com', '2024-05-01', 'Đang làm việc'),
(10, 1, 'Ngô Minh Hiếu', '0910000000', 'hieu@gmail.com', '2024-05-05', 'Đang làm việc'),
(11, 2, 'Huỳnh Quốc Việt', '0911111112', 'viet@gmail.com', '2024-05-10', 'Đang làm việc'),
(12, 2, 'Phan Gia Hưng', '0912222222', 'hung@gmail.com', '2024-05-15', 'Đang làm việc');

INSERT INTO HO_SO_LUONG
(MANHANVIEN, MUCLUONG, GIAMTRUBANTHAN,
SONGUOIPHUTHUOC, TIENGIAMNPT, NGAYCAPNHAP)
VALUES
(1, 18000000, 15500000, 1, 6200000, '2025-01-01'),
(2, 12000000, 15500000, 0, 0, '2025-01-01'),
(3, 10000000, 15500000, 0, 0, '2025-01-01'),
(4, 11000000, 15500000, 1, 6200000, '2025-01-01'),
(5, 9500000, 15500000, 0, 0, '2025-01-01'),
(6, 13000000, 15500000, 2, 12400000, '2025-01-01'),
(7, 9000000, 15500000, 0, 0, '2025-01-01'),
(8, 10500000, 15500000, 1, 6200000, '2025-01-01'),
(9, 11500000, 15500000, 0, 0, '2025-01-01'),
(10, 14000000, 15500000, 2, 12400000, '2025-01-01'),
(11, 12500000, 15500000, 1, 6200000, '2025-01-01'),
(12, 9800000, 15500000, 0, 0, '2025-01-01');

INSERT INTO VAI_TRO(MAVAITRO, TENVAITRO, QUYENHAN)
VALUES
(1, 'Admin', JSON_ARRAY('ALL')),
(2, 'Nhân viên bán hàng', JSON_ARRAY('SELL'));

INSERT INTO VAI_TRO(MAVAITRO, TENVAITRO, QUYENHAN)
VALUES
(3, 'Quản lý kho', JSON_ARRAY('WAREHOUSE_VIEW', 'WAREHOUSE_UPDATE')),
(4, 'Nhân viên kho', JSON_ARRAY('WAREHOUSE_VIEW')),
(5, 'Kế toán', JSON_ARRAY('ACCOUNTING', 'INVOICE_VIEW')),
(6, 'Quản lý nhân sự', JSON_ARRAY('HR_MANAGE', 'EMPLOYEE_VIEW')),
(7, 'Thu ngân', JSON_ARRAY('POS_SELL', 'PAYMENT')),
(8, 'Nhân viên chăm sóc khách hàng', JSON_ARRAY('CUSTOMER_SUPPORT')),
(9, 'Quản lý chi nhánh', JSON_ARRAY('BRANCH_MANAGE', 'REPORT_VIEW')),
(10, 'Nhân viên kiểm kê', JSON_ARRAY('INVENTORY_CHECK')),
(11, 'Quản lý tài chính', JSON_ARRAY('FINANCE_MANAGE', 'REPORT_FINANCE')),
(12, 'Nhân viên marketing', JSON_ARRAY('MARKETING_CAMPAIGN'));


INSERT INTO PHAN_QUYEN_NHAN_VIEN(MAVAITRO, MANHANVIEN, NGAYGAN)
VALUES
(1, 1, '2025-01-01'),
(2, 2, '2025-01-01');

INSERT INTO PHAN_QUYEN_NHAN_VIEN
(MAVAITRO, MANHANVIEN, NGAYGAN)
VALUES
(3, 3, '2025-01-01'),
(4, 4, '2025-01-01'),
(5, 5, '2025-01-01'),
(6, 6, '2025-01-01'),
(7, 7, '2025-01-01'),
(8, 8, '2025-01-01'),
(9, 9, '2025-01-01'),
(10, 10, '2025-01-01'),
(11, 11, '2025-01-01'),
(12, 12, '2025-01-01');

INSERT INTO KHO(MAKHO, MACUAHANG, TENKHO, DIACHI)
VALUES
(1, 1, 'Kho Thức Ăn', 'Thủ Đức');

INSERT INTO TON_KHO(MASANPHAM, MAKHO, SOLUONGTON, NGAYCAPNHAP)
VALUES
(1, 1, 100, NOW()),
(2, 1, 50, NOW());

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

SELECT * FROM nhan_vien;
SELECT * FROM HO_SO_LUONG;


-- ================== procedure =============

DELIMITER //

CREATE PROCEDURE sp_GetDanhSachNhanVien(IN p_MaCuaHang INT)
BEGIN
    SELECT 
        nv.MANHANVIEN,
        nv.HOTEN AS 'Họ tên',
        IFNULL(vt.TENVAITRO, 'Chưa cấp quyền') AS 'Chức vụ',
        nv.SDT AS 'Số điện thoại',
        nv.EMAIL AS 'Email',
        IFNULL(hsl.MUCLUONG, 0) AS 'Lương'
    FROM NHAN_VIEN nv
    LEFT JOIN PHAN_QUYEN_NHAN_VIEN pq ON nv.MANHANVIEN = pq.MANHANVIEN
    LEFT JOIN VAI_TRO vt ON pq.MAVAITRO = vt.MAVAITRO
    LEFT JOIN HO_SO_LUONG hsl ON nv.MANHANVIEN = hsl.MANHANVIEN
    WHERE 
        (nv.MACUAHANG = p_MaCuaHang OR p_MaCuaHang IS NULL)
        AND nv.TRANGTHAI = 'Đang làm việc' -- Chỉ hiển thị người đang làm
    ORDER BY nv.MANHANVIEN DESC;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE sp_ThemNhanVien(
    IN p_MaCuaHang INT,
    IN p_HoTen NVARCHAR(255),
    IN p_SDT NVARCHAR(255),
    IN p_Email NVARCHAR(255),
    IN p_NgayVaoLam DATE,
    IN p_MaVaiTro INT,
    IN p_MucLuong BIGINT,
    IN p_Username NVARCHAR(255),
    IN p_PasswordHash NVARCHAR(255)
)
BEGIN
    DECLARE v_MaNhanVien INT;
    
    -- Xử lý lỗi: Nếu có bất kỳ lỗi SQLEXCEPTION nào xảy ra thì Rollback
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL; -- Ném lỗi ra ngoài cho Backend bắt
    END;

    START TRANSACTION;

    -- 1. Thêm thông tin cơ bản
    INSERT INTO NHAN_VIEN (MACUAHANG, HOTEN, SDT, EMAIL, NGAYVAOLAM, TRANGTHAI)
    VALUES (p_MaCuaHang, p_HoTen, p_SDT, p_Email, p_NgayVaoLam, 'Đang làm việc');
    
    -- Lấy ID vừa insert
    SET v_MaNhanVien = LAST_INSERT_ID();

    -- 2. Thêm vai trò (Chức vụ)
    IF p_MaVaiTro IS NOT NULL THEN
        INSERT INTO PHAN_QUYEN_NHAN_VIEN (MAVAITRO, MANHANVIEN, NGAYGAN)
        VALUES (p_MaVaiTro, v_MaNhanVien, CURDATE());
    END IF;

    -- 3. Thêm hồ sơ lương
    IF p_MucLuong IS NOT NULL THEN
        INSERT INTO HO_SO_LUONG (MANHANVIEN, MUCLUONG, NGAYCAPNHAP)
        VALUES (v_MaNhanVien, p_MucLuong, CURDATE());
    END IF;

    -- 4. Tạo tài khoản đăng nhập (Tùy chọn)
    IF p_Username IS NOT NULL AND p_PasswordHash IS NOT NULL THEN
        INSERT INTO TAI_KHOAN_NHAN_VIEN (MANHANVIEN, USERNAME, PASSWORDHASH, PASSWORDHASHHISTORY, NGAYTAO, TRANG_THAI)
        VALUES (v_MaNhanVien, p_Username, p_PasswordHash, p_PasswordHash, CURDATE(), TRUE);
    END IF;

    COMMIT;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE sp_CapNhatNhanVien(
    IN p_MaNhanVien INT,
    IN p_HoTen NVARCHAR(255),
    IN p_SDT NVARCHAR(255),
    IN p_Email NVARCHAR(255),
    IN p_MaVaiTro INT,
    IN p_MucLuong BIGINT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Cập nhật bảng Nhân viên
    UPDATE NHAN_VIEN 
    SET HOTEN = p_HoTen, SDT = p_SDT, EMAIL = p_Email
    WHERE MANHANVIEN = p_MaNhanVien;

    -- Cập nhật Vai trò (xóa cũ thêm mới cho nhanh, hoặc update nếu tồn tại)
    DELETE FROM PHAN_QUYEN_NHAN_VIEN WHERE MANHANVIEN = p_MaNhanVien;
    IF p_MaVaiTro IS NOT NULL THEN
        INSERT INTO PHAN_QUYEN_NHAN_VIEN (MAVAITRO, MANHANVIEN, NGAYGAN)
        VALUES (p_MaVaiTro, p_MaNhanVien, CURDATE());
    END IF;

    -- Cập nhật Lương
    UPDATE HO_SO_LUONG 
    SET MUCLUONG = p_MucLuong, NGAYCAPNHAP = CURDATE()
    WHERE MANHANVIEN = p_MaNhanVien;

    COMMIT;
END //

DELIMITER ;

-- ============trigger =================

DELIMITER //

CREATE TRIGGER trg_KhoaTaiKhoanKhiNghiViec
AFTER UPDATE ON NHAN_VIEN
FOR EACH ROW
BEGIN
    -- Nếu trạng thái đổi từ "Đang làm việc" sang "Đã nghỉ việc"
    IF NEW.TRANGTHAI = 'Đã nghỉ việc' AND OLD.TRANGTHAI != 'Đã nghỉ việc' THEN
        -- Khóa tài khoản của nhân viên này
        UPDATE TAI_KHOAN_NHAN_VIEN 
        SET TRANG_THAI = FALSE 
        WHERE MANHANVIEN = NEW.MANHANVIEN;
    END IF;
    
    -- Nếu nhân viên đi làm lại (Đổi lại trạng thái) thì mở khóa tài khoản
    IF NEW.TRANGTHAI = 'Đang làm việc' AND OLD.TRANGTHAI = 'Đã nghỉ việc' THEN
        UPDATE TAI_KHOAN_NHAN_VIEN 
        SET TRANG_THAI = TRUE 
        WHERE MANHANVIEN = NEW.MANHANVIEN;
    END IF;
END //

DELIMITER ;

COMMIT;


INSERT INTO `SAN_PHAM` (
    `TENSANPHAM`, 
    `DONVITINH`, 
    `GIANIEMYET`, 
    `THUE`, 
    `MAVACH`, 
    `COTHEMUA`, 
    `COTHEBAN`, 
    `POS`, 
    `THUONGHIEU`, 
    `XUATXU`, 
    `PHUHOP`, 
    `THANHPHAN`, 
    `HUONGDAN`, 
    `MOTA`
) VALUES 
(
    -- 1. Thức ăn cho chó con
    'Thức ăn hạt SmartHeart Gold Puppy',
    'Bao 1kg',
    165000,
    0.1,
    '8851234560012',
    TRUE, TRUE, TRUE,
    'SmartHeart',
    'Thái Lan',
    'Chó con dưới 1 năm tuổi',
    'Gạo, bột gia cầm, mỡ gà, bột củ cải đường, dầu cá, prebiotic (Galacto-oligosaccharide).',
    'Chia đều thành 3-4 bữa/ngày dựa theo cân nặng của cún. Cung cấp đủ nước.',
    'Hỗ trợ phát triển não bộ, tăng cường hệ miễn dịch và giúp hệ tiêu hóa của cún con khỏe mạnh.'
),
(
    -- 2. Súp thưởng cho mèo
    'Súp thưởng cho mèo Ciao Churu vị cá ngừ',
    'Gói 4 thanh',
    45000,
    0.1,
    '4901133718229',
    TRUE, TRUE, TRUE,
    'Inaba Ciao',
    'Nhật Bản',
    'Mọi giống mèo ở mọi độ tuổi',
    'Cá ngừ, chiết xuất cá ngừ, vitamin E, chiết xuất trà xanh, chất làm đặc.',
    'Cho ăn trực tiếp như món ăn vặt hoặc trộn chung với hạt để kích thích mèo ăn ngon miệng hơn.',
    'Súp thưởng dạng sệt thơm ngon, giàu độ ẩm, hỗ trợ hạn chế các bệnh về đường tiết niệu ở mèo.'
),
(
    -- 3. Cát vệ sinh cho mèo
    'Cát vệ sinh cho mèo Nhật Bản Đen hương Cà phê',
    'Túi 5L',
    60000,
    0.1,
    '8938521140023',
    TRUE, TRUE, TRUE,
    'Japan Litter',
    'Việt Nam',
    'Mèo đi vệ sinh trong khay',
    '100% Bentonite tự nhiên và hạt lưu hương cà phê.',
    'Đổ cát vào khay với độ dày từ 5-7cm. Dùng xẻng xúc bỏ phần cát vón cục hàng ngày.',
    'Cát bentonite khử mùi cực tốt, vón cục nhanh, không gây bụi bẩn và an toàn cho chân mèo.'
),
(
    -- 4. Đồ chơi thú cưng
    'Đồ chơi cần câu mèo lông vũ gắn chuông',
    'Cái',
    25000,
    0.1,
    '8931597530045',
    TRUE, TRUE, TRUE,
    'No Brand',
    'Trung Quốc',
    'Mèo ở mọi lứa tuổi',
    'Nhựa dẻo, lông vũ tự nhiên, chuông kim loại.',
    'Cầm tay lắc nhẹ trước mặt mèo để kích thích bản năng săn mồi và cùng chơi với chúng.',
    'Giúp mèo giải tỏa căng thẳng, tăng cường vận động tránh béo phì khi nuôi trong nhà.'
),
(
    -- 5. Nệm ngủ cho chó mèo
    'Nệm tròn bông cho thú cưng Size M',
    'Cái',
    180000,
    0.1,
    '8930012234556',
    TRUE, TRUE, TRUE,
    'PetStyle',
    'Trung Quốc',
    'Chó mèo dưới 7kg',
    'Vải thô cao cấp, bông gòn PP đàn hồi, đế chống trượt.',
'Đặt ở nơi khô ráo, thoáng mát trong nhà. Có thể giặt máy ở chế độ nhẹ.',
    'Ổ đệm siêu êm ái, giữ ấm tốt, tạo không gian riêng tư và đem lại giấc ngủ sâu cho thú cưng.'
),
(
    -- 6. Thực phẩm chức năng / Gel dinh dưỡng
    'Gel dinh dưỡng tăng cân cho chó mèo Virbac Megaderm',
    'Hộp 28 gói',
    315000,
    0.1,
    '3423240015504',
    TRUE, TRUE, TRUE,
    'Virbac',
    'Pháp',
    'Chó mèo bị rụng lông, da khô hoặc sau khi ốm',
    'Các axit béo thiết yếu Omega 3 và Omega 6 từ nguồn dầu cá và dầu cây lưu ly.',
    'Trộn vào thức ăn hoặc cho ăn trực tiếp. 1 gói/ngày cho thú cưng dưới 10kg.',
    'Bổ sung dưỡng chất tối ưu giúp cải thiện độ mượt của lông, giảm ngứa và hỗ trợ điều trị viêm da.'
),
(
    -- 7. Dịch vụ lưu trú (Pet Hotel)
    'Dịch vụ Khách sạn thú cưng - Phòng Deluxe',
    'Ngày',
    200000,
    0.1,
    'HOTELDX01',
    FALSE, TRUE, TRUE, -- COTHEMUA = FALSE vì là dịch vụ tự có
    'PetStore Hotel',
    'Việt Nam',
    'Chó hoặc mèo dưới 10kg',
    'Không áp dụng (Dịch vụ lưu trú)',
    'Nhận và trả thú cưng từ 8h00 đến 20h00 hàng ngày. Yêu cầu thú cưng đã tiêm ngừa đầy đủ.',
    'Phòng lưu trú riêng biệt có điều hòa 24/7, camera giám sát cho chủ nuôi và chế độ ăn ngày 2 bữa.'
),
(
    -- 8. Dịch vụ đưa đón tận nơi
    'Dịch vụ đưa đón thú cưng nội thành dưới 5km',
    'Lượt',
    80000,
    0.1,
    'DELIVER05',
    FALSE, TRUE, TRUE, -- COTHEMUA = FALSE vì là dịch vụ tự có
    'PetStore Express',
    'Việt Nam',
    'Tất cả các loại thú cưng',
    'Không áp dụng (Dịch vụ vận chuyển)',
    'Khách hàng đặt lịch trước tối thiểu 1 tiếng để tài xế chuẩn bị lồng vận chuyển chuyên dụng.',
    'Dịch vụ đưa đón an toàn bằng ô tô/xe máy chuyên dụng, đảm bảo thú cưng không bị hoảng sợ.'
),
(
    -- 9. Vật tư tiêu hao nội bộ (Test logic hệ thống)
    'Găng tay y tế dùng trong Spa & Khám bệnh',
    'Hộp 100 cái',
    95000,
    0.1,
    '8936001122334',
    TRUE, 
    FALSE, -- COTHEBAN = FALSE (Không bán ra ngoài)
    FALSE, -- POS = FALSE (Thu ngân không quét mã bán lẻ cái này)
    'Vglove',
    'Việt Nam',
    'Nhân viên Spa và kỹ thuật viên',
    'Cao su tự nhiên Nitrile không bột.',
    'Sử dụng 1 lần khi tắm rửa, cắt tỉa hoặc xử lý vệ sinh cho thú cưng nhiễm bệnh.',
    'Vật tư tiêu hao dùng nội bộ trong chuỗi cửa hàng, phục vụ công tác vệ sinh và chăm sóc.'
),
(
    -- 10. Thú cưng (Hàng hóa sống)
    'Chó Phốc Sóc (Pomeranian) thuần chủng 2 tháng tuổi',
    'Con',
    8500000,
0.0, -- Thuế 0% đối với con giống vật nuôi theo quy định
    'PETPOM2M',
    TRUE, TRUE, TRUE,
    'PetStore Kennel',
    'Việt Nam',
    'Khách hàng tìm kiếm thú cưng',
    'Động vật sống (Đã tiêm 1 mũi vắc xin và tẩy giun)',
    'Nuôi ở nơi ấm áp, cho ăn cháo hoặc hạt ngâm mềm. Tránh tắm trong 1 tuần đầu sau khi đón.',
    'Bé Phốc sóc màu trắng siêu thuần chủng, năng động, quấn người, có sổ sức khỏe kèm theo.'
);

-- Kiểm tra lại toàn bộ danh mục sản phẩm sau khi chèn thêm
SELECT* from `SAN_PHAM`;
UPDATE `SAN_PHAM`
SET `SL` = 10
WHERE `MASANPHAM` >3;

ALTER TABLE `NHAN_VIEN` MODIFY COLUMN `TRANGTHAI` BOOLEAN;

ALTER TABLE `NHAN_VIEN` DROP COLUMN `TAIKHOAN` nvarchar(255);
ALTER TABLE `NHAN_VIEN` DROP COLUMN `MATKHAU` nvarchar(255);
INSERT INTO `CUA_HANG` (`TENCUAHANG`,`TRANGTHAI`) VALUES ('Cửa hàng chính',true)
select * from `NHAN_VIEN`
INSERT INTO `NHAN_VIEN` (`MACUAHANG`,`HOTEN`,`SDT`,`EMAIL`,`NGAYVAOLAM`,`TRANGTHAI`) VALUES
(1,'Nguyen Van An',0912345678,'an@gmail.com','2026-12-12',true)
INSERT INTO `NHAN_VIEN` (`MACUAHANG`,`HOTEN`,`SDT`,`EMAIL`,`NGAYVAOLAM`,`TRANGTHAI`) VALUES

SELECT * FROM `TAI_KHOAN_NHAN_VIEN`
alter table `TAI_KHOAN_NHAN_VIEN` DROP COLUMN `PASSWORDHASH`
alter table `TAI_KHOAN_NHAN_VIEN` DROP COLUMN `PASSWORDHASHHISTORY`
alter table `TAI_KHOAN_NHAN_VIEN` ADD COLUMN `PASSWORD` nvarchar(255)
INSERT INTO `TAI_KHOAN_NHAN_VIEN` (`MANHANVIEN`,`USERNAME`,`PASSWORD`) VALUES (1,'admin','123');


