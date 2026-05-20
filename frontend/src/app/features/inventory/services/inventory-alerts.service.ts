import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from '../../../core/api/api.service';
import { InventoryAlertsSummary } from '../models/inventory-alerts-summary.model';

@Injectable({
  providedIn: 'root'
})
export class InventoryAlertsService {
  private readonly api = inject(ApiService);

  getSummary(): Observable<InventoryAlertsSummary> {
    return this.api.get<InventoryAlertsSummary>('inventory/alerts/summary');
  }
}
