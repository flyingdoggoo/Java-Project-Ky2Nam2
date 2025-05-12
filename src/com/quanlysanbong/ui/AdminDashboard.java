package com.quanlysanbong.ui;

import com.quanlysanbong.util.CurrentUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Admin Dashboard - Quản lý sân bóng");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Handle close manually
        setLocationRelativeTo(null);

        // --- Menu Bar ---
        JMenuBar menuBar = new JMenuBar();

        // -- Quản lý Menu --
        JMenu manageMenu = new JMenu("Quản lý");
        JMenuItem fieldManagementItem = new JMenuItem("Quản lý Sân");
        JMenuItem userManagementItem = new JMenuItem("Quản lý Người dùng");
        JMenuItem viewAvailabilityItem = new JMenuItem("Xem Lịch Trống Sân");
        manageMenu.addSeparator(); // Phân cách nếu muốn
        manageMenu.add(viewAvailabilityItem);
        manageMenu.add(fieldManagementItem);
        manageMenu.add(userManagementItem);
        menuBar.add(manageMenu);

        // -- Doanh thu Menu --
        JMenu revenueMenu = new JMenu("Doanh thu");
//        JMenuItem historyItem = new JMenuItem("Lịch sử Đặt sân (Tất cả)");
        JMenuItem statsItem = new JMenuItem("Thống kê Doanh thu");
//        revenueMenu.add(historyItem);
        revenueMenu.add(statsItem);
        menuBar.add(revenueMenu);

        // -- Tài khoản Menu --
        JMenu accountMenu = new JMenu("Tài khoản");
        JMenuItem profileItem = new JMenuItem("Xem thông tin cá nhân");
        JMenuItem logoutItem = new JMenuItem("Đăng xuất");
        accountMenu.add(profileItem);
        accountMenu.addSeparator();
        accountMenu.add(logoutItem);
        menuBar.add(accountMenu);

        setJMenuBar(menuBar);

        // --- Welcome Panel (Optional) ---
        JPanel welcomePanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Chào mừng Admin: " + CurrentUser.getUser().getFullName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);
        add(welcomePanel); // Add to frame

        // --- Menu Item Actions ---
        fieldManagementItem.addActionListener(e -> openFieldManagement());
        userManagementItem.addActionListener(e -> openUserManagement());
//        historyItem.addActionListener(e -> openBookingHistory());
        statsItem.addActionListener(e -> openRevenueStats());
        profileItem.addActionListener(e -> openProfile());
        logoutItem.addActionListener(e -> logout());
        viewAvailabilityItem.addActionListener(e -> openFieldAvailability());

        // --- Window Closing Action ---
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });
    }
    private void openFieldAvailability() {
        FieldAvailabilityDialog dialog = new FieldAvailabilityDialog(this); // Owner là AdminDashboard
        dialog.setVisible(true);
    }
    private void openFieldManagement() {
        FieldManagementDialog dialog = new FieldManagementDialog(this);
        dialog.setVisible(true);
    }

    private void openUserManagement() {
        UserManagementDialog dialog = new UserManagementDialog(this);
        dialog.setVisible(true);
    }

    private void openBookingHistory() {
        // Dùng chung dialog xem lịch sử, truyền null hoặc role để biết là xem của ai
        BookingHistoryDialog dialog = new BookingHistoryDialog(this, null); // null user ID means view all
        dialog.setVisible(true);
    }

    private void openRevenueStats() {
        RevenueDialog dialog = new RevenueDialog(this);
        dialog.setVisible(true);
    }

    private void openProfile() {
        ProfileDialog dialog = new ProfileDialog(this, CurrentUser.getUser());
        dialog.setVisible(true);
        // Sau khi dialog đóng, có thể cần cập nhật lại welcome label nếu thông tin thay đổi
        // JLabel welcomeLabel = (JLabel)((JPanel)getContentPane().getComponent(0)).getComponent(0); // Cách lấy lại label hơi phức tạp
        // welcomeLabel.setText("Chào mừng Admin: " + CurrentUser.getUser().getFullName());
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            CurrentUser.logout();
            this.dispose(); // Đóng dashboard
            // Mở lại form đăng nhập
            SwingUtilities.invokeLater(() -> {
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
            });
        }
    }

    // --- Placeholder Dialog classes (Cần tạo các file .java riêng) ---
    // Ví dụ:
    // class FieldManagementDialog extends JDialog { ... }
    // class UserManagementDialog extends JDialog { ... }
    // class BookingHistoryDialog extends JDialog { ... }
    // class RevenueDialog extends JDialog { ... }
    // class ProfileDialog extends JDialog { ... }

}