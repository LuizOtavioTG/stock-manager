package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Buscar todos os produtos
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    // Salvar novo produto
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // Buscar produto por ID
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    // Buscar produto por SKU
    public Optional<Product> findProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    // Atualizar produto existente
    public Product updateProduct(Product updatedProduct) {
        if (updatedProduct.getId() == null || !productRepository.existsById(updatedProduct.getId())) {
            throw new IllegalArgumentException("Produto com ID inexistente ou nulo.");
        }
        return productRepository.save(updatedProduct);
    }

    // Deletar produto por ID
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

}
