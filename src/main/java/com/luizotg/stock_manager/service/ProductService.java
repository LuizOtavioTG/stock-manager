package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.product.ProductCreateDTO;
import com.luizotg.stock_manager.dto.product.ProductUpdateDTO;
import com.luizotg.stock_manager.exception.ResourceNotFoundException;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.repository.CategoryRepository;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    public Product saveProduct(ProductCreateDTO dto) {
        Product product = new Product(dto, categoryRepository, supplierRepository);
        return productRepository.save(product);
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + id + " não encontrado."));
    }

    public Page<Product> findAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public void deleteProductById(Long id) {
        Product product = findProductById(id);
        productRepository.delete(product);
    }

    public Product updateProduct(Long id, ProductUpdateDTO dto) {
        Product product = findProductById(id);
        product.updateFromDTO(dto, categoryRepository, supplierRepository);
        return productRepository.save(product);
    }
}
