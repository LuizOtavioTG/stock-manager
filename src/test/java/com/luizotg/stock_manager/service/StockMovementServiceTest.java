package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.stockMovement.StockAdjustmentRequestDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockInboundRequestDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockOutboundRequestDTO;
import com.luizotg.stock_manager.exception.InsufficientStockException;
import com.luizotg.stock_manager.exception.InvalidStockMovementException;
import com.luizotg.stock_manager.model.Inventory;
import com.luizotg.stock_manager.model.MovementType;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.repository.InventoryRepository;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.StockMovementRepository;
import com.luizotg.stock_manager.repository.StorageLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class StockMovementServiceTest {

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

    private StockMovementService stockMovementService;

    @BeforeEach
    void setUp() {
        InventoryService inventoryService = new InventoryService(
                inventoryRepository,
                productRepository,
                storageLocationRepository,
                stockMovementRepository
        );
        stockMovementService = new StockMovementService(stockMovementRepository, inventoryService);
    }

    @Test
    void registerInboundIncreasesInventoryAndCreatesInboundMovement() {
        Inventory inventory = inventoryWithQuantity(10);
        mockProductAndStorageLocation();
        when(inventoryRepository.findByProductIdAndStorageLocationId(PRODUCT_ID, STORAGE_LOCATION_ID))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stockMovementService.registerInbound(new StockInboundRequestDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                5,
                "Compra",
                "NF-001",
                "Luiz",
                null
        ));

        assertThat(inventory.getQuantity()).isEqualTo(15);
        StockMovement movement = savedMovement();
        assertThat(movement.getMovementType()).isEqualTo(MovementType.INBOUND);
        assertThat(movement.getQuantity()).isEqualTo(5);
        assertThat(movement.getProduct().getId()).isEqualTo(PRODUCT_ID);
        assertThat(movement.getStorageLocation().getId()).isEqualTo(STORAGE_LOCATION_ID);
    }

    @Test
    void registerOutboundDecreasesInventoryAndCreatesOutboundMovement() {
        Inventory inventory = inventoryWithQuantity(10);
        mockProductAndStorageLocation();
        when(inventoryRepository.findByProductIdAndStorageLocationId(PRODUCT_ID, STORAGE_LOCATION_ID))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stockMovementService.registerOutbound(new StockOutboundRequestDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                4,
                "Venda",
                "ORDER-001",
                "Luiz",
                null
        ));

        assertThat(inventory.getQuantity()).isEqualTo(6);
        StockMovement movement = savedMovement();
        assertThat(movement.getMovementType()).isEqualTo(MovementType.OUTBOUND);
        assertThat(movement.getQuantity()).isEqualTo(4);
        assertThat(movement.getProduct().getId()).isEqualTo(PRODUCT_ID);
        assertThat(movement.getStorageLocation().getId()).isEqualTo(STORAGE_LOCATION_ID);
    }

    @Test
    void registerOutboundWithoutEnoughStockThrowsAndDoesNotCreateMovement() {
        Inventory inventory = inventoryWithQuantity(3);
        mockProductAndStorageLocation();
        when(inventoryRepository.findByProductIdAndStorageLocationId(PRODUCT_ID, STORAGE_LOCATION_ID))
                .thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> stockMovementService.registerOutbound(new StockOutboundRequestDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                5,
                "Venda",
                "ORDER-001",
                "Luiz",
                null
        ))).isInstanceOf(InsufficientStockException.class);

        assertThat(inventory.getQuantity()).isEqualTo(3);
        verify(inventoryRepository, never()).save(any(Inventory.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void registerAdjustmentChangesInventoryAndCreatesAdjustmentMovement() {
        Inventory inventory = inventoryWithQuantity(10);
        mockProductAndStorageLocation();
        when(inventoryRepository.findByProductIdAndStorageLocationId(PRODUCT_ID, STORAGE_LOCATION_ID))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        stockMovementService.registerAdjustment(new StockAdjustmentRequestDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                7,
                "Contagem de estoque",
                "Luiz",
                null
        ));

        assertThat(inventory.getQuantity()).isEqualTo(7);
        StockMovement movement = savedMovement();
        assertThat(movement.getMovementType()).isEqualTo(MovementType.ADJUSTMENT);
        assertThat(movement.getQuantity()).isEqualTo(3);
        assertThat(movement.getReason()).isEqualTo("Contagem de estoque");
    }

    @Test
    void registerAdjustmentRequiresReason() {
        assertThatThrownBy(() -> stockMovementService.registerAdjustment(new StockAdjustmentRequestDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                7,
                " ",
                "Luiz",
                null
        ))).isInstanceOf(InvalidStockMovementException.class);

        verify(inventoryRepository, never()).save(any(Inventory.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void registerInboundRequiresQuantity() {
        assertThatThrownBy(() -> stockMovementService.registerInbound(new StockInboundRequestDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                null,
                "Compra",
                "NF-001",
                "Luiz",
                null
        ))).isInstanceOf(InvalidStockMovementException.class)
                .hasMessage("Quantidade movimentada deve ser maior que zero.");

        verify(inventoryRepository, never()).save(any(Inventory.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void registerInboundRejectsZeroQuantity() {
        assertThatThrownBy(() -> stockMovementService.registerInbound(new StockInboundRequestDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                0,
                "Compra",
                "NF-001",
                "Luiz",
                null
        ))).isInstanceOf(InvalidStockMovementException.class)
                .hasMessage("Quantidade movimentada deve ser maior que zero.");

        verify(inventoryRepository, never()).save(any(Inventory.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void registerOutboundRejectsNegativeQuantity() {
        assertThatThrownBy(() -> stockMovementService.registerOutbound(new StockOutboundRequestDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                -1,
                "Venda",
                "ORDER-001",
                "Luiz",
                null
        ))).isInstanceOf(InvalidStockMovementException.class)
                .hasMessage("Quantidade movimentada deve ser maior que zero.");

        verify(inventoryRepository, never()).save(any(Inventory.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void registerAdjustmentRejectsMovementWithZeroQuantity() {
        Inventory inventory = inventoryWithQuantity(10);
        mockProductAndStorageLocation();
        when(inventoryRepository.findByProductIdAndStorageLocationId(PRODUCT_ID, STORAGE_LOCATION_ID))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> stockMovementService.registerAdjustment(new StockAdjustmentRequestDTO(
                PRODUCT_ID,
                STORAGE_LOCATION_ID,
                10,
                "Contagem de estoque",
                "Luiz",
                null
        ))).isInstanceOf(InvalidStockMovementException.class)
                .hasMessage("Quantidade movimentada deve ser maior que zero.");

        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    private void mockProductAndStorageLocation() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product(PRODUCT_ID)));
        when(storageLocationRepository.findById(STORAGE_LOCATION_ID))
                .thenReturn(Optional.of(new StorageLocation(STORAGE_LOCATION_ID)));
    }

    private Inventory inventoryWithQuantity(Integer quantity) {
        return new Inventory(new Product(PRODUCT_ID), new StorageLocation(STORAGE_LOCATION_ID), quantity);
    }

    private StockMovement savedMovement() {
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        return captor.getValue();
    }
}
