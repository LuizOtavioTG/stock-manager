export interface StorageLocation {
  id: number;
  name: string;
  type: string | null;
  address: string | null;
  phoneNumber: string | null;
  email: string | null;
  responsibleName: string | null;
  notes: string | null;
  capacity: number | null;
  defaultLocation: boolean;
  latitude: number | null;
  longitude: number | null;
  createdAt: string | null;
  updatedAt: string | null;
}
