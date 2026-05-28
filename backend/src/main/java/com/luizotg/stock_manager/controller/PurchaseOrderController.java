package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderCreateDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderDetailDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderReceiptDetailDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderReceiptReverseDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderReceiveDTO;
import com.luizotg.stock_manager.dto.purchaseOrder.PurchaseOrderUpdateDTO;
import com.luizotg.stock_manager.model.PurchaseOrder;
import com.luizotg.stock_manager.model.PurchaseOrderStatus;
import com.luizotg.stock_manager.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseOrderDetailDTO>> listAll(
            @PageableDefault(size = 20, sort = "orderDate") Pageable pageable
    ) {
        Page<PurchaseOrderDetailDTO> dtoPage = purchaseOrderService.findAll(pageable)
                .map(PurchaseOrderDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDetailDTO> detail(@PathVariable Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderService.findById(id);
        return ResponseEntity.ok(new PurchaseOrderDetailDTO(purchaseOrder));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<Page<PurchaseOrderDetailDTO>> listBySupplier(
            @PathVariable Long supplierId,
            @PageableDefault(size = 20, sort = "orderDate") Pageable pageable
    ) {
        Page<PurchaseOrderDetailDTO> dtoPage = purchaseOrderService.findBySupplierId(supplierId, pageable)
                .map(PurchaseOrderDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<PurchaseOrderDetailDTO>> listByStatus(
            @PathVariable PurchaseOrderStatus status,
            @PageableDefault(size = 20, sort = "orderDate") Pageable pageable
    ) {
        Page<PurchaseOrderDetailDTO> dtoPage = purchaseOrderService.findByStatus(status, pageable)
                .map(PurchaseOrderDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderDetailDTO> create(@RequestBody @Valid PurchaseOrderCreateDTO dto) {
        PurchaseOrder purchaseOrder = purchaseOrderService.create(dto);
        var uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/purchase-orders/{id}")
                .buildAndExpand(purchaseOrder.getId())
                .toUri();
        return ResponseEntity.created(uri).body(new PurchaseOrderDetailDTO(purchaseOrder));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderDetailDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid PurchaseOrderUpdateDTO dto
    ) {
        PurchaseOrder purchaseOrder = purchaseOrderService.update(id, dto);
        return ResponseEntity.ok(new PurchaseOrderDetailDTO(purchaseOrder));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderDetailDTO> cancel(@PathVariable Long id) {
        purchaseOrderService.cancel(id);
        PurchaseOrder purchaseOrder = purchaseOrderService.findById(id);
        return ResponseEntity.ok(new PurchaseOrderDetailDTO(purchaseOrder));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderDetailDTO> receive(
            @PathVariable Long id,
            @RequestBody @Valid PurchaseOrderReceiveDTO dto
    ) {
        PurchaseOrder purchaseOrder = purchaseOrderService.receive(id, dto);
        return ResponseEntity.ok(new PurchaseOrderDetailDTO(purchaseOrder));
    }

    @GetMapping("/{id}/receipts")
    public ResponseEntity<Page<PurchaseOrderReceiptDetailDTO>> listReceipts(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "receiptDate") Pageable pageable
    ) {
        Page<PurchaseOrderReceiptDetailDTO> dtoPage = purchaseOrderService.findReceiptsByPurchaseOrderId(id, pageable)
                .map(PurchaseOrderReceiptDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping("/{purchaseOrderId}/receipts/{receiptId}/reverse")
    public ResponseEntity<PurchaseOrderReceiptDetailDTO> reverseReceipt(
            @PathVariable Long purchaseOrderId,
            @PathVariable Long receiptId,
            @RequestBody @Valid PurchaseOrderReceiptReverseDTO dto
    ) {
        return ResponseEntity.ok(new PurchaseOrderReceiptDetailDTO(
                purchaseOrderService.reverseReceipt(purchaseOrderId, receiptId, dto)
        ));
    }
}
