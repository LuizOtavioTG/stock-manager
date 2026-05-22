export interface SupplierCreateRequest {
  name: string;
  contactName?: string | null;
  phoneNumber?: string | null;
  email?: string | null;
  address?: string | null;
}

export interface SupplierUpdateRequest {
  name: string;
  contactName?: string | null;
  phoneNumber?: string | null;
  email?: string | null;
  address?: string | null;
  active?: boolean | null;
}
