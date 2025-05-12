package com.quanlysanbong.ui;

import com.quanlysanbong.dao.UserDAO;
import com.quanlysanbong.model.User;
import com.quanlysanbong.util.CurrentUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class ProfileDialog extends JDialog {

    private JTextField usernameField;
    private JTextField fullNameField;
    private JTextField phoneField;
    private JComboBox<User.Role> roleComboBox; // Dùng ComboBox để Admin sửa Role
    private JLabel balanceLabel; // Chỉ hiển thị số dư, không cho sửa trực tiếp
    private JButton saveButton;
    private JButton closeButton;

    private UserDAO userDAO;
    private User userToView; // User đang được xem/sửa
    private boolean isAdminEdit; // Cờ xác định Admin có đang sửa user khác không

    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // Constructor cho User tự xem/sửa
    public ProfileDialog(Window owner, User user) {
        this(owner, user, false); // Gọi constructor đầy đủ với isAdminEdit = false
    }

    // Constructor cho Admin xem/sửa user khác
    public ProfileDialog(Window owner, User user, boolean isAdminEdit) {
        // Xác định tiêu đề dialog
        String dialogTitle = "Thông Tin Người Dùng" + (user != null ? " - " + user.getUsername() : "");
        // Thêm "(Admin Edit)" nếu Admin đang sửa người khác (không phải tự sửa)
        boolean isEditingSelf = CurrentUser.isLoggedIn() && user != null && CurrentUser.getUser().getId() == user.getId();
        if (isAdminEdit && !isEditingSelf) {
            dialogTitle += " (Admin Edit)";
        }
        // Gọi constructor của JDialog với owner, title và modality
        super(owner, dialogTitle, Dialog.ModalityType.APPLICATION_MODAL);

        this.userDAO = new UserDAO();
        this.userToView = user;
        this.isAdminEdit = isAdminEdit; // Lưu lại cờ Admin đang sửa

        initComponents(isEditingSelf); // Khởi tạo giao diện, truyền cờ isEditingSelf
        populateUserData();      // Điền dữ liệu user vào các trường
        configureFieldAccess();  // Bật/tắt các trường dựa trên quyền
        pack();                  // Tự điều chỉnh kích thước
        setLocationRelativeTo(owner); // Hiển thị giữa owner
    }

    // Khởi tạo các thành phần giao diện
    private void initComponents(boolean isEditingSelf) { // Nhận cờ isEditingSelf
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Khởi tạo Components ---
        usernameField = new JTextField(20);
        fullNameField = new JTextField(20);
        phoneField = new JTextField(20);
        roleComboBox = new JComboBox<>(User.Role.values()); // ComboBox cho Role
        balanceLabel = new JLabel("Số dư: "); // JLabel hiển thị số dư
        saveButton = new JButton("Lưu Thay Đổi");
        closeButton = new JButton("Đóng");

        // --- Sắp xếp Layout ---
        int gridy = 0;
        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridy++; add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridy++; add(fullNameField, gbc);

        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridy++; add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = gridy; add(new JLabel("Vai trò:"), gbc);
        gbc.gridx = 1; gbc.gridy = gridy++; add(roleComboBox, gbc); // Thêm ComboBox Role

        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth=2; add(balanceLabel, gbc); // Thêm Label Balance
        gbc.gridwidth=1; // Reset gridwidth
        gridy++;

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        // Chỉ hiển thị nút "Lưu" nếu là Admin sửa người khác HOẶC User tự sửa
        if (isAdminEdit || isEditingSelf) {
            buttonPanel.add(saveButton);
        }
        buttonPanel.add(closeButton);

        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        add(buttonPanel, gbc);

        // --- Action Listeners ---
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProfileChanges(); // Gọi hàm lưu thay đổi
            }
        });
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Đóng dialog
            }
        });
    }

    // Điền dữ liệu của user vào các trường trên form
    private void populateUserData() {
        if (userToView != null) {
            usernameField.setText(userToView.getUsername());
            fullNameField.setText(userToView.getFullName() != null ? userToView.getFullName() : "");
            phoneField.setText(userToView.getPhoneNumber() != null ? userToView.getPhoneNumber() : "");
            roleComboBox.setSelectedItem(userToView.getRole()); // Đặt giá trị cho ComboBox Role
            balanceLabel.setText("Số dư: " + (userToView.getBalance() != null ? CURRENCY_FORMATTER.format(userToView.getBalance()) : "N/A"));
        } else {
            // Xử lý trường hợp user null (ít khi xảy ra nếu gọi từ UserManagementDialog đúng cách)
            usernameField.setText("N/A");
            fullNameField.setText("");
            phoneField.setText("");
            roleComboBox.setSelectedItem(User.Role.USER);
            balanceLabel.setText("Số dư: N/A");
            saveButton.setEnabled(false); // Không cho lưu nếu không có user
        }
    }

    // Cấu hình quyền truy cập (sửa/chỉ đọc) cho các trường
    private void configureFieldAccess() {
        // Username không bao giờ được sửa
        usernameField.setEditable(false);

        // Kiểm tra xem người dùng hiện tại có phải là người đang xem/sửa không
        boolean isEditingSelf = CurrentUser.isLoggedIn() && userToView != null && CurrentUser.getUser().getId() == userToView.getId();

        // Cho phép sửa Họ tên và SĐT nếu là Admin sửa người khác HOẶC User tự sửa
        boolean allowBasicEdit = isAdminEdit || isEditingSelf;
        fullNameField.setEditable(allowBasicEdit);
        phoneField.setEditable(allowBasicEdit);

        // Cho phép sửa Role CHỈ KHI là Admin sửa người khác (không phải tự sửa)
        boolean allowRoleEdit = isAdminEdit && !isEditingSelf;
        roleComboBox.setEnabled(allowRoleEdit);

        // Số dư không được sửa trực tiếp qua form này
    }

    // Xử lý logic khi nhấn nút "Lưu Thay Đổi"
    private void saveProfileChanges() {
        // Kiểm tra lại quyền trước khi lưu
        boolean isEditingSelf = CurrentUser.isLoggedIn() && userToView != null && CurrentUser.getUser().getId() == userToView.getId();
        boolean allowSave = isAdminEdit || isEditingSelf;

        if (!allowSave || userToView == null) {
            System.err.println("Save attempt denied. isAdminEdit=" + isAdminEdit + ", isEditingSelf=" + isEditingSelf);
            return; // Không có quyền lưu
        }

        String newFullName = fullNameField.getText().trim();
        String newPhone = phoneField.getText().trim();
        User.Role newRole = (User.Role) roleComboBox.getSelectedItem(); // Lấy vai trò mới từ ComboBox

        // --- Validation cơ bản ---
        if (newFullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ và tên không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // (Optional) Thêm validation cho định dạng SĐT

        // --- Cập nhật đối tượng userToView ---
        userToView.setFullName(newFullName);
        userToView.setPhoneNumber(newPhone);

        // --- Xử lý cập nhật Role (chỉ khi Admin sửa người khác) ---
        boolean roleUpdatedByAdmin = false;
        if (isAdminEdit && !isEditingSelf && userToView.getRole() != newRole) {
            // Kiểm tra an toàn: không cho đổi admin chính thành User
            if ("admin".equalsIgnoreCase(userToView.getUsername()) && newRole == User.Role.USER) {
                JOptionPane.showMessageDialog(this, "Không thể đổi vai trò của admin chính ('admin') thành User.", "Hành động bị chặn", JOptionPane.WARNING_MESSAGE);
                roleComboBox.setSelectedItem(userToView.getRole()); // Đặt lại giá trị cũ
                return;
            }
            // Cập nhật role trong object
            userToView.setRole(newRole);
            roleUpdatedByAdmin = true; // Đánh dấu là role đã được admin thay đổi
            System.out.println("Admin changed role for User ID: " + userToView.getId() + " to " + newRole); // Log debug
        } else if (isAdminEdit && !isEditingSelf) {
            // Nếu Admin sửa nhưng không đổi role, vẫn set role cũ vào object để đảm bảo đúng khi gọi DAO
            userToView.setRole(newRole); // newRole lúc này bằng role cũ
        }
        // User tự sửa thì không được đổi role, nên không cần làm gì với role ở đây

        // --- Gọi DAO để lưu vào CSDL ---
        boolean success;
        System.out.println("Saving profile changes for User ID: " + userToView.getId()); // Log debug
        if (isAdminEdit) {
            // Admin có thể cập nhật tên, sđt, và role (nếu được phép)
            System.out.println("Calling updateUserByAdmin..."); // Log debug
            success = userDAO.updateUserByAdmin(userToView);
        } else { // User tự sửa
            // User chỉ cập nhật tên, sđt
            System.out.println("Calling updateUserInfo..."); // Log debug
            success = userDAO.updateUserInfo(userToView);
        }

        // --- Xử lý kết quả ---
        if (success) {
            // Cập nhật thông tin trong CurrentUser nếu user tự sửa
            if (isEditingSelf) {
                System.out.println("Updating CurrentUser..."); // Log debug
                CurrentUser.updateUser(userToView);
                // TODO: Cập nhật welcome label trên UserDashboard nếu cần
            }
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Đóng dialog
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            // Không đóng dialog để người dùng xem lại
        }
    }
}