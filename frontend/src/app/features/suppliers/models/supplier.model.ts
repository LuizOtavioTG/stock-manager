export interface Supplier {
  id: number;
  name: string;
  contactName: string | null;
  phoneNumber: string | null;
  email: string | null;
  address: string | null;
  active: boolean;
  createdAt: string | null;
  updatedAt: string | null;
}
