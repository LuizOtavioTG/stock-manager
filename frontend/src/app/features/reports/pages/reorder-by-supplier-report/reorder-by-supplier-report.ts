import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';

import { CsvExportService } from '../../../../shared/services/csv-export.service';
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

interface SupplierOption {
  label: string;
  value: string;
}

interface SupplierReorderGroup {
  supplierId: number | null;
  supplierName: string;
  items: InventoryAlertItem[];
  totalSuggestedQuantity: number;
  totalProducts: number;
}

interface SummaryCard {
  title: string;
  value: number;
  icon: string;
}

const NO_SUPPLIER_KEY = '__no_supplier__';

@Component({
  selector: 'app-reorder-by-supplier-report',
  imports: [
    ButtonModule,
    ReactiveFormsModule,
    SelectModule,
    TableModule,
    TagModule
  ],
  templateUrl: './reorder-by-supplier-report.html',
  styleUrl: './reorder-by-supplier-report.scss'
})
export class ReorderBySupplierReportComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly csvExportService = inject(CsvExportService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly inventoryAlertsService = inject(InventoryAlertsService);
  private readonly messageService = inject(MessageService);
  private readonly productService = inject(ProductService);
  private readonly storageLocationService = inject(StorageLocationService);

  protected readonly isLoading = signal(false);
  protected readonly isExporting = signal(false);
  protected readonly isLoadingProductOptions = signal(false);
  protected readonly isLoadingStorageLocationOptions = signal(false);
  protected readonly allItems = signal<InventoryAlertItem[]>([]);
  protected readonly supplierFilter = signal<string | null>(null);
  protected readonly productOptions = signal<ProductOption[]>([]);
  protected readonly storageLocationOptions = signal<StorageLocation[]>([]);

  protected readonly filterForm = this.formBuilder.group({
    supplierKey: this.formBuilder.control<string | null>(null),
    productId: this.formBuilder.control<number | null>(null),
    storageLocationId: this.formBuilder.control<number | null>(null)
  });

  protected readonly supplierOptions = computed<SupplierOption[]>(() => {
    const options = new Map<string, SupplierOption>();
    let hasItemsWithoutSupplier = false;

    this.allItems().forEach((item) => {
      if (!item.suppliers.length) {
        hasItemsWithoutSupplier = true;
        return;
      }

      item.suppliers.forEach((supplier) => {
        options.set(String(supplier.id), {
          value: String(supplier.id),
          label: supplier.name
        });
      });
    });

    const sortedOptions = Array.from(options.values()).sort((a, b) => a.label.localeCompare(b.label));

    if (hasItemsWithoutSupplier) {
      sortedOptions.push({ value: NO_SUPPLIER_KEY, label: 'Sem fornecedor' });
    }

    return sortedOptions;
  });

  protected readonly groups = computed<SupplierReorderGroup[]>(() => {
    const selectedSupplier = this.supplierFilter();
    const groups = new Map<string, SupplierReorderGroup>();

    this.allItems().forEach((item) => {
      const suppliers = item.suppliers.length
        ? item.suppliers.map((supplier) => ({
          key: String(supplier.id),
          id: supplier.id,
          name: supplier.name
        }))
        : [{ key: NO_SUPPLIER_KEY, id: null, name: 'Sem fornecedor' }];

      suppliers.forEach((supplier) => {
        if (selectedSupplier && supplier.key !== selectedSupplier) {
          return;
        }

        const group = groups.get(supplier.key) ?? {
          supplierId: supplier.id,
          supplierName: supplier.name,
          items: [],
          totalSuggestedQuantity: 0,
          totalProducts: 0
        };

        group.items.push(item);
        group.totalSuggestedQuantity += item.suggestedReorderQuantity;
        group.totalProducts = group.items.length;
        groups.set(supplier.key, group);
      });
    });

    return Array.from(groups.values()).sort((a, b) => {
      if (a.supplierId === null) {
        return 1;
      }
      if (b.supplierId === null) {
        return -1;
      }

      return a.supplierName.localeCompare(b.supplierName);
    });
  });

  protected readonly flattenedRows = computed(() =>
    this.groups().flatMap((group) =>
      group.items.map((item) => ({
        supplierName: group.supplierName,
        item
      }))
    )
  );

  protected readonly summaryCards = computed<SummaryCard[]>(() => {
    const groups = this.groups();
    const rows = this.flattenedRows();

    return [
      {
        title: 'Fornecedores com reposição',
        value: groups.filter((group) => group.supplierId !== null).length,
        icon: 'pi pi-truck'
      },
      {
        title: 'Produtos para repor',
        value: rows.length,
        icon: 'pi pi-box'
      },
      {
        title: 'Quantidade total sugerida',
        value: rows.reduce((total, row) => total + row.item.suggestedReorderQuantity, 0),
        icon: 'pi pi-shopping-cart'
      },
      {
        title: 'Itens sem fornecedor',
        value: this.allItems().filter((item) => !item.suppliers.length).length,
        icon: 'pi pi-exclamation-circle'
      }
    ];
  });

  ngOnInit(): void {
    this.loadOptions();
    this.loadReport();
  }

  protected applyFilters(): void {
    const filters = this.filterForm.getRawValue();
    this.supplierFilter.set(filters.supplierKey ?? null);
    this.loadReport();
  }

  protected clearFilters(): void {
    this.filterForm.reset({
      supplierKey: null,
      productId: null,
      storageLocationId: null
    });
    this.supplierFilter.set(null);
    this.loadReport();
  }

  protected loadReport(): void {
    const filters = this.filterForm.getRawValue();

    this.isLoading.set(true);
    this.inventoryAlertsService
      .exportAllReorderNeeded({
        productId: filters.productId,
        storageLocationId: filters.storageLocationId,
        sort: 'id,asc'
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (items) => {
          this.allItems.set(items);
          this.isLoading.set(false);
        },
        error: () => {
          this.allItems.set([]);
          this.isLoading.set(false);
          this.showLoadError();
        }
      });
  }

  protected exportCsv(): void {
    if (this.isExporting()) {
      return;
    }

    const rows = this.flattenedRows();

    if (!rows.length) {
      this.showEmptyExportWarning();
      return;
    }

    this.isExporting.set(true);
    this.csvExportService.exportToCsv('reposicao-por-fornecedor.csv', rows, [
      { header: 'Fornecedor', value: (row) => row.supplierName },
      { header: 'Produto', value: (row) => row.item.productName },
      { header: 'Local de estoque', value: (row) => row.item.storageLocationName },
      { header: 'Quantidade atual', value: (row) => row.item.quantity },
      { header: 'Ponto de reposição', value: (row) => row.item.reorderPoint },
      { header: 'Estoque máximo', value: (row) => row.item.maximumStock },
      { header: 'Quantidade sugerida', value: (row) => row.item.suggestedReorderQuantity },
      { header: 'Status', value: (row) => this.statusLabel(row.item.stockStatus) }
    ]);
    this.isExporting.set(false);
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

  private showLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar relatório de reposição por fornecedor',
      detail: 'Não foi possível buscar os itens para reposição. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showEmptyExportWarning(): void {
    this.messageService.add({
      severity: 'warn',
      summary: 'Não há dados para exportar',
      detail: 'Carregue ou filtre dados antes de exportar o CSV.',
      life: 3000
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
