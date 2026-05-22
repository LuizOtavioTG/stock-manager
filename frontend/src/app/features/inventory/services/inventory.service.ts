import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiQueryParams, ApiService } from '../../../core/api/api.service';
import { Page } from '../../../models/page.model';
import { InventoryItem } from '../models/inventory-item.model';
import { InventoryCreateRequest, InventoryUpdateRequest } from '../models/inventory-request.model';

export interface InventoryPageRequest {
  page: number;
  size: number;
  sort?: string;
}

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private readonly api = inject(ApiService);

  listInventories(page: number, size: number, sort?: string): Observable<Page<InventoryItem>> {
    return this.api.get<Page<InventoryItem>>('inventory', {
      params: this.pageParams({ page, size, sort })
    });
  }

  list(request: InventoryPageRequest): Observable<Page<InventoryItem>> {
    return this.listInventories(request.page, request.size, request.sort);
  }

  getInventoryById(id: number): Observable<InventoryItem> {
    return this.api.get<InventoryItem>(`inventory/${id}`);
  }

  createInventory(payload: InventoryCreateRequest): Observable<InventoryItem> {
    return this.api.post<InventoryItem, InventoryCreateRequest>('inventory', payload);
  }

  updateInventory(id: number, payload: InventoryUpdateRequest): Observable<InventoryItem> {
    return this.api.put<InventoryItem, InventoryUpdateRequest>(`inventory/${id}`, payload);
  }

  private pageParams(request: InventoryPageRequest): ApiQueryParams {
    return {
      page: request.page,
      size: request.size,
      sort: request.sort ?? 'id,asc'
    };
  }
}
