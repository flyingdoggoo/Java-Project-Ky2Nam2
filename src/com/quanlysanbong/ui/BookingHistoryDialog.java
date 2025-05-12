package com.quanlysanbong.ui;

import com.quanlysanbong.dao.BookingDAO;
import com.quanlysanbong.model.Booking;
import com.quanlysanbong.model.User; // Cần User
import com.quanlysanbong.util.CurrentUser; // Cần CurrentUser

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class BookingHistoryDialog extends JDialog {

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton closeButton;

    private BookingDAO bookingDAO;
    private User targetUser; // null nếu là admin xem tất cả, != null nếu là user xem của mình
    private boolean isAdminView;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));


    // Constructor cho User xem lịch sử của chính mình
    public BookingHistoryDialog(Window owner, User user) {
        this(owner, user, false);
    }

    // Constructor cho Admin xem tất cả
    public BookingHistoryDialog(Window owner) {
        this(owner, null, true);
    }


    // Constructor đầy đủ
    private BookingHistoryDialog(Window owner, User user, boolean isAdmin) {
        // Xác định parent là JDialog hay JFrame để gọi super đúng cách
        String dialogTitle = "Lịch Sử Đặt Sân" + (isAdmin ? " (Tất cả)" : (user != null ? " (" + user.getUsername() + ")" : ""));
        super(owner, dialogTitle, Dialog.ModalityType.APPLICATION_MODAL);

        this.targetUser = user;
        this.isAdminView = isAdmin;
        this.bookingDAO = new BookingDAO();
        initComponents();
        loadBookingHistory();
        setSize(800, 500);
        setLocationRelativeTo(owner);
    }


    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Table Columns - Khác nhau giữa Admin và User view
        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID Đặt");
        if (isAdminView) {
            columnNames.add("Người Đặt"); // Chỉ Admin thấy cột này
        }
        columnNames.add("Tên Sân");
        columnNames.add("Thời Gian Đặt");
        columnNames.add("Tổng Tiền");
        columnNames.add("Trạng Thái");
        columnNames.add("Thời Gian Tạo");


        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Cho phép scroll ngang nếu cần
        setColumnWidths();


        JScrollPane scrollPane = new JScrollPane(historyTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        refreshButton = new JButton("Làm Mới");
        closeButton = new JButton("Đóng");

        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        refreshButton.addActionListener(e -> loadBookingHistory());
        closeButton.addActionListener(e -> dispose());
    }

    private void setColumnWidths() {
        TableColumnModel columnModel = historyTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60); // ID Đặt
        int colIndex = 1;
        if (isAdminView) {
            columnModel.getColumn(colIndex++).setPreferredWidth(100); // Người đặt
        }
        columnModel.getColumn(colIndex++).setPreferredWidth(150); // Tên Sân
        columnModel.getColumn(colIndex++).setPreferredWidth(130); // Thời gian đặt
        columnModel.getColumn(colIndex++).setPreferredWidth(100); // Tổng tiền
        columnModel.getColumn(colIndex++).setPreferredWidth(80);  // Trạng thái
        columnModel.getColumn(colIndex++).setPreferredWidth(130); // Thời gian tạo
    }


    private void loadBookingHistory() {
        tableModel.setRowCount(0);
        List<Booking> bookings = new ArrayList<>();

        if (isAdminView) {
            bookings = bookingDAO.getAllBookings();
        } else if (targetUser != null) {
            bookings = bookingDAO.getBookingsByUserId(targetUser.getId());
        } else {
            // Trường hợp không mong muốn, không có user và không phải admin
            System.err.println("Lỗi: Không xác định được người dùng để xem lịch sử.");
            return;
        }


        for (Booking booking : bookings) {
            Vector<Object> row = new Vector<>();
            row.add(booking.getId());
            if (isAdminView) {
                row.add(booking.getUserName()); // Lấy username từ DAO
            }
            row.add(booking.getFieldName()); // Lấy field name từ DAO
            row.add(booking.getBookingTime() != null ? booking.getBookingTime().format(DATETIME_FORMATTER) : "N/A");
            row.add(CURRENCY_FORMATTER.format(booking.getTotalPrice()));
            // Sử dụng toString() hoặc getDisplayName() của enum BookingStatus
            row.add(booking.getBookingStatus() != null ? booking.getBookingStatus().getDisplayName() : "N/A");
            row.add(booking.getCreatedAt() != null ? booking.getCreatedAt().format(DATETIME_FORMATTER) : "N/A");
            tableModel.addRow(row);
        }
    }
}