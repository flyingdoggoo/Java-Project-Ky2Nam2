package com.quanlysanbong.dao;

import com.quanlysanbong.db.DatabaseConnection;
import com.quanlysanbong.model.User;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // --- Các phương thức hiện có (login, register, getUserById) ---

    public User login(String username, String password) {
        String sql = "SELECT id, full_name, phone_number, balance, role FROM users WHERE username = ? AND password = ?"; // Password không hash!
        User user = null;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(username);
                user.setFullName(rs.getString("full_name"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setBalance(rs.getBigDecimal("balance"));
                user.setRole(User.Role.valueOf(rs.getString("role")));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đăng nhập: " + e.getMessage()); e.printStackTrace();
        }
        return user;
    }

    public boolean register(User user) { // Chỉ dùng cho User tự đăng ký ban đầu
        String sql = "INSERT INTO users (username, password, full_name, phone_number, balance, role) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Password không hash!
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getPhoneNumber());
            pstmt.setBigDecimal(5, BigDecimal.ZERO); // Số dư ban đầu luôn là 0 khi tự đăng ký
            pstmt.setString(6, User.Role.USER.name()); // Vai trò luôn là USER khi tự đăng ký

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi đăng ký người dùng: " + e.getMessage());
            if (e.getErrorCode() == 1062) { System.err.println("Username đã tồn tại."); }
            e.printStackTrace();
            return false;
        }
    }

    public User getUserById(int userId) {
        String sql = "SELECT id, username, full_name, phone_number, balance, role FROM users WHERE id = ?";
        User user = null;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setBalance(rs.getBigDecimal("balance"));
                user.setRole(User.Role.valueOf(rs.getString("role")));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy user theo ID " + userId + ": " + e.getMessage()); e.printStackTrace();
        }
        return user;
    }

    // --- Phương thức lấy tất cả User (Read) ---
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT id, username, full_name, phone_number, balance, role FROM users";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("getAllUsers: Cannot get DB connection.");
            return userList; // Trả về list rỗng
        }

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setBalance(rs.getBigDecimal("balance"));
                user.setRole(User.Role.valueOf(rs.getString("role")));
                userList.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách người dùng: " + e.getMessage()); e.printStackTrace();
        }
        return userList;
    }

    // --- Phương thức Thêm User (Create - do Admin) ---
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, phone_number, balance, role) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) { System.err.println("addUser: Cannot get DB connection."); return false; }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Password không hash!
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getPhoneNumber());
            pstmt.setBigDecimal(5, user.getBalance()); // Lấy số dư từ Admin
            pstmt.setString(6, user.getRole().name()); // Lấy vai trò từ Admin

            int affectedRows = pstmt.executeUpdate();
            System.out.println("addUser - Affected rows: " + affectedRows); // Log debug
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi admin thêm người dùng '" + user.getUsername() + "': " + e.getMessage());
            if (e.getErrorCode() == 1062) { // Mã lỗi trùng username của MySQL
                System.err.println(" -> Username đã tồn tại.");
            }
            e.printStackTrace();
            return false;
        }
    }

    // --- Phương thức Sửa User (Update - do Admin) ---
    // Chỉ cập nhật Họ tên, SĐT, Vai trò. Số dư cập nhật riêng.
    public boolean updateUserByAdmin(User user) {
        String sql = "UPDATE users SET full_name = ?, phone_number = ?, role = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) { System.err.println("updateUserByAdmin: Cannot get DB connection."); return false; }

        System.out.println("Attempting admin update for User ID: " + user.getId() + " with Role: " + user.getRole()); // Log debug

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getPhoneNumber());
            pstmt.setString(3, user.getRole().name()); // Cập nhật vai trò
            pstmt.setInt(4, user.getId());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("updateUserByAdmin - Affected rows: " + affectedRows); // Log debug
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi admin cập nhật thông tin user ID " + user.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- Phương thức Sửa User (Update - do User tự sửa) ---
    // Chỉ cập nhật Họ tên, SĐT.
    public boolean updateUserInfo(User user) {
        String sql = "UPDATE users SET full_name = ?, phone_number = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) { System.err.println("updateUserInfo: Cannot get DB connection."); return false; }

        System.out.println("Attempting self update for User ID: " + user.getId()); // Log debug

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getPhoneNumber());
            pstmt.setInt(3, user.getId());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("updateUserInfo - Affected rows: " + affectedRows); // Log debug
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi user cập nhật thông tin cá nhân ID " + user.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // --- Phương thức Cập nhật Số dư (Update - do Admin Nạp/Rút) ---
    public boolean updateUserBalance(int userId, BigDecimal newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) { System.err.println("updateUserBalance: Cannot get DB connection."); return false; }

        System.out.println("Attempting to update balance for User ID: " + userId + " to New Balance: " + newBalance);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, newBalance);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            System.out.println("updateUserBalance - Affected rows: " + affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật số dư cho User ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- Phương thức Xóa User (Delete - do Admin) ---
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) { System.err.println("deleteUser: Cannot get DB connection."); return false; }

        System.out.println("Attempting to delete User ID: " + userId); // Log debug

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("deleteUser - Affected rows: " + affectedRows); // Log debug
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa người dùng ID " + userId + ": " + e.getMessage());
            // Kiểm tra lỗi khóa ngoại (nếu user đã đặt sân)
            if (e.getErrorCode() == 1451) { // MySQL foreign key constraint fails
                System.err.println(" -> Không thể xóa người dùng vì đã có lịch sử đặt sân liên quan.");
            }
            e.printStackTrace();
            return false;
        }
    }
}