import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
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
  PaginatedListEmptyDirective,
  PaginatedListToolbarDirective
} from '../../../../shared/components/paginated-list/paginated-list';
import { Product } from '../../../products/models/product.model';
import { ProductService } from '../../../products/services/product.service';
import { StorageLocation } from '../../../storage-locations/models/storage-location.model';
import { StorageLocationService } from '../../../storage-locations/services/storage-location.service';
import { Supplier } from '../../../suppliers/models/supplier.model';
import { SupplierService } from '../../../suppliers/services/supplier.service';
import { PurchaseOrder, PurchaseOrderItem, PurchaseOrderStatus } from '../../models/purchase-order.model';
import { PurchaseOrderReceipt } from '../../models/purchase-order-receipt.model';
import {
  PurchaseOrderCreateRequest,
  PurchaseOrderItemRequest,
  PurchaseOrderReceiptReverseRequest,
  PurchaseOrderReceiveItemRequest,
  PurchaseOrderReceiveRequest,
  PurchaseOrderUpdateRequest
} from '../../models/purchase-order-request.model';
import { PurchaseOrderService } from '../../services/purchase-order.service';

type TagSeverity = 'success' | 'secondary' | 'info' | 'warn' | 'danger';

interface StatusOption {
  label: string;
  value: PurchaseOrderStatus;
}

type PurchaseOrderItemForm = FormGroup<{
  productId: FormControl<number | null>;
  quantity: FormControl<number | null>;
  unitCost: FormControl<number | null>;
  notes: FormControl<string | null>;
}>;

type ReceiveItemForm = FormGroup<{
  purchaseOrderItemId: FormControl<number>;
  receivedQuantity: FormControl<number | null>;
}>;

function minFormArrayLengthValidator(minLength: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as unknown[];

    return Array.isArray(value) && value.length >= minLength
      ? null
      : { minItems: { requiredLength: minLength } };
  };
}

function receiveQuantityValidator(maxQuantity: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as number | null;

    if (value === null || value === undefined || value === 0) {
      return null;
    }

    if (value < 0) {
      return { min: true };
    }

    if (value > maxQuantity) {
      return { maxPending: { max: maxQuantity } };
    }

    return null;
  };
}

const EMPTY_PAGE: Page<PurchaseOrder> = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: true
};

const EMPTY_RECEIPTS_PAGE: Page<PurchaseOrderReceipt> = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: 20,
  number: 0,
  first: true,
  last: true,
  empty: true
};

@Component({
  selector: 'app-purchase-order-list',
  imports: [
    ButtonModule,
    ConfirmDialogModule,
    CurrencyPipe,
    DatePipe,
    DialogModule,
    InputNumberModule,
    InputTextModule,
    PaginatedListActionsDirective,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    PaginatedListToolbarDirective,
    ReactiveFormsModule,
    SelectModule,
    TableModule,
    TagModule,
    TextareaModule
  ],
  templateUrl: './purchase-order-list.html',
  styleUrl: './purchase-order-list.scss'
})
export class PurchaseOrderListComponent implements OnInit {
  private readonly confirmationService = inject(ConfirmationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly messageService = inject(MessageService);
  private readonly productService = inject(ProductService);
  private readonly purchaseOrderService = inject(PurchaseOrderService);
  private readonly storageLocationService = inject(StorageLocationService);
  private readonly supplierService = inject(SupplierService);

  protected readonly columns: PaginatedListColumn[] = [
    { label: 'ID', field: 'id' },
    { label: 'Fornecedor', field: 'supplierName' },
    { label: 'Status', field: 'status' },
    { label: 'Data do pedido', field: 'orderDate' },
    { label: 'Entrega prevista', field: 'expectedDeliveryDate' },
    { label: 'Total estimado', field: 'totalEstimatedCost', styleClass: 'numeric-column' },
    { label: 'Criado em', field: 'createdAt' },
    { label: 'Ações', sortable: false, styleClass: 'actions-column' }
  ];

  protected readonly statusOptions: StatusOption[] = [
    { label: 'Rascunho', value: 'DRAFT' },
    { label: 'Enviado', value: 'SENT' },
    { label: 'Parcialmente recebido', value: 'PARTIALLY_RECEIVED' },
    { label: 'Recebido', value: 'RECEIVED' },
    { label: 'Cancelado', value: 'CANCELLED' }
  ];

  protected readonly isLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDetailsLoading = signal(false);
  protected readonly isFormDialogVisible = signal(false);
  protected readonly isDetailsDialogVisible = signal(false);
  protected readonly isReceiptItemsDialogVisible = signal(false);
  protected readonly isReverseDialogVisible = signal(false);
  protected readonly isReceiveDialogVisible = signal(false);
  protected readonly isReceiveLoading = signal(false);
  protected readonly isReceiptsLoading = signal(false);
  protected readonly isReversingReceipt = signal(false);
  protected readonly isReceiving = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('orderDate,desc');
  protected readonly purchaseOrdersPage = signal<Page<PurchaseOrder>>(EMPTY_PAGE);
  protected readonly selectedPurchaseOrder = signal<PurchaseOrder | null>(null);
  protected readonly selectedReceipt = signal<PurchaseOrderReceipt | null>(null);
  protected readonly receiptBeingReversed = signal<PurchaseOrderReceipt | null>(null);
  protected readonly receiptsPage = signal<Page<PurchaseOrderReceipt>>(EMPTY_RECEIPTS_PAGE);
  protected readonly purchaseOrderBeingReceived = signal<PurchaseOrder | null>(null);
  protected readonly purchaseOrderBeingEdited = signal<PurchaseOrder | null>(null);
  protected readonly supplierOptions = signal<Supplier[]>([]);
  protected readonly productOptions = signal<Product[]>([]);
  protected readonly storageLocationOptions = signal<StorageLocation[]>([]);
  protected readonly isLoadingSupplierOptions = signal(false);
  protected readonly isLoadingProductOptions = signal(false);
  protected readonly isLoadingStorageLocationOptions = signal(false);

  protected readonly purchaseOrders = computed(() => this.purchaseOrdersPage().content);
  protected readonly receipts = computed(() => this.receiptsPage().content);
  protected readonly totalElements = computed(() => this.purchaseOrdersPage().totalElements);
  protected readonly first = computed(() => this.purchaseOrdersPage().number * this.purchaseOrdersPage().size);
  protected readonly formTitle = computed(() => (this.purchaseOrderBeingEdited() ? 'Editar pedido de compra' : 'Novo pedido de compra'));
  protected readonly pendingReceiveItems = computed(() =>
    this.purchaseOrderBeingReceived()?.items.filter((item) => item.pendingQuantity > 0) ?? []
  );

  protected readonly filterForm = this.formBuilder.group({
    supplierId: this.formBuilder.control<number | null>(null),
    status: this.formBuilder.control<PurchaseOrderStatus | null>(null)
  });

  protected readonly purchaseOrderForm = this.formBuilder.group({
    supplierId: this.formBuilder.control<number | null>(null, [Validators.required]),
    status: this.formBuilder.control<PurchaseOrderStatus | null>('DRAFT'),
    expectedDeliveryDate: this.formBuilder.control<string | null>(null),
    notes: this.formBuilder.control<string | null>(null, [Validators.maxLength(500)]),
    items: this.formBuilder.array<PurchaseOrderItemForm>([], [minFormArrayLengthValidator(1)])
  });

  protected readonly receiveForm = this.formBuilder.group({
    storageLocationId: this.formBuilder.control<number | null>(null, [Validators.required]),
    notes: this.formBuilder.control<string | null>(null, [Validators.maxLength(500)]),
    items: this.formBuilder.array<ReceiveItemForm>([])
  });

  protected readonly reverseReceiptForm = this.formBuilder.group({
    reason: this.formBuilder.control<string | null>(null, [Validators.required, Validators.maxLength(500), Validators.pattern(/\S/)])
  });

  protected get itemForms(): FormArray<PurchaseOrderItemForm> {
    return this.purchaseOrderForm.controls.items;
  }

  protected get receiveItemForms(): FormArray<ReceiveItemForm> {
    return this.receiveForm.controls.items;
  }

  ngOnInit(): void {
    this.loadPurchaseOrders(0, this.pageSize(), this.sort());
    this.loadSupplierOptions();
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null; sortField?: string | string[] | null; sortOrder?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);
    const sort = this.resolveSort(event.sortField, event.sortOrder);

    this.pageSize.set(rows);
    this.sort.set(sort);
    this.loadPurchaseOrders(page, rows, sort);
  }

  protected loadPurchaseOrders(page: number, size: number, sort = this.sort()): void {
    this.isLoading.set(true);

    const filters = this.filterForm.getRawValue();
    const request = filters.supplierId
      ? this.purchaseOrderService.listBySupplier(filters.supplierId, page, size, sort)
      : filters.status
        ? this.purchaseOrderService.listByStatus(filters.status, page, size, sort)
        : this.purchaseOrderService.listPurchaseOrders(page, size, sort);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (purchaseOrdersPage) => {
        this.purchaseOrdersPage.set(purchaseOrdersPage);
        this.isLoading.set(false);
      },
      error: () => {
        this.purchaseOrdersPage.set({ ...EMPTY_PAGE, size, number: page });
        this.isLoading.set(false);
        this.showLoadError();
      }
    });
  }

  protected onSupplierFilterChange(supplierId: number | null): void {
    if (supplierId) {
      this.filterForm.controls.status.setValue(null);
    }
  }

  protected onStatusFilterChange(status: PurchaseOrderStatus | null): void {
    if (status) {
      this.filterForm.controls.supplierId.setValue(null);
    }
  }

  protected applyFilters(): void {
    this.loadPurchaseOrders(0, this.pageSize(), this.sort());
  }

  protected clearFilters(): void {
    this.filterForm.reset({ supplierId: null, status: null });
    this.loadPurchaseOrders(0, this.pageSize(), this.sort());
  }

  protected openCreateDialog(): void {
    this.purchaseOrderBeingEdited.set(null);
    this.purchaseOrderForm.controls.supplierId.enable();
    this.purchaseOrderForm.reset({
      supplierId: null,
      status: 'DRAFT',
      expectedDeliveryDate: null,
      notes: null
    });
    this.itemForms.clear();
    this.addItem();
    this.purchaseOrderForm.markAsPristine();
    this.purchaseOrderForm.markAsUntouched();
    this.isFormDialogVisible.set(true);
    this.loadFormOptions();
  }

  protected openEditDialog(order: PurchaseOrder): void {
    this.purchaseOrderService
      .getPurchaseOrderById(order.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (orderDetail) => {
          this.purchaseOrderBeingEdited.set(orderDetail);
          this.purchaseOrderForm.controls.supplierId.disable();
          this.purchaseOrderForm.reset({
            supplierId: orderDetail.supplierId,
            status: orderDetail.status,
            expectedDeliveryDate: orderDetail.expectedDeliveryDate?.slice(0, 10) ?? null,
            notes: orderDetail.notes
          });
          this.itemForms.clear();
          orderDetail.items.forEach((item) => {
            this.itemForms.push(
              this.createItemForm({
                productId: item.productId,
                quantity: item.quantity,
                unitCost: item.unitCost,
                notes: item.notes
              })
            );
          });
          this.purchaseOrderForm.markAsPristine();
          this.purchaseOrderForm.markAsUntouched();
          this.isFormDialogVisible.set(true);
          this.loadFormOptions();
        },
        error: () => this.showDetailsLoadError()
      });
  }

  protected savePurchaseOrder(): void {
    if (this.purchaseOrderForm.invalid) {
      this.purchaseOrderForm.markAllAsTouched();
      this.itemForms.controls.forEach((itemForm) => itemForm.markAllAsTouched());
      return;
    }

    const editingOrder = this.purchaseOrderBeingEdited();
    const request = editingOrder
      ? this.purchaseOrderService.updatePurchaseOrder(editingOrder.id, this.purchaseOrderUpdatePayload())
      : this.purchaseOrderService.createPurchaseOrder(this.purchaseOrderCreatePayload());

    this.isSaving.set(true);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.isFormDialogVisible.set(false);
        this.showSuccess(editingOrder ? 'Pedido atualizado com sucesso.' : 'Pedido criado com sucesso.');
        this.loadPurchaseOrders(this.purchaseOrdersPage().number, this.pageSize(), this.sort());
      },
      error: () => {
        this.isSaving.set(false);
        this.showSaveError();
      }
    });
  }

  protected openDetailsDialog(order: PurchaseOrder): void {
    this.isDetailsDialogVisible.set(true);
    this.isDetailsLoading.set(true);
    this.selectedPurchaseOrder.set(null);

    this.purchaseOrderService
      .getPurchaseOrderById(order.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (orderDetail) => {
          this.selectedPurchaseOrder.set(orderDetail);
          this.isDetailsLoading.set(false);
          this.loadReceipts(orderDetail.id);
        },
        error: () => {
          this.isDetailsDialogVisible.set(false);
          this.isDetailsLoading.set(false);
          this.showDetailsLoadError();
        }
      });
  }

  protected openReceiptItems(receipt: PurchaseOrderReceipt): void {
    this.selectedReceipt.set(receipt);
    this.isReceiptItemsDialogVisible.set(true);
  }

  protected openReverseReceiptDialog(receipt: PurchaseOrderReceipt): void {
    this.receiptBeingReversed.set(receipt);
    this.reverseReceiptForm.reset({ reason: null });
    this.reverseReceiptForm.markAsPristine();
    this.reverseReceiptForm.markAsUntouched();
    this.isReverseDialogVisible.set(true);
  }

  protected confirmCancelOrder(order: PurchaseOrder): void {
    this.confirmationService.confirm({
      header: 'Cancelar pedido',
      message: `Deseja cancelar o pedido #${order.id} de ${order.supplierName}?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Cancelar pedido',
      rejectLabel: 'Voltar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.cancelOrder(order)
    });
  }

  protected openReceiveDialog(order: PurchaseOrder): void {
    this.isReceiveDialogVisible.set(true);
    this.isReceiveLoading.set(true);
    this.purchaseOrderBeingReceived.set(null);
    this.receiveForm.reset({
      storageLocationId: null,
      notes: null
    });
    this.receiveItemForms.clear();
    this.loadStorageLocationOptions();

    this.purchaseOrderService
      .getPurchaseOrderById(order.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (orderDetail) => {
          this.purchaseOrderBeingReceived.set(orderDetail);
          orderDetail.items
            .filter((item) => item.pendingQuantity > 0)
            .forEach((item) => this.receiveItemForms.push(this.createReceiveItemForm(item)));
          this.receiveForm.markAsPristine();
          this.receiveForm.markAsUntouched();
          this.isReceiveLoading.set(false);
        },
        error: () => {
          this.isReceiveDialogVisible.set(false);
          this.isReceiveLoading.set(false);
          this.showDetailsLoadError();
        }
      });
  }

  protected confirmReceivePurchaseOrder(): void {
    if (this.receiveForm.invalid) {
      this.receiveForm.markAllAsTouched();
      this.receiveItemForms.controls.forEach((itemForm) => itemForm.markAllAsTouched());
      return;
    }

    const payload = this.receivePayload();
    if (!payload.items.length) {
      this.showEmptyReceiveWarning();
      return;
    }

    this.confirmationService.confirm({
      header: 'Confirmar recebimento',
      message: 'Deseja confirmar o recebimento deste pedido?',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Confirmar',
      rejectLabel: 'Voltar',
      accept: () => this.receivePurchaseOrder(payload)
    });
  }

  protected addItem(): void {
    this.itemForms.push(this.createItemForm());
    this.itemForms.markAsDirty();
  }

  protected removeItem(index: number): void {
    if (this.itemForms.length === 1) {
      this.itemForms.at(0).reset({
        productId: null,
        quantity: null,
        unitCost: null,
        notes: null
      });
      return;
    }

    this.itemForms.removeAt(index);
    this.itemForms.markAsDirty();
  }

  protected onProductSelected(index: number, productId: number | null): void {
    if (!productId) {
      return;
    }

    const product = this.productOptions().find((option) => option.id === productId);
    if (product?.costPrice !== null && product?.costPrice !== undefined) {
      this.itemForms.at(index).controls.unitCost.setValue(product.costPrice);
    }
  }

  protected canEdit(order: PurchaseOrder): boolean {
    return order.status !== 'CANCELLED' && order.status !== 'RECEIVED';
  }

  protected canCancel(order: PurchaseOrder): boolean {
    return order.status !== 'CANCELLED' && order.status !== 'RECEIVED';
  }

  protected canReceive(order: PurchaseOrder): boolean {
    return order.status === 'DRAFT' || order.status === 'SENT' || order.status === 'PARTIALLY_RECEIVED';
  }

  protected statusLabel(status: PurchaseOrderStatus): string {
    return this.statusOptions.find((option) => option.value === status)?.label ?? status;
  }

  protected statusSeverity(status: PurchaseOrderStatus): TagSeverity {
    const severityByStatus: Record<PurchaseOrderStatus, TagSeverity> = {
      DRAFT: 'secondary',
      SENT: 'info',
      PARTIALLY_RECEIVED: 'warn',
      RECEIVED: 'success',
      CANCELLED: 'danger'
    };

    return severityByStatus[status];
  }

  protected receiptStatusLabel(status: PurchaseOrderReceipt['status']): string {
    const labelByStatus: Record<PurchaseOrderReceipt['status'], string> = {
      ACTIVE: 'Ativo',
      REVERSED: 'Estornado'
    };

    return labelByStatus[status];
  }

  protected receiptStatusSeverity(status: PurchaseOrderReceipt['status']): TagSeverity {
    const severityByStatus: Record<PurchaseOrderReceipt['status'], TagSeverity> = {
      ACTIVE: 'success',
      REVERSED: 'danger'
    };

    return severityByStatus[status];
  }

  protected itemSubtotal(itemForm: PurchaseOrderItemForm): number {
    const quantity = itemForm.controls.quantity.value ?? 0;
    const unitCost = itemForm.controls.unitCost.value ?? 0;

    return quantity * unitCost;
  }

  protected formTotal(): number {
    return this.itemForms.controls.reduce((total, itemForm) => total + this.itemSubtotal(itemForm), 0);
  }

  protected shouldShowOrderError(controlName: 'supplierId' | 'notes'): boolean {
    const control = this.purchaseOrderForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  protected shouldShowItemError(itemForm: PurchaseOrderItemForm, controlName: 'productId' | 'quantity' | 'unitCost' | 'notes'): boolean {
    const control = itemForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  protected shouldShowReceiveOrderError(controlName: 'storageLocationId' | 'notes'): boolean {
    const control = this.receiveForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  protected shouldShowReceiveItemError(itemForm: ReceiveItemForm): boolean {
    const control = itemForm.controls.receivedQuantity;

    return control.invalid && (control.dirty || control.touched);
  }

  protected shouldShowReverseReasonError(): boolean {
    const control = this.reverseReceiptForm.controls.reason;

    return control.invalid && (control.dirty || control.touched);
  }

  protected confirmReverseReceipt(): void {
    if (this.reverseReceiptForm.invalid) {
      this.reverseReceiptForm.markAllAsTouched();
      return;
    }

    this.confirmationService.confirm({
      header: 'Estornar recebimento',
      message: 'Deseja estornar este recebimento?',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Estornar',
      rejectLabel: 'Voltar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.reverseReceipt()
    });
  }

  private createItemForm(value?: Partial<PurchaseOrderItemRequest>): PurchaseOrderItemForm {
    return this.formBuilder.group({
      productId: this.formBuilder.control<number | null>(value?.productId ?? null, [Validators.required]),
      quantity: this.formBuilder.control<number | null>(value?.quantity ?? null, [Validators.required, Validators.min(1)]),
      unitCost: this.formBuilder.control<number | null>(value?.unitCost ?? null, [Validators.required, Validators.min(0)]),
      notes: this.formBuilder.control<string | null>(value?.notes ?? null, [Validators.maxLength(500)])
    });
  }

  private createReceiveItemForm(item: PurchaseOrderItem): ReceiveItemForm {
    return this.formBuilder.nonNullable.group({
      purchaseOrderItemId: this.formBuilder.nonNullable.control(item.id),
      receivedQuantity: this.formBuilder.control<number | null>(item.pendingQuantity, [receiveQuantityValidator(item.pendingQuantity)])
    });
  }

  private resolveSort(sortField?: string | string[] | null, sortOrder?: number | null): string {
    if (!sortField || Array.isArray(sortField)) {
      return this.sort();
    }

    return `${sortField},${sortOrder === -1 ? 'desc' : 'asc'}`;
  }

  private loadFormOptions(): void {
    this.loadSupplierOptions();
    this.loadProductOptions();
  }

  private loadSupplierOptions(): void {
    this.isLoadingSupplierOptions.set(true);

    this.supplierService
      .listSuppliers(0, 100, 'name,asc')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (suppliersPage) => {
          this.supplierOptions.set(suppliersPage.content);
          this.isLoadingSupplierOptions.set(false);
        },
        error: () => {
          this.supplierOptions.set([]);
          this.isLoadingSupplierOptions.set(false);
          this.showOptionsLoadError();
        }
      });
  }

  private loadProductOptions(): void {
    this.isLoadingProductOptions.set(true);

    this.productService
      .listProducts(0, 100, 'name,asc')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (productsPage) => {
          this.productOptions.set(productsPage.content);
          this.isLoadingProductOptions.set(false);
        },
        error: () => {
          this.productOptions.set([]);
          this.isLoadingProductOptions.set(false);
          this.showOptionsLoadError();
        }
      });
  }

  private loadReceipts(purchaseOrderId: number): void {
    this.isReceiptsLoading.set(true);
    this.receiptsPage.set(EMPTY_RECEIPTS_PAGE);

    this.purchaseOrderService
      .listReceipts(purchaseOrderId, 0, 20, 'receiptDate,desc')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (receiptsPage) => {
          this.receiptsPage.set(receiptsPage);
          this.isReceiptsLoading.set(false);
        },
        error: () => {
          this.receiptsPage.set(EMPTY_RECEIPTS_PAGE);
          this.isReceiptsLoading.set(false);
          this.showReceiptsLoadError();
        }
      });
  }

  private refreshSelectedPurchaseOrderDetails(orderId: number): void {
    this.isDetailsLoading.set(true);

    this.purchaseOrderService
      .getPurchaseOrderById(orderId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (orderDetail) => {
          this.selectedPurchaseOrder.set(orderDetail);
          this.isDetailsLoading.set(false);
          this.loadReceipts(orderDetail.id);
        },
        error: () => {
          this.isDetailsLoading.set(false);
          this.showDetailsLoadError();
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

  private purchaseOrderCreatePayload(): PurchaseOrderCreateRequest {
    const value = this.purchaseOrderForm.getRawValue();

    return {
      supplierId: value.supplierId as number,
      status: value.status,
      expectedDeliveryDate: value.expectedDeliveryDate || null,
      notes: this.optionalText(value.notes),
      items: this.itemsPayload()
    };
  }

  private purchaseOrderUpdatePayload(): PurchaseOrderUpdateRequest {
    const value = this.purchaseOrderForm.getRawValue();

    return {
      status: value.status,
      expectedDeliveryDate: value.expectedDeliveryDate || null,
      notes: this.optionalText(value.notes),
      items: this.itemsPayload()
    };
  }

  private itemsPayload(): PurchaseOrderItemRequest[] {
    return this.itemForms.controls.map((itemForm) => {
      const value = itemForm.getRawValue();

      return {
        productId: value.productId as number,
        quantity: value.quantity as number,
        unitCost: value.unitCost as number,
        notes: this.optionalText(value.notes)
      };
    });
  }

  private receivePayload(): PurchaseOrderReceiveRequest {
    const value = this.receiveForm.getRawValue();

    return {
      storageLocationId: value.storageLocationId as number,
      notes: this.optionalText(value.notes),
      items: this.receiveItemsPayload()
    };
  }

  private receiveItemsPayload(): PurchaseOrderReceiveItemRequest[] {
    return this.receiveItemForms.controls
      .map((itemForm) => itemForm.getRawValue())
      .filter((item) => item.receivedQuantity !== null && item.receivedQuantity > 0)
      .map((item) => ({
        purchaseOrderItemId: item.purchaseOrderItemId,
        receivedQuantity: item.receivedQuantity as number
      }));
  }

  private optionalText(value: string | null | undefined): string | null {
    return value?.trim() || null;
  }

  private cancelOrder(order: PurchaseOrder): void {
    this.purchaseOrderService
      .cancelPurchaseOrder(order.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.showSuccess('Pedido cancelado com sucesso.');
          this.loadPurchaseOrders(this.purchaseOrdersPage().number, this.pageSize(), this.sort());
        },
        error: () => this.showCancelError()
      });
  }

  private receivePurchaseOrder(payload: PurchaseOrderReceiveRequest): void {
    const order = this.purchaseOrderBeingReceived();

    if (!order || this.isReceiving()) {
      return;
    }

    this.isReceiving.set(true);

    this.purchaseOrderService
      .receivePurchaseOrder(order.id, payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isReceiving.set(false);
          this.isReceiveDialogVisible.set(false);
          this.showSuccess('Pedido recebido com sucesso.');
          this.loadPurchaseOrders(this.purchaseOrdersPage().number, this.pageSize(), this.sort());
        },
        error: () => {
          this.isReceiving.set(false);
          this.showReceiveError();
        }
      });
  }

  private reverseReceipt(): void {
    const receipt = this.receiptBeingReversed();

    if (!receipt || this.isReversingReceipt()) {
      return;
    }

    const payload: PurchaseOrderReceiptReverseRequest = {
      reason: this.optionalText(this.reverseReceiptForm.controls.reason.value) as string
    };

    this.isReversingReceipt.set(true);

    this.purchaseOrderService
      .reverseReceipt(receipt.purchaseOrderId, receipt.id, payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isReversingReceipt.set(false);
          this.isReverseDialogVisible.set(false);
          this.receiptBeingReversed.set(null);
          this.showSuccess('Recebimento estornado com sucesso.');
          this.refreshSelectedPurchaseOrderDetails(receipt.purchaseOrderId);
          this.loadPurchaseOrders(this.purchaseOrdersPage().number, this.pageSize(), this.sort());
        },
        error: () => {
          this.isReversingReceipt.set(false);
          this.showReverseReceiptError();
        }
      });
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
      summary: 'Erro ao carregar pedidos',
      detail: 'Não foi possível buscar os pedidos de compra. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showSaveError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao salvar pedido',
      detail: 'Não foi possível salvar o pedido de compra. Verifique os dados e tente novamente.',
      life: 5000
    });
  }

  private showCancelError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao cancelar pedido',
      detail: 'Não foi possível cancelar o pedido de compra.',
      life: 5000
    });
  }

  private showReceiveError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao receber pedido',
      detail: 'Não foi possível registrar o recebimento do pedido.',
      life: 5000
    });
  }

  private showReverseReceiptError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao estornar recebimento',
      detail: 'Não foi possível estornar o recebimento.',
      life: 5000
    });
  }

  private showEmptyReceiveWarning(): void {
    this.messageService.add({
      severity: 'warn',
      summary: 'Nenhum item selecionado',
      detail: 'Informe uma quantidade a receber em pelo menos um item pendente.',
      life: 4000
    });
  }

  private showDetailsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar pedido',
      detail: 'Não foi possível buscar os detalhes do pedido de compra.',
      life: 5000
    });
  }

  private showReceiptsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar recebimentos',
      detail: 'Não foi possível buscar o histórico de recebimentos do pedido.',
      life: 5000
    });
  }

  private showOptionsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar opções',
      detail: 'Não foi possível buscar fornecedores ou produtos. Verifique se a API está disponível.',
      life: 5000
    });
  }
}
