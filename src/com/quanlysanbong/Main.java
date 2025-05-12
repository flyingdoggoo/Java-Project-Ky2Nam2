package com.quanlysanbong;

import com.quanlysanbong.db.DatabaseConnection;
import com.quanlysanbong.ui.LoginForm;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Cố gắng thiết lập Look and Feel hệ thống cho đẹp hơn
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Không thể đặt Look and Feel hệ thống: " + e.getMessage());
        }

        // Hiển thị màn hình đăng nhập khi ứng dụng bắt đầu
        SwingUtilities.invokeLater(() -> {
            // Kiểm tra kết nối DB trước khi mở UI
            if (DatabaseConnection.getConnection() == null) {
                JOptionPane.showMessageDialog(null,
                        "Không thể kết nối đến cơ sở dữ liệu. Vui lòng kiểm tra cấu hình và khởi động lại.",
                        "Lỗi kết nối CSDL", JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Thoát ứng dụng nếu không kết nối được DB
            }

            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });

        // Thêm shutdown hook để đóng kết nối CSDL khi ứng dụng thoát
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Ứng dụng đang đóng, đóng kết nối CSDL...");
            DatabaseConnection.closeConnection();
        }));
    }
}