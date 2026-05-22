import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from '../../../core/api/api.service';
import { Page } from '../../../models/page.model';
import { Supplier } from '../models/supplier.model';
import { SupplierCreateRequest, SupplierUpdateRequest } from '../models/supplier-request.model';

@Injectable({
  providedIn: 'root'
})
export class SupplierService {
  private readonly api = inject(ApiService);

  listSuppliers(page: number, size: number, sort = 'name,asc'): Observable<Page<Supplier>> {
    return this.api.get<Page<Supplier>>('supplier', {
      params: {
        page,
        size,
        sort
      }
    });
  }

  getSupplierById(id: number): Observable<Supplier> {
    return this.api.get<Supplier>(`supplier/${id}`);
  }

  createSupplier(payload: SupplierCreateRequest): Observable<Supplier> {
    return this.api.post<Supplier, SupplierCreateRequest>('supplier', payload);
  }

  updateSupplier(id: number, payload: SupplierUpdateRequest): Observable<Supplier> {
    return this.api.put<Supplier, SupplierUpdateRequest>(`supplier/${id}`, payload);
  }

  deleteSupplier(id: number): Observable<void> {
    return this.api.delete<void>(`supplier/${id}`);
  }
}
