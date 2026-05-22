import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from '../../../core/api/api.service';
import { Page } from '../../../models/page.model';
import { Product } from '../models/product.model';
import { ProductCreateRequest, ProductUpdateRequest } from '../models/product-request.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly api = inject(ApiService);

  listProducts(page: number, size: number, sort = 'name,asc'): Observable<Page<Product>> {
    return this.api.get<Page<Product>>('product', {
      params: {
        page,
        size,
        sort
      }
    });
  }

  getProductById(id: number): Observable<Product> {
    return this.api.get<Product>(`product/${id}`);
  }

  createProduct(payload: ProductCreateRequest): Observable<Product> {
    return this.api.post<Product, ProductCreateRequest>('product', payload);
  }

  updateProduct(id: number, payload: ProductUpdateRequest): Observable<Product> {
    return this.api.put<Product, ProductUpdateRequest>(`product/${id}`, payload);
  }

  deleteProduct(id: number): Observable<void> {
    return this.api.delete<void>(`product/${id}`);
  }
}
