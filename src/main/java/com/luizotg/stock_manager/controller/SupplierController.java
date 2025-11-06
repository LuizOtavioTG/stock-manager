package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.dto.supplier.SupplierCreateDTO;
import com.luizotg.stock_manager.dto.supplier.SupplierDetailDTO;
import com.luizotg.stock_manager.model.Supplier;
import com.luizotg.stock_manager.service.SupplierService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/supplier")
public class SupplierController {
    
    private final SupplierService supplierService;
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<SupplierDetailDTO> createSupplier(
            @RequestBody @Valid SupplierCreateDTO dto
    ) {
        Supplier supplier = supplierService.saveSupplier(dto);
        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(supplier.getId())
                .toUri();
        return ResponseEntity.created(uri).body(new SupplierDetailDTO(supplier));
    }


}
