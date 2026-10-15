package com.example.support.tools;

import com.example.support.model.Order;
import com.example.support.repository.OrderRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
public class OrderTools {

    private final OrderRepository orderRepository;

    public OrderTools(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Tool(description = "Get order items, amount, and customer details by orderId.")
    public Object getOrder(@ToolParam(description = "Order ID number, e.g. 1025", required = false) Long orderId) {
        if (orderId == null) {
            return Map.of("error", "No order ID provided. Please ask the customer for their order number.");
        }
        return orderRepository.findById(orderId)
                .<Object>map(o -> o)
                .orElse(Map.of("error", "No order found with ID " + orderId));
    }

    @Tool(description = "Get tracking number, carrier, shipping location, delivery date, and delay days by orderId.")
    public Object getDeliveryStatus(@ToolParam(description = "Order ID number, e.g. 1025", required = false) Long orderId) {
        if (orderId == null) {
            return Map.of("error", "No order ID provided. Please ask the customer for their order number.");
        }
        return orderRepository.findById(orderId)
                .<Object>map(this::toDeliveryInfo)
                .orElse(Map.of("error", "No order found with ID " + orderId));
    }

    private Map<String, Object> toDeliveryInfo(Order o) {
        LocalDate today = LocalDate.now();
        boolean delivered = o.status() == Order.OrderStatus.DELIVERED;
        boolean open = !delivered
                && o.status() != Order.OrderStatus.CANCELLED
                && o.status() != Order.OrderStatus.REFUNDED;
        long daysLate = (open && o.estimatedDelivery() != null && today.isAfter(o.estimatedDelivery()))
                ? ChronoUnit.DAYS.between(o.estimatedDelivery(), today) : 0;

        return Map.of(
                "orderId", o.orderId(),
                "status", o.status().name(),
                "carrier", String.valueOf(o.carrier()),
                "trackingNumber", String.valueOf(o.trackingNumber()),
                "lastKnownLocation", String.valueOf(o.lastKnownLocation()),
                "estimatedDelivery", String.valueOf(o.estimatedDelivery()),
                "deliveredDate", String.valueOf(o.deliveredDate()),
                "isDelayed", daysLate > 0,
                "daysLate", daysLate
        );
    }
}
