package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderCreateDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderItemCreateDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderReceiptReverseDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderReceiveDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderReceiveItemDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderUpdateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockInboundRequestDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockOutboundRequestDTO;
import com.luizotg.stock_manager.exception.BusinessException;
import com.luizotg.stock_manager.exception.InsufficientStockException;
import com.luizotg.stock_manager.exception.ResourceNotFoundException;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.PurchaseOrder;
import com.luizotg.stock_manager.model.PurchaseOrderItem;
import com.luizotg.stock_manager.model.PurchaseOrderReceipt;
import com.luizotg.stock_manager.model.PurchaseOrderStatus;
import com.luizotg.stock_manager.model.StorageLocation;
import com.luizotg.stock_manager.model.Supplier;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.PurchaseOrderReceiptRepository;
import com.luizotg.stock_manager.repository.PurchaseOrderRepository;
import com.luizotg.stock_manager.repository.StorageLocationRepository;
import com.luizotg.stock_manager.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    private static final Long PURCHASE_ORDER_ID = 1L;
    private static final Long SUPPLIER_ID = 10L;
    private static final Long PRODUCT_ID = 20L;
    private static final Long SECOND_PRODUCT_ID = 21L;
    private static final Long STORAGE_LOCATION_ID = 30L;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementService stockMovementService;

    @Mock
    private StorageLocationRepository storageLocationRepository;

    @Mock
    private PurchaseOrderReceiptRepository purchaseOrderReceiptRepository;

    private PurchaseOrderService purchaseOrderService;

    @BeforeEach
    void setUp() {
        purchaseOrderService = new PurchaseOrderService(
                purchaseOrderRepository,
                supplierRepository,
                productRepository,
                stockMovementService,
                storageLocationRepository,
                purchaseOrderReceiptRepository
        );
    }

    @Test
    void createPurchaseOrderCalculatesSubtotalsAndTotal() {
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(supplier()));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product(PRODUCT_ID, "SKU-001")));
        when(productRepository.findById(SECOND_PRODUCT_ID)).thenReturn(Optional.of(product(SECOND_PRODUCT_ID, "SKU-002")));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder order = purchaseOrderService.create(new PurchaseOrderCreateDTO(
                SUPPLIER_ID,
                null,
                LocalDate.now().plusDays(7),
                "Pedido inicial",
                List.of(
                        new PurchaseOrderItemCreateDTO(PRODUCT_ID, 2, 10.0, null),
                        new PurchaseOrderItemCreateDTO(SECOND_PRODUCT_ID, 3, 5.0, "Prioridade")
                )
        ));

        assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getItems().get(0).getEstimatedSubtotal()).isEqualTo(20.0);
        assertThat(order.getItems().get(1).getEstimatedSubtotal()).isEqualTo(15.0);
        assertThat(order.getTotalEstimatedCost()).isEqualTo(35.0);
        assertThat(order.getItems()).allSatisfy(item -> assertThat(item.getPurchaseOrder()).isSameAs(order));
        verify(purchaseOrderRepository).save(order);
    }

    @Test
    void createPurchaseOrderAllowsStatusWhenProvided() {
        mockSupplierAndProduct();
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder order = purchaseOrderService.create(new PurchaseOrderCreateDTO(
                SUPPLIER_ID,
                PurchaseOrderStatus.SENT,
                null,
                null,
                List.of(new PurchaseOrderItemCreateDTO(PRODUCT_ID, 1, 10.0, null))
        ));

        assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.SENT);
    }

    @Test
    void createPurchaseOrderThrowsWhenSupplierDoesNotExist() {
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseOrderService.create(new PurchaseOrderCreateDTO(
                SUPPLIER_ID,
                null,
                null,
                null,
                List.of(new PurchaseOrderItemCreateDTO(PRODUCT_ID, 1, 10.0, null))
        ))).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Fornecedor com ID " + SUPPLIER_ID + " não encontrado.");

        verify(productRepository, never()).findById(any());
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void createPurchaseOrderThrowsWhenProductDoesNotExist() {
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(supplier()));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseOrderService.create(new PurchaseOrderCreateDTO(
                SUPPLIER_ID,
                null,
                null,
                null,
                List.of(new PurchaseOrderItemCreateDTO(PRODUCT_ID, 1, 10.0, null))
        ))).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto com ID " + PRODUCT_ID + " não encontrado.");

        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void updatePurchaseOrderReplacesItemsAndRecalculatesTotal() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.DRAFT);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));
        when(productRepository.findById(SECOND_PRODUCT_ID)).thenReturn(Optional.of(product(SECOND_PRODUCT_ID, "SKU-002")));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder updated = purchaseOrderService.update(PURCHASE_ORDER_ID, new PurchaseOrderUpdateDTO(
                LocalDate.now().plusDays(10),
                "Atualizado",
                PurchaseOrderStatus.SENT,
                List.of(new PurchaseOrderItemCreateDTO(SECOND_PRODUCT_ID, 4, 7.5, null))
        ));

        assertThat(updated.getStatus()).isEqualTo(PurchaseOrderStatus.SENT);
        assertThat(updated.getItems()).hasSize(1);
        assertThat(updated.getItems().get(0).getProduct().getId()).isEqualTo(SECOND_PRODUCT_ID);
        assertThat(updated.getTotalEstimatedCost()).isEqualTo(30.0);
        verify(purchaseOrderRepository).save(order);
    }

    @Test
    void updatePurchaseOrderThrowsWhenOrderIsCancelled() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.CANCELLED);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> purchaseOrderService.update(PURCHASE_ORDER_ID, new PurchaseOrderUpdateDTO(
                null,
                "Não deve atualizar",
                PurchaseOrderStatus.SENT,
                List.of(new PurchaseOrderItemCreateDTO(PRODUCT_ID, 1, 10.0, null))
        ))).isInstanceOf(BusinessException.class)
                .hasMessage("Pedido cancelado não pode ser editado.");

        verify(productRepository, never()).findById(any());
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void cancelPurchaseOrderSetsStatusCancelled() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.SENT);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        purchaseOrderService.cancel(PURCHASE_ORDER_ID);

        assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
        verify(purchaseOrderRepository).save(order);
    }

    @Test
    void findBySupplierIdDelegatesToRepository() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(purchaseOrderRepository.findBySupplierId(SUPPLIER_ID, pageable)).thenReturn(Page.empty());

        Page<PurchaseOrder> result = purchaseOrderService.findBySupplierId(SUPPLIER_ID, pageable);

        assertThat(result).isEmpty();
        verify(purchaseOrderRepository).findBySupplierId(SUPPLIER_ID, pageable);
    }

    @Test
    void findByStatusDelegatesToRepository() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(purchaseOrderRepository.findByStatus(PurchaseOrderStatus.DRAFT, pageable)).thenReturn(Page.empty());

        Page<PurchaseOrder> result = purchaseOrderService.findByStatus(PurchaseOrderStatus.DRAFT, pageable);

        assertThat(result).isEmpty();
        verify(purchaseOrderRepository).findByStatus(PurchaseOrderStatus.DRAFT, pageable);
    }

    @Test
    void receivePurchaseOrderPartiallyCreatesInboundMovementAndUpdatesStatus() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.SENT, 10);
        PurchaseOrderItem item = order.getItems().get(0);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));
        when(storageLocationRepository.findById(STORAGE_LOCATION_ID)).thenReturn(Optional.of(storageLocation()));
        when(purchaseOrderReceiptRepository.save(any(PurchaseOrderReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder received = purchaseOrderService.receive(PURCHASE_ORDER_ID, new PurchaseOrderReceiveDTO(
                STORAGE_LOCATION_ID,
                List.of(new PurchaseOrderReceiveItemDTO(item.getId(), 4)),
                "NF 123"
        ));

        assertThat(item.getReceivedQuantity()).isEqualTo(4);
        assertThat(item.getPendingQuantity()).isEqualTo(6);
        assertThat(received.getStatus()).isEqualTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        verify(stockMovementService).registerInbound(argThat((StockInboundRequestDTO dto) ->
                dto.productId().equals(PRODUCT_ID)
                        && dto.storageLocationId().equals(STORAGE_LOCATION_ID)
                        && dto.quantity().equals(4)
                        && dto.reason().equals("Recebimento de pedido de compra")
                        && dto.reference().equals("PURCHASE-ORDER-" + PURCHASE_ORDER_ID)
                        && dto.responsible().equals("Sistema")
                        && dto.notes().equals("NF 123")
        ));
        verify(purchaseOrderReceiptRepository).save(argThat((PurchaseOrderReceipt receipt) ->
                receipt.getPurchaseOrder() == order
                        && receipt.getStorageLocation().getId().equals(STORAGE_LOCATION_ID)
                        && receipt.getNotes().equals("NF 123")
                        && receipt.getItems().size() == 1
                        && receipt.getItems().get(0).getPurchaseOrderItem() == item
                        && receipt.getItems().get(0).getReceivedQuantity().equals(4)
        ));
        verify(purchaseOrderRepository).save(order);
    }

    @Test
    void receivePurchaseOrderTotallySetsStatusReceived() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.SENT, 10);
        PurchaseOrderItem item = order.getItems().get(0);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));
        when(storageLocationRepository.findById(STORAGE_LOCATION_ID)).thenReturn(Optional.of(storageLocation()));
        when(purchaseOrderReceiptRepository.save(any(PurchaseOrderReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder received = purchaseOrderService.receive(PURCHASE_ORDER_ID, new PurchaseOrderReceiveDTO(
                STORAGE_LOCATION_ID,
                List.of(new PurchaseOrderReceiveItemDTO(item.getId(), 10)),
                null
        ));

        assertThat(item.getReceivedQuantity()).isEqualTo(10);
        assertThat(item.getPendingQuantity()).isZero();
        assertThat(received.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
        verify(stockMovementService).registerInbound(any(StockInboundRequestDTO.class));
    }

    @Test
    void receiveCancelledPurchaseOrderThrows() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.CANCELLED, 10);
        PurchaseOrderItem item = order.getItems().get(0);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> purchaseOrderService.receive(PURCHASE_ORDER_ID, new PurchaseOrderReceiveDTO(
                30L,
                List.of(new PurchaseOrderReceiveItemDTO(item.getId(), 1)),
                null
        ))).isInstanceOf(BusinessException.class)
                .hasMessage("Pedido cancelado não pode ser recebido.");

        verify(stockMovementService, never()).registerInbound(any(StockInboundRequestDTO.class));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void receiveAlreadyReceivedPurchaseOrderThrows() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.RECEIVED, 10);
        PurchaseOrderItem item = order.getItems().get(0);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> purchaseOrderService.receive(PURCHASE_ORDER_ID, new PurchaseOrderReceiveDTO(
                30L,
                List.of(new PurchaseOrderReceiveItemDTO(item.getId(), 1)),
                null
        ))).isInstanceOf(BusinessException.class)
                .hasMessage("Pedido já recebido não pode ser recebido novamente.");

        verify(stockMovementService, never()).registerInbound(any(StockInboundRequestDTO.class));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void receiveItemThatDoesNotBelongToPurchaseOrderThrows() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.SENT, 10);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));
        when(storageLocationRepository.findById(STORAGE_LOCATION_ID)).thenReturn(Optional.of(storageLocation()));

        assertThatThrownBy(() -> purchaseOrderService.receive(PURCHASE_ORDER_ID, new PurchaseOrderReceiveDTO(
                STORAGE_LOCATION_ID,
                List.of(new PurchaseOrderReceiveItemDTO(999L, 1)),
                null
        ))).isInstanceOf(BusinessException.class)
                .hasMessage("Item informado não pertence ao pedido.");

        verify(stockMovementService, never()).registerInbound(any(StockInboundRequestDTO.class));
        verify(purchaseOrderReceiptRepository, never()).save(any(PurchaseOrderReceipt.class));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void receiveQuantityGreaterThanPendingThrows() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.SENT, 10);
        PurchaseOrderItem item = order.getItems().get(0);
        item.receive(8);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));
        when(storageLocationRepository.findById(STORAGE_LOCATION_ID)).thenReturn(Optional.of(storageLocation()));

        assertThatThrownBy(() -> purchaseOrderService.receive(PURCHASE_ORDER_ID, new PurchaseOrderReceiveDTO(
                STORAGE_LOCATION_ID,
                List.of(new PurchaseOrderReceiveItemDTO(item.getId(), 3)),
                null
        ))).isInstanceOf(BusinessException.class)
                .hasMessage("Quantidade recebida não pode ser maior que a quantidade pendente do item.");

        assertThat(item.getReceivedQuantity()).isEqualTo(8);
        assertThat(item.getPendingQuantity()).isEqualTo(2);
        verify(stockMovementService, never()).registerInbound(any(StockInboundRequestDTO.class));
        verify(purchaseOrderReceiptRepository, never()).save(any(PurchaseOrderReceipt.class));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void reverseReceiptCreatesOutboundMovementAndUpdatesPurchaseOrderStatus() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.PARTIALLY_RECEIVED, 10);
        PurchaseOrderItem item = order.getItems().get(0);
        item.receive(4);
        PurchaseOrderReceipt receipt = purchaseOrderReceipt(order, item, 4);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));
        when(purchaseOrderReceiptRepository.findById(receipt.getId())).thenReturn(Optional.of(receipt));
        when(purchaseOrderReceiptRepository.save(any(PurchaseOrderReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrderReceipt reversed = purchaseOrderService.reverseReceipt(
                PURCHASE_ORDER_ID,
                receipt.getId(),
                new PurchaseOrderReceiptReverseDTO("Local errado")
        );

        assertThat(item.getReceivedQuantity()).isZero();
        assertThat(item.getPendingQuantity()).isEqualTo(10);
        assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.SENT);
        assertThat(reversed.getStatus().name()).isEqualTo("REVERSED");
        assertThat(reversed.getReversedAt()).isNotNull();
        assertThat(reversed.getReversalReason()).isEqualTo("Local errado");
        verify(stockMovementService).registerOutbound(argThat((StockOutboundRequestDTO dto) ->
                dto.productId().equals(PRODUCT_ID)
                        && dto.storageLocationId().equals(STORAGE_LOCATION_ID)
                        && dto.quantity().equals(4)
                        && dto.reason().equals("Estorno de recebimento de pedido de compra")
                        && dto.reference().equals("PURCHASE-ORDER-RECEIPT-" + receipt.getId())
                        && dto.responsible().equals("Sistema")
                        && dto.notes().equals("Local errado")
        ));
        verify(purchaseOrderRepository).save(order);
        verify(purchaseOrderReceiptRepository).save(receipt);
    }

    @Test
    void reverseReceiptThrowsWhenReceiptDoesNotBelongToPurchaseOrder() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.PARTIALLY_RECEIVED, 10);
        PurchaseOrder otherOrder = purchaseOrder(PurchaseOrderStatus.PARTIALLY_RECEIVED, 10);
        ReflectionTestUtils.setField(otherOrder, "id", 999L);
        PurchaseOrderReceipt receipt = purchaseOrderReceipt(otherOrder, otherOrder.getItems().get(0), 4);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));
        when(purchaseOrderReceiptRepository.findById(receipt.getId())).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> purchaseOrderService.reverseReceipt(
                PURCHASE_ORDER_ID,
                receipt.getId(),
                new PurchaseOrderReceiptReverseDTO("Pedido errado")
        )).isInstanceOf(BusinessException.class)
                .hasMessage("Recebimento informado não pertence ao pedido.");

        verify(stockMovementService, never()).registerOutbound(any(StockOutboundRequestDTO.class));
        verify(purchaseOrderReceiptRepository, never()).save(any(PurchaseOrderReceipt.class));
    }

    @Test
    void reverseReceiptThrowsWhenAlreadyReversed() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.PARTIALLY_RECEIVED, 10);
        PurchaseOrderItem item = order.getItems().get(0);
        item.receive(4);
        PurchaseOrderReceipt receipt = purchaseOrderReceipt(order, item, 4);
        receipt.reverse("Estorno anterior");
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));
        when(purchaseOrderReceiptRepository.findById(receipt.getId())).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> purchaseOrderService.reverseReceipt(
                PURCHASE_ORDER_ID,
                receipt.getId(),
                new PurchaseOrderReceiptReverseDTO("Duplicado")
        )).isInstanceOf(BusinessException.class)
                .hasMessage("Recebimento já estornado não pode ser estornado novamente.");

        verify(stockMovementService, never()).registerOutbound(any(StockOutboundRequestDTO.class));
        verify(purchaseOrderReceiptRepository, never()).save(any(PurchaseOrderReceipt.class));
    }

    @Test
    void reverseReceiptThrowsFriendlyMessageWhenStockIsInsufficient() {
        PurchaseOrder order = purchaseOrder(PurchaseOrderStatus.PARTIALLY_RECEIVED, 10);
        PurchaseOrderItem item = order.getItems().get(0);
        item.receive(4);
        PurchaseOrderReceipt receipt = purchaseOrderReceipt(order, item, 4);
        when(purchaseOrderRepository.findById(PURCHASE_ORDER_ID)).thenReturn(Optional.of(order));
        when(purchaseOrderReceiptRepository.findById(receipt.getId())).thenReturn(Optional.of(receipt));
        doThrow(new InsufficientStockException("Estoque insuficiente para realizar saída."))
                .when(stockMovementService)
                .registerOutbound(any(StockOutboundRequestDTO.class));

        assertThatThrownBy(() -> purchaseOrderService.reverseReceipt(
                PURCHASE_ORDER_ID,
                receipt.getId(),
                new PurchaseOrderReceiptReverseDTO("Sem saldo")
        )).isInstanceOf(InsufficientStockException.class)
                .hasMessage("Não há estoque suficiente para estornar este recebimento.");

        assertThat(item.getReceivedQuantity()).isEqualTo(4);
        verify(purchaseOrderReceiptRepository, never()).save(any(PurchaseOrderReceipt.class));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    private void mockSupplierAndProduct() {
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(supplier()));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product(PRODUCT_ID, "SKU-001")));
    }

    private PurchaseOrder purchaseOrder(PurchaseOrderStatus status) {
        return purchaseOrder(status, 1);
    }

    private PurchaseOrder purchaseOrder(PurchaseOrderStatus status, int quantity) {
        PurchaseOrderItem item = new PurchaseOrderItem(
                product(PRODUCT_ID, "SKU-001"),
                quantity,
                10.0,
                null
        );
        ReflectionTestUtils.setField(item, "id", 100L);

        PurchaseOrder order = new PurchaseOrder(
                supplier(),
                status,
                null,
                null,
                List.of(item)
        );
        ReflectionTestUtils.setField(order, "id", PURCHASE_ORDER_ID);

        return order;
    }

    private Supplier supplier() {
        return new Supplier(
                SUPPLIER_ID,
                "Fornecedor",
                null,
                null,
                null,
                null,
                true,
                null,
                null,
                null,
                null,
                null
        );
    }

    private StorageLocation storageLocation() {
        return new StorageLocation(
                STORAGE_LOCATION_ID,
                "Depósito",
                "WAREHOUSE",
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private PurchaseOrderReceipt purchaseOrderReceipt(PurchaseOrder order, PurchaseOrderItem item, int receivedQuantity) {
        PurchaseOrderReceipt receipt = new PurchaseOrderReceipt(
                order,
                storageLocation(),
                "NF",
                List.of(new com.luizotg.stock_manager.model.PurchaseOrderReceiptItem(
                        item,
                        item.getProduct(),
                        receivedQuantity
                ))
        );
        ReflectionTestUtils.setField(receipt, "id", 200L);

        return receipt;
    }

    private Product product(Long id, String sku) {
        return new Product(
                id,
                sku,
                "Produto " + id,
                null,
                "Marca",
                null,
                "UN",
                10.0,
                15.0,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
