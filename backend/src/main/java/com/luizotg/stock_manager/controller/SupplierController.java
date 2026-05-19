package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.dto.supplier.SupplierCreateDTO;
import com.luizotg.stock_manager.dto.supplier.SupplierDetailDTO;
import com.luizotg.stock_manager.dto.supplier.SupplierUpdateDTO;
import com.luizotg.stock_manager.model.Supplier;
import com.luizotg.stock_manager.service.SupplierService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/supplier")
public class SupplierController {
    
    private final SupplierService supplierService;
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER)")
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

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER, T(com.luizotg.stock_manager.security.Roles).USER)")
    public ResponseEntity<SupplierDetailDTO> detailSupplier(@PathVariable Long id) {
        Supplier supplier = supplierService.findSupplierById(id);
        return ResponseEntity.ok(new SupplierDetailDTO(supplier));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER, T(com.luizotg.stock_manager.security.Roles).USER)")
    public ResponseEntity<Page<SupplierDetailDTO>> listSuppliers(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<Supplier> suppliers = supplierService.findAllSuppliers(pageable);
        Page<SupplierDetailDTO> dtoPage = suppliers.map(SupplierDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER)")
    @Transactional
    public ResponseEntity<SupplierDetailDTO> updateSupplier(
            @PathVariable Long id,
            @RequestBody @Valid SupplierUpdateDTO dto
    ) {
        Supplier supplier = supplierService.updateSupplier(id, dto);
        return ResponseEntity.ok(new SupplierDetailDTO(supplier));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(T(com.luizotg.stock_manager.security.Roles).ADMIN)")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplierById(id);
        return ResponseEntity.noContent().build();
    }


}
