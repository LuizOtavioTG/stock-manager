import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from '../../../core/api/api.service';
import { Page } from '../../../models/page.model';
import { Category } from '../models/category.model';
import { CategoryCreateRequest, CategoryUpdateRequest } from '../models/category-request.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private readonly api = inject(ApiService);

  listCategories(page: number, size: number, sort = 'name,asc'): Observable<Page<Category>> {
    return this.api.get<Page<Category>>('category', {
      params: {
        page,
        size,
        sort
      }
    });
  }

  getCategoryById(id: number): Observable<Category> {
    return this.api.get<Category>(`category/${id}`);
  }

  createCategory(payload: CategoryCreateRequest): Observable<Category> {
    return this.api.post<Category, CategoryCreateRequest>('category', payload);
  }

  updateCategory(id: number, payload: CategoryUpdateRequest): Observable<Category> {
    return this.api.put<Category, CategoryUpdateRequest>(`category/${id}`, payload);
  }

  deleteCategory(id: number): Observable<void> {
    return this.api.delete<void>(`category/${id}`);
  }
}
