/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package is216.petshop;

import is216.petshop.Login.LoginForm;
import is216.petshop.Login.LoginController;
import is216.petshop.dao.UserDAO;
import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;

/**
 *
 * @author Admin
 */
public class Main {

    public static void main(String[] args) {
        // 1. CÀI ĐẶT GIAO DIỆN FLATLAF
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Không thể khởi tạo giao diện FlatLaf");
        }

        // 2. Chạy giao diện
        SwingUtilities.invokeLater(() -> {
            // Khởi tạo Model (DAO)
            UserDAO modelDAO = new UserDAO();

            // Khởi tạo View (Giao diện đẹp)
            LoginForm loginView = new LoginForm();

            // Khởi tạo Controller (Kết nối View + Model)
            new LoginController(loginView, modelDAO);

            // Hiển thị
            loginView.setVisible(true);
        });
    }
}
