package com.luizotg.stock_manager.repository;


import com.luizotg.stock_manager.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
