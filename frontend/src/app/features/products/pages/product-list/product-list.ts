import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';

import { Page } from '../../../../models/page.model';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';

const EMPTY_PAGE: Page<Product> = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: true
};

@Component({
  selector: 'app-product-list',
  imports: [ButtonModule, CardModule, CurrencyPipe, DatePipe, TableModule, TagModule],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss'
})
export class ProductListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly productService = inject(ProductService);
  private readonly messageService = inject(MessageService);

  protected readonly isLoading = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('name,asc');
  protected readonly productsPage = signal<Page<Product>>(EMPTY_PAGE);

  protected readonly products = computed(() => this.productsPage().content);
  protected readonly totalElements = computed(() => this.productsPage().totalElements);
  protected readonly first = computed(() => this.productsPage().number * this.productsPage().size);

  ngOnInit(): void {
    this.loadProducts(0, this.pageSize(), this.sort());
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null; sortField?: string | string[] | null; sortOrder?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);
    const sort = this.resolveSort(event.sortField, event.sortOrder);

    this.pageSize.set(rows);
    this.sort.set(sort);
    this.loadProducts(page, rows, sort);
  }

  protected loadProducts(page: number, size: number, sort = this.sort()): void {
    this.isLoading.set(true);

    this.productService
      .listProducts(page, size, sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (productsPage) => {
          this.productsPage.set(productsPage);
          this.isLoading.set(false);
        },
        error: () => {
          this.productsPage.set({ ...EMPTY_PAGE, size, number: page });
          this.isLoading.set(false);
          this.showLoadError();
        }
      });
  }

  protected supplierSummary(product: Product): string {
    const suppliers = product.suppliers ?? [];

    if (!suppliers.length) {
      return '-';
    }

    if (suppliers.length === 1) {
      return suppliers[0].name;
    }

    return `${suppliers[0].name} +${suppliers.length - 1}`;
  }

  protected showComingSoon(action: string): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Em breve',
      detail: `A ação "${action}" será implementada nas próximas etapas.`,
      life: 3000
    });
  }

  private resolveSort(sortField?: string | string[] | null, sortOrder?: number | null): string {
    if (!sortField || Array.isArray(sortField)) {
      return this.sort();
    }

    return `${sortField},${sortOrder === -1 ? 'desc' : 'asc'}`;
  }

  private showLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar produtos',
      detail: 'Não foi possível buscar os produtos cadastrados. Verifique se a API está disponível.',
      life: 5000
    });
  }
}
