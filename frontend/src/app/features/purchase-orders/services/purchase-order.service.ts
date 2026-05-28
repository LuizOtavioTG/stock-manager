import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from '../../../core/api/api.service';
import { Page } from '../../../models/page.model';
import { PurchaseOrder, PurchaseOrderStatus } from '../models/purchase-order.model';
import { PurchaseOrderReceipt } from '../models/purchase-order-receipt.model';
import {
  PurchaseOrderCreateRequest,
  PurchaseOrderReceiveRequest,
  PurchaseOrderUpdateRequest
} from '../models/purchase-order-request.model';

@Injectable({
  providedIn: 'root'
})
export class PurchaseOrderService {
  private readonly api = inject(ApiService);

  listPurchaseOrders(page: number, size: number, sort = 'orderDate,desc'): Observable<Page<PurchaseOrder>> {
    return this.api.get<Page<PurchaseOrder>>('api/purchase-orders', {
      params: { page, size, sort }
    });
  }

  getPurchaseOrderById(id: number): Observable<PurchaseOrder> {
    return this.api.get<PurchaseOrder>(`api/purchase-orders/${id}`);
  }

  listBySupplier(supplierId: number, page: number, size: number, sort = 'orderDate,desc'): Observable<Page<PurchaseOrder>> {
    return this.api.get<Page<PurchaseOrder>>(`api/purchase-orders/supplier/${supplierId}`, {
      params: { page, size, sort }
    });
  }

  listByStatus(status: PurchaseOrderStatus, page: number, size: number, sort = 'orderDate,desc'): Observable<Page<PurchaseOrder>> {
    return this.api.get<Page<PurchaseOrder>>(`api/purchase-orders/status/${status}`, {
      params: { page, size, sort }
    });
  }

  createPurchaseOrder(payload: PurchaseOrderCreateRequest): Observable<PurchaseOrder> {
    return this.api.post<PurchaseOrder, PurchaseOrderCreateRequest>('api/purchase-orders', payload);
  }

  updatePurchaseOrder(id: number, payload: PurchaseOrderUpdateRequest): Observable<PurchaseOrder> {
    return this.api.put<PurchaseOrder, PurchaseOrderUpdateRequest>(`api/purchase-orders/${id}`, payload);
  }

  cancelPurchaseOrder(id: number): Observable<PurchaseOrder> {
    return this.api.patch<PurchaseOrder, Record<string, never>>(`api/purchase-orders/${id}/cancel`, {});
  }

  receivePurchaseOrder(id: number, payload: PurchaseOrderReceiveRequest): Observable<PurchaseOrder> {
    return this.api.post<PurchaseOrder, PurchaseOrderReceiveRequest>(`api/purchase-orders/${id}/receive`, payload);
  }

  listReceipts(purchaseOrderId: number, page: number, size: number, sort = 'receiptDate,desc'): Observable<Page<PurchaseOrderReceipt>> {
    return this.api.get<Page<PurchaseOrderReceipt>>(`api/purchase-orders/${purchaseOrderId}/receipts`, {
      params: { page, size, sort }
    });
  }
}
