package com.quanlysanbong.dao;

import com.quanlysanbong.db.DatabaseConnection;
import com.quanlysanbong.model.Field;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FieldDAO {

    // Lấy tất cả các sân (Không đổi, vì fromString đọc displayName)
    public List<Field> getAllFields() {
        List<Field> fieldList = new ArrayList<>();
        String sql = "SELECT id, name, price_per_hour, status FROM fields";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return fieldList;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Field field = new Field();
                field.setId(rs.getInt("id"));
                field.setName(rs.getString("name"));
                field.setPricePerHour(rs.getBigDecimal("price_per_hour"));
                // Dùng fromString để chuyển đổi chuỗi tiếng Việt từ DB sang Enum
                field.setStatus(Field.Status.fromString(rs.getString("status")));
                fieldList.add(field);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách sân: " + e.getMessage());
            e.printStackTrace();
        }
        return fieldList;
    }

    // Lấy sân theo ID (Không đổi)
    public Field getFieldById(int fieldId) {
        String sql = "SELECT id, name, price_per_hour, status FROM fields WHERE id = ?";
        Field field = null;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fieldId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                field = new Field();
                field.setId(rs.getInt("id"));
                field.setName(rs.getString("name"));
                field.setPricePerHour(rs.getBigDecimal("price_per_hour"));
                field.setStatus(Field.Status.fromString(rs.getString("status")));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy thông tin sân theo ID: " + e.getMessage());
            e.printStackTrace();
        }
        return field;
    }


    // Thêm sân mới (Cập nhật: Lưu displayName)
    public boolean addField(Field field) {
        String sql = "INSERT INTO fields (name, price_per_hour, status) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, field.getName());
            pstmt.setBigDecimal(2, field.getPricePerHour());
            // *** LƯU GIÁ TRỊ TIẾNG VIỆT VÀO DB ***
            pstmt.setString(3, field.getStatus().getDisplayName());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm sân mới: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật thông tin sân (Cập nhật: Lưu displayName)
    public boolean updateField(Field field) {
        String sql = "UPDATE fields SET name = ?, price_per_hour = ?, status = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, field.getName());
            pstmt.setBigDecimal(2, field.getPricePerHour());
            // *** LƯU GIÁ TRỊ TIẾNG VIỆT VÀO DB ***
            pstmt.setString(3, field.getStatus().getDisplayName());
            pstmt.setInt(4, field.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật sân: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật trạng thái sân (Cập nhật: Lưu displayName)
    public boolean updateFieldStatus(int fieldId, Field.Status newStatus) {
        String sql = "UPDATE fields SET status = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // *** LƯU GIÁ TRỊ TIẾNG VIỆT VÀO DB ***
            pstmt.setString(1, newStatus.getDisplayName());
            pstmt.setInt(2, fieldId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái sân: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // Xóa sân (Không đổi)
    public boolean deleteField(int fieldId) {
        String sql = "DELETE FROM fields WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fieldId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa sân: " + e.getMessage());
            if (e.getErrorCode() == 1451) {
                System.err.println("Không thể xóa sân vì đã có lịch sử đặt sân.");
            }
            e.printStackTrace();
            return false;
        }
    }

    // Kiểm tra xem sân có trống vào thời gian cụ thể không (Không đổi)
    public boolean isFieldAvailable(int fieldId, Timestamp bookingTimestamp) {
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE field_id = ? AND booking_time = ? AND booking_status = 'Đã đặt'"; // Vẫn kiểm tra 'Đã đặt' trong booking
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fieldId);
            pstmt.setTimestamp(2, bookingTimestamp);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra sân trống: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}