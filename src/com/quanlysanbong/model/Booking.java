package com.quanlysanbong.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Booking {
    private int id;
    private int userId;
    private int fieldId;
    private int durationHours;
    private LocalDateTime bookingTime;
    private BigDecimal totalPrice;
    private BookingStatus bookingStatus; // Sử dụng enum mới
    private LocalDateTime createdAt;

    // Thông tin thêm để hiển thị
    private String userName;
    private String fieldName;


    // *** ĐÃ CẬP NHẬT ENUM BookingStatus ***
    public enum BookingStatus {
        BOOKED("Đã đặt"),       // Tên hằng số: BOOKED, giá trị hiển thị/DB: "Đã đặt"
        CANCELLED("Đã hủy");    // Tên hằng số: CANCELLED, giá trị hiển thị/DB: "Đã hủy"

        private final String displayName;

        BookingStatus(String displayName) {
            this.displayName = displayName;
        }

        // Trả về giá trị tiếng Việt để lưu/đọc DB và hiển thị UI
        public String getDisplayName() {
            return displayName;
        }

        // Helper để lấy enum từ String (tiếng Việt) đọc từ DB
        public static BookingStatus fromString(String text) {
            if(text == null) return BOOKED; // Hoặc throw exception? Mặc định là BOOKED

            for (BookingStatus b : BookingStatus.values()) {
                // So sánh với displayName (tiếng Việt)
                if (b.displayName.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            // Nếu không tìm thấy giá trị khớp trong DB
            System.err.println("Không nhận dạng được trạng thái đặt sân từ DB: '" + text + "'. Sử dụng trạng thái mặc định 'Đã đặt'.");
            return BOOKED; // Mặc định
        }

        @Override
        public String toString() {
            // Hiển thị tên tiếng Việt trong UI nếu cần
            return displayName;
        }
    }
    // *** KẾT THÚC CẬP NHẬT ENUM BookingStatus ***


    // Constructors, Getters, Setters (không thay đổi cấu trúc)
    public Booking() {}

    public Booking(int id, int userId, int fieldId, LocalDateTime bookingTime, int durationHours, BigDecimal totalPrice, BookingStatus bookingStatus, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.fieldId = fieldId;
        this.bookingTime = bookingTime;
        this.durationHours = durationHours; // Gán giá trị durationHours
        this.totalPrice = totalPrice;
        this.bookingStatus = bookingStatus;
        this.createdAt = createdAt;
    }


    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getFieldId() { return fieldId; }
    public void setFieldId(int fieldId) { this.fieldId = fieldId; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BookingStatus getBookingStatus() { return bookingStatus; } // Trả về enum mới
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; } // Nhận enum mới
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
}