package com.example.support.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "orders")
public record Order(
        @Id Long orderId,
        String customerName,
        String customerEmail,
        OrderStatus status,
        LocalDate orderDate,
        LocalDate estimatedDelivery,
        LocalDate deliveredDate,
        String carrier,
        String trackingNumber,
        String lastKnownLocation,
        BigDecimal totalAmount,
        List<OrderItem> items
) {
    public enum OrderStatus {
        PROCESSING, SHIPPED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, RETURN_REQUESTED, REFUNDED
    }

    public record OrderItem(String productId, String name, int quantity, BigDecimal price) {}
}
