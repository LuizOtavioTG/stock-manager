import { PurchaseOrderStatus } from './purchase-order.model';

export interface PurchaseOrderCreateRequest {
  supplierId: number;
  status?: PurchaseOrderStatus | null;
  expectedDeliveryDate?: string | null;
  notes?: string | null;
  items: PurchaseOrderItemRequest[];
}

export interface PurchaseOrderUpdateRequest {
  status?: PurchaseOrderStatus | null;
  expectedDeliveryDate?: string | null;
  notes?: string | null;
  items: PurchaseOrderItemRequest[];
}

export interface PurchaseOrderItemRequest {
  productId: number;
  quantity: number;
  unitCost: number;
  notes?: string | null;
}

export interface PurchaseOrderReceiveRequest {
  storageLocationId: number;
  items: PurchaseOrderReceiveItemRequest[];
  notes?: string | null;
}

export interface PurchaseOrderReceiveItemRequest {
  purchaseOrderItemId: number;
  receivedQuantity: number;
}
