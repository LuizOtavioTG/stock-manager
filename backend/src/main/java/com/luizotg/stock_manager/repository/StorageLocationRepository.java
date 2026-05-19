package com.luizotg.stock_manager.repository;

import com.luizotg.stock_manager.model.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {
}
