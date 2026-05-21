import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';

import { Page } from '../../../../models/page.model';
import {
  PaginatedListBodyDirective,
  PaginatedListColumn,
  PaginatedListComponent,
  PaginatedListEmptyDirective
} from '../../../../shared/components/paginated-list/paginated-list';
import { InventoryItem, StockStatus } from '../../models/inventory-item.model';
import { InventoryService } from '../../services/inventory.service';

const EMPTY_PAGE: Page<InventoryItem> = {
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
  selector: 'app-inventory-list',
  imports: [
    ButtonModule,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    TableModule,
    TagModule
  ],
  templateUrl: './inventory-list.html',
  styleUrl: './inventory-list.scss'
})
export class InventoryListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly inventoryService = inject(InventoryService);
  private readonly messageService = inject(MessageService);

  protected readonly columns: PaginatedListColumn[] = [
    { label: 'Produto', field: 'productName' },
    { label: 'Local de estoque', field: 'storageLocationName' },
    { label: 'Quantidade', field: 'quantity', styleClass: 'numeric-column' },
    { label: 'Estoque mínimo', field: 'minimumStock', styleClass: 'numeric-column' },
    { label: 'Ponto de reposição', field: 'reorderPoint', styleClass: 'numeric-column' },
    { label: 'Estoque máximo', field: 'maximumStock', styleClass: 'numeric-column' },
    { label: 'Status', field: 'stockStatus' },
    { label: 'Reposição sugerida', sortable: false, styleClass: 'numeric-column' },
    { label: 'Ações', sortable: false, styleClass: 'actions-column' }
  ];

  protected readonly isLoading = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('id,asc');
  protected readonly inventoryPage = signal<Page<InventoryItem>>(EMPTY_PAGE);

  protected readonly items = computed(() => this.inventoryPage().content);
  protected readonly totalElements = computed(() => this.inventoryPage().totalElements);
  protected readonly first = computed(() => this.inventoryPage().number * this.inventoryPage().size);

  ngOnInit(): void {
    this.loadInventory(0, this.pageSize(), this.sort());
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null; sortField?: string | string[] | null; sortOrder?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);
    const sort = this.resolveSort(event.sortField, event.sortOrder);

    this.pageSize.set(rows);
    this.sort.set(sort);
    this.loadInventory(page, rows, sort);
  }

  protected loadInventory(page: number, size: number, sort = this.sort()): void {
    this.isLoading.set(true);

    this.inventoryService
      .listInventories(page, size, sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (inventoryPage) => {
          this.inventoryPage.set(inventoryPage);
          this.isLoading.set(false);
        },
        error: () => {
          this.inventoryPage.set({ ...EMPTY_PAGE, size, number: page });
          this.isLoading.set(false);
          this.showLoadError();
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

  protected statusLabel(status: StockStatus): string {
    const labels: Record<StockStatus, string> = {
      OUT_OF_STOCK: 'Sem estoque',
      LOW_STOCK: 'Estoque baixo',
      REORDER_NEEDED: 'Reposição necessária',
      OVERSTOCK: 'Estoque excedente',
      NORMAL: 'Normal'
    };

    return labels[status];
  }

  protected statusSeverity(status: StockStatus): 'danger' | 'warn' | 'info' | 'success' | 'secondary' {
    const severities: Record<StockStatus, 'danger' | 'warn' | 'info' | 'success' | 'secondary'> = {
      OUT_OF_STOCK: 'danger',
      LOW_STOCK: 'warn',
      REORDER_NEEDED: 'info',
      OVERSTOCK: 'secondary',
      NORMAL: 'success'
    };

    return severities[status];
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
      summary: 'Erro ao carregar inventário',
      detail: 'Não foi possível buscar os itens de inventário. Verifique se a API está disponível.',
      life: 5000
    });
  }
}
