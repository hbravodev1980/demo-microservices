package com.investigation.products_service.services;

import com.investigation.products_service.model.dtos.ProductRequest;
import com.investigation.products_service.model.dtos.ProductResponse;
import com.investigation.products_service.model.entities.Product;
import com.investigation.products_service.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public void addProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .sku(productRequest.sku())
                .name(productRequest.name())
                .description(productRequest.description())
                .price(productRequest.price())
                .status(productRequest.status())
                .build();

        productRepository.save(product);
        log.info("Product added: {}", product);
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToProductResponse).toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus());
    }
}