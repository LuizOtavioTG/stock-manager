import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin, of } from 'rxjs';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
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

interface PurchaseSuggestionItem {
  productId: number | null;
  productName: string | null;
  storageLocationName: string | null;
  currentQuantity: number;
  suggestedQuantity: number;
  unitCost: number | null;
  estimatedSubtotal: number | null;
}

interface PurchaseSuggestion {
  supplierId: number | null;
  supplierName: string;
  items: PurchaseSuggestionItem[];
  estimatedTotal: number;
}

const NO_SUPPLIER_KEY = '__no_supplier__';

@Component({
  selector: 'app-reorder-by-supplier-report',
  imports: [
    ButtonModule,
    DialogModule,
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
  private readonly productDetailsCache = new Map<number, Product>();

  protected readonly isLoading = signal(false);
  protected readonly isExporting = signal(false);
  protected readonly isSuggestionLoading = signal(false);
  protected readonly isSuggestionDialogVisible = signal(false);
  protected readonly isLoadingProductOptions = signal(false);
  protected readonly isLoadingStorageLocationOptions = signal(false);
  protected readonly allItems = signal<InventoryAlertItem[]>([]);
  protected readonly purchaseSuggestion = signal<PurchaseSuggestion | null>(null);
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

  protected openPurchaseSuggestion(group: SupplierReorderGroup): void {
    this.isSuggestionDialogVisible.set(true);
    this.isSuggestionLoading.set(true);
    this.purchaseSuggestion.set(null);

    const productIds = Array.from(
      new Set(
        group.items
          .map((item) => item.productId)
          .filter((productId): productId is number => productId !== null)
      )
    );
    const missingProductIds = productIds.filter((productId) => !this.productDetailsCache.has(productId));

    const detailsRequest = missingProductIds.length
      ? forkJoin(missingProductIds.map((productId) => this.productService.getProductById(productId)))
      : of([]);

    detailsRequest
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (products) => {
          products.forEach((product) => this.productDetailsCache.set(product.id, product));
          this.purchaseSuggestion.set(this.buildPurchaseSuggestion(group));
          this.isSuggestionLoading.set(false);
        },
        error: () => {
          this.isSuggestionDialogVisible.set(false);
          this.isSuggestionLoading.set(false);
          this.showSuggestionError();
        }
      });
  }

  protected exportPurchaseSuggestionCsv(): void {
    const suggestion = this.purchaseSuggestion();

    if (!suggestion?.items.length) {
      this.showEmptyExportWarning();
      return;
    }

    this.csvExportService.exportToCsv(`sugestao-pedido-${this.filenameSlug(suggestion.supplierName)}.csv`, suggestion.items, [
      { header: 'Fornecedor', value: () => suggestion.supplierName },
      { header: 'Produto', value: (row) => row.productName },
      { header: 'Local de estoque', value: (row) => row.storageLocationName },
      { header: 'Quantidade atual', value: (row) => row.currentQuantity },
      { header: 'Quantidade sugerida', value: (row) => row.suggestedQuantity },
      { header: 'Custo unitário', value: (row) => row.unitCost },
      { header: 'Subtotal estimado', value: (row) => row.estimatedSubtotal }
    ]);
  }

  protected suggestionTotalQuantity(): number {
    return this.purchaseSuggestion()?.items.reduce((total, item) => total + item.suggestedQuantity, 0) ?? 0;
  }

  protected hasProductsWithoutCost(): boolean {
    return this.purchaseSuggestion()?.items.some((item) => item.unitCost === null) ?? false;
  }

  protected currencyValue(value: number | null): string {
    if (value === null) {
      return '-';
    }

    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
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

  private buildPurchaseSuggestion(group: SupplierReorderGroup): PurchaseSuggestion {
    const items = group.items.map<PurchaseSuggestionItem>((item) => {
      const unitCost = item.productId ? this.productDetailsCache.get(item.productId)?.costPrice ?? null : null;
      const estimatedSubtotal = unitCost !== null ? item.suggestedReorderQuantity * unitCost : null;

      return {
        productId: item.productId,
        productName: item.productName,
        storageLocationName: item.storageLocationName,
        currentQuantity: item.quantity,
        suggestedQuantity: item.suggestedReorderQuantity,
        unitCost,
        estimatedSubtotal
      };
    });

    return {
      supplierId: group.supplierId,
      supplierName: group.supplierName,
      items,
      estimatedTotal: items.reduce((total, item) => total + (item.estimatedSubtotal ?? 0), 0)
    };
  }

  private filenameSlug(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '') || 'fornecedor';
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

  private showSuggestionError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao gerar sugestão de pedido',
      detail: 'Não foi possível buscar os custos dos produtos. Tente novamente.',
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
