import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';

import { Page } from '../../../../models/page.model';
import {
  PaginatedListBodyDirective,
  PaginatedListColumn,
  PaginatedListComponent,
  PaginatedListEmptyDirective
} from '../../../../shared/components/paginated-list/paginated-list';
import { InventoryAlertItem, StockStatus } from '../../../inventory/models/inventory-alert-item.model';
import { InventoryAlertsService } from '../../../inventory/services/inventory-alerts.service';
import { Product } from '../../../products/models/product.model';
import { ProductService } from '../../../products/services/product.service';
import { StorageLocation } from '../../../storage-locations/models/storage-location.model';
import { StorageLocationService } from '../../../storage-locations/services/storage-location.service';

interface ProductOption {
  id: number;
  label: string;
}

interface SummaryCard {
  title: string;
  value: number;
  icon: string;
}

const EMPTY_PAGE: Page<InventoryAlertItem> = {
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
  selector: 'app-reorder-suggestion-report',
  imports: [
    ButtonModule,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    ReactiveFormsModule,
    SelectModule,
    TableModule,
    TagModule
  ],
  templateUrl: './reorder-suggestion-report.html',
  styleUrl: './reorder-suggestion-report.scss'
})
export class ReorderSuggestionReportComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly inventoryAlertsService = inject(InventoryAlertsService);
  private readonly messageService = inject(MessageService);
  private readonly productService = inject(ProductService);
  private readonly storageLocationService = inject(StorageLocationService);

  protected readonly columns: PaginatedListColumn[] = [
    { label: 'Produto', field: 'productName' },
    { label: 'Local de estoque', field: 'storageLocationName' },
    { label: 'Quantidade atual', field: 'quantity', styleClass: 'numeric-column' },
    { label: 'Estoque mínimo', field: 'minimumStock', styleClass: 'numeric-column' },
    { label: 'Ponto de reposição', field: 'reorderPoint', styleClass: 'numeric-column' },
    { label: 'Estoque máximo', field: 'maximumStock', styleClass: 'numeric-column' },
    { label: 'Quantidade sugerida', sortable: false, styleClass: 'numeric-column' },
    { label: 'Status', field: 'stockStatus' }
  ];

  protected readonly isLoading = signal(false);
  protected readonly isLoadingProductOptions = signal(false);
  protected readonly isLoadingStorageLocationOptions = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('id,asc');
  protected readonly reportPage = signal<Page<InventoryAlertItem>>(EMPTY_PAGE);
  protected readonly productOptions = signal<ProductOption[]>([]);
  protected readonly storageLocationOptions = signal<StorageLocation[]>([]);

  protected readonly items = computed(() => this.reportPage().content);
  protected readonly totalElements = computed(() => this.reportPage().totalElements);
  protected readonly first = computed(() => this.reportPage().number * this.reportPage().size);
  protected readonly summaryCards = computed<SummaryCard[]>(() => {
    const items = this.items();

    return [
      {
        title: 'Total de itens para reposição',
        value: items.length,
        icon: 'pi pi-list'
      },
      {
        title: 'Soma das quantidades sugeridas',
        value: items.reduce((total, item) => total + item.suggestedReorderQuantity, 0),
        icon: 'pi pi-shopping-cart'
      },
      {
        title: 'Itens sem estoque',
        value: items.filter((item) => item.stockStatus === 'OUT_OF_STOCK').length,
        icon: 'pi pi-times-circle'
      },
      {
        title: 'Itens em estoque baixo',
        value: items.filter((item) => item.stockStatus === 'LOW_STOCK').length,
        icon: 'pi pi-exclamation-triangle'
      }
    ];
  });

  protected readonly filterForm = this.formBuilder.group({
    productId: this.formBuilder.control<number | null>(null),
    storageLocationId: this.formBuilder.control<number | null>(null)
  });

  ngOnInit(): void {
    this.loadOptions();
    this.loadReport(0, this.pageSize(), this.sort());
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null; sortField?: string | string[] | null; sortOrder?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);
    const sort = this.resolveSort(event.sortField, event.sortOrder);

    this.pageSize.set(rows);
    this.sort.set(sort);
    this.loadReport(page, rows, sort);
  }

  protected applyFilters(): void {
    this.loadReport(0, this.pageSize(), this.sort());
  }

  protected clearFilters(): void {
    this.filterForm.reset({
      productId: null,
      storageLocationId: null
    });
    this.loadReport(0, this.pageSize(), this.sort());
  }

  protected loadReport(page: number, size: number, sort = this.sort()): void {
    const filters = this.filterForm.getRawValue();

    this.isLoading.set(true);
    this.inventoryAlertsService
      .getAlerts('reorder-needed', {
        page,
        size,
        sort,
        productId: filters.productId,
        storageLocationId: filters.storageLocationId
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (reportPage) => {
          this.reportPage.set(reportPage);
          this.isLoading.set(false);
        },
        error: () => {
          this.reportPage.set({ ...EMPTY_PAGE, size, number: page });
          this.isLoading.set(false);
          this.showLoadError();
        }
      });
  }

  protected statusLabel(status: StockStatus): string {
    const labels: Record<StockStatus, string> = {
      OUT_OF_STOCK: 'Sem estoque',
      LOW_STOCK: 'Estoque baixo',
      REORDER_NEEDED: 'Reposição necessária',
      OVERSTOCK: 'Excedente',
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

  private loadOptions(): void {
    this.loadProductOptions();
    this.loadStorageLocationOptions();
  }

  private loadProductOptions(): void {
    this.isLoadingProductOptions.set(true);

    this.productService
      .listProducts(0, 100, 'name,asc')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (productsPage) => {
          this.productOptions.set(productsPage.content.map((product) => this.productOption(product)));
          this.isLoadingProductOptions.set(false);
        },
        error: () => {
          this.productOptions.set([]);
          this.isLoadingProductOptions.set(false);
          this.showOptionsLoadError();
        }
      });
  }

  private loadStorageLocationOptions(): void {
    this.isLoadingStorageLocationOptions.set(true);

    this.storageLocationService
      .listStorageLocations(0, 100, 'name,asc')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (locationsPage) => {
          this.storageLocationOptions.set(locationsPage.content);
          this.isLoadingStorageLocationOptions.set(false);
        },
        error: () => {
          this.storageLocationOptions.set([]);
          this.isLoadingStorageLocationOptions.set(false);
          this.showOptionsLoadError();
        }
      });
  }

  private productOption(product: Product): ProductOption {
    return {
      id: product.id,
      label: product.sku ? `${product.name} - ${product.sku}` : product.name
    };
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
      summary: 'Erro ao carregar relatório de reposição sugerida',
      detail: 'Não foi possível buscar os itens para reposição. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showOptionsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar filtros',
      detail: 'Não foi possível buscar produtos ou locais de estoque.',
      life: 5000
    });
  }
}
