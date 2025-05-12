package com.quanlysanbong.util;

import com.quanlysanbong.model.User;

// Lớp đơn giản để lưu thông tin người dùng đang đăng nhập
public class CurrentUser {
    private static User loggedInUser;

    private CurrentUser() {} // Private constructor

    public static void login(User user) {
        loggedInUser = user;
    }

    public static void logout() {
        loggedInUser = null;
    }

    public static User getUser() {
        return loggedInUser;
    }

    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }

    // Tiện ích để cập nhật thông tin user (ví dụ: sau khi nạp tiền)
    public static void updateUser(User updatedUser) {
        if (loggedInUser != null && loggedInUser.getId() == updatedUser.getId()) {
            loggedInUser = updatedUser;
        }
    }
}