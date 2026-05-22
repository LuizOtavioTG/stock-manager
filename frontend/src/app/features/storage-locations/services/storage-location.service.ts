import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from '../../../core/api/api.service';
import { Page } from '../../../models/page.model';
import { StorageLocation } from '../models/storage-location.model';
import {
  StorageLocationCreateRequest,
  StorageLocationUpdateRequest
} from '../models/storage-location-request.model';

@Injectable({
  providedIn: 'root'
})
export class StorageLocationService {
  private readonly api = inject(ApiService);

  listStorageLocations(page: number, size: number, sort = 'id,asc'): Observable<Page<StorageLocation>> {
    return this.api.get<Page<StorageLocation>>('storage-location', {
      params: {
        page,
        size,
        sort
      }
    });
  }

  getStorageLocationById(id: number): Observable<StorageLocation> {
    return this.api.get<StorageLocation>(`storage-location/${id}`);
  }

  createStorageLocation(payload: StorageLocationCreateRequest): Observable<StorageLocation> {
    return this.api.post<StorageLocation, StorageLocationCreateRequest>('storage-location', payload);
  }

  updateStorageLocation(id: number, payload: StorageLocationUpdateRequest): Observable<StorageLocation> {
    return this.api.put<StorageLocation, StorageLocationUpdateRequest>(`storage-location/${id}`, payload);
  }

  deleteStorageLocation(id: number): Observable<void> {
    return this.api.delete<void>(`storage-location/${id}`);
  }
}
