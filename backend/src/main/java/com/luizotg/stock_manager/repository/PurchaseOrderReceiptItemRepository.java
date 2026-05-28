package com.luizotg.stock_manager.repository;

import com.luizotg.stock_manager.model.PurchaseOrderReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderReceiptItemRepository extends JpaRepository<PurchaseOrderReceiptItem, Long> {
}
