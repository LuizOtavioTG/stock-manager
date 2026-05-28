package com.luizotg.stock_manager.repository;

import com.luizotg.stock_manager.model.PurchaseOrderReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderReceiptRepository extends JpaRepository<PurchaseOrderReceipt, Long> {

    Page<PurchaseOrderReceipt> findByPurchaseOrderId(Long purchaseOrderId, Pageable pageable);
}
