import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiQueryParams, ApiService } from '../../../core/api/api.service';
import { Page } from '../../../models/page.model';
import { InventoryAlertItem } from '../models/inventory-alert-item.model';
import { InventoryAlertsSummary } from '../models/inventory-alerts-summary.model';

export type InventoryAlertType = 'out-of-stock' | 'low-stock' | 'reorder-needed' | 'overstock';

export interface InventoryAlertsPageRequest {
  page: number;
  size: number;
  sort?: string;
  productId?: number | null;
  storageLocationId?: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class InventoryAlertsService {
  private readonly api = inject(ApiService);

  getSummary(): Observable<InventoryAlertsSummary> {
    return this.api.get<InventoryAlertsSummary>('inventory/alerts/summary');
  }

  getAlerts(type: InventoryAlertType, request: InventoryAlertsPageRequest): Observable<Page<InventoryAlertItem>> {
    return this.api.get<Page<InventoryAlertItem>>(`inventory/${type}`, {
      params: this.pageParams(request)
    });
  }

  getReorderNeeded(page: number, size: number, sort = 'id,asc'): Observable<Page<InventoryAlertItem>> {
    return this.getAlerts('reorder-needed', { page, size, sort });
  }

  private pageParams(request: InventoryAlertsPageRequest): ApiQueryParams {
    return {
      page: request.page,
      size: request.size,
      sort: request.sort ?? 'id,asc',
      productId: request.productId,
      storageLocationId: request.storageLocationId
    };
  }
}
