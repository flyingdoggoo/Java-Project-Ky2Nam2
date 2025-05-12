package com.quanlysanbong.model;

import java.math.BigDecimal;

public class Field {
    private int id;
    private String name;
    private BigDecimal pricePerHour;
    private Status status;

    // *** ĐÃ CẬP NHẬT ENUM STATUS ***
    public enum Status {
        AVAILABLE("Trống"),
        BOOKED("Đã đặt"),
        MAINTENANCE("Đang bảo dưỡng");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Status fromString(String text) {
            if (text == null) return AVAILABLE;

            for (Status s : Status.values()) {
                if (s.displayName.equalsIgnoreCase(text)) {
                    return s;
                }
            }
            System.err.println("Không nhận dạng được trạng thái sân từ DB: '" + text + "'. Sử dụng trạng thái mặc định 'Trống'.");
            return AVAILABLE;
        }

        @Override
        public String toString() {
            // Vẫn hiển thị tên tiếng Việt trong các thành phần UI như JComboBox
            return displayName;
        }
    }

    public Field() {}

    public Field(int id, String name, BigDecimal pricePerHour, Status status) {
        this.id = id;
        this.name = name;
        this.pricePerHour = pricePerHour;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal pricePerHour) { this.pricePerHour = pricePerHour; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return name + " (" + pricePerHour + " VND/giờ)";
    }
}