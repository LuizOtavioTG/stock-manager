package com.luizotg.stock_manager.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "PurchaseOrderReceipt")
@Table(name = "purchase_order_receipt")
@NoArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class PurchaseOrderReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "storage_location_id", nullable = false)
    private StorageLocation storageLocation;

    @Column(nullable = false)
    private LocalDateTime receiptDate;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderReceiptItem> items = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public PurchaseOrderReceipt(
            PurchaseOrder purchaseOrder,
            StorageLocation storageLocation,
            String notes,
            List<PurchaseOrderReceiptItem> items
    ) {
        this.purchaseOrder = purchaseOrder;
        this.storageLocation = storageLocation;
        this.notes = notes;
        this.receiptDate = LocalDateTime.now();
        replaceItems(items);
    }

    @PrePersist
    void prePersist() {
        if (receiptDate == null) {
            receiptDate = LocalDateTime.now();
        }
    }

    private void replaceItems(List<PurchaseOrderReceiptItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            newItems.forEach(this::addItem);
        }
    }

    private void addItem(PurchaseOrderReceiptItem item) {
        item.setReceipt(this);
        this.items.add(item);
    }
}
