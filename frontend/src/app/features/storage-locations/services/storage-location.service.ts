import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from '../../../core/api/api.service';
import { Page } from '../../../models/page.model';
import { StorageLocation } from '../models/storage-location.model';

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
}
