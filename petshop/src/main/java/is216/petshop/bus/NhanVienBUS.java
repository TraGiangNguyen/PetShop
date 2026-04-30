/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.bus;

import is216.petshop.dao.NhanVienDAO;
import is216.petshop.model.NhanVien;
import java.util.List;
/**
 *
 * @author Trần Minh Quyền
 */
public class NhanVienBUS {
    private NhanVienDAO nhanVienDAO;

    public NhanVienBUS() {
        nhanVienDAO = new NhanVienDAO();
    }

    public List<NhanVien> getDanhSachNhanVien() {
        // Có thể thêm logic kiểm tra quyền, định dạng dữ liệu ở đây
        return nhanVienDAO.getAllNhanVien();
    }
}
