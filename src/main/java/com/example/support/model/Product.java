package com.example.support.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "products")
public record Product(
        @Id String productId,
        String name,
        String category,
        String description,
        BigDecimal price,
        int stock,
        String warrantyInfo
) {}
