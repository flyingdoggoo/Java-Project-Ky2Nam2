package com.quanlysanbong.ui;

import com.quanlysanbong.dao.UserDAO;
import com.quanlysanbong.model.User;
import com.quanlysanbong.util.CurrentUser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class UserManagementDialog extends JDialog {

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton closeButton;
    private JButton btnAdd, btnEdit, btnDelete, btnDepositWithdraw, btnRefresh, btnClose;
    // Có thể thêm các nút Add, Edit, Delete nếu muốn mở rộng chức năng

    private UserDAO userDAO;
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));


    public UserManagementDialog(Frame owner) {
        super(owner, "Quản Lý Người Dùng", Dialog.ModalityType.APPLICATION_MODAL);
        this.userDAO = new UserDAO();
        initComponents();
        loadUserData();
        setSize(850, 450);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        // Thêm padding cho toàn bộ dialog
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table View (Hiển thị danh sách User)
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Tên Đăng Nhập", "Họ Tên", "Số Điện Thoại", "Số Dư", "Vai Trò"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho phép chọn một dòng
        userTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Tắt tự động resize để setColumnWidths hoạt động tốt
        userTable.setRowHeight(25); // Tăng chiều cao dòng
        setColumnWidths(); // Đặt chiều rộng cho từng cột

        JScrollPane scrollPane = new JScrollPane(userTable); // Đặt bảng vào thanh cuộn
        add(scrollPane, BorderLayout.CENTER); // Thêm bảng (trong thanh cuộn) vào giữa dialog

        // Button Panel (Chứa các nút chức năng)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Căn giữa các nút, khoảng cách 10px

        // Khởi tạo các nút bấm
        btnAdd = new JButton("Thêm User");
        btnEdit = new JButton("Sửa User");
        btnDelete = new JButton("Xóa User");
        btnDepositWithdraw = new JButton("Nạp/Rút Tiền");
        btnRefresh = new JButton("Làm Mới");
        btnClose = new JButton("Đóng");

        // Đặt kích thước ưa thích cho các nút (tùy chọn, để đồng đều)
        Dimension buttonSize = new Dimension(120, 30);
        btnAdd.setPreferredSize(buttonSize);
        btnEdit.setPreferredSize(buttonSize);
        btnDelete.setPreferredSize(buttonSize);
        btnDepositWithdraw.setPreferredSize(buttonSize);
        btnRefresh.setPreferredSize(buttonSize);
        btnClose.setPreferredSize(buttonSize);

        // Thêm các nút vào panel
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDepositWithdraw);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);

        // Thêm panel nút vào phía dưới dialog
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners (Xử lý sự kiện khi nhấn nút) ---
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUser(); // Gọi phương thức xử lý thêm user
            }
        });

        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editUser(); // Gọi phương thức xử lý sửa user
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUser(); // Gọi phương thức xử lý xóa user
            }
        });

        btnDepositWithdraw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                depositWithdrawMoney(); // Gọi phương thức xử lý nạp/rút tiền
            }
        });

        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadUserData(); // Gọi phương thức tải lại dữ liệu bảng
            }
        });

        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Đóng dialog hiện tại
            }
        });
    }

    private void setColumnWidths() {
        TableColumnModel columnModel = userTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);  // ID
        columnModel.getColumn(1).setPreferredWidth(120); // Username
        columnModel.getColumn(2).setPreferredWidth(180); // Ho Ten
        columnModel.getColumn(3).setPreferredWidth(120); // SDT
        columnModel.getColumn(4).setPreferredWidth(120); // So Du
        columnModel.getColumn(5).setPreferredWidth(80);  // Vai Tro
    }


    private void loadUserData() {
        tableModel.setRowCount(0); // Xóa hết các dòng cũ
        List<User> users = userDAO.getAllUsers(); // Lấy danh sách user từ DAO

        if (users == null) {
            System.err.println("Lỗi: Không thể tải danh sách người dùng từ DAO.");
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu người dùng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Duyệt qua danh sách user và thêm vào bảng
        for (User user : users) {
            Vector<Object> row = new Vector<>();
            row.add(user.getId());
            row.add(user.getUsername());
            row.add(user.getFullName() != null ? user.getFullName() : ""); // Xử lý null
            row.add(user.getPhoneNumber() != null ? user.getPhoneNumber() : ""); // Xử lý null
            row.add(user.getBalance() != null ? CURRENCY_FORMATTER.format(user.getBalance()) : "N/A"); // Định dạng tiền tệ, xử lý null
            row.add(user.getRole() != null ? user.getRole().name() : "N/A"); // Lấy tên Enum, xử lý null
            tableModel.addRow(row); // Thêm dòng vào model của bảng
        }
        System.out.println("User data loaded into table."); // Log debug
    }
    private void addUser() {
        System.out.println("Add User button clicked."); // Log debug
        // Tạo dialog AddUserDialog, truyền dialog hiện tại (this) làm owner
        AddUserDialog addUserDialog = new AddUserDialog(this);
        addUserDialog.setVisible(true); // Hiển thị dialog thêm user (dạng modal)

        // Sau khi dialog thêm đóng, tải lại dữ liệu để cập nhật bảng
        System.out.println("AddUserDialog closed, reloading user data..."); // Log debug
        loadUserData();
    }

    // Mở dialog để sửa thông tin người dùng đã chọn
    private void editUser() {
        System.out.println("Edit User button clicked."); // Log debug
        int selectedRow = userTable.getSelectedRow(); // Lấy chỉ số của dòng đang được chọn
        if (selectedRow == -1) { // Nếu không có dòng nào được chọn
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng để sửa.", "Chưa chọn người dùng", JOptionPane.WARNING_MESSAGE);
            return; // Dừng thực hiện
        }

        // Lấy ID người dùng từ cột đầu tiên (cột 0) của dòng được chọn
        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        System.out.println("Editing User ID: " + userId); // Log debug

        // Lấy thông tin đầy đủ của user từ DAO dựa trên ID
        User userToEdit = userDAO.getUserById(userId);

        if (userToEdit == null) { // Nếu không tìm thấy user trong CSDL
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin người dùng đã chọn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            loadUserData(); // Tải lại bảng phòng trường hợp dữ liệu không nhất quán
            return;
        }

        // Tạo dialog ProfileDialog, truyền dialog hiện tại (this), user cần sửa, và cờ true (isAdminEdit)
        ProfileDialog profileDialog = new ProfileDialog(this, userToEdit, true); // true = chế độ Admin sửa
        profileDialog.setVisible(true); // Hiển thị dialog sửa (dạng modal)

        // Sau khi dialog sửa đóng, tải lại dữ liệu để cập nhật bảng
        System.out.println("ProfileDialog (Edit) closed, reloading user data..."); // Log debug
        loadUserData();
    }

    // Xóa người dùng đã chọn
    private void deleteUser() {
        System.out.println("Delete User button clicked."); // Log debug
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng để xóa.", "Chưa chọn người dùng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1); // Lấy username để hiển thị xác nhận
        User.Role role = User.Role.valueOf((String) tableModel.getValueAt(selectedRow, 5)); // Lấy vai trò để kiểm tra

        System.out.println("Attempting to delete User ID: " + userId + ", Username: " + username); // Log debug

        // --- Các kiểm tra an toàn ---
        // 1. Không cho xóa tài khoản admin chính (quy ước username là 'admin')
        if ("admin".equalsIgnoreCase(username)) {
            JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản quản trị viên chính ('admin').", "Hành động bị chặn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 2. Không cho Admin tự xóa tài khoản của mình
        if (CurrentUser.isLoggedIn() && CurrentUser.getUser().getId() == userId) {
            JOptionPane.showMessageDialog(this, "Bạn không thể tự xóa tài khoản của mình.", "Hành động bị chặn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- Hiển thị hộp thoại xác nhận ---
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa người dùng '" + username + "' (ID: " + userId + ")?\n" +
                        "Hành động này KHÔNG THỂ hoàn tác và có thể thất bại nếu người dùng có lịch sử đặt sân.",
                "Xác nhận xóa người dùng",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        // Nếu người dùng chọn "Yes"
        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("User confirmed deletion for User ID: " + userId); // Log debug
            // Gọi phương thức xóa trong DAO
            boolean success = userDAO.deleteUser(userId);

            if (success) { // Nếu DAO trả về true (xóa thành công)
                JOptionPane.showMessageDialog(this, "Xóa người dùng '" + username + "' thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadUserData(); // Tải lại bảng để cập nhật
            } else { // Nếu DAO trả về false (xóa thất bại)
                // DAO thường đã in lỗi cụ thể ra console
                JOptionPane.showMessageDialog(this, "Xóa người dùng '" + username + "' thất bại.\nNguyên nhân có thể do người dùng đã có lịch sử đặt sân hoặc lỗi kết nối.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("User cancelled deletion for User ID: " + userId); // Log debug
        }
    }

    // Nạp hoặc rút tiền cho người dùng đã chọn
    private void depositWithdrawMoney() {
        System.out.println("Deposit/Withdraw button clicked."); // Log debug
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng để nạp/rút tiền.", "Chưa chọn người dùng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        System.out.println("Modifying balance for User ID: " + userId); // Log debug

        // Lấy thông tin user để biết số dư hiện tại
        User userToModify = userDAO.getUserById(userId);
        if (userToModify == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin người dùng đã chọn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            loadUserData();
            return;
        }

        // Hiển thị InputDialog để Admin nhập số tiền
        String amountStr = JOptionPane.showInputDialog(this,
                "Nhập số tiền cần NẠP (số dương) hoặc RÚT (số âm) cho user '" + userToModify.getUsername() + "':\n" +
                        "(Ví dụ: 50000 để nạp, -20000 để rút)\n\n" +
                        "Số dư hiện tại: " + CURRENCY_FORMATTER.format(userToModify.getBalance()),
                "Nạp/Rút Tiền",
                JOptionPane.PLAIN_MESSAGE); // Kiểu dialog nhập liệu đơn giản

        // Nếu người dùng nhấn Cancel hoặc không nhập gì
        if (amountStr == null || amountStr.trim().isEmpty()) {
            System.out.println("Deposit/Withdraw cancelled or empty input."); // Log debug
            return;
        }

        try {
            // Xóa các ký tự định dạng tiền tệ (dấu phẩy, dấu chấm) trước khi parse
            amountStr = amountStr.replace(",", "").replace(".", "").trim();
            BigDecimal amount = new BigDecimal(amountStr); // Chuyển chuỗi thành số BigDecimal

            BigDecimal currentBalance = userToModify.getBalance();
            BigDecimal newBalance = currentBalance.add(amount); // Tính số dư mới (cộng số tiền, có thể âm)

            System.out.println("Amount entered: " + amount + ", Current Balance: " + currentBalance + ", New Balance: " + newBalance); // Log debug

            // Kiểm tra logic: Không cho phép số dư mới bị âm (khi rút tiền)
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Số dư không đủ để thực hiện rút số tiền này.\nSố dư mới sẽ là: " + CURRENCY_FORMATTER.format(newBalance), "Lỗi số dư không đủ", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Xác nhận lại hành động với Admin
            String action = amount.compareTo(BigDecimal.ZERO) >= 0 ? "NẠP" : "RÚT"; // Xác định là Nạp hay Rút
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Xác nhận %s số tiền %s cho người dùng '%s'?\nSố dư mới sẽ là: %s",
                            action,
                            CURRENCY_FORMATTER.format(amount.abs()), // Hiển thị giá trị tuyệt đối của số tiền
                            userToModify.getUsername(),
                            CURRENCY_FORMATTER.format(newBalance)),
                    "Xác nhận " + action + " tiền",
                    JOptionPane.YES_NO_OPTION);

            // Nếu Admin chọn "Yes"
            if (confirm == JOptionPane.YES_OPTION) {
                System.out.println("User confirmed " + action + " for User ID: " + userId); // Log debug
                // Gọi DAO để cập nhật số dư trong CSDL
                boolean success = userDAO.updateUserBalance(userId, newBalance);

                if (success) { // Nếu cập nhật thành công
                    JOptionPane.showMessageDialog(this, action + " tiền thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadUserData(); // Tải lại bảng để hiển thị số dư mới
                } else { // Nếu cập nhật thất bại
                    JOptionPane.showMessageDialog(this, action + " tiền thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.out.println("User cancelled " + action + " for User ID: " + userId); // Log debug
            }

        } catch (NumberFormatException ex) { // Bắt lỗi nếu nhập không phải là số
            JOptionPane.showMessageDialog(this, "Số tiền nhập không hợp lệ. Vui lòng chỉ nhập số (có thể có dấu âm).", "Lỗi định dạng số", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { // Bắt các lỗi khác có thể xảy ra
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi không mong muốn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // In chi tiết lỗi ra console
        }
    }
    // --- Các phương thức Add/Edit/Delete nếu cần ---
    // private void addUser() { /* Mở dialog khác để nhập thông tin user mới */ }
    // private void editUser() { /* Lấy user đã chọn, mở dialog ProfileDialog để sửa */ }
    // private void deleteUser() { /* Lấy user đã chọn, xác nhận và gọi userDAO.deleteUser() */ }

}