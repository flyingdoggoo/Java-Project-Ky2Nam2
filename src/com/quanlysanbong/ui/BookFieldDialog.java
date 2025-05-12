package com.quanlysanbong.ui;

import com.quanlysanbong.dao.BookingDAO;
import com.quanlysanbong.dao.FieldDAO;
import com.quanlysanbong.model.Field;
import com.quanlysanbong.util.CurrentUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class BookFieldDialog extends JDialog {

    private JComboBox<Field> fieldComboBox;
    private JTextField dateField; // Format YYYY-MM-DD
    private JComboBox<Integer> hourComboBox; // Giờ bắt đầu
    private JSpinner durationSpinner;
    private JButton bookButton;
    private JButton cancelButton;
    private JLabel priceLabel;
    private JLabel infoLabel;

    private FieldDAO fieldDAO;
    private BookingDAO bookingDAO;
    private UserDashboard parentDashboard; // Để cập nhật số dư

    // Định dạng ngày và tiền tệ
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final int START_HOUR = 7; // Có thể thêm giờ bắt đầu nếu cần
    private static final int END_HOUR = 23;
    public BookFieldDialog(UserDashboard owner) {
        super(owner, "Đặt Sân", Dialog.ModalityType.APPLICATION_MODAL);
        this.parentDashboard = owner;
        fieldDAO = new FieldDAO();
        bookingDAO = new BookingDAO();
        initComponents();
        loadAvailableFields();
        pack(); // Tự động điều chỉnh kích thước
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // --- Components ---
        fieldComboBox = new JComboBox<>();
        dateField = new JTextField(10);
        dateField.setToolTipText("Nhập ngày theo định dạng YYYY-MM-DD");
        dateField.setText(LocalDate.now().format(DATE_FORMATTER)); // Gợi ý ngày hiện tại

        hourComboBox = new JComboBox<>();
        for (int i = 7; i <= 22; i++) { // Giờ hoạt động ví dụ: 7h - 22h
            hourComboBox.addItem(i);
        }
        SpinnerModel durationModel = new SpinnerNumberModel(1, 1, 5, 1); // value, min, max, step
        durationSpinner = new JSpinner(durationModel);
        durationSpinner.setPreferredSize(new Dimension(60, durationSpinner.getPreferredSize().height));

        priceLabel = new JLabel("Giá: ");
        infoLabel = new JLabel("Chọn sân, ngày, giờ bắt đầu và thời lượng đặt.");
        infoLabel.setForeground(Color.BLUE);

        bookButton = new JButton("Đặt Sân");
        cancelButton = new JButton("Hủy Bỏ");

        // --- Layout ---
        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Chọn sân:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; add(fieldComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; add(new JLabel("Ngày (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; add(dateField, gbc); // Span 2 columns

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; add(new JLabel("Giờ bắt đầu:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; add(hourComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; add(new JLabel("Thời lượng (giờ):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; add(durationSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; add(priceLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; add(infoLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(bookButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        add(buttonPanel, gbc);

        // --- Action Listeners ---
        fieldComboBox.addActionListener(e -> updatePriceLabel());
        durationSpinner.addChangeListener(e -> updatePriceLabel()); // Lắng nghe thay đổi spinner

        bookButton.addActionListener(e -> bookField());
        cancelButton.addActionListener(e -> dispose());
    }

    private void loadAvailableFields() {
        fieldComboBox.removeAllItems();
        List<Field> fields = fieldDAO.getAllFields();
        for (Field field : fields) {
            // Chỉ thêm các sân không trong tình trạng bảo dưỡng vào lựa chọn đặt
            if (field.getStatus() != Field.Status.MAINTENANCE) {
                // Sử dụng đối tượng Field trực tiếp, JComboBox sẽ gọi toString() để hiển thị
                fieldComboBox.addItem(field);
            }
        }
        updatePriceLabel(); // Cập nhật giá cho sân được chọn mặc định
    }

    private void updatePriceLabel() {
        Field selectedField = (Field) fieldComboBox.getSelectedItem();
        int duration = (int) durationSpinner.getValue(); // Lấy giá trị từ spinner

        if (selectedField != null) {
            BigDecimal pricePerHour = selectedField.getPricePerHour();
            BigDecimal totalPrice = pricePerHour.multiply(BigDecimal.valueOf(duration));
            priceLabel.setText("Giá: " + CURRENCY_FORMATTER.format(pricePerHour) + "/giờ. " +
                    "Tổng cộng cho " + duration + " giờ: " + CURRENCY_FORMATTER.format(totalPrice));
        } else {
            priceLabel.setText("Giá: Vui lòng chọn sân");
        }
    }

    private void bookField() {
        Field selectedField = (Field) fieldComboBox.getSelectedItem();
        String dateStr = dateField.getText().trim();
        Integer selectedHour = (Integer) hourComboBox.getSelectedItem();
        int duration = (int) durationSpinner.getValue(); // Lấy thời lượng

        if (selectedField == null || dateStr.isEmpty() || selectedHour == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sân, nhập ngày và giờ bắt đầu.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedHour + duration > END_HOUR) {
            JOptionPane.showMessageDialog(this,
                    "Thời gian đặt vượt quá giờ hoạt động (" + END_HOUR + ":00). Vui lòng chọn giờ bắt đầu hoặc thời lượng khác.",
                    "Lỗi thời gian", JOptionPane.WARNING_MESSAGE);
            return;
        }


        try {
            LocalDate selectedDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            LocalTime startTime = LocalTime.of(selectedHour, 0);
            LocalDateTime startDateTime = LocalDateTime.of(selectedDate, startTime);

            if (startDateTime.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(this, "Không thể đặt sân cho thời gian trong quá khứ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int userId = CurrentUser.getUser().getId();
            int fieldId = selectedField.getId();
            BigDecimal totalPrice = selectedField.getPricePerHour().multiply(BigDecimal.valueOf(duration)); // Tính tổng tiền

            // Hiển thị xác nhận
            String confirmMessage = String.format(
                    "Xác nhận đặt sân:\nSân: %s\nNgày: %s\nThời gian: %s:00 - %s:00 (%d giờ)\nTổng tiền: %s\nSố dư hiện tại: %s",
                    selectedField.getName(),
                    selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    selectedHour, selectedHour + duration, duration, // Hiển thị giờ kết thúc
                    CURRENCY_FORMATTER.format(totalPrice),
                    CURRENCY_FORMATTER.format(CurrentUser.getUser().getBalance())
            );
            int confirm = JOptionPane.showConfirmDialog(this, confirmMessage, "Xác nhận đặt sân", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // *** GỌI bookingDAO VỚI DURATION ***
                boolean success = bookingDAO.createBooking(userId, fieldId, startDateTime, duration);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Đặt sân thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    if (parentDashboard != null) {
                        parentDashboard.updateBalanceLabel();
                    }
                    dispose();
                }
                // else: DAO đã hiển thị lỗi (hết tiền, trùng lịch)
            }

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ. Vui lòng nhập theo YYYY-MM-DD.", "Lỗi định dạng ngày", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi đặt sân: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}