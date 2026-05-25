import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
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
import { CsvExportService } from '../../../../shared/services/csv-export.service';
import { Product } from '../../../products/models/product.model';
import { ProductService } from '../../../products/services/product.service';
import { StorageLocation } from '../../../storage-locations/models/storage-location.model';
import { StorageLocationService } from '../../../storage-locations/services/storage-location.service';
import { MovementType, StockMovement } from '../../../stock-movements/models/stock-movement.model';
import { StockMovementSearchRequest } from '../../../stock-movements/models/stock-movement-request.model';
import { StockMovementService } from '../../../stock-movements/services/stock-movement.service';

interface ProductOption {
  id: number;
  label: string;
}

interface MovementTypeOption {
  label: string;
  value: MovementType;
}

interface SummaryCard {
  title: string;
  value: number;
  icon: string;
}

interface AppliedMovementFilters {
  productId: number | null;
  storageLocationId: number | null;
  movementType: MovementType | null;
  startDate: string | null;
  endDate: string | null;
}

const EMPTY_PAGE: Page<StockMovement> = {
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
  selector: 'app-stock-movement-report',
  imports: [
    ButtonModule,
    DatePipe,
    DatePickerModule,
    DialogModule,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    ReactiveFormsModule,
    SelectModule,
    TableModule,
    TagModule
  ],
  templateUrl: './stock-movement-report.html',
  styleUrl: './stock-movement-report.scss'
})
export class StockMovementReportComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly csvExportService = inject(CsvExportService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly messageService = inject(MessageService);
  private readonly productService = inject(ProductService);
  private readonly stockMovementService = inject(StockMovementService);
  private readonly storageLocationService = inject(StorageLocationService);

  protected readonly columns: PaginatedListColumn[] = [
    { label: 'Data', field: 'movementDate' },
    { label: 'Tipo', field: 'movementType' },
    { label: 'Produto', field: 'product.name' },
    { label: 'Local de estoque', field: 'storageLocation.name' },
    { label: 'Quantidade', field: 'quantity', styleClass: 'numeric-column' },
    { label: 'Motivo', field: 'reason' },
    { label: 'Referência', field: 'reference' },
    { label: 'Responsável', field: 'responsible' },
    { label: 'Observações', field: 'notes' },
    { label: 'Ações', sortable: false, styleClass: 'actions-column' }
  ];

  protected readonly movementTypeOptions: MovementTypeOption[] = [
    { label: 'Entrada', value: 'INBOUND' },
    { label: 'Saída', value: 'OUTBOUND' },
    { label: 'Ajuste', value: 'ADJUSTMENT' },
    { label: 'Saldo inicial', value: 'INITIAL_BALANCE' },
    { label: 'Retorno', value: 'RETURN' },
    { label: 'Perda', value: 'LOSS' },
    { label: 'Danificado', value: 'DAMAGED' },
    { label: 'Transferência', value: 'TRANSFER' }
  ];

  protected readonly isLoading = signal(false);
  protected readonly isDetailsLoading = signal(false);
  protected readonly isDetailsDialogVisible = signal(false);
  protected readonly isExporting = signal(false);
  protected readonly isLoadingProductOptions = signal(false);
  protected readonly isLoadingStorageLocationOptions = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('movementDate,desc');
  protected readonly appliedFilters = signal<AppliedMovementFilters>({
    productId: null,
    storageLocationId: null,
    movementType: null,
    startDate: null,
    endDate: null
  });
  protected readonly movementsPage = signal<Page<StockMovement>>(EMPTY_PAGE);
  protected readonly selectedMovement = signal<StockMovement | null>(null);
  protected readonly productOptions = signal<ProductOption[]>([]);
  protected readonly storageLocationOptions = signal<StorageLocation[]>([]);

  protected readonly movements = computed(() => this.movementsPage().content);
  protected readonly totalElements = computed(() => this.movementsPage().totalElements);
  protected readonly first = computed(() => this.movementsPage().number * this.movementsPage().size);
  protected readonly summaryCards = computed<SummaryCard[]>(() => {
    const movements = this.movements();

    return [
      {
        title: 'Total de movimentações na página',
        value: movements.length,
        icon: 'pi pi-list'
      },
      {
        title: 'Total de entradas',
        value: movements.filter((movement) => movement.movementType === 'INBOUND' || movement.movementType === 'RETURN' || movement.movementType === 'INITIAL_BALANCE').length,
        icon: 'pi pi-arrow-down-left'
      },
      {
        title: 'Total de saídas',
        value: movements.filter((movement) => movement.movementType === 'OUTBOUND' || movement.movementType === 'LOSS' || movement.movementType === 'DAMAGED').length,
        icon: 'pi pi-arrow-up-right'
      },
      {
        title: 'Total de ajustes',
        value: movements.filter((movement) => movement.movementType === 'ADJUSTMENT').length,
        icon: 'pi pi-sliders-h'
      },
      {
        title: 'Quantidade movimentada na página',
        value: movements.reduce((total, movement) => total + movement.quantity, 0),
        icon: 'pi pi-calculator'
      }
    ];
  });

  protected readonly filterForm = this.formBuilder.group({
    productId: this.formBuilder.control<number | null>(null),
    storageLocationId: this.formBuilder.control<number | null>(null),
    movementType: this.formBuilder.control<MovementType | null>(null),
    startDate: this.formBuilder.control<Date | null>(null),
    endDate: this.formBuilder.control<Date | null>(null)
  });

  ngOnInit(): void {
    this.loadOptions();
    this.loadMovements(0, this.pageSize(), this.sort());
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null; sortField?: string | string[] | null; sortOrder?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);
    const sort = this.resolveSort(event.sortField, event.sortOrder);

    this.pageSize.set(rows);
    this.sort.set(sort);
    this.loadMovements(page, rows, sort);
  }

  protected applyFilters(): void {
    if (!this.isPeriodValid()) {
      this.showInvalidPeriodError();
      return;
    }

    this.appliedFilters.set(this.currentFilters());
    this.loadMovements(0, this.pageSize(), this.sort());
  }

  protected clearFilters(): void {
    this.filterForm.reset({
      productId: null,
      storageLocationId: null,
      movementType: null,
      startDate: null,
      endDate: null
    });
    this.appliedFilters.set({
      productId: null,
      storageLocationId: null,
      movementType: null,
      startDate: null,
      endDate: null
    });
    this.loadMovements(0, this.pageSize(), this.sort());
  }

  protected loadMovements(page: number, size: number, sort = this.sort()): void {
    this.isLoading.set(true);

    this.movementRequest(page, size, sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (movementsPage) => {
          this.movementsPage.set(movementsPage);
          this.isLoading.set(false);
        },
        error: () => {
          this.movementsPage.set({ ...EMPTY_PAGE, size, number: page });
          this.isLoading.set(false);
          this.showLoadError();
        }
      });
  }

  protected openMovementDetails(movement: StockMovement): void {
    this.isDetailsDialogVisible.set(true);
    this.isDetailsLoading.set(true);
    this.selectedMovement.set(null);

    this.stockMovementService
      .getMovementById(movement.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (movementDetail) => {
          this.selectedMovement.set(movementDetail);
          this.isDetailsLoading.set(false);
        },
        error: () => {
          this.isDetailsDialogVisible.set(false);
          this.isDetailsLoading.set(false);
          this.showDetailsLoadError();
        }
      });
  }

  protected exportCsv(): void {
    if (this.isExporting()) {
      return;
    }

    this.isExporting.set(true);

    this.stockMovementService
      .exportAllMovements({
        ...this.appliedFilters(),
        sort: this.sort()
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (rows) => {
          this.isExporting.set(false);

          if (!rows.length) {
            this.showEmptyExportWarning();
            return;
          }

          this.exportRows(rows);
        },
        error: () => {
          this.isExporting.set(false);
          this.showExportError();
        }
      });
  }

  private exportRows(rows: StockMovement[]): void {
    this.csvExportService.exportToCsv('movimentacoes-estoque.csv', rows, [
      { header: 'Data', value: (row) => row.movementDate },
      { header: 'Tipo', value: (row) => this.movementLabel(row.movementType) },
      { header: 'Produto', value: (row) => this.productLabel(row) },
      { header: 'Local de estoque', value: (row) => row.storageLocation?.name },
      { header: 'Quantidade', value: (row) => row.quantity },
      { header: 'Motivo', value: (row) => row.reason },
      { header: 'Referência', value: (row) => row.reference },
      { header: 'Responsável', value: (row) => row.responsible },
      { header: 'Observações', value: (row) => row.notes }
    ]);
  }

  protected movementLabel(type: MovementType): string {
    const labels: Record<MovementType, string> = {
      INBOUND: 'Entrada',
      OUTBOUND: 'Saída',
      ADJUSTMENT: 'Ajuste',
      INITIAL_BALANCE: 'Saldo inicial',
      RETURN: 'Retorno',
      LOSS: 'Perda',
      DAMAGED: 'Danificado',
      TRANSFER: 'Transferência'
    };

    return labels[type];
  }

  protected movementSeverity(type: MovementType): 'success' | 'danger' | 'warn' | 'info' | 'secondary' {
    const severities: Record<MovementType, 'success' | 'danger' | 'warn' | 'info' | 'secondary'> = {
      INBOUND: 'success',
      OUTBOUND: 'danger',
      ADJUSTMENT: 'warn',
      INITIAL_BALANCE: 'info',
      RETURN: 'success',
      LOSS: 'danger',
      DAMAGED: 'danger',
      TRANSFER: 'secondary'
    };

    return severities[type];
  }

  protected productLabel(movement: StockMovement): string {
    if (!movement.product) {
      return '-';
    }

    return movement.product.sku ? `${movement.product.name} - ${movement.product.sku}` : movement.product.name;
  }

  private movementRequest(page: number, size: number, sort: string) {
    const request: StockMovementSearchRequest = {
      ...this.appliedFilters(),
      page,
      size,
      sort
    };

    return this.stockMovementService.searchMovements(request);
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

  private currentFilters(): AppliedMovementFilters {
    const value = this.filterForm.getRawValue();

    return {
      productId: value.productId ?? null,
      storageLocationId: value.storageLocationId ?? null,
      movementType: value.movementType ?? null,
      startDate: this.dateToApi(value.startDate),
      endDate: this.dateToApi(value.endDate)
    };
  }

  private isPeriodValid(): boolean {
    const { startDate, endDate } = this.filterForm.getRawValue();

    if (!startDate || !endDate) {
      return true;
    }

    return this.stripTime(startDate).getTime() <= this.stripTime(endDate).getTime();
  }

  private dateToApi(date: Date | null | undefined): string | null {
    if (!date) {
      return null;
    }

    const normalizedDate = this.stripTime(date);
    const year = normalizedDate.getFullYear();
    const month = String(normalizedDate.getMonth() + 1).padStart(2, '0');
    const day = String(normalizedDate.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  private stripTime(date: Date): Date {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
  }

  private showLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar relatório de movimentações',
      detail: 'Não foi possível buscar as movimentações. Verifique se a API está disponível.',
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

  private showInvalidPeriodError(): void {
    this.messageService.add({
      severity: 'warn',
      summary: 'Data inicial não pode ser maior que a data final.',
      detail: 'Ajuste o período informado antes de aplicar os filtros.',
      life: 4000
    });
  }

  private showExportError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao exportar movimentações',
      detail: 'Não foi possível gerar o CSV. Tente novamente.',
      life: 5000
    });
  }

  private showDetailsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar movimentação',
      detail: 'Não foi possível buscar os detalhes da movimentação.',
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
