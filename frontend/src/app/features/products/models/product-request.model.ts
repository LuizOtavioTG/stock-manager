export interface ProductCreateRequest {
  sku: string;
  name: string;
  description?: string | null;
  brand: string;
  categoryId: number;
  unitOfMeasure: string;
  costPrice: number;
  salePrice: number;
  expirationDate?: string | null;
  supplierIds: number[];
}

export interface ProductUpdateRequest {
  sku?: string | null;
  name?: string | null;
  description?: string | null;
  brand?: string | null;
  categoryId?: number | null;
  unitOfMeasure?: string | null;
  costPrice?: number | null;
  salePrice?: number | null;
  active?: boolean | null;
  expirationDate?: string | null;
  supplierIds?: number[] | null;
}
