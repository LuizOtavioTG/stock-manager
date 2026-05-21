export type StockStatus = 'OUT_OF_STOCK' | 'LOW_STOCK' | 'REORDER_NEEDED' | 'NORMAL' | 'OVERSTOCK';

export interface InventoryAlertItem {
  id: number;
  productId: number | null;
  productName: string | null;
  storageLocationId: number | null;
  storageLocationName: string | null;
  quantity: number;
  minimumStock: number;
  maximumStock: number;
  reorderPoint: number;
  stockStatus: StockStatus;
  lowStock: boolean;
  reorderNeeded: boolean;
  suggestedReorderQuantity: number;
}
