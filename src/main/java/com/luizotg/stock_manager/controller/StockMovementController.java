package com.luizotg.stock_manager.controller;


import com.luizotg.stock_manager.dto.stockMovement.StockMovementCreateDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementDetailDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementUpdateDTO;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.service.StockMovementService;
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
@RequestMapping("/stock-movement")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER)")
    @Transactional
    public ResponseEntity<StockMovementDetailDTO> createStockMovement(@RequestBody @Valid StockMovementCreateDTO stockMovementCreateDTO) {
        StockMovement stockMovement = stockMovementService.saveStockMovement(stockMovementCreateDTO);
        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(stockMovement.getId())
                .toUri();
        return ResponseEntity.created(uri).body(new StockMovementDetailDTO(stockMovement));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER, T(com.luizotg.stock_manager.security.Roles).USER)")
    public ResponseEntity<StockMovementDetailDTO> detailStockMovement(@PathVariable Long id) {
        StockMovement stockMovement = stockMovementService.findStockMovementById(id);
        return ResponseEntity.ok(new StockMovementDetailDTO(stockMovement));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(T(com.luizotg.stock_manager.security.Roles).ADMIN)")
    public ResponseEntity<Void> deleteStockMovement(@PathVariable Long id) {
        stockMovementService.deleteStockMovementById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER, T(com.luizotg.stock_manager.security.Roles).USER)")
    public ResponseEntity<Page<StockMovementDetailDTO>> listAllStockMovement(@PageableDefault(size = 20, sort = "movementDate") Pageable pageable) {
        Page<StockMovement> stockMovements = stockMovementService.findAllStockMovements(pageable);
        Page<StockMovementDetailDTO> dtoPage = stockMovements.map(StockMovementDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole(T(com.luizotg.stock_manager.security.Roles).ADMIN, T(com.luizotg.stock_manager.security.Roles).MANAGER)")
    @Transactional
    public ResponseEntity<StockMovementDetailDTO> updateStockMovement(@PathVariable Long id, @RequestBody @Valid StockMovementUpdateDTO stockMovementUpdateDTO) {
        StockMovement stockMovement = stockMovementService.updateStockMovement(id,stockMovementUpdateDTO);
        return ResponseEntity.ok(new StockMovementDetailDTO(stockMovement));
    }


}
