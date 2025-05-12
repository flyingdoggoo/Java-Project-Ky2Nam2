package com.quanlysanbong.ui;

import com.quanlysanbong.dao.UserDAO;
import com.quanlysanbong.model.User;
import com.quanlysanbong.util.CurrentUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private UserDAO userDAO;

    public LoginForm() {
        userDAO = new UserDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng nhập - Quản lý sân bóng");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the form
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding

        // Username Label and TextField
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Tên đăng nhập:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Allow text field to expand horizontally
        txtUsername = new JTextField(20);
        add(txtUsername, gbc);

        // Password Label and PasswordField
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE; // Reset fill
        gbc.weightx = 0; // Reset weight
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Mật khẩu:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtPassword = new JPasswordField(20);
        add(txtPassword, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); // Center buttons with spacing
        btnLogin = new JButton("Đăng nhập");
        btnRegister = new JButton("Đăng ký");
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Span two columns
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);


        // --- Action Listeners ---
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginUser();
            }
        });

        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegisterForm();
            }
        });
    }

    private void loginUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập và mật khẩu.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.login(username, password);

        if (user != null) {
            CurrentUser.login(user); // Lưu thông tin người dùng đăng nhập
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Xin chào " + user.getFullName(), "Thành công", JOptionPane.INFORMATION_MESSAGE);

            // Mở Dashboard tương ứng
            if (user.getRole() == User.Role.ADMIN) {
                AdminDashboard adminDashboard = new AdminDashboard();
                adminDashboard.setVisible(true);
            } else {
                UserDashboard userDashboard = new UserDashboard();
                userDashboard.setVisible(true);
            }
            this.dispose(); // Đóng form đăng nhập

        } else {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng.", "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterForm() {
        RegisterForm registerForm = new RegisterForm(this); // Truyền form login để có thể quay lại
        registerForm.setVisible(true);
        this.setVisible(false); // Ẩn form login đi
    }

    // Phương thức để hiện lại form Login khi RegisterForm đóng
    public void showLoginAgain() {
        this.setVisible(true);
        txtUsername.setText(""); // Clear fields
        txtPassword.setText("");
    }

     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> {
             // Set Look and Feel (Optional, for better appearance)
             try {
                  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             } catch (Exception e) {
                 e.printStackTrace();
             }
             new LoginForm().setVisible(true);
         });
     }
}