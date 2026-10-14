package com.example.support.config;

import com.example.support.model.Order;
import com.example.support.model.Order.OrderItem;
import com.example.support.model.Order.OrderStatus;
import com.example.support.model.Product;
import com.example.support.repository.OrderRepository;
import com.example.support.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Inserts demo orders and products the first time the app starts against an empty database. */
@Component
public class SeedDataRunner implements CommandLineRunner {

    private final OrderRepository orders;
    private final ProductRepository products;

    public SeedDataRunner(OrderRepository orders, ProductRepository products) {
        this.orders = orders;
        this.products = products;
    }

    @Override
    public void run(String... args) {
        LocalDate today = LocalDate.now();

        if (products.count() == 0) {
            products.saveAll(List.of(
                    new Product("P-100", "Wireless Headphones X1", "Audio",
                            "Over-ear Bluetooth headphones, 30h battery, active noise cancelling.",
                            new BigDecimal("4999.00"), 42, "1 year manufacturer warranty"),
                    new Product("P-200", "Smart Watch S2", "Wearables",
                            "Fitness tracker with heart-rate monitor, GPS and 7-day battery.",
                            new BigDecimal("7999.00"), 15, "1 year manufacturer warranty"),
                    new Product("P-300", "USB-C Fast Charger 65W", "Accessories",
                            "GaN charger with two USB-C ports and one USB-A port.",
                            new BigDecimal("1499.00"), 0, "6 months warranty")
            ));
        }

        if (orders.count() == 0) {
            orders.saveAll(List.of(
                    // Delayed order: estimated delivery already passed -> good demo for the delay scenario
                    new Order(1025L, "Riya Sharma", "riya@example.com", OrderStatus.IN_TRANSIT,
                            today.minusDays(9), today.minusDays(3), null,
                            "BlueDart", "BD778899", "Pune Sorting Hub",
                            new BigDecimal("4999.00"),
                            List.of(new OrderItem("P-100", "Wireless Headphones X1", 1, new BigDecimal("4999.00")))),
                    // Delivered order -> good for refund / return questions
                    new Order(1026L, "Amit Verma", "amit@example.com", OrderStatus.DELIVERED,
                            today.minusDays(12), today.minusDays(7), today.minusDays(6),
                            "Delhivery", "DL112233", "Delivered",
                            new BigDecimal("7999.00"),
                            List.of(new OrderItem("P-200", "Smart Watch S2", 1, new BigDecimal("7999.00")))),
                    // Just shipped, on time
                    new Order(1027L, "Neha Patil", "neha@example.com", OrderStatus.SHIPPED,
                            today.minusDays(1), today.plusDays(4), null,
                            "Ecom Express", "EE445566", "Mumbai Warehouse",
                            new BigDecimal("1499.00"),
                            List.of(new OrderItem("P-300", "USB-C Fast Charger 65W", 1, new BigDecimal("1499.00"))))
            ));
        }
    }
}
