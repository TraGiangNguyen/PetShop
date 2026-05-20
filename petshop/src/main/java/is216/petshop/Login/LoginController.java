/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.Login;

import is216.petshop.dao.UserDAO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import is216.petshop.view.MainFrame;
import is216.petshop.Login.LoginModel;

public class LoginController {
    private LoginForm view;
    private UserDAO modelDAO;

    public LoginController(LoginForm view, UserDAO modelDAO) {
        this.view = view;
        this.modelDAO = modelDAO;

        // Gắn sự kiện lắng nghe vào View
        this.view.addLoginListener(new LoginListener());
    }

    // Lớp nội bộ xử lý logic khi nút Đăng nhập được bấm
    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsername();
            String password = view.getPassword();

            // 1. Kiểm tra rỗng
            if (username.isEmpty() || password.isEmpty()) {
                view.showMessage("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!", true);
                return;
            }

            // 2. Gọi Model (UserDAO) để kiểm tra Database
            boolean isValid = modelDAO.checkLogin(username, password);

            // 3. Xử lý kết quả
            if (isValid) {
                view.showMessage("Đăng nhập thành công! Chào mừng, " + username + " 🐾", false);
                
            // 1. Đóng cửa sổ đăng nhập hiện tại
            view.dispose(); 
            
            // 2. Mở cửa sổ Chính (MainFrame)
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            } else {
                view.showMessage("Tên đăng nhập hoặc mật khẩu không đúng!", true);
                view.clearPassword();
            }
        }
    }
}