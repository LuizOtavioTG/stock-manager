package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.supplier.SupplierCreateDTO;
import com.luizotg.stock_manager.model.Supplier;
import com.luizotg.stock_manager.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier saveSupplier(SupplierCreateDTO dto) {
        Supplier supplier = new Supplier(dto);
        return supplierRepository.save(supplier);
    }

    public void deleteSupplierById(Long id) {
        supplierRepository.deleteById(id);
    }
}
