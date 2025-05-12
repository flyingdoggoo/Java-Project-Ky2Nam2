package com.quanlysanbong.ui;

import com.quanlysanbong.dao.UserDAO;
import com.quanlysanbong.model.User;
import com.quanlysanbong.util.CurrentUser;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class DepositDialog extends JDialog {

    private JLabel currentBalanceLabel;
    private JTextField amountField;
    private JButton depositButton;
    private JButton cancelButton;

    private UserDAO userDAO;
    private UserDashboard parentDashboard;
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));


    public DepositDialog(UserDashboard owner) {
        super(owner, "Nạp Tiền Vào Tài Khoản", true);
        this.parentDashboard = owner;
        this.userDAO = new UserDAO();
        initComponents();
        displayCurrentBalance();
        setSize(400, 200);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Components
        currentBalanceLabel = new JLabel("Số dư hiện tại: ");
        currentBalanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        amountField = new JTextField(15);
        depositButton = new JButton("Nạp Tiền");
        cancelButton = new JButton("Hủy Bỏ");

        // Layout
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; add(currentBalanceLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; add(new JLabel("Số tiền cần nạp (VND):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; add(amountField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(depositButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        add(buttonPanel, gbc);

        // Action Listeners
        depositButton.addActionListener(e -> performDeposit());
        cancelButton.addActionListener(e -> dispose());
    }

    private void displayCurrentBalance() {
        User currentUser = CurrentUser.getUser();
        if (currentUser != null) {
            currentBalanceLabel.setText("Số dư hiện tại: " + CURRENCY_FORMATTER.format(currentUser.getBalance()));
        }
    }

    private void performDeposit() {
        String amountStr = amountField.getText().trim();
        if (amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền cần nạp.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Loại bỏ dấu phẩy hoặc dấu chấm ngăn cách hàng nghìn nếu có
            amountStr = amountStr.replace(",", "").replace(".", "");
            BigDecimal amount = new BigDecimal(amountStr);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Số tiền nạp phải là một số dương.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User currentUser = CurrentUser.getUser();
            BigDecimal currentBalance = currentUser.getBalance();
            BigDecimal newBalance = currentBalance.add(amount);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn nạp " + CURRENCY_FORMATTER.format(amount) + " vào tài khoản?\nSố dư mới sẽ là: " + CURRENCY_FORMATTER.format(newBalance),
                    "Xác nhận nạp tiền",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = userDAO.updateUserBalance(currentUser.getId(), newBalance);
                if (success) {
                    // Cập nhật lại thông tin trong CurrentUser
                    currentUser.setBalance(newBalance);
                    CurrentUser.updateUser(currentUser);

                    JOptionPane.showMessageDialog(this, "Nạp tiền thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);

                    // Cập nhật label trên dashboard cha
                    if (parentDashboard != null) {
                        parentDashboard.updateBalanceLabel();
                    }
                    dispose(); // Đóng dialog
                } else {
                    JOptionPane.showMessageDialog(this, "Nạp tiền thất bại. Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số tiền nhập không hợp lệ. Vui lòng chỉ nhập số.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}