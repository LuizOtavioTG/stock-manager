export interface InventoryCreateRequest {
  productId: number;
  storageLocationId: number;
  quantity: number;
  minimumStock: number;
  maximumStock?: number | null;
  reorderPoint?: number | null;
}

export interface InventoryUpdateRequest {
  storageLocationId?: number | null;
  minimumStock?: number | null;
  maximumStock?: number | null;
  reorderPoint?: number | null;
}
