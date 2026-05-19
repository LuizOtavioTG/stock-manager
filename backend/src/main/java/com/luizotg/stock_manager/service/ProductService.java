package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.product.ProductCreateDTO;
import com.luizotg.stock_manager.dto.product.ProductUpdateDTO;
import com.luizotg.stock_manager.exception.BusinessException;
import com.luizotg.stock_manager.exception.DuplicateResourceException;
import com.luizotg.stock_manager.exception.InactiveResourceException;
import com.luizotg.stock_manager.exception.ResourceNotFoundException;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.Supplier;
import com.luizotg.stock_manager.repository.CategoryRepository;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

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
        validateProductName(dto.name());
        validatePrices(dto.costPrice(), dto.salePrice());
        validateActiveSuppliers(dto.supplierIds());

        if (productRepository.existsBySku(dto.sku())) {
            throw new DuplicateResourceException("SKU já está em uso.");
        }

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
        if (dto.name() != null) {
            validateProductName(dto.name());
        }
        validatePrices(dto.costPrice(), dto.salePrice());
        validateActiveSuppliers(dto.supplierIds());

        if (dto.sku() != null && productRepository.existsBySkuAndIdNot(dto.sku(), id)) {
            throw new DuplicateResourceException("SKU já está em uso.");
        }

        product.updateFromDTO(dto, categoryRepository, supplierRepository);
        return productRepository.save(product);
    }

    private void validateProductName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Nome do produto não pode ser vazio.");
        }
    }

    private void validateActiveSuppliers(Set<Long> supplierIds) {
        if (supplierIds == null || supplierIds.isEmpty()) {
            return;
        }

        Set<Supplier> suppliers = new HashSet<>(supplierRepository.findAllById(supplierIds));
        boolean hasInactiveSupplier = suppliers.stream()
                .anyMatch(supplier -> Boolean.FALSE.equals(supplier.getActive()));

        if (hasInactiveSupplier) {
            throw new InactiveResourceException("Fornecedor inativo não pode ser associado a um novo produto.");
        }
    }

    private void validatePrices(Double costPrice, Double salePrice) {
        if (costPrice != null && costPrice < 0) {
            throw new BusinessException("Preço de custo não pode ser negativo.");
        }
        if (salePrice != null && salePrice < 0) {
            throw new BusinessException("Preço de venda não pode ser negativo.");
        }
    }
}
