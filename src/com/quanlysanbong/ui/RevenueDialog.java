package com.quanlysanbong.ui;

import com.quanlysanbong.dao.BookingDAO;
import com.quanlysanbong.model.Booking;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class RevenueDialog extends JDialog {

    private JLabel totalRevenueLabel;
    private JTable bookedHistoryTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton closeButton;

    private BookingDAO bookingDAO;
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public RevenueDialog(Frame owner) {
        // Dùng Frame làm owner và ModalityType
        super(owner, "Thống Kê Doanh Thu", Dialog.ModalityType.APPLICATION_MODAL);
        this.bookingDAO = new BookingDAO();
        initComponents();
        loadRevenueData();
        setSize(900, 500); // Tăng chiều rộng
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel (không đổi)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        totalRevenueLabel = new JLabel("Tổng Doanh Thu (từ các lịch đã đặt): LOADING...");
        totalRevenueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalRevenueLabel.setForeground(Color.RED.darker());
        topPanel.add(totalRevenueLabel);
        add(topPanel, BorderLayout.NORTH);


        // Table View (*** THÊM CỘT "Thời Lượng" ***)
        tableModel = new DefaultTableModel(
                new String[]{"ID Đặt", "Người Đặt", "Tên Sân", "Thời Gian Đặt", "Thời Lượng (giờ)", "Tổng Tiền", "T.Gian Tạo"}, 0) { // Thêm cột
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        bookedHistoryTable = new JTable(tableModel);
        bookedHistoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setColumnWidths(); // Gọi sau khi tạo JTable

        JScrollPane scrollPane = new JScrollPane(bookedHistoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel (không đổi)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        refreshButton = new JButton("Làm Mới");
        closeButton = new JButton("Đóng");
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners (không đổi)
        refreshButton.addActionListener(e -> loadRevenueData());
        closeButton.addActionListener(e -> dispose());
    }

    private void setColumnWidths() {
        // *** ĐIỀU CHỈNH CHIỀU RỘNG CỘT ***
        TableColumnModel columnModel = bookedHistoryTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(60);  // ID Đặt
        columnModel.getColumn(1).setPreferredWidth(120); // Người Đặt
        columnModel.getColumn(2).setPreferredWidth(150); // Tên Sân
        columnModel.getColumn(3).setPreferredWidth(130); // Thời gian Đặt
        columnModel.getColumn(4).setPreferredWidth(100); // *** Thời Lượng ***
        columnModel.getColumn(5).setPreferredWidth(110); // Tổng Tiền
        columnModel.getColumn(6).setPreferredWidth(130); // Thời gian Tạo
    }

    private void loadRevenueData() {
        // 1. Load Total Revenue (không đổi)
        BigDecimal totalRevenue = bookingDAO.getTotalRevenue();
        totalRevenueLabel.setText("Tổng Doanh Thu (từ các lịch 'Đã đặt'): " + CURRENCY_FORMATTER.format(totalRevenue));

        // 2. Load Details of 'Booked' bookings into table
        tableModel.setRowCount(0); // Clear old data
        List<Booking> allBookings = bookingDAO.getAllBookings(); // Giả sử hàm này đã lấy duration_hours

        // *** KIỂM TRA NULL CHO allBookings ***
        if (allBookings == null) {
            System.err.println("Lỗi: Không thể tải danh sách booking từ DAO.");
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu doanh thu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }


        for (Booking booking : allBookings) {
            // Chỉ hiển thị những booking có trạng thái là BOOKED (Đã đặt)
            if (booking != null && booking.getBookingStatus() == Booking.BookingStatus.BOOKED) {
                Vector<Object> row = new Vector<>();
                row.add(booking.getId());
                row.add(booking.getUserName() != null ? booking.getUserName() : "N/A"); // Kiểm tra null
                row.add(booking.getFieldName() != null ? booking.getFieldName() : "N/A"); // Kiểm tra null
                row.add(booking.getBookingTime() != null ? booking.getBookingTime().format(DATETIME_FORMATTER) : "N/A");

                // *** LẤY durationHours TRỰC TIẾP TỪ booking object ***
                // Đảm bảo getDurationHours() trả về giá trị đúng từ DB
                row.add(booking.getDurationHours() > 0 ? booking.getDurationHours() : "N/A"); // Lấy giá trị đã đọc từ DB

                row.add(booking.getTotalPrice() != null ? CURRENCY_FORMATTER.format(booking.getTotalPrice()) : "N/A"); // Kiểm tra null
                row.add(booking.getCreatedAt() != null ? booking.getCreatedAt().format(DATETIME_FORMATTER) : "N/A");
                tableModel.addRow(row);
            }
        }
        System.out.println("Revenue data loaded into table."); // DEBUG
    }
}