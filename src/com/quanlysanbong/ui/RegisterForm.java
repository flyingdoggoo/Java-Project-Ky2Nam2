package com.quanlysanbong.ui;

import com.quanlysanbong.dao.UserDAO;
import com.quanlysanbong.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RegisterForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtFullName;
    private JTextField txtPhoneNumber;
    private JButton btnRegister;
    private JButton btnCancel;
    private UserDAO userDAO;
    private LoginForm loginForm; // Tham chiếu đến form đăng nhập

    public RegisterForm(LoginForm loginForm) {
        this.loginForm = loginForm;
        userDAO = new UserDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng ký tài khoản");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Handle close manually
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // --- Fields ---
        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Tên đăng nhập (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; txtUsername = new JTextField(20); add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; add(new JLabel("Mật khẩu (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; txtPassword = new JPasswordField(20); add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; add(new JLabel("Xác nhận mật khẩu (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; txtConfirmPassword = new JPasswordField(20); add(txtConfirmPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; txtFullName = new JTextField(20); add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; txtPhoneNumber = new JTextField(20); add(txtPhoneNumber, gbc);

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnRegister = new JButton("Đăng ký");
        btnCancel = new JButton("Hủy bỏ");
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnCancel);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        add(buttonPanel, gbc);


        // --- Action Listeners ---
        btnRegister.addActionListener(e -> registerUser());

        btnCancel.addActionListener(e -> closeForm());

        // Handle window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeForm();
            }
        });
    }

    private void registerUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        String fullName = txtFullName.getText().trim();
        String phoneNumber = txtPhoneNumber.getText().trim();

        // Basic Validation
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập, mật khẩu và xác nhận mật khẩu không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu và xác nhận mật khẩu không khớp.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Thêm các validation khác nếu cần (độ dài pass, định dạng sđt,...)

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // !! KHÔNG AN TOÀN
        newUser.setFullName(fullName);
        newUser.setPhoneNumber(phoneNumber);
        // Role và Balance sẽ được set mặc định trong DAO

        boolean success = userDAO.register(newUser);

        if (success) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            closeForm(); // Đóng form đăng ký và quay lại đăng nhập
        } else {
            // DAO đã in lỗi cụ thể (vd: trùng username), ở đây chỉ báo lỗi chung
            JOptionPane.showMessageDialog(this, "Đăng ký thất bại. Có thể tên đăng nhập đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void closeForm() {
        this.dispose(); // Đóng cửa sổ hiện tại
        if (loginForm != null) {
            loginForm.showLoginAgain(); // Hiển thị lại cửa sổ đăng nhập
        }
    }
}