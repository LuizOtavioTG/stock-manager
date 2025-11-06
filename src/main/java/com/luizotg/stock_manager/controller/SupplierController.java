package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.dto.supplier.SupplierCreateDTO;
import com.luizotg.stock_manager.dto.supplier.SupplierDetailDTO;
import com.luizotg.stock_manager.model.Supplier;
import com.luizotg.stock_manager.service.SupplierService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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


}
