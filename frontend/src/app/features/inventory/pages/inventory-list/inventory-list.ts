import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';

import { Page } from '../../../../models/page.model';
import {
  PaginatedListActionsDirective,
  PaginatedListBodyDirective,
  PaginatedListColumn,
  PaginatedListComponent,
  PaginatedListEmptyDirective
} from '../../../../shared/components/paginated-list/paginated-list';
import { Product } from '../../../products/models/product.model';
import { ProductService } from '../../../products/services/product.service';
import { StorageLocation } from '../../../storage-locations/models/storage-location.model';
import { StorageLocationService } from '../../../storage-locations/services/storage-location.service';
import { InventoryItem, StockStatus } from '../../models/inventory-item.model';
import { InventoryCreateRequest, InventoryUpdateRequest } from '../../models/inventory-request.model';
import { InventoryService } from '../../services/inventory.service';

interface ProductOption {
  id: number;
  label: string;
}

function stockLimitsValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const minimumStock = control.get('minimumStock')?.value as number | null;
    const maximumStock = control.get('maximumStock')?.value as number | null;
    const reorderPoint = control.get('reorderPoint')?.value as number | null;
    const errors: ValidationErrors = {};

    if (minimumStock === null || minimumStock === undefined) {
      return null;
    }

    if (maximumStock !== null && maximumStock !== undefined && maximumStock < minimumStock) {
      errors['maximumLessThanMinimum'] = true;
    }

    if (reorderPoint !== null && reorderPoint !== undefined && reorderPoint < minimumStock) {
      errors['reorderLessThanMinimum'] = true;
    }

    if (
      reorderPoint !== null &&
      reorderPoint !== undefined &&
      maximumStock !== null &&
      maximumStock !== undefined &&
      reorderPoint > maximumStock
    ) {
      errors['reorderGreaterThanMaximum'] = true;
    }

    return Object.keys(errors).length ? errors : null;
  };
}

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
    DialogModule,
    InputNumberModule,
    PaginatedListActionsDirective,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    ReactiveFormsModule,
    SelectModule,
    TableModule,
    TagModule
  ],
  templateUrl: './inventory-list.html',
  styleUrl: './inventory-list.scss'
})
export class InventoryListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly inventoryService = inject(InventoryService);
  private readonly messageService = inject(MessageService);
  private readonly productService = inject(ProductService);
  private readonly storageLocationService = inject(StorageLocationService);

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
  protected readonly isFormDialogVisible = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly inventoryBeingEdited = signal<InventoryItem | null>(null);
  protected readonly productOptions = signal<ProductOption[]>([]);
  protected readonly storageLocationOptions = signal<StorageLocation[]>([]);
  protected readonly isLoadingProductOptions = signal(false);
  protected readonly isLoadingStorageLocationOptions = signal(false);

  protected readonly items = computed(() => this.inventoryPage().content);
  protected readonly totalElements = computed(() => this.inventoryPage().totalElements);
  protected readonly first = computed(() => this.inventoryPage().number * this.inventoryPage().size);
  protected readonly formTitle = computed(() => (this.inventoryBeingEdited() ? 'Editar inventário' : 'Novo inventário'));

  protected readonly inventoryForm = this.formBuilder.group(
    {
      productId: this.formBuilder.control<number | null>(null, [Validators.required]),
      storageLocationId: this.formBuilder.control<number | null>(null, [Validators.required]),
      quantity: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
      minimumStock: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
      maximumStock: this.formBuilder.control<number | null>(null, [Validators.min(0)]),
      reorderPoint: this.formBuilder.control<number | null>(null, [Validators.min(0)])
    },
    { validators: [stockLimitsValidator()] }
  );

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

  protected openCreateDialog(): void {
    this.inventoryBeingEdited.set(null);
    this.inventoryForm.controls.productId.enable();
    this.inventoryForm.controls.quantity.enable();
    this.inventoryForm.reset({
      productId: null,
      storageLocationId: null,
      quantity: null,
      minimumStock: null,
      maximumStock: null,
      reorderPoint: null
    });
    this.inventoryForm.markAsPristine();
    this.inventoryForm.markAsUntouched();
    this.isFormDialogVisible.set(true);
    this.loadFormOptions();
  }

  protected openEditDialog(item: InventoryItem): void {
    this.inventoryService
      .getInventoryById(item.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (inventoryItem) => {
          this.inventoryBeingEdited.set(inventoryItem);
          this.inventoryForm.controls.productId.disable();
          this.inventoryForm.controls.quantity.disable();
          this.inventoryForm.reset({
            productId: inventoryItem.productId,
            storageLocationId: inventoryItem.storageLocationId,
            quantity: inventoryItem.quantity,
            minimumStock: inventoryItem.minimumStock,
            maximumStock: inventoryItem.maximumStock,
            reorderPoint: inventoryItem.reorderPoint
          });
          this.inventoryForm.markAsPristine();
          this.inventoryForm.markAsUntouched();
          this.isFormDialogVisible.set(true);
          this.loadStorageLocationOptions();
        },
        error: () => this.showDetailsLoadError()
      });
  }

  protected saveInventory(): void {
    if (this.inventoryForm.invalid) {
      this.inventoryForm.markAllAsTouched();
      return;
    }

    const editingItem = this.inventoryBeingEdited();
    const request = editingItem
      ? this.inventoryService.updateInventory(editingItem.id, this.inventoryUpdatePayload())
      : this.inventoryService.createInventory(this.inventoryCreatePayload());

    this.isSaving.set(true);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.isFormDialogVisible.set(false);
        this.showSuccess(editingItem ? 'Inventário atualizado com sucesso.' : 'Inventário criado com sucesso.');
        this.loadInventory(this.inventoryPage().number, this.pageSize(), this.sort());
      },
      error: () => {
        this.isSaving.set(false);
        this.showSaveError();
      }
    });
  }

  protected shouldShowError(
    controlName: 'productId' | 'storageLocationId' | 'quantity' | 'minimumStock' | 'maximumStock' | 'reorderPoint'
  ): boolean {
    const control = this.inventoryForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  protected shouldShowLimitError(errorName: string): boolean {
    return this.inventoryForm.hasError(errorName) && (this.inventoryForm.dirty || this.inventoryForm.touched);
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

  private loadFormOptions(): void {
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
        next: (storageLocationsPage) => {
          this.storageLocationOptions.set(storageLocationsPage.content);
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

  private inventoryCreatePayload(): InventoryCreateRequest {
    const value = this.inventoryForm.getRawValue();

    return {
      productId: value.productId as number,
      storageLocationId: value.storageLocationId as number,
      quantity: value.quantity as number,
      minimumStock: value.minimumStock as number,
      maximumStock: value.maximumStock ?? null,
      reorderPoint: value.reorderPoint ?? null
    };
  }

  private inventoryUpdatePayload(): InventoryUpdateRequest {
    const value = this.inventoryForm.getRawValue();

    return {
      storageLocationId: value.storageLocationId,
      minimumStock: value.minimumStock,
      maximumStock: value.maximumStock ?? null,
      reorderPoint: value.reorderPoint ?? null
    };
  }

  private showSuccess(detail: string): void {
    this.messageService.add({
      severity: 'success',
      summary: 'Sucesso',
      detail,
      life: 3000
    });
  }

  private showSaveError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao salvar inventário',
      detail: 'Não foi possível salvar o inventário. Verifique os dados e tente novamente.',
      life: 5000
    });
  }

  private showOptionsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar opções',
      detail: 'Não foi possível buscar produtos ou locais de estoque. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showDetailsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar inventário',
      detail: 'Não foi possível buscar os dados do inventário. Verifique se a API está disponível.',
      life: 5000
    });
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
