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
    public ResponseEntity<SupplierDetailDTO> detailSupplier(@PathVariable Long id) {
        Supplier supplier = supplierService.findSupplierById(id);
        return ResponseEntity.ok(new SupplierDetailDTO(supplier));
    }

    @GetMapping
    public ResponseEntity<Page<SupplierDetailDTO>> listSuppliers(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<Supplier> suppliers = supplierService.findAllSuppliers(pageable);
        Page<SupplierDetailDTO> dtoPage = suppliers.map(SupplierDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<SupplierDetailDTO> updateSupplier(
            @PathVariable Long id,
            @RequestBody @Valid SupplierUpdateDTO dto
    ) {
        Supplier supplier = supplierService.updateSupplier(id, dto);
        return ResponseEntity.ok(new SupplierDetailDTO(supplier));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplierById(id);
        return ResponseEntity.noContent().build();
    }


}
