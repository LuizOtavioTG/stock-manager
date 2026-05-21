export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  brand: string | null;
  unitOfMeasure: string | null;
  costPrice: number | null;
  salePrice: number | null;
  active: boolean;
  expirationDate: string | null;
  category: ProductCategory | null;
  suppliers: ProductSupplier[] | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface ProductCategory {
  id: number;
  name: string;
  active?: boolean;
}

export interface ProductSupplier {
  id: number;
  name: string;
  active?: boolean;
}
