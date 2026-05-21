export type StockStatus = 'OUT_OF_STOCK' | 'LOW_STOCK' | 'REORDER_NEEDED' | 'OVERSTOCK' | 'NORMAL';

export interface InventoryItem {
  id: number;
  productId: number | null;
  productName: string | null;
  storageLocationId: number | null;
  storageLocationName: string | null;
  quantity: number;
  minimumStock: number;
  maximumStock: number | null;
  reorderPoint: number;
  stockStatus: StockStatus;
  lowStock: boolean;
  reorderNeeded: boolean;
  suggestedReorderQuantity: number;
}
