package com.example.support.tools;

import com.example.support.model.Product;
import com.example.support.repository.ProductRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductTools {

    private final ProductRepository productRepository;

    public ProductTools(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Tool(description = "Search products by keyword to check price, stock, and warranty.")
    public List<Product> findProducts(@ToolParam(description = "Product keyword, e.g. 'charger', 'headphones'", required = false) String name) {
        if (name == null || name.isBlank()) {
            return productRepository.findAll();
        }
        String trimmed = name.trim();
        List<Product> direct = productRepository.findByNameContainingIgnoreCase(trimmed);
        if (!direct.isEmpty()) {
            return direct;
        }
        String[] keywords = trimmed.split("\\s+");
        List<Product> all = productRepository.findAll();
        List<Product> matches = all.stream()
                .filter(p -> {
                    String combined = (p.name() + " " + p.category() + " " + p.description()).toLowerCase();
                    for (String kw : keywords) {
                        if (kw.length() > 2 && combined.contains(kw.toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();
        return matches.isEmpty() ? all : matches;
    }
}
