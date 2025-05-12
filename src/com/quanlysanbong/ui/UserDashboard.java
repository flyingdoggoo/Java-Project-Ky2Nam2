package com.quanlysanbong.ui;

import com.quanlysanbong.util.CurrentUser;
import com.quanlysanbong.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.Locale;

public class UserDashboard extends JFrame {

    private JLabel balanceLabel; // Để cập nhật số dư

    public UserDashboard() {
        initComponents();
        updateBalanceLabel(); // Hiển thị số dư ban đầu
    }

    private void initComponents() {
        setTitle("User Dashboard - Quản lý sân bóng");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Menu Bar ---
        JMenuBar menuBar = new JMenuBar();

        // -- Sân bóng Menu --
        JMenu fieldMenu = new JMenu("Sân bóng");
        JMenuItem bookFieldItem = new JMenuItem("Đặt sân");
        JMenuItem cancelBookingItem = new JMenuItem("Hủy đặt sân");
        JMenuItem historyItem = new JMenuItem("Xem lịch sử đặt sân");
        JMenuItem viewAvailabilityItem = new JMenuItem("Xem Lịch Trống Sân");
        fieldMenu.addSeparator(); // Đặt trước "Xem lịch sử"
        fieldMenu.add(viewAvailabilityItem);
        fieldMenu.add(bookFieldItem);
        fieldMenu.add(cancelBookingItem);
        fieldMenu.addSeparator();
        fieldMenu.add(historyItem);
        menuBar.add(fieldMenu);

        // -- Tài khoản Menu --
        JMenu accountMenu = new JMenu("Tài khoản");
//        JMenuItem depositItem = new JMenuItem("Nạp tiền");
        JMenuItem profileItem = new JMenuItem("Xem thông tin cá nhân");
        JMenuItem logoutItem = new JMenuItem("Đăng xuất");
//        accountMenu.add(depositItem);
        accountMenu.add(profileItem);
        accountMenu.addSeparator();
        accountMenu.add(logoutItem);
        menuBar.add(accountMenu);

        setJMenuBar(menuBar);

        // --- Main Panel (Welcome & Balance) ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)); // Add gaps
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Add padding

        JLabel welcomeLabel = new JLabel("Xin chào: " + CurrentUser.getUser().getFullName(), SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        balanceLabel = new JLabel("Số dư: LOADING...", SwingConstants.RIGHT);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setForeground(Color.BLUE.darker()); // Màu cho số dư

        mainPanel.add(welcomeLabel, BorderLayout.NORTH);
        mainPanel.add(new JSeparator(), BorderLayout.CENTER); // Đường kẻ phân cách
        mainPanel.add(balanceLabel, BorderLayout.SOUTH);

        add(mainPanel);

        // --- Menu Item Actions ---
        bookFieldItem.addActionListener(e -> openBookField());
        cancelBookingItem.addActionListener(e -> openCancelBooking());
        historyItem.addActionListener(e -> openBookingHistory());
//        depositItem.addActionListener(e -> openDeposit());
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
        FieldAvailabilityDialog dialog = new FieldAvailabilityDialog(this); // Owner là UserDashboard
        dialog.setVisible(true);
    }
    // Phương thức cập nhật hiển thị số dư
    public void updateBalanceLabel() {
        User currentUser = CurrentUser.getUser();
        if (currentUser != null && balanceLabel != null) {
            // Định dạng tiền tệ Việt Nam
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedBalance = currencyFormatter.format(currentUser.getBalance());
            balanceLabel.setText("Số dư: " + formattedBalance);
        }
    }


    private void openBookField() {
        BookFieldDialog dialog = new BookFieldDialog(this); // Pass 'this' to update balance later
        dialog.setVisible(true);
        // Sau khi dialog đặt sân đóng, cập nhật lại số dư
        updateBalanceLabel();
    }

    private void openCancelBooking() {
        CancelBookingDialog dialog = new CancelBookingDialog(this);
        dialog.setVisible(true);
        // Sau khi dialog hủy sân đóng, cập nhật lại số dư
        updateBalanceLabel();
    }

    private void openBookingHistory() {
        BookingHistoryDialog dialog = new BookingHistoryDialog(this, CurrentUser.getUser()); // Pass current user
        dialog.setVisible(true);
    }

    private void openDeposit() {
        DepositDialog dialog = new DepositDialog(this); // Pass 'this' to update balance
        dialog.setVisible(true);
        // Sau khi dialog nạp tiền đóng, cập nhật lại số dư
        updateBalanceLabel();
    }

    private void openProfile() {
        ProfileDialog dialog = new ProfileDialog(this, CurrentUser.getUser());
        dialog.setVisible(true);
        // Có thể cần cập nhật welcome label nếu tên thay đổi
        // JLabel welcomeLabel = ... ; welcomeLabel.setText(...);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            CurrentUser.logout();
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginForm loginForm = new LoginForm();
                loginForm.setVisible(true);
            });
        }
    }

    // --- Placeholder Dialog classes (Cần tạo các file .java riêng) ---
    // Ví dụ:
    // class BookFieldDialog extends JDialog { ... }
    // class CancelBookingDialog extends JDialog { ... }
    // class DepositDialog extends JDialog { ... }
    // class BookingHistoryDialog extends JDialog { ... } // Có thể dùng chung với Admin
    // class ProfileDialog extends JDialog { ... } // Có thể dùng chung với Admin

}