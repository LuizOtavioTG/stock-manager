package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderCreateDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderItemCreateDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderUpdateDTO;
import com.luizotg.stock_manager.exception.BusinessException;
import com.luizotg.stock_manager.exception.ResourceNotFoundException;
import com.luizotg.stock_manager.model.Product;
import com.luizotg.stock_manager.model.PurchaseOrder;
import com.luizotg.stock_manager.model.PurchaseOrderItem;
import com.luizotg.stock_manager.model.PurchaseOrderStatus;
import com.luizotg.stock_manager.model.Supplier;
import com.luizotg.stock_manager.repository.ProductRepository;
import com.luizotg.stock_manager.repository.PurchaseOrderRepository;
import com.luizotg.stock_manager.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    public Page<PurchaseOrder> findAll(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable);
    }

    public PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra com ID " + id + " não encontrado."));
    }

    public Page<PurchaseOrder> findBySupplierId(Long supplierId, Pageable pageable) {
        return purchaseOrderRepository.findBySupplierId(supplierId, pageable);
    }

    public Page<PurchaseOrder> findByStatus(PurchaseOrderStatus status, Pageable pageable) {
        return purchaseOrderRepository.findByStatus(status, pageable);
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrderCreateDTO dto) {
        Supplier supplier = supplierRepository.findById(dto.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor com ID " + dto.supplierId() + " não encontrado."));

        PurchaseOrderStatus status = dto.status() != null ? dto.status() : PurchaseOrderStatus.DRAFT;
        List<PurchaseOrderItem> items = buildItems(dto.items());

        PurchaseOrder purchaseOrder = new PurchaseOrder(
                supplier,
                status,
                dto.expectedDeliveryDate(),
                dto.notes(),
                items
        );

        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public PurchaseOrder update(Long id, PurchaseOrderUpdateDTO dto) {
        PurchaseOrder purchaseOrder = findById(id);
        if (purchaseOrder.isCancelled()) {
            throw new BusinessException("Pedido cancelado não pode ser editado.");
        }

        List<PurchaseOrderItem> items = buildItems(dto.items());
        purchaseOrder.update(dto.expectedDeliveryDate(), dto.notes(), dto.status(), items);

        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public void cancel(Long id) {
        PurchaseOrder purchaseOrder = findById(id);
        purchaseOrder.cancel();
        purchaseOrderRepository.save(purchaseOrder);
    }

    private List<PurchaseOrderItem> buildItems(List<PurchaseOrderItemCreateDTO> itemDtos) {
        if (itemDtos == null || itemDtos.isEmpty()) {
            throw new BusinessException("Pedido deve ter pelo menos um item.");
        }

        return itemDtos.stream()
                .map(this::buildItem)
                .toList();
    }

    private PurchaseOrderItem buildItem(PurchaseOrderItemCreateDTO dto) {
        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + dto.productId() + " não encontrado."));

        return new PurchaseOrderItem(product, dto.quantity(), dto.unitCost(), dto.notes());
    }
}
