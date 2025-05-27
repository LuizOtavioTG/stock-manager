package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.product.ProductCreateDTO;
import com.luizotg.stock_manager.dto.product.ProductUpdateDTO;
import com.luizotg.stock_manager.model.Category;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.Supplier;
import com.luizotg.stock_manager.repository.CategoryRepository;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
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
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria com ID " + dto.categoryId() + " não encontrada."));

        Set<Supplier> suppliers = new HashSet<>(supplierRepository.findAllById(dto.supplierIds()));
        if (suppliers.size() != dto.supplierIds().size()) {
            throw new EntityNotFoundException("Um ou mais fornecedores não foram encontrados.");
        }

        Product product = new Product();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setBrand(dto.brand());
        product.setCategory(category);
        product.setUnitOfMeasure(dto.unitOfMeasure());
        product.setCostPrice(dto.costPrice());
        product.setSalePrice(dto.salePrice());
        product.setExpirationDate(dto.expirationDate());
        product.setSuppliers(suppliers);
        product.setActive(true);

        return productRepository.save(product);
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto com ID " + id + " não encontrado."));
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

        if (dto.name() != null) product.setName(dto.name());
        if (dto.description() != null) product.setDescription(dto.description());
        if (dto.brand() != null) product.setBrand(dto.brand());
        if (dto.unitOfMeasure() != null) product.setUnitOfMeasure(dto.unitOfMeasure());
        if (dto.costPrice() != null) product.setCostPrice(dto.costPrice());
        if (dto.salePrice() != null) product.setSalePrice(dto.salePrice());
        if (dto.expirationDate() != null) product.setExpirationDate(dto.expirationDate());
        if (dto.active() != null) product.setActive(dto.active());

        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria com ID " + dto.categoryId() + " não encontrada."));
            product.setCategory(category);
        }

        if (dto.supplierIds() != null) {
            Set<Supplier> suppliers = new HashSet<>(supplierRepository.findAllById(dto.supplierIds()));
            if (suppliers.size() != dto.supplierIds().size()) {
                throw new EntityNotFoundException("Um ou mais fornecedores não foram encontrados.");
            }
            product.setSuppliers(suppliers);
        }

        return productRepository.save(product);
    }
}
