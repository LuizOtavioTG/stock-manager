package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.inventory.InventoryCreateDTO;
import com.luizotg.stock_manager.dto.inventory.InventoryUpdateDTO;
import com.luizotg.stock_manager.exception.DuplicateInventoryException;
import com.luizotg.stock_manager.exception.InvalidStockMovementException;
import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.repository.InventoryRepository;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.StockMovementRepository;
import com.luizotg.stock_manager.repository.StorageLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    private static final Long PRODUCT_ID = 1L;
    private static final Long STORAGE_LOCATION_ID = 1L;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StorageLocationRepository storageLocationRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(
                inventoryRepository,
                productRepository,
                storageLocationRepository,
                stockMovementRepository
        );
    }

    @Test
    void saveInventoryThrowsWhenProductAndStorageLocationAlreadyHaveInventory() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product(PRODUCT_ID)));
        when(storageLocationRepository.findById(STORAGE_LOCATION_ID))
                .thenReturn(Optional.of(new StorageLocation(STORAGE_LOCATION_ID)));
        when(inventoryRepository.existsByProductIdAndStorageLocationId(PRODUCT_ID, STORAGE_LOCATION_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> inventoryService.saveInventory(new InventoryCreateDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                10,
                null,
                null,
                null
        ))).isInstanceOf(DuplicateInventoryException.class);

        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void saveInventoryPersistsStockControlFields() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product(PRODUCT_ID)));
        when(storageLocationRepository.findById(STORAGE_LOCATION_ID))
                .thenReturn(Optional.of(new StorageLocation(STORAGE_LOCATION_ID)));
        when(inventoryRepository.existsByProductIdAndStorageLocationId(PRODUCT_ID, STORAGE_LOCATION_ID))
                .thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory inventory = inventoryService.saveInventory(new InventoryCreateDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                0,
                20,
                100,
                30
        ));

        assertThat(inventory.getMinimumStock()).isEqualTo(20);
        assertThat(inventory.getMaximumStock()).isEqualTo(100);
        assertThat(inventory.getReorderPoint()).isEqualTo(30);
    }

    @Test
    void saveInventoryThrowsWhenStockControlFieldsAreInvalid() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product(PRODUCT_ID)));
        when(storageLocationRepository.findById(STORAGE_LOCATION_ID))
                .thenReturn(Optional.of(new StorageLocation(STORAGE_LOCATION_ID)));
        when(inventoryRepository.existsByProductIdAndStorageLocationId(PRODUCT_ID, STORAGE_LOCATION_ID))
                .thenReturn(false);

        assertThatThrownBy(() -> inventoryService.saveInventory(new InventoryCreateDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                0,
                20,
                100,
                10
        ))).isInstanceOf(InvalidStockMovementException.class)
                .hasMessage("Ponto de reposição deve ser maior ou igual ao estoque mínimo.");

        verify(inventoryRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void updateInventoryUpdatesOnlyStockControlFields() {
        Inventory inventory = new Inventory(new Product(PRODUCT_ID), new StorageLocation(STORAGE_LOCATION_ID), 10);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventory updated = inventoryService.updateInventory(1L, new InventoryUpdateDTO(
                null,
                null,
                null,
                10,
                50,
                20
        ));

        assertThat(updated.getQuantity()).isEqualTo(10);
        assertThat(updated.getMinimumStock()).isEqualTo(10);
        assertThat(updated.getMaximumStock()).isEqualTo(50);
        assertThat(updated.getReorderPoint()).isEqualTo(20);
    }
}
