import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from '../../../core/api/api.service';
import { Page } from '../../../models/page.model';
import { Category } from '../models/category.model';

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
}
