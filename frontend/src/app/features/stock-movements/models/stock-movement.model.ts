export type MovementType =
  | 'INBOUND'
  | 'OUTBOUND'
  | 'ADJUSTMENT'
  | 'INITIAL_BALANCE'
  | 'RETURN'
  | 'LOSS'
  | 'DAMAGED'
  | 'TRANSFER';

export interface StockMovement {
  id: number;
  product: {
    id: number;
    sku?: string | null;
    name: string;
  } | null;
  storageLocation: {
    id: number;
    name: string;
  } | null;
  quantity: number;
  movementType: MovementType;
  reason: string | null;
  movementDate: string | null;
  reference: string | null;
  responsible: string | null;
  notes: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}
