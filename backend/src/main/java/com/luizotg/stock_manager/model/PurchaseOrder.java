package com.luizotg.stock_manager.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "PurchaseOrder")
@Table(name = "purchase_order")
@NoArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Double totalEstimatedCost = 0.0;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

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

    public PurchaseOrder(
            Supplier supplier,
            PurchaseOrderStatus status,
            LocalDate expectedDeliveryDate,
            String notes,
            List<PurchaseOrderItem> items
    ) {
        this.supplier = supplier;
        this.status = status != null ? status : PurchaseOrderStatus.DRAFT;
        this.orderDate = LocalDate.now();
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.notes = notes;
        replaceItems(items);
    }

    @PrePersist
    void prePersist() {
        if (orderDate == null) {
            orderDate = LocalDate.now();
        }
        if (status == null) {
            status = PurchaseOrderStatus.DRAFT;
        }
        if (totalEstimatedCost == null) {
            recalculateTotal();
        }
    }

    public boolean isCancelled() {
        return PurchaseOrderStatus.CANCELLED.equals(status);
    }

    public void update(LocalDate expectedDeliveryDate, String notes, PurchaseOrderStatus status, List<PurchaseOrderItem> items) {
        if (expectedDeliveryDate != null) {
            this.expectedDeliveryDate = expectedDeliveryDate;
        }
        if (notes != null) {
            this.notes = notes;
        }
        if (status != null) {
            this.status = status;
        }
        replaceItems(items);
    }

    public void cancel() {
        this.status = PurchaseOrderStatus.CANCELLED;
    }

    public void replaceItems(List<PurchaseOrderItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            newItems.forEach(this::addItem);
        }
        recalculateTotal();
    }

    private void addItem(PurchaseOrderItem item) {
        item.setPurchaseOrder(this);
        item.recalculateSubtotal();
        this.items.add(item);
    }

    public void recalculateTotal() {
        this.totalEstimatedCost = items.stream()
                .map(PurchaseOrderItem::getEstimatedSubtotal)
                .filter(subtotal -> subtotal != null)
                .reduce(0.0, Double::sum);
    }
}
