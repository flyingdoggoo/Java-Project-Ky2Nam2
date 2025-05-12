package com.quanlysanbong.ui;

import com.quanlysanbong.dao.BookingDAO;
import com.quanlysanbong.model.Booking;
import com.quanlysanbong.util.CurrentUser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class CancelBookingDialog extends JDialog {

    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private JButton cancelButton;
    private JButton refreshButton;
    private JButton closeButton;

    private BookingDAO bookingDAO;
    private UserDashboard parentDashboard;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));


    public CancelBookingDialog(UserDashboard owner) {
        super(owner, "Hủy Đặt Sân", true);
        this.parentDashboard = owner;
        bookingDAO = new BookingDAO();
        initComponents();
        loadUserActiveBookings();
        setSize(700, 400);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Table View
        tableModel = new DefaultTableModel(
                new String[]{"ID Lịch Đặt", "Tên Sân", "Thời Gian Đặt", "Tổng Tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp
            }
        };
        bookingTable = new JTable(tableModel);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        add(scrollPane, BorderLayout.CENTER);

        // Info Label
        JLabel infoLabel = new JLabel("Chỉ hiển thị các lịch đặt chưa diễn ra và chưa bị hủy.", SwingConstants.CENTER);
        infoLabel.setForeground(Color.BLUE);
        add(infoLabel, BorderLayout.NORTH);


        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        cancelButton = new JButton("Hủy Lịch Đặt Đã Chọn");
        refreshButton = new JButton("Làm Mới Danh Sách");
        closeButton = new JButton("Đóng");

        buttonPanel.add(cancelButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        cancelButton.addActionListener(e -> cancelSelectedBooking());
        refreshButton.addActionListener(e -> loadUserActiveBookings());
        closeButton.addActionListener(e -> dispose());
    }

    private void loadUserActiveBookings() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        int userId = CurrentUser.getUser().getId();
        List<Booking> bookings = bookingDAO.getBookingsByUserId(userId);

        for (Booking booking : bookings) {
            // Chỉ hiển thị các booking 'Đã đặt' (BOOKED) và thời gian đặt là trong tương lai
            if (booking.getBookingStatus() == Booking.BookingStatus.BOOKED &&
                    booking.getBookingTime().isAfter(LocalDateTime.now()))
            {
                Vector<Object> row = new Vector<>();
                row.add(booking.getId());
                row.add(booking.getFieldName()); // Lấy tên sân từ thông tin join trong DAO
                row.add(booking.getBookingTime().format(DATETIME_FORMATTER));
                row.add(CURRENCY_FORMATTER.format(booking.getTotalPrice()));
                tableModel.addRow(row);
            }
        }
        if (tableModel.getRowCount() == 0) {
            // Optional: Hiển thị thông báo nếu không có lịch nào để hủy
            // JOptionPane.showMessageDialog(this, "Bạn không có lịch đặt nào sắp tới để hủy.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            // Hoặc để trống bảng
        }
    }

    private void cancelSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lịch đặt từ bảng để hủy.", "Chưa chọn lịch đặt", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lấy ID từ cột đầu tiên của dòng được chọn
        int bookingId = (int) tableModel.getValueAt(selectedRow, 0);
        String fieldName = (String) tableModel.getValueAt(selectedRow, 1);
        String bookingTimeStr = (String) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn hủy lịch đặt sân '" + fieldName + "' vào lúc " + bookingTimeStr + "?\nTiền sẽ được hoàn vào tài khoản.",
                "Xác nhận hủy",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = bookingDAO.cancelBooking(bookingId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Hủy lịch đặt thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadUserActiveBookings(); // Tải lại danh sách
                // Cập nhật số dư trên dashboard cha
                if (parentDashboard != null) {
                    parentDashboard.updateBalanceLabel();
                }
            } else {
                // DAO có thể đã hiển thị lỗi cụ thể
                JOptionPane.showMessageDialog(this, "Hủy lịch đặt thất bại. Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                loadUserActiveBookings(); // Tải lại để xem trạng thái mới nhất
            }
        }
    }
}