export interface StockInboundRequest {
  productId: number;
  storageLocationId: number;
  quantity: number;
  reason?: string | null;
  reference?: string | null;
  responsible?: string | null;
  notes?: string | null;
}

export interface StockOutboundRequest {
  productId: number;
  storageLocationId: number;
  quantity: number;
  reason?: string | null;
  reference?: string | null;
  responsible?: string | null;
  notes?: string | null;
}

export interface StockAdjustmentRequest {
  productId: number;
  storageLocationId: number;
  newQuantity: number;
  reason: string;
  responsible?: string | null;
  notes?: string | null;
}
