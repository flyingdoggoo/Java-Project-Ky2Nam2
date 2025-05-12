package com.quanlysanbong.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/quanlysanbong_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";
    private static Connection con = null;
    public static Connection getConnection() {
        if(con == null)
        {
            try
            {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Kết nối với cơ sở dữ liệu thành công!");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return con;
    }
    public static void closeConnection() {
        if(con != null) {
            try
            {
                con.close();
                con = null;
                System.out.println("Đã đóng kết nối với cơ sở dữ liệu!");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối CSDL!");
                e.printStackTrace();
            }
        }
    }
    private DatabaseConnection() {}
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();
    }
}
