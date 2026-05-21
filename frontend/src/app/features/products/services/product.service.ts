import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiQueryParams, ApiService } from '../../../core/api/api.service';
import { Page } from '../../../models/page.model';
import { Product } from '../models/product.model';

export interface ProductPageRequest {
  page: number;
  size: number;
  sort?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly api = inject(ApiService);

  listProducts(page: number, size: number, sort?: string): Observable<Page<Product>> {
    return this.api.get<Page<Product>>('product', {
      params: this.pageParams({ page, size, sort })
    });
  }

  list(request: ProductPageRequest): Observable<Page<Product>> {
    return this.listProducts(request.page, request.size, request.sort);
  }

  private pageParams(request: ProductPageRequest): ApiQueryParams {
    return {
      page: request.page,
      size: request.size,
      sort: request.sort ?? 'name,asc'
    };
  }
}
