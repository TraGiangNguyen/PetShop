package test;

import is216.petshop.view.MainFrame;

public class testMain {
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}