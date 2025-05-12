package com.quanlysanbong.ui;

import com.quanlysanbong.dao.UserDAO;
import com.quanlysanbong.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat; // Cần cho định dạng tiền tệ (mặc dù không hiển thị)
import java.util.Locale;

public class AddUserDialog extends JDialog {
    private JTextField txtUsername;
    private JPasswordField txtPassword; // Mật khẩu chỉ cần khi thêm mới
    private JTextField txtFullName;
    private JTextField txtPhoneNumber;
    private JComboBox<User.Role> roleComboBox; // Cho phép chọn vai trò
    private JTextField txtInitialBalance; // Số dư ban đầu
    private JButton btnAdd;
    private JButton btnCancel;
    private UserDAO userDAO;

    // Định dạng tiền tệ (dùng để parse input nếu cần)
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getNumberInstance(Locale.US); // Dùng locale US để parse dễ hơn

    public AddUserDialog(Dialog owner) { // Owner là UserManagementDialog
        super(owner, "Thêm Người Dùng Mới", true); // true = modal
        userDAO = new UserDAO();
        initComponents();
        pack(); // Tự động điều chỉnh kích thước dialog dựa trên nội dung
        setLocationRelativeTo(owner); // Hiển thị giữa dialog cha
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Khoảng cách giữa các thành phần
        gbc.anchor = GridBagConstraints.WEST; // Căn lề trái các label
        gbc.fill = GridBagConstraints.HORIZONTAL; // Cho phép các trường text mở rộng theo chiều ngang

        // --- Khởi tạo các trường nhập liệu ---
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtFullName = new JTextField(20);
        txtPhoneNumber = new JTextField(20);
        roleComboBox = new JComboBox<>(User.Role.values()); // Lấy tất cả giá trị từ Enum Role
        txtInitialBalance = new JTextField("0", 15); // Mặc định số dư ban đầu là 0

        // --- Sắp xếp các thành phần trên giao diện bằng GridBagLayout ---
        int gridy = 0; // Biến đếm dòng

        // Dòng Tên đăng nhập
        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Tên đăng nhập (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = gridy++; add(txtUsername, gbc); // Tăng gridy sau mỗi dòng

        // Dòng Mật khẩu
        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Mật khẩu (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = gridy++; add(txtPassword, gbc);

        // Dòng Họ và tên
        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridy++; add(txtFullName, gbc);

        // Dòng Số điện thoại
        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridy++; add(txtPhoneNumber, gbc);

        // Dòng Vai trò
        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Vai trò (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = gridy++; add(roleComboBox, gbc);

        // Dòng Số dư ban đầu
        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Số dư ban đầu (VND):"), gbc);
        gbc.gridx = 1; gbc.gridy = gridy++; add(txtInitialBalance, gbc);

        // --- Panel chứa nút bấm ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Căn giữa, khoảng cách ngang 15, dọc 10
        btnAdd = new JButton("Thêm");
        btnCancel = new JButton("Hủy Bỏ");
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnCancel);

        // Thêm panel nút vào cuối layout
        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 2; // Chiếm 2 cột
        gbc.anchor = GridBagConstraints.CENTER; // Căn giữa panel nút
        gbc.fill = GridBagConstraints.NONE; // Không cho panel nút fill ngang
        add(buttonPanel, gbc);

        // --- Action Listeners ---
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performAddUser(); // Gọi hàm xử lý khi nhấn nút Thêm
            }
        });
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Đóng dialog khi nhấn nút Hủy
            }
        });
    }

    // Hàm xử lý logic khi nhấn nút "Thêm"
    private void performAddUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String fullName = txtFullName.getText().trim();
        String phoneNumber = txtPhoneNumber.getText().trim();
        User.Role role = (User.Role) roleComboBox.getSelectedItem(); // Lấy vai trò được chọn
        String balanceStr = txtInitialBalance.getText().trim();

        // --- Validation đầu vào ---
        if (username.isEmpty() || password.isEmpty() || role == null) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập, mật khẩu và vai trò là bắt buộc.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // (Optional) Thêm các validation khác: độ dài mật khẩu, định dạng username/sđt...

        BigDecimal initialBalance;
        try {
            // Xóa ký tự định dạng tiền tệ trước khi parse
            balanceStr = balanceStr.replace(",", "").replace(".", "");
            initialBalance = new BigDecimal(balanceStr); // Chuyển chuỗi thành số
            if (initialBalance.compareTo(BigDecimal.ZERO) < 0) { // Kiểm tra số âm
                JOptionPane.showMessageDialog(this, "Số dư ban đầu không được là số âm.", "Lỗi số dư", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) { // Bắt lỗi nếu nhập không phải số
            JOptionPane.showMessageDialog(this, "Số dư ban đầu nhập không hợp lệ. Vui lòng chỉ nhập số.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- Tạo đối tượng User mới ---
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // !! LƯU Ý: Mật khẩu chưa được hash!
        newUser.setFullName(fullName);
        newUser.setPhoneNumber(phoneNumber);
        newUser.setRole(role);
        newUser.setBalance(initialBalance); // Set số dư ban đầu

        System.out.println("Attempting to add new user: " + newUser); // Log debug

        // --- Gọi DAO để thêm vào CSDL ---
        boolean success = userDAO.addUser(newUser);

        if (success) { // Nếu DAO trả về true
            JOptionPane.showMessageDialog(this, "Thêm người dùng mới '" + username + "' thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Đóng dialog thêm user
        } else { // Nếu DAO trả về false
            // DAO thường đã in lỗi cụ thể (vd: trùng username)
            JOptionPane.showMessageDialog(this, "Thêm người dùng thất bại. Có thể tên đăng nhập đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            // Không đóng dialog để người dùng sửa lại
        }
    }
}