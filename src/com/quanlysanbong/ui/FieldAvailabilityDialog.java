package com.quanlysanbong.ui;

import com.quanlysanbong.dao.BookingDAO;
import com.quanlysanbong.dao.FieldDAO;
import com.quanlysanbong.model.Field;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FieldAvailabilityDialog extends JDialog {

    private JComboBox<Field> fieldComboBox;
    private JTextField dateField; // Format YYYY-MM-DD
    private JPanel schedulePanel; // Panel để hiển thị các khung giờ
    private JButton viewButton;
    private JButton closeButton;

    private FieldDAO fieldDAO;
    private BookingDAO bookingDAO;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    private static final int START_HOUR = 7; // Giờ bắt đầu hoạt động
    private static final int END_HOUR = 23; // Giờ kết thúc hoạt động (Khung cuối là 22h-23h)

    public FieldAvailabilityDialog(Window owner) {
        // Dùng Window làm owner và ModalityType
        super(owner, "Xem Lịch Trống Của Sân", Dialog.ModalityType.APPLICATION_MODAL);
        fieldDAO = new FieldDAO();
        bookingDAO = new BookingDAO();
        initComponents();
        loadFields();
        setSize(450, 600); // Tăng chiều cao
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel for Selection
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        fieldComboBox = new JComboBox<>();
        dateField = new JTextField(10);
        dateField.setToolTipText("Nhập ngày xem lịch (YYYY-MM-DD)");
        dateField.setText(LocalDate.now().format(DATE_FORMATTER)); // Ngày hiện tại
        viewButton = new JButton("Xem Lịch");

        gbc.gridx = 0; gbc.gridy = 0; topPanel.add(new JLabel("Chọn sân:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; topPanel.add(fieldComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; topPanel.add(new JLabel("Chọn ngày:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; topPanel.add(dateField, gbc);
        gbc.gridx = 2; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; topPanel.add(viewButton, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Schedule Panel (Center)
        schedulePanel = new JPanel();
        // Sử dụng GridLayout để xếp các khung giờ
        schedulePanel.setLayout(new GridLayout(0, 1, 5, 5)); // 0 rows = tự động thêm, 1 cột, khoảng cách 5px
        JScrollPane scrollPane = new JScrollPane(schedulePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel (Close Button)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        closeButton = new JButton("Đóng");
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action Listeners
        viewButton.addActionListener(e -> displaySchedule());
        closeButton.addActionListener(e -> dispose());

        // Tự động xem lịch khi chọn ngày hoặc sân (tùy chọn)
        // fieldComboBox.addActionListener(e -> displaySchedule());
        // dateField.addActionListener(e -> displaySchedule()); // Xem khi nhấn Enter vào dateField
    }

    private void loadFields() {
        fieldComboBox.removeAllItems();
        List<Field> fields = fieldDAO.getAllFields();
        if (fields != null) {
            for (Field field : fields) {
                fieldComboBox.addItem(field); // Add Field object
            }
        }
    }

    private void displaySchedule() {
        Field selectedField = (Field) fieldComboBox.getSelectedItem();
        String dateStr = dateField.getText().trim();

        // ... (kiểm tra null/empty)

        try {
            LocalDate selectedDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            System.out.println("\n--- Displaying Schedule ---"); // DEBUG
            System.out.println("Selected Field: " + (selectedField != null ? selectedField.getId() : "null") + ", Date: " + selectedDate); // DEBUG

            // *** GỌI HÀM DAO ĐÚNG ***
            Set<LocalTime> coveredHours = bookingDAO.getCoveredHoursForFieldOnDate(selectedField.getId(), selectedDate);
            System.out.println("Covered hours received from DAO: " + coveredHours); // DEBUG

            schedulePanel.removeAll(); // Xóa các label cũ

            for (int hour = START_HOUR; hour < END_HOUR; hour++) {
                LocalTime slotStartTime = LocalTime.of(hour, 0);
                String timeSlot = String.format("%02d:00 - %02d:00", hour, hour + 1);

                JLabel timeSlotLabel = new JLabel(timeSlot, SwingConstants.CENTER);
                timeSlotLabel.setOpaque(true);
                timeSlotLabel.setBorder(new LineBorder(Color.GRAY));
                // Cố định kích thước hoặc dùng layout manager tốt hơn
                timeSlotLabel.setPreferredSize(new Dimension(200, 30)); // Ví dụ kích thước cố định

                // *** KIỂM TRA contains TRÊN SET ***
                boolean isCovered = coveredHours.contains(slotStartTime);
                System.out.println("  Checking hour " + slotStartTime + ": " + (isCovered ? "COVERED" : "AVAILABLE")); // DEBUG

                if (isCovered) {
                    timeSlotLabel.setBackground(Color.RED);
                    timeSlotLabel.setForeground(Color.WHITE);
                    timeSlotLabel.setText(timeSlot + " - ĐÃ ĐẶT");
                } else {
                    timeSlotLabel.setBackground(Color.GREEN);
                    timeSlotLabel.setForeground(Color.BLACK);
                    timeSlotLabel.setText(timeSlot + " - CÒN TRỐNG");
                }
                schedulePanel.add(timeSlotLabel);
            }

            // Cập nhật giao diện panel
            schedulePanel.revalidate();
            schedulePanel.repaint();
            System.out.println("Schedule Panel updated."); // DEBUG

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            schedulePanel.removeAll(); schedulePanel.revalidate(); schedulePanel.repaint(); // Clear panel on error
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xem lịch: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            schedulePanel.removeAll(); schedulePanel.revalidate(); schedulePanel.repaint(); // Clear panel on error
        }
    }
}