import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';

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
import { MovementType, StockMovement } from '../../models/stock-movement.model';
import { StockMovementService } from '../../services/stock-movement.service';

type MovementFormMode = 'INBOUND' | 'OUTBOUND' | 'ADJUSTMENT';

interface ProductOption {
  id: number;
  label: string;
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
  selector: 'app-stock-movement-list',
  imports: [
    ButtonModule,
    DatePipe,
    DialogModule,
    InputNumberModule,
    InputTextModule,
    PaginatedListActionsDirective,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    ReactiveFormsModule,
    SelectModule,
    TableModule,
    TagModule,
    TextareaModule
  ],
  templateUrl: './stock-movement-list.html',
  styleUrl: './stock-movement-list.scss'
})
export class StockMovementListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
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
    { label: 'Ações', sortable: false, styleClass: 'actions-column' }
  ];

  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDetailsLoading = signal(false);
  protected readonly isFormDialogVisible = signal(false);
  protected readonly isDetailsDialogVisible = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('movementDate,desc');
  protected readonly movementsPage = signal<Page<StockMovement>>(EMPTY_PAGE);
  protected readonly formMode = signal<MovementFormMode>('INBOUND');
  protected readonly selectedMovement = signal<StockMovement | null>(null);
  protected readonly productOptions = signal<ProductOption[]>([]);
  protected readonly storageLocationOptions = signal<StorageLocation[]>([]);
  protected readonly isLoadingProductOptions = signal(false);
  protected readonly isLoadingStorageLocationOptions = signal(false);

  protected readonly movements = computed(() => this.movementsPage().content);
  protected readonly totalElements = computed(() => this.movementsPage().totalElements);
  protected readonly first = computed(() => this.movementsPage().number * this.movementsPage().size);
  protected readonly formTitle = computed(() => {
    const titles: Record<MovementFormMode, string> = {
      INBOUND: 'Nova entrada',
      OUTBOUND: 'Nova saída',
      ADJUSTMENT: 'Novo ajuste'
    };

    return titles[this.formMode()];
  });
  protected readonly quantityLabel = computed(() => (this.formMode() === 'ADJUSTMENT' ? 'Nova quantidade' : 'Quantidade'));

  protected readonly movementForm = this.formBuilder.group({
    productId: this.formBuilder.control<number | null>(null, [Validators.required]),
    storageLocationId: this.formBuilder.control<number | null>(null, [Validators.required]),
    quantity: this.formBuilder.control<number | null>(null, [Validators.required, Validators.min(0)]),
    reason: this.formBuilder.control<string | null>(null, [Validators.maxLength(255)]),
    reference: this.formBuilder.control<string | null>(null, [Validators.maxLength(100)]),
    responsible: this.formBuilder.control<string | null>(null, [Validators.maxLength(100)]),
    notes: this.formBuilder.control<string | null>(null, [Validators.maxLength(500)])
  });

  ngOnInit(): void {
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

  protected loadMovements(page: number, size: number, sort = this.sort()): void {
    this.isLoading.set(true);

    this.stockMovementService
      .listMovements(page, size, sort)
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

  protected openForm(mode: MovementFormMode): void {
    this.formMode.set(mode);
    this.movementForm.reset({
      productId: null,
      storageLocationId: null,
      quantity: null,
      reason: null,
      reference: null,
      responsible: null,
      notes: null
    });
    this.updateFormValidators(mode);
    this.movementForm.markAsPristine();
    this.movementForm.markAsUntouched();
    this.isFormDialogVisible.set(true);
    this.loadFormOptions();
  }

  protected saveMovement(): void {
    if (this.movementForm.invalid) {
      this.movementForm.markAllAsTouched();
      return;
    }

    const mode = this.formMode();
    const request =
      mode === 'INBOUND'
        ? this.stockMovementService.createInbound(this.inboundOutboundPayload())
        : mode === 'OUTBOUND'
          ? this.stockMovementService.createOutbound(this.inboundOutboundPayload())
          : this.stockMovementService.createAdjustment(this.adjustmentPayload());

    this.isSaving.set(true);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.isFormDialogVisible.set(false);
        this.showSuccess(this.successMessage(mode));
        this.loadMovements(this.movementsPage().number, this.pageSize(), this.sort());
      },
      error: () => {
        this.isSaving.set(false);
        this.showSaveError();
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

  protected shouldShowError(
    controlName: 'productId' | 'storageLocationId' | 'quantity' | 'reason' | 'reference' | 'responsible' | 'notes'
  ): boolean {
    const control = this.movementForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  private resolveSort(sortField?: string | string[] | null, sortOrder?: number | null): string {
    if (!sortField || Array.isArray(sortField)) {
      return this.sort();
    }

    return `${sortField},${sortOrder === -1 ? 'desc' : 'asc'}`;
  }

  private updateFormValidators(mode: MovementFormMode): void {
    const quantityValidators = mode === 'ADJUSTMENT'
      ? [Validators.required, Validators.min(0)]
      : [Validators.required, Validators.min(0.0000001)];
    const reasonValidators = mode === 'ADJUSTMENT'
      ? [Validators.required, Validators.maxLength(255), Validators.pattern(/\S/)]
      : [Validators.maxLength(255)];

    this.movementForm.controls.quantity.setValidators(quantityValidators);
    this.movementForm.controls.reason.setValidators(reasonValidators);
    this.movementForm.controls.reference.setValidators([Validators.maxLength(100)]);
    this.movementForm.controls.quantity.updateValueAndValidity();
    this.movementForm.controls.reason.updateValueAndValidity();
    this.movementForm.controls.reference.updateValueAndValidity();
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

  private inboundOutboundPayload() {
    const value = this.movementForm.getRawValue();

    return {
      productId: value.productId as number,
      storageLocationId: value.storageLocationId as number,
      quantity: value.quantity as number,
      reason: this.optionalText(value.reason),
      reference: this.optionalText(value.reference),
      responsible: this.optionalText(value.responsible),
      notes: this.optionalText(value.notes)
    };
  }

  private adjustmentPayload() {
    const value = this.movementForm.getRawValue();

    return {
      productId: value.productId as number,
      storageLocationId: value.storageLocationId as number,
      newQuantity: value.quantity as number,
      reason: value.reason?.trim() as string,
      responsible: this.optionalText(value.responsible),
      notes: this.optionalText(value.notes)
    };
  }

  private optionalText(value: string | null): string | null {
    return value?.trim() || null;
  }

  private successMessage(mode: MovementFormMode): string {
    const messages: Record<MovementFormMode, string> = {
      INBOUND: 'Entrada registrada com sucesso.',
      OUTBOUND: 'Saída registrada com sucesso.',
      ADJUSTMENT: 'Ajuste registrado com sucesso.'
    };

    return messages[mode];
  }

  private showSuccess(detail: string): void {
    this.messageService.add({
      severity: 'success',
      summary: 'Sucesso',
      detail,
      life: 3000
    });
  }

  private showLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar movimentações',
      detail: 'Não foi possível buscar as movimentações. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showSaveError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao registrar movimentação',
      detail: 'Não foi possível registrar a movimentação. Verifique os dados e tente novamente.',
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
      summary: 'Erro ao carregar opções',
      detail: 'Não foi possível buscar produtos ou locais de estoque.',
      life: 5000
    });
  }
}
