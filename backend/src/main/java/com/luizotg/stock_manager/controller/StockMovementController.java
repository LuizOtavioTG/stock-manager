package com.luizotg.stock_manager.controller;


import com.luizotg.stock_manager.dto.stockMovement.StockAdjustmentRequestDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockInboundRequestDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockMovementDetailDTO;
import com.luizotg.stock_manager.dto.stockMovement.StockOutboundRequestDTO;
import com.luizotg.stock_manager.model.MovementType;
import com.luizotg.stock_manager.model.StockMovement;
import com.luizotg.stock_manager.service.StockMovementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PostMapping("/inbound")
    public ResponseEntity<StockMovementDetailDTO> createInboundMovement(@RequestBody @Valid StockInboundRequestDTO dto) {
        StockMovement stockMovement = stockMovementService.registerInbound(dto);
        var uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/stock-movements/{id}")
                .buildAndExpand(stockMovement.getId())
                .toUri();
        return ResponseEntity.created(uri).body(new StockMovementDetailDTO(stockMovement));
    }

    @PostMapping("/outbound")
    public ResponseEntity<StockMovementDetailDTO> createOutboundMovement(@RequestBody @Valid StockOutboundRequestDTO dto) {
        StockMovement stockMovement = stockMovementService.registerOutbound(dto);
        var uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/stock-movements/{id}")
                .buildAndExpand(stockMovement.getId())
                .toUri();
        return ResponseEntity.created(uri).body(new StockMovementDetailDTO(stockMovement));
    }

    @PostMapping("/adjustment")
    public ResponseEntity<StockMovementDetailDTO> createAdjustmentMovement(@RequestBody @Valid StockAdjustmentRequestDTO dto) {
        StockMovement stockMovement = stockMovementService.registerAdjustment(dto);
        var uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/stock-movements/{id}")
                .buildAndExpand(stockMovement.getId())
                .toUri();
        return ResponseEntity.created(uri).body(new StockMovementDetailDTO(stockMovement));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockMovementDetailDTO> detailStockMovement(@PathVariable Long id) {
        StockMovement stockMovement = stockMovementService.findStockMovementById(id);
        return ResponseEntity.ok(new StockMovementDetailDTO(stockMovement));
    }

    @GetMapping
    public ResponseEntity<Page<StockMovementDetailDTO>> listAllStockMovement(@PageableDefault(size = 20, sort = "movementDate") Pageable pageable) {
        Page<StockMovement> stockMovements = stockMovementService.findAllStockMovements(pageable);
        Page<StockMovementDetailDTO> dtoPage = stockMovements.map(StockMovementDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<StockMovementDetailDTO>> listStockMovementsByProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "movementDate") Pageable pageable) {
        Page<StockMovement> stockMovements = stockMovementService.findStockMovementsByProductId(productId, pageable);
        Page<StockMovementDetailDTO> dtoPage = stockMovements.map(StockMovementDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/location/{storageLocationId}")
    public ResponseEntity<Page<StockMovementDetailDTO>> listStockMovementsByStorageLocation(
            @PathVariable Long storageLocationId,
            @PageableDefault(size = 20, sort = "movementDate") Pageable pageable) {
        Page<StockMovement> stockMovements = stockMovementService.findStockMovementsByStorageLocationId(storageLocationId, pageable);
        Page<StockMovementDetailDTO> dtoPage = stockMovements.map(StockMovementDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/type/{movementType}")
    public ResponseEntity<Page<StockMovementDetailDTO>> listStockMovementsByType(
            @PathVariable MovementType movementType,
            @PageableDefault(size = 20, sort = "movementDate") Pageable pageable) {
        Page<StockMovement> stockMovements = stockMovementService.findStockMovementsByMovementType(movementType, pageable);
        Page<StockMovementDetailDTO> dtoPage = stockMovements.map(StockMovementDetailDTO::new);
        return ResponseEntity.ok(dtoPage);
    }
}
