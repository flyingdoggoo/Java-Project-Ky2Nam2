package com.quanlysanbong.ui;

import com.quanlysanbong.dao.FieldDAO;
import com.quanlysanbong.model.Field;

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

public class FieldManagementDialog extends JDialog {
    private JTable fieldTable;
    private DefaultTableModel tableModel;
    private FieldDAO fieldDAO;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnClose;

    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public FieldManagementDialog(Frame owner) {
        super(owner, "Quản lý Sân", true); // true = modal dialog
        fieldDAO = new FieldDAO();
        initComponents();
        loadFieldData();
        setSize(650, 400); // Tăng chiều rộng một chút
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        // Thêm padding cho dialog
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table View
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Tên Sân", "Giá/Giờ", "Trạng Thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên bảng
            }
        };
        fieldTable = new JTable(tableModel);
        fieldTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho chọn 1 dòng
        fieldTable.setFillsViewportHeight(true); // Cho bảng tô màu nền nếu không đủ dòng
        fieldTable.setRowHeight(25); // Tăng chiều cao dòng cho dễ nhìn
        setColumnWidths(); // Đặt chiều rộng cột

        JScrollPane scrollPane = new JScrollPane(fieldTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAdd = new JButton("Thêm");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xóa");
        btnRefresh = new JButton("Làm mới");
        btnClose = new JButton("Đóng");

        // Thiết lập kích thước ưa thích cho các nút (tùy chọn)
        Dimension buttonSize = new Dimension(100, 30);
        btnAdd.setPreferredSize(buttonSize);
        btnEdit.setPreferredSize(buttonSize);
        btnDelete.setPreferredSize(buttonSize);
        btnRefresh.setPreferredSize(buttonSize);
        btnClose.setPreferredSize(buttonSize);


        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        btnAdd.addActionListener(e -> showAddEditDialog(null)); // null = Add mode
        btnEdit.addActionListener(e -> {
            int selectedRow = fieldTable.getSelectedRow();
            if (selectedRow != -1) {
                int fieldId = (int) tableModel.getValueAt(selectedRow, 0);
                Field fieldToEdit = fieldDAO.getFieldById(fieldId);
                if (fieldToEdit != null) {
                    showAddEditDialog(fieldToEdit); // fieldToEdit != null = Edit mode
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin sân để sửa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    loadFieldData(); // Tải lại nếu dữ liệu không nhất quán
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một sân để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnDelete.addActionListener(e -> deleteField());
        btnRefresh.addActionListener(e -> loadFieldData());
        btnClose.addActionListener(e -> dispose()); // Đóng dialog

    }

    private void setColumnWidths() {
        TableColumnModel columnModel = fieldTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);  // ID
        columnModel.getColumn(1).setPreferredWidth(250); // Tên Sân
        columnModel.getColumn(2).setPreferredWidth(150); // Giá/Giờ
        columnModel.getColumn(3).setPreferredWidth(150); // Trạng Thái
    }


    private void loadFieldData() {
        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);
        List<Field> fields = fieldDAO.getAllFields();
        if (fields == null) {
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu sân.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (Field field : fields) {
            Vector<Object> row = new Vector<>();
            row.add(field.getId());
            row.add(field.getName());
            row.add(CURRENCY_FORMATTER.format(field.getPricePerHour())); // Định dạng tiền tệ
            row.add(field.getStatus().getDisplayName()); // Hiển thị tên tiếng Việt
            tableModel.addRow(row);
        }
    }

    // Dialog chung cho Thêm và Sửa
    private void showAddEditDialog(Field existingField) {
        boolean isEditMode = (existingField != null);
        String dialogTitle = isEditMode ? "Sửa Thông Tin Sân" : "Thêm Sân Mới";

        JDialog addEditDialog = new JDialog(this, dialogTitle, true);
        addEditDialog.setLayout(new GridBagLayout());
        addEditDialog.setSize(400, 300); // Kích thước dialog con
        addEditDialog.setLocationRelativeTo(this);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Tăng padding
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Cho phép trường text mở rộng

        // Components
        JTextField nameField = new JTextField(20);
        JTextField priceField = new JTextField(15);
        // Sử dụng enum Field.Status trực tiếp, JComboBox sẽ gọi toString() để hiển thị displayName
        JComboBox<Field.Status> statusComboBox = new JComboBox<>(Field.Status.values());

        // Pre-populate if in Edit mode
        if (isEditMode) {
            nameField.setText(existingField.getName());
            priceField.setText(existingField.getPricePerHour().toPlainString()); // Dùng toPlainString để tránh ký hiệu khoa học
            statusComboBox.setSelectedItem(existingField.getStatus());
        }

        // Layout for sub-dialog
        gbc.gridx=0; gbc.gridy=0; addEditDialog.add(new JLabel("Tên Sân (*):"), gbc);
        gbc.gridx=1; gbc.gridy=0; addEditDialog.add(nameField, gbc);

        gbc.gridx=0; gbc.gridy=1; addEditDialog.add(new JLabel("Giá/Giờ (VND) (*):"), gbc);
        gbc.gridx=1; gbc.gridy=1; addEditDialog.add(priceField, gbc);

        gbc.gridx=0; gbc.gridy=2; addEditDialog.add(new JLabel("Trạng Thái (*):"), gbc);
        gbc.gridx=1; gbc.gridy=2; addEditDialog.add(statusComboBox, gbc);

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Căn phải nút
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2; gbc.anchor = GridBagConstraints.EAST; // Căn phải panel nút
        gbc.fill = GridBagConstraints.NONE; // Không fill panel nút
        addEditDialog.add(btnPanel, gbc);

        // Action Listener for Save button in sub-dialog
        btnSave.addActionListener(ev -> {
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim().replace(",", ""); // Bỏ dấu phẩy nếu có
            Field.Status status = (Field.Status) statusComboBox.getSelectedItem();

            if (name.isEmpty() || priceStr.isEmpty() || status == null) {
                JOptionPane.showMessageDialog(addEditDialog, "Vui lòng nhập đủ thông tin bắt buộc (*).", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                BigDecimal price = new BigDecimal(priceStr);
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(addEditDialog, "Giá tiền không được là số âm.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean success;
                if (isEditMode) {
                    // Update existing field object
                    existingField.setName(name);
                    existingField.setPricePerHour(price);
                    existingField.setStatus(status);
                    success = fieldDAO.updateField(existingField);
                } else {
                    // Create new field object
                    Field newField = new Field();
                    newField.setName(name);
                    newField.setPricePerHour(price);
                    newField.setStatus(status);
                    success = fieldDAO.addField(newField);
                }

                if (success) {
                    JOptionPane.showMessageDialog(addEditDialog, (isEditMode ? "Cập nhật" : "Thêm") + " sân thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadFieldData(); // Tải lại bảng chính
                    addEditDialog.dispose(); // Đóng dialog con
                } else {
                    JOptionPane.showMessageDialog(addEditDialog, (isEditMode ? "Cập nhật" : "Thêm") + " sân thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addEditDialog, "Giá tiền phải là một số hợp lệ.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(addEditDialog, "Đã xảy ra lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // Action Listener for Cancel button in sub-dialog
        btnCancel.addActionListener(ev -> addEditDialog.dispose());

        addEditDialog.setVisible(true); // Hiển thị dialog con
    }


    private void deleteField() {
        int selectedRow = fieldTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sân để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int fieldId = (int) tableModel.getValueAt(selectedRow, 0);
        // Lấy tên sân từ model thay vì từ bảng (để đảm bảo dữ liệu gốc)
        Field fieldToDelete = fieldDAO.getFieldById(fieldId);
        String fieldName = (fieldToDelete != null) ? fieldToDelete.getName() : "ID " + fieldId; // Lấy tên nếu tìm được

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa sân '" + fieldName + "'?\n" +
                        "Lưu ý: Hành động này sẽ thất bại nếu sân đã có lịch sử đặt.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = fieldDAO.deleteField(fieldId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa sân thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadFieldData(); // Tải lại bảng
            } else {
                // DAO đã in lỗi cụ thể (vd: do khóa ngoại)
                JOptionPane.showMessageDialog(this,
                        "Xóa sân thất bại.\nNguyên nhân có thể do sân này đã có lịch sử đặt hoặc lỗi kết nối.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}