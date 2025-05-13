package com.luizotg.stock_manager.controller;

import com.luizotg.stock_manager.repository.StockMovementRepository;
import com.luizotg.stock_manager.service.StockMovementService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock-movement")
public class StockMovementController {
 private final StockMovementService stockMovementService;
 public StockMovementController(StockMovementService stockMovementService) {
     this.stockMovementService = stockMovementService;
 }
}
