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

    @Tool(description = "Search the product catalogue by (part of) a product name. "
            + "Returns description, price, stock level and warranty information.")
    public List<Product> findProducts(@ToolParam(description = "Product name or keyword, e.g. 'headphones'") String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }
}
