package com.quanlysanbong.dao;

import com.quanlysanbong.db.DatabaseConnection;
import com.quanlysanbong.model.Booking;
import com.quanlysanbong.model.Field;
import com.quanlysanbong.model.User;
import com.quanlysanbong.util.CurrentUser; // Import CurrentUser
import java.util.Set;
import java.util.HashSet;
import java.time.LocalDate;     // <--- THÊM IMPORT NÀY
import java.time.LocalTime;     // <--- THÊM IMPORT NÀY
import java.time.LocalDateTime; // (Chắc đã có)
import java.util.ArrayList;     // <--- THÊM IMPORT NÀY
import java.util.List;          // <--- THÊM IMPORT NÀY
import java.sql.Timestamp;      // <--- THÊM IMPORT NÀY (Nếu dùng trong hàm isFieldAvailable...)
import java.sql.Date;
import javax.swing.*; // Import JOptionPane
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    private final UserDAO userDAO = new UserDAO();
    private final FieldDAO fieldDAO = new FieldDAO();

    // Tạo một lượt đặt sân mới (sử dụng Transaction)
    public boolean createBooking(int userId, int fieldId, LocalDateTime startDateTime, int durationHours) {
        System.out.println("--- Starting createBooking ---");
        System.out.println("User ID: " + userId + ", Field ID: " + fieldId + ", Start: " + startDateTime + ", Duration: " + durationHours);

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) { System.err.println("Lỗi: Không thể kết nối CSDL."); return false; }

        User user = userDAO.getUserById(userId);
        Field field = fieldDAO.getFieldById(fieldId);

        if (user == null || field == null) { System.err.println("Lỗi: Không tìm thấy User hoặc Field."); return false; }

        BigDecimal pricePerHour = field.getPricePerHour();
        BigDecimal totalPrice = pricePerHour.multiply(BigDecimal.valueOf(durationHours)); // Tính tổng tiền
        System.out.println("Price Per Hour: " + pricePerHour + ", Total Price: " + totalPrice);

        BigDecimal currentBalance = user.getBalance();
        System.out.println("Current Balance: " + currentBalance);
        if (currentBalance.compareTo(totalPrice) < 0) {
            System.err.println("Số dư không đủ.");
            JOptionPane.showMessageDialog(null, "Số dư không đủ!", "Không đủ tiền", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (field.getStatus() == Field.Status.MAINTENANCE) {
            System.err.println("Sân đang bảo dưỡng.");
            JOptionPane.showMessageDialog(null, "Sân đang bảo dưỡng.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        LocalDateTime endDateTime = startDateTime.plusHours(durationHours);
        // *** GỌI HÀM KIỂM TRA MỚI/ĐÚNG ***
        if (!isFieldAvailableForDuration(fieldId, startDateTime, endDateTime)) {
            System.err.println("Một hoặc nhiều khung giờ đã được đặt.");
            JOptionPane.showMessageDialog(null, "Một hoặc nhiều khung giờ đã có người đặt.", "Trùng lịch", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Timestamp bookingTimestamp = Timestamp.valueOf(startDateTime);
        try {
            conn.setAutoCommit(false);

            // *** CẬP NHẬT INSERT SQL VÀ THAM SỐ ***
            String insertBookingSQL = "INSERT INTO bookings (user_id, field_id, booking_time, duration_hours, total_price, booking_status) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtBooking = conn.prepareStatement(insertBookingSQL)) {
                pstmtBooking.setInt(1, userId);
                pstmtBooking.setInt(2, fieldId);
                pstmtBooking.setTimestamp(3, bookingTimestamp);
                pstmtBooking.setInt(4, durationHours); // *** TRUYỀN durationHours ***
                pstmtBooking.setBigDecimal(5, totalPrice);
                pstmtBooking.setString(6, Booking.BookingStatus.BOOKED.getDisplayName());
                int bookingRows = pstmtBooking.executeUpdate();
                if (bookingRows == 0) throw new SQLException("Tạo booking thất bại.");
                System.out.println("Booking record inserted.");
            }

            BigDecimal newBalance = currentBalance.subtract(totalPrice); // Tính lại ngay trước khi update
            System.out.println("New Balance calculated: " + newBalance);
            boolean balanceUpdated = userDAO.updateUserBalance(userId, newBalance); // Gọi hàm update balance
            if (!balanceUpdated) throw new SQLException("Không thể cập nhật số dư.");
            System.out.println("User balance updated.");

            user.setBalance(newBalance); // Cập nhật object User
            CurrentUser.updateUser(user); // Cập nhật CurrentUser
            System.out.println("CurrentUser updated.");

            conn.commit();
            System.out.println("Transaction committed successfully.");
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi Transaction: " + e.getMessage()); e.printStackTrace();
            try { System.err.println("Rolling back transaction..."); conn.rollback(); System.err.println("Rollback successful."); }
            catch (SQLException rollbackEx) { System.err.println("Lỗi Rollback: " + rollbackEx.getMessage()); }
            JOptionPane.showMessageDialog(null, "Đặt sân thất bại do lỗi hệ thống:\n" + e.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try { conn.setAutoCommit(true); }
            catch (SQLException e) { System.err.println("Lỗi setAutoCommit(true): " + e.getMessage()); }
            System.out.println("--- Ending createBooking ---");
        }
    }


    // Hủy một lượt đặt sân (sử dụng Transaction)
    public boolean cancelBooking(int bookingId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        Booking booking = getBookingById(bookingId);

        // *** SỬ DỤNG ENUM MỚI ĐỂ KIỂM TRA ***
        if (booking == null || booking.getBookingStatus() != Booking.BookingStatus.BOOKED) {
            System.err.println("Booking không tồn tại hoặc không ở trạng thái có thể hủy.");
            JOptionPane.showMessageDialog(null, "Lịch đặt này không tồn tại hoặc đã ở trạng thái không thể hủy.", "Không thể hủy", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (booking.getBookingTime().isBefore(LocalDateTime.now())) {
            System.err.println("Không thể hủy lịch đặt đã qua.");
            JOptionPane.showMessageDialog(null, "Không thể hủy lịch đặt đã diễn ra.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return false;
        }


        int userId = booking.getUserId();
        BigDecimal refundAmount = booking.getTotalPrice();

        try {
            conn.setAutoCommit(false);

            // 1. Cập nhật trạng thái booking thành 'Đã hủy'
            String updateBookingSQL = "UPDATE bookings SET booking_status = ? WHERE id = ? AND booking_status = ?"; // Thêm điều kiện kiểm tra trạng thái cũ
            try (PreparedStatement pstmtBooking = conn.prepareStatement(updateBookingSQL)) {
                // *** SỬ DỤNG ENUM MỚI VÀ LƯU GIÁ TRỊ TIẾNG VIỆT ***
                pstmtBooking.setString(1, Booking.BookingStatus.CANCELLED.getDisplayName());
                pstmtBooking.setInt(2, bookingId);
                pstmtBooking.setString(3, Booking.BookingStatus.BOOKED.getDisplayName()); // Chỉ hủy nếu đang là BOOKED
                int updatedRows = pstmtBooking.executeUpdate();
                if(updatedRows == 0) {
                    throw new SQLException("Không cập nhật được trạng thái booking (có thể đã bị hủy bởi người khác).");
                }
            }

            // 2. Hoàn tiền cho người dùng
            User user = userDAO.getUserById(userId);
            if (user == null) throw new SQLException("Không tìm thấy user để hoàn tiền.");
            BigDecimal newBalance = user.getBalance().add(refundAmount);
            if (!userDAO.updateUserBalance(userId, newBalance)) {
                throw new SQLException("Không thể cập nhật số dư người dùng khi hoàn tiền.");
            }
            // Cập nhật CurrentUser nếu là user đang đăng nhập
            if(CurrentUser.isLoggedIn() && CurrentUser.getUser().getId() == userId) {
                user.setBalance(newBalance);
                CurrentUser.updateUser(user);
            }

            // 3. Không cập nhật field status

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi trong quá trình hủy sân (Transaction): " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi trong quá trình hủy sân:\n" + e.getMessage(), "Lỗi Hủy Sân", JOptionPane.ERROR_MESSAGE);
            try { conn.rollback(); System.err.println("Transaction hủy sân đã được rollback."); }
            catch (SQLException rollbackEx) { System.err.println("Lỗi khi rollback transaction hủy sân: " + rollbackEx.getMessage()); }
            return false;
        } finally {
            try { conn.setAutoCommit(true); }
            catch (SQLException e) { System.err.println("Lỗi khi bật lại auto-commit sau hủy sân: " + e.getMessage()); }
        }
    }


    // Lấy lịch sử đặt sân của một người dùng (Không đổi)
    public List<Booking> getBookingsByUserId(int userId) {
        List<Booking> bookingList = new ArrayList<>();
        // *** THÊM b.duration_hours VÀO SELECT ***
        String sql = "SELECT b.id, b.user_id, b.field_id, b.booking_time, b.duration_hours, b.total_price, b.booking_status, b.created_at, " +
                "u.username, f.name AS field_name " +
                "FROM bookings b " +
                "JOIN users u ON b.user_id = u.id " +
                "JOIN fields f ON b.field_id = f.id " +
                "WHERE b.user_id = ? ORDER BY b.booking_time DESC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return bookingList;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Booking booking = mapResultSetToBooking(rs); // mapResultSetToBooking sẽ đọc duration
                booking.setUserName(rs.getString("username"));
                booking.setFieldName(rs.getString("field_name"));
                bookingList.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử đặt sân của user: " + e.getMessage()); e.printStackTrace();
        }
        return bookingList;
    }

    // Lấy tất cả lịch sử đặt sân (Không đổi)
    public List<Booking> getAllBookings() {
        List<Booking> bookingList = new ArrayList<>();
        // *** THÊM b.duration_hours VÀO SELECT ***
        String sql = "SELECT b.id, b.user_id, b.field_id, b.booking_time, b.duration_hours, b.total_price, b.booking_status, b.created_at, " +
                "u.username, f.name AS field_name " +
                "FROM bookings b " +
                "JOIN users u ON b.user_id = u.id " +
                "JOIN fields f ON b.field_id = f.id " +
                "ORDER BY b.booking_time DESC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return bookingList;

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Booking booking = mapResultSetToBooking(rs); // mapResultSetToBooking sẽ đọc duration
                booking.setUserName(rs.getString("username"));
                booking.setFieldName(rs.getString("field_name"));
                bookingList.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy toàn bộ lịch sử đặt sân: " + e.getMessage()); e.printStackTrace();
        }
        return bookingList;
    }


    // Lấy booking theo ID (Không đổi)
    public Booking getBookingById(int bookingId) {
        // *** THÊM b.duration_hours VÀO SELECT ***
        String sql = "SELECT b.id, b.user_id, b.field_id, b.booking_time, b.duration_hours, b.total_price, b.booking_status, b.created_at, " +
                "u.username, f.name AS field_name " +
                "FROM bookings b " +
                "JOIN users u ON b.user_id = u.id " +
                "JOIN fields f ON b.field_id = f.id " +
                "WHERE b.id = ?";
        Booking booking = null;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                booking = mapResultSetToBooking(rs); // mapResultSetToBooking sẽ đọc duration
                booking.setUserName(rs.getString("username"));
                booking.setFieldName(rs.getString("field_name"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy booking theo ID: " + e.getMessage()); e.printStackTrace();
        }
        return booking;
    }

    // Helper method để map ResultSet sang Booking object (Không đổi, đã dùng fromString)
    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getInt("id"));
        booking.setUserId(rs.getInt("user_id"));
        booking.setFieldId(rs.getInt("field_id"));
        Timestamp bookingTimestamp = rs.getTimestamp("booking_time");
        booking.setBookingTime(bookingTimestamp != null ? bookingTimestamp.toLocalDateTime() : null);
        booking.setDurationHours(rs.getInt("duration_hours")); // *** ĐỌC duration_hours ***
        booking.setTotalPrice(rs.getBigDecimal("total_price"));
        booking.setBookingStatus(Booking.BookingStatus.fromString(rs.getString("booking_status")));
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        booking.setCreatedAt(createdTimestamp != null ? createdTimestamp.toLocalDateTime() : null);
        return booking;
    }
    public Set<LocalTime> getCoveredHoursForFieldOnDate(int fieldId, LocalDate date) {
        Set<LocalTime> coveredHours = new HashSet<>();
        // *** ĐẢM BẢO LẤY duration_hours ***
        String sql = "SELECT booking_time, duration_hours FROM bookings " +
                "WHERE field_id = ? " +
                "AND DATE(booking_time) = ? " +
                "AND booking_status = ?";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return coveredHours;

        System.out.println("Fetching covered hours for Field ID: " + fieldId + " on Date: " + date);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fieldId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            pstmt.setString(3, Booking.BookingStatus.BOOKED.getDisplayName());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Timestamp startTimestamp = rs.getTimestamp("booking_time");
                int duration = rs.getInt("duration_hours"); // *** Đọc duration từ DB ***

                System.out.println("  Found booking starting at: " + startTimestamp + " with duration: " + duration);

                if (startTimestamp != null && duration > 0) {
                    LocalDateTime startTime = startTimestamp.toLocalDateTime();
                    System.out.print("    Adding hours: ");
                    for (int i = 0; i < duration; i++) { // Lặp đúng số giờ duration
                        LocalTime coveredHour = startTime.plusHours(i).toLocalTime();
                        coveredHours.add(coveredHour);
                        System.out.print(coveredHour + " ");
                    }
                    System.out.println();
                } else if (startTimestamp != null && duration <= 0) {
                    System.err.println("  Warning: Found booking with non-positive duration: " + duration);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy các giờ bị chiếm dụng: " + e.getMessage()); e.printStackTrace();
        }
        System.out.println("Final Covered Hours Set: " + coveredHours);
        return coveredHours;
    }
    public boolean isFieldAvailableForDuration(int fieldId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE field_id = ? " +
                "AND booking_status = ? " +
                "AND booking_time >= ? " + // >= start
                "AND booking_time < ?";   // < end

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        System.out.println("Checking availability for Field ID: " + fieldId + " from " + startDateTime + " to " + endDateTime); // DEBUG

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fieldId);
            pstmt.setString(2, Booking.BookingStatus.BOOKED.getDisplayName());
            pstmt.setTimestamp(3, Timestamp.valueOf(startDateTime));
            pstmt.setTimestamp(4, Timestamp.valueOf(endDateTime));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("  Found " + count + " bookings starting in the interval."); // DEBUG
                return count == 0; // Trống nếu count = 0
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra sân trống trong khoảng: " + e.getMessage()); e.printStackTrace();
        }
        return false; // Mặc định không trống nếu lỗi
    }
    // Thống kê doanh thu
    public BigDecimal getTotalRevenue() {
        // *** SỬ DỤNG ENUM MỚI VÀ LẤY GIÁ TRỊ TIẾNG VIỆT CHO SQL ***
        String sql = "SELECT SUM(total_price) AS total FROM bookings WHERE booking_status = ?";
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return totalRevenue;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // *** TRUYỀN GIÁ TRỊ TIẾNG VIỆT "Đã đặt" VÀO SQL ***
            pstmt.setString(1, Booking.BookingStatus.BOOKED.getDisplayName());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                BigDecimal sum = rs.getBigDecimal("total");
                if (sum != null) {
                    totalRevenue = sum;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính tổng doanh thu: " + e.getMessage());
            e.printStackTrace();
        }
        return totalRevenue;
    }
}