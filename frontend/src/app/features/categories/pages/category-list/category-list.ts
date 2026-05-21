import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';

import { Page } from '../../../../models/page.model';
import {
  PaginatedListBodyDirective,
  PaginatedListColumn,
  PaginatedListComponent,
  PaginatedListEmptyDirective
} from '../../../../shared/components/paginated-list/paginated-list';
import { Category } from '../../models/category.model';
import { CategoryService } from '../../services/category.service';

const EMPTY_PAGE: Page<Category> = {
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
  selector: 'app-category-list',
  standalone: true,
  imports: [
    ButtonModule,
    DialogModule,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    TableModule,
    TagModule
  ],
  templateUrl: './category-list.html',
  styleUrl: './category-list.scss'
})
export class CategoryListComponent implements OnInit {
  private readonly categoryService = inject(CategoryService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);

  protected readonly columns: PaginatedListColumn[] = [
    { label: 'Nome', field: 'name' },
    { label: 'Descrição', field: 'description' },
    { label: 'Categoria pai', field: 'parent.name' },
    { label: 'Subcategorias', sortable: false, styleClass: 'numeric-column' },
    { label: 'Status', field: 'active' },
    { label: 'Ações', sortable: false, styleClass: 'actions-column' }
  ];

  protected readonly isLoading = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('name,asc');
  protected readonly categoriesPage = signal<Page<Category>>(EMPTY_PAGE);
  protected readonly selectedCategory = signal<Category | null>(null);
  protected readonly isDetailsDialogVisible = signal(false);
  protected readonly isDetailsLoading = signal(false);

  protected readonly categories = computed(() => this.categoriesPage().content);
  protected readonly totalElements = computed(() => this.categoriesPage().totalElements);
  protected readonly first = computed(() => this.categoriesPage().number * this.categoriesPage().size);

  ngOnInit(): void {
    this.loadCategories(0, this.pageSize(), this.sort());
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null; sortField?: string | string[] | null; sortOrder?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);
    const sort = this.resolveSort(event.sortField, event.sortOrder);

    this.pageSize.set(rows);
    this.sort.set(sort);
    this.loadCategories(page, rows, sort);
  }

  protected loadCategories(page: number, size: number, sort = this.sort()): void {
    this.isLoading.set(true);

    this.categoryService
      .listCategories(page, size, sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (categoriesPage) => {
          this.categoriesPage.set(categoriesPage);
          this.isLoading.set(false);
        },
        error: () => {
          this.categoriesPage.set({ ...EMPTY_PAGE, size, number: page });
          this.isLoading.set(false);
          this.showLoadError();
        }
      });
  }

  protected childrenCount(category: Category): number {
    return category.children?.length ?? 0;
  }

  protected childrenSummary(category: Category | null): string {
    const children = category?.children ?? [];

    if (!children.length) {
      return '-';
    }

    return children.map((child) => child.name).join(', ');
  }

  protected openCategoryDetails(category: Category): void {
    this.isDetailsDialogVisible.set(true);
    this.isDetailsLoading.set(true);
    this.selectedCategory.set(null);

    this.categoryService
      .getCategoryById(category.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (categoryDetail) => {
          this.selectedCategory.set(categoryDetail);
          this.isDetailsLoading.set(false);
        },
        error: () => {
          this.isDetailsDialogVisible.set(false);
          this.isDetailsLoading.set(false);
          this.showDetailsLoadError();
        }
      });
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
      summary: 'Erro ao carregar categorias',
      detail: 'Não foi possível buscar as categorias cadastradas. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showDetailsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar categoria',
      detail: 'Não foi possível buscar os detalhes da categoria. Verifique se a API está disponível.',
      life: 5000
    });
  }
}
