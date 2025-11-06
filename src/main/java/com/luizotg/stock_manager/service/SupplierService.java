package com.luizotg.stock_manager.service;

import com.luizotg.stock_manager.dto.supplier.SupplierCreateDTO;
import com.luizotg.stock_manager.model.Supplier;
import com.luizotg.stock_manager.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Page<Supplier> findAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }

    public Supplier saveSupplier(SupplierCreateDTO dto) {
        Supplier supplier = new Supplier(dto);
        return supplierRepository.save(supplier);
    }

    public Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Fornecedor com ID " + id + " não encontrado."
                ));
    }

    public void deleteSupplierById(Long id) {
        Supplier supplier = findSupplierById(id);
        supplierRepository.delete(supplier);
    }
}
