/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop;

import is216.petshop.view.NhanVienPanel;
import com.formdev.flatlaf.FlatLightLaf; // Import thư viện FlatLaf
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
/**
 *
 * @author Trần Minh Quyền
 */
public class Main {
    public static void main(String[] args) {
        
        // 1. CÀI ĐẶT GIAO DIỆN FLATLAF (Phải gọi đoạn này ĐẦU TIÊN)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Kích hoạt giao diện sáng, phẳng, hiện đại
        } catch (Exception ex) {
            System.err.println("Không thể khởi tạo giao diện FlatLaf");
        }

        // 2. Chạy giao diện như bình thường
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Hệ thống quản lý cửa hàng thú cưng");
                frame.setSize(1200, 800);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                
                NhanVienPanel nhanVienPanel = new NhanVienPanel();
                frame.add(nhanVienPanel);
                
                frame.setVisible(true);
            }
        });
    }
}
