package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.product.ProductCreateDTO;
import com.luizotg.stock_manager.dto.product.ProductUpdateDTO;
import com.luizotg.stock_manager.exception.BusinessException;
import com.luizotg.stock_manager.exception.DuplicateResourceException;
import com.luizotg.stock_manager.exception.InactiveResourceException;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.Supplier;
import com.luizotg.stock_manager.repository.CategoryRepository;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final Long PRODUCT_ID = 1L;
    private static final Long SUPPLIER_ID = 1L;
    private static final String SKU = "SKU-001";

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository, supplierRepository);
    }

    @Test
    void saveProductThrowsWhenSkuAlreadyExists() {
        when(productRepository.existsBySku(SKU)).thenReturn(true);

        assertThatThrownBy(() -> productService.saveProduct(createDTO(SKU)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("SKU já está em uso.");

        verify(productRepository).existsBySku(SKU);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void saveProductPersistsSkuWhenItIsUnique() {
        when(productRepository.existsBySku(SKU)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product product = productService.saveProduct(createDTO(SKU));

        assertThat(product.getSku()).isEqualTo(SKU);
        verify(productRepository).existsBySku(SKU);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProductThrowsWhenSkuBelongsToAnotherProduct() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product(PRODUCT_ID)));
        when(productRepository.existsBySkuAndIdNot(SKU, PRODUCT_ID)).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, updateDTO(SKU)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("SKU já está em uso.");

        verify(productRepository).existsBySkuAndIdNot(SKU, PRODUCT_ID);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductAllowsKeepingOwnSku() {
        Product product = new Product(PRODUCT_ID);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot(SKU, PRODUCT_ID)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProduct(PRODUCT_ID, updateDTO(SKU));

        assertThat(updated.getSku()).isEqualTo(SKU);
        verify(productRepository).existsBySkuAndIdNot(SKU, PRODUCT_ID);
        verify(productRepository).save(product);
    }

    @Test
    void saveProductThrowsWhenNameIsBlank() {
        assertThatThrownBy(() -> productService.saveProduct(createDTO(SKU, " ")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Nome do produto não pode ser vazio.");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductThrowsWhenNameIsBlank() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product(PRODUCT_ID)));

        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, updateDTO(null, " ")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Nome do produto não pode ser vazio.");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void saveProductThrowsWhenCostPriceIsNegative() {
        assertThatThrownBy(() -> productService.saveProduct(createDTO(SKU, "Product", -1.0, 15.0)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Preço de custo não pode ser negativo.");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductThrowsWhenCostPriceIsNegative() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product(PRODUCT_ID)));

        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, updateDTO(null, null, -1.0, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Preço de custo não pode ser negativo.");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void saveProductThrowsWhenSalePriceIsNegative() {
        assertThatThrownBy(() -> productService.saveProduct(createDTO(SKU, "Product", 10.0, -1.0)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Preço de venda não pode ser negativo.");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductThrowsWhenSalePriceIsNegative() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product(PRODUCT_ID)));

        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, updateDTO(null, null, null, -1.0)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Preço de venda não pode ser negativo.");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void saveProductThrowsWhenSupplierIsInactive() {
        when(supplierRepository.findAllById(Set.of(SUPPLIER_ID)))
                .thenReturn(List.of(supplier(false)));

        assertThatThrownBy(() -> productService.saveProduct(createDTO(SKU, "Product", 10.0, 15.0, Set.of(SUPPLIER_ID))))
                .isInstanceOf(InactiveResourceException.class)
                .hasMessage("Fornecedor inativo não pode ser associado a um novo produto.");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductThrowsWhenSupplierIsInactive() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product(PRODUCT_ID)));
        when(supplierRepository.findAllById(Set.of(SUPPLIER_ID)))
                .thenReturn(List.of(supplier(false)));

        assertThatThrownBy(() -> productService.updateProduct(
                PRODUCT_ID,
                updateDTO(null, null, null, null, Set.of(SUPPLIER_ID))
        )).isInstanceOf(InactiveResourceException.class)
                .hasMessage("Fornecedor inativo não pode ser associado a um novo produto.");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void saveProductAllowsActiveSupplier() {
        when(supplierRepository.findAllById(Set.of(SUPPLIER_ID)))
                .thenReturn(List.of(supplier(true)));
        when(productRepository.existsBySku(SKU)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product product = productService.saveProduct(createDTO(SKU, "Product", 10.0, 15.0, Set.of(SUPPLIER_ID)));

        assertThat(product.getSuppliers()).hasSize(1);
        verify(productRepository).save(any(Product.class));
    }

    private ProductCreateDTO createDTO(String sku) {
        return createDTO(sku, "Product");
    }

    private ProductCreateDTO createDTO(String sku, String name) {
        return createDTO(sku, name, 10.0, 15.0);
    }

    private ProductCreateDTO createDTO(String sku, String name, Double costPrice, Double salePrice) {
        return createDTO(sku, name, costPrice, salePrice, null);
    }

    private ProductCreateDTO createDTO(
            String sku,
            String name,
            Double costPrice,
            Double salePrice,
            Set<Long> supplierIds
    ) {
        return new ProductCreateDTO(
                sku,
                name,
                null,
                "Brand",
                null,
                "UN",
                costPrice,
                salePrice,
                null,
                supplierIds
        );
    }

    private ProductUpdateDTO updateDTO(String sku) {
        return updateDTO(sku, null);
    }

    private ProductUpdateDTO updateDTO(String sku, String name) {
        return updateDTO(sku, name, null, null);
    }

    private ProductUpdateDTO updateDTO(String sku, String name, Double costPrice, Double salePrice) {
        return updateDTO(sku, name, costPrice, salePrice, null);
    }

    private ProductUpdateDTO updateDTO(
            String sku,
            String name,
            Double costPrice,
            Double salePrice,
            Set<Long> supplierIds
    ) {
        return new ProductUpdateDTO(
                sku,
                name,
                null,
                null,
                null,
                null,
                costPrice,
                salePrice,
                null,
                null,
                supplierIds
        );
    }

    private Supplier supplier(Boolean active) {
        return new Supplier(
                SUPPLIER_ID,
                "Supplier",
                null,
                null,
                null,
                null,
                active,
                null,
                null,
                null
        );
    }
}
