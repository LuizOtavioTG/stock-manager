export interface StorageLocationCreateRequest {
  name: string;
  type: string;
  address?: string | null;
  phoneNumber?: string | null;
  email?: string | null;
  responsibleName?: string | null;
  notes?: string | null;
  capacity?: number | null;
  defaultLocation: boolean;
  latitude?: number | null;
  longitude?: number | null;
}

export interface StorageLocationUpdateRequest {
  name: string;
  type: string;
  address?: string | null;
  phoneNumber?: string | null;
  email?: string | null;
  responsibleName?: string | null;
  notes?: string | null;
  capacity?: number | null;
  defaultLocation: boolean;
  latitude?: number | null;
  longitude?: number | null;
}
