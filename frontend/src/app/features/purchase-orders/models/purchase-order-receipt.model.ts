export type PurchaseOrderReceiptStatus = 'ACTIVE' | 'REVERSED';

export interface PurchaseOrderReceipt {
  id: number;
  purchaseOrderId: number;
  storageLocationId: number;
  storageLocationName: string | null;
  receiptDate: string | null;
  notes: string | null;
  status: PurchaseOrderReceiptStatus;
  reversedAt: string | null;
  reversalReason: string | null;
  items: PurchaseOrderReceiptItem[];
  createdAt: string | null;
  updatedAt: string | null;
}

export interface PurchaseOrderReceiptItem {
  id: number;
  purchaseOrderItemId: number;
  productId: number;
  productName: string | null;
  productSku: string | null;
  receivedQuantity: number;
}
