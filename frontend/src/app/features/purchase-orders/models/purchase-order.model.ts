export type PurchaseOrderStatus = 'DRAFT' | 'SENT' | 'PARTIALLY_RECEIVED' | 'RECEIVED' | 'CANCELLED';

export interface PurchaseOrder {
  id: number;
  supplierId: number;
  supplierName: string;
  status: PurchaseOrderStatus;
  orderDate: string | null;
  expectedDeliveryDate: string | null;
  notes: string | null;
  totalEstimatedCost: number;
  items: PurchaseOrderItem[];
  createdAt: string | null;
  updatedAt: string | null;
}

export interface PurchaseOrderItem {
  id: number;
  productId: number;
  productName: string;
  productSku: string | null;
  quantity: number;
  receivedQuantity: number;
  pendingQuantity: number;
  unitCost: number;
  estimatedSubtotal: number;
  notes: string | null;
}
