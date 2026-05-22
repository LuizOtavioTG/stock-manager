import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from '../../../core/api/api.service';
import { Page } from '../../../models/page.model';
import { MovementType, StockMovement } from '../models/stock-movement.model';
import {
  StockAdjustmentRequest,
  StockInboundRequest,
  StockOutboundRequest
} from '../models/stock-movement-request.model';

@Injectable({
  providedIn: 'root'
})
export class StockMovementService {
  private readonly api = inject(ApiService);

  listMovements(page: number, size: number, sort = 'movementDate,desc'): Observable<Page<StockMovement>> {
    return this.api.get<Page<StockMovement>>('api/stock-movements', {
      params: {
        page,
        size,
        sort
      }
    });
  }

  listMovementsByProduct(
    productId: number,
    page: number,
    size: number,
    sort = 'movementDate,desc'
  ): Observable<Page<StockMovement>> {
    return this.api.get<Page<StockMovement>>(`api/stock-movements/product/${productId}`, {
      params: {
        page,
        size,
        sort
      }
    });
  }

  listMovementsByStorageLocation(
    storageLocationId: number,
    page: number,
    size: number,
    sort = 'movementDate,desc'
  ): Observable<Page<StockMovement>> {
    return this.api.get<Page<StockMovement>>(`api/stock-movements/location/${storageLocationId}`, {
      params: {
        page,
        size,
        sort
      }
    });
  }

  listMovementsByType(
    movementType: MovementType,
    page: number,
    size: number,
    sort = 'movementDate,desc'
  ): Observable<Page<StockMovement>> {
    return this.api.get<Page<StockMovement>>(`api/stock-movements/type/${movementType}`, {
      params: {
        page,
        size,
        sort
      }
    });
  }

  getMovementById(id: number): Observable<StockMovement> {
    return this.api.get<StockMovement>(`api/stock-movements/${id}`);
  }

  createInbound(payload: StockInboundRequest): Observable<StockMovement> {
    return this.api.post<StockMovement, StockInboundRequest>('api/stock-movements/inbound', payload);
  }

  createOutbound(payload: StockOutboundRequest): Observable<StockMovement> {
    return this.api.post<StockMovement, StockOutboundRequest>('api/stock-movements/outbound', payload);
  }

  createAdjustment(payload: StockAdjustmentRequest): Observable<StockMovement> {
    return this.api.post<StockMovement, StockAdjustmentRequest>('api/stock-movements/adjustment', payload);
  }
}
