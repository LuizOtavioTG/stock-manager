package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.dto.product.ProductCreateDTO;
import com.luizotg.stock_manager.dto.product.ProductDetailDTO;
import com.luizotg.stock_manager.dto.product.ProductUpdateDTO;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.service.ProductService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER)")
    @Transactional
    public ResponseEntity<ProductDetailDTO> createProduct(
            @RequestBody @Valid ProductCreateDTO productDTO
    ) {
        Product product = productService.saveProduct(productDTO);
        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(product.getId())
                .toUri();
        return ResponseEntity.created(uri)
                .body(new ProductDetailDTO(product));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER, T(com.luizotg.stock_manager.security.Roles).USER)")
    public ResponseEntity<ProductDetailDTO> detailProduct(
            @PathVariable Long id
    ) {
        Product product = productService.findProductById(id);
        return ResponseEntity.ok(new ProductDetailDTO(product));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER, T(com.luizotg.stock_manager.security.Roles).USER)")
    public ResponseEntity<Page<ProductDetailDTO>> listAllProducts(
            @PageableDefault(size = 10, sort = {"name"}) Pageable pageable
    ) {
        Page<Product> products = productService.findAllProducts(pageable);
        Page<ProductDetailDTO> dtoPage = products.map(ProductDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(T(com.luizotg.stock_manager.security.Roles).ADMIN)")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id
    ) {
        productService.findProductById(id);
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER)")
    @Transactional
    public ResponseEntity<ProductDetailDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid ProductUpdateDTO productUpdateDTO
    ) {
        Product product = productService.updateProduct(id, productUpdateDTO);
        return ResponseEntity.ok(new ProductDetailDTO(product));
    }
}
