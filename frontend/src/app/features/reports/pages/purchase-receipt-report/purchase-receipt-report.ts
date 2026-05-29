import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';

import { Page } from '../../../../models/page.model';
import {
  PaginatedListBodyDirective,
  PaginatedListColumn,
  PaginatedListComponent,
  PaginatedListEmptyDirective,
  PaginatedListToolbarDirective
} from '../../../../shared/components/paginated-list/paginated-list';
import { CsvExportService } from '../../../../shared/services/csv-export.service';
import { PurchaseOrder, PurchaseOrderStatus } from '../../../purchase-orders/models/purchase-order.model';
import { PurchaseOrderReceipt } from '../../../purchase-orders/models/purchase-order-receipt.model';
import { PurchaseOrderService } from '../../../purchase-orders/services/purchase-order.service';
import { Supplier } from '../../../suppliers/models/supplier.model';
import { SupplierService } from '../../../suppliers/services/supplier.service';

type TagSeverity = 'success' | 'secondary' | 'info' | 'warn' | 'danger';

interface StatusOption {
  label: string;
  value: PurchaseOrderStatus;
}

interface SummaryCard {
  title: string;
  value: string | number;
  icon: string;
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
  selector: 'app-purchase-receipt-report',
  imports: [
    ButtonModule,
    CurrencyPipe,
    DatePipe,
    DecimalPipe,
    DialogModule,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    PaginatedListToolbarDirective,
    ReactiveFormsModule,
    SelectModule,
    TableModule,
    TagModule
  ],
  templateUrl: './purchase-receipt-report.html',
  styleUrl: './purchase-receipt-report.scss'
})
export class PurchaseReceiptReportComponent implements OnInit {
  private readonly csvExportService = inject(CsvExportService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly messageService = inject(MessageService);
  private readonly purchaseOrderService = inject(PurchaseOrderService);
  private readonly supplierService = inject(SupplierService);

  protected readonly columns: PaginatedListColumn[] = [
    { label: 'ID', field: 'id' },
    { label: 'Fornecedor', field: 'supplierName' },
    { label: 'Status', field: 'status' },
    { label: 'Data do pedido', field: 'orderDate' },
    { label: 'Entrega prevista', field: 'expectedDeliveryDate' },
    { label: 'Total estimado', field: 'totalEstimatedCost', styleClass: 'numeric-column' },
    { label: 'Total de itens', sortable: false, styleClass: 'numeric-column' },
    { label: 'Quantidade pedida', sortable: false, styleClass: 'numeric-column' },
    { label: 'Quantidade recebida', sortable: false, styleClass: 'numeric-column' },
    { label: 'Quantidade pendente', sortable: false, styleClass: 'numeric-column' },
    { label: 'Percentual recebido', sortable: false, styleClass: 'numeric-column' },
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
  protected readonly isLoadingReceipts = signal(false);
  protected readonly isLoadingSupplierOptions = signal(false);
  protected readonly isReceiptsDialogVisible = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('orderDate,desc');
  protected readonly ordersPage = signal<Page<PurchaseOrder>>(EMPTY_PAGE);
  protected readonly receiptsPage = signal<Page<PurchaseOrderReceipt>>(EMPTY_RECEIPTS_PAGE);
  protected readonly selectedOrder = signal<PurchaseOrder | null>(null);
  protected readonly supplierOptions = signal<Supplier[]>([]);

  protected readonly orders = computed(() => this.ordersPage().content);
  protected readonly receipts = computed(() => this.receiptsPage().content);
  protected readonly totalElements = computed(() => this.ordersPage().totalElements);
  protected readonly first = computed(() => this.ordersPage().number * this.ordersPage().size);
  protected readonly summaryCards = computed<SummaryCard[]>(() => {
    const orders = this.orders();
    const totalEstimatedCost = orders.reduce((total, order) => total + order.totalEstimatedCost, 0);
    const orderedQuantity = orders.reduce((total, order) => total + this.orderedQuantity(order), 0);
    const receivedQuantity = orders.reduce((total, order) => total + this.receivedQuantity(order), 0);
    const pendingQuantity = orders.reduce((total, order) => total + this.pendingQuantity(order), 0);
    const pendingOrders = orders.filter((order) => this.isPendingOrder(order)).length;

    return [
      { title: 'Total de pedidos', value: orders.length, icon: 'pi pi-list' },
      { title: 'Valor total estimado', value: this.currencyValue(totalEstimatedCost), icon: 'pi pi-dollar' },
      { title: 'Quantidade total pedida', value: orderedQuantity, icon: 'pi pi-shopping-cart' },
      { title: 'Quantidade total recebida', value: receivedQuantity, icon: 'pi pi-inbox' },
      { title: 'Quantidade total pendente', value: pendingQuantity, icon: 'pi pi-clock' },
      { title: 'Pedidos pendentes', value: pendingOrders, icon: 'pi pi-exclamation-circle' }
    ];
  });

  protected readonly filterForm = this.formBuilder.group({
    supplierId: this.formBuilder.control<number | null>(null),
    status: this.formBuilder.control<PurchaseOrderStatus | null>(null)
  });

  ngOnInit(): void {
    this.loadSupplierOptions();
    this.loadOrders(0, this.pageSize(), this.sort());
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null; sortField?: string | string[] | null; sortOrder?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);
    const sort = this.resolveSort(event.sortField, event.sortOrder);

    this.pageSize.set(rows);
    this.sort.set(sort);
    this.loadOrders(page, rows, sort);
  }

  protected loadOrders(page: number, size: number, sort = this.sort()): void {
    this.isLoading.set(true);

    const filters = this.filterForm.getRawValue();
    const request = filters.supplierId
      ? this.purchaseOrderService.listBySupplier(filters.supplierId, page, size, sort)
      : filters.status
        ? this.purchaseOrderService.listByStatus(filters.status, page, size, sort)
        : this.purchaseOrderService.listPurchaseOrders(page, size, sort);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (ordersPage) => {
        this.ordersPage.set(ordersPage);
        this.isLoading.set(false);
      },
      error: () => {
        this.ordersPage.set({ ...EMPTY_PAGE, size, number: page });
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
    this.loadOrders(0, this.pageSize(), this.sort());
  }

  protected clearFilters(): void {
    this.filterForm.reset({ supplierId: null, status: null });
    this.loadOrders(0, this.pageSize(), this.sort());
  }

  protected openReceiptsDialog(order: PurchaseOrder): void {
    this.selectedOrder.set(order);
    this.receiptsPage.set(EMPTY_RECEIPTS_PAGE);
    this.isReceiptsDialogVisible.set(true);
    this.loadReceipts(order.id);
  }

  protected exportCsv(): void {
    const rows = this.orders();

    if (!rows.length) {
      this.showEmptyExportWarning();
      return;
    }

    this.csvExportService.exportToCsv('relatorio-compras-recebimentos.csv', rows, [
      { header: 'ID', value: (row) => row.id },
      { header: 'Fornecedor', value: (row) => row.supplierName },
      { header: 'Status', value: (row) => this.statusLabel(row.status) },
      { header: 'Data do pedido', value: (row) => row.orderDate },
      { header: 'Entrega prevista', value: (row) => row.expectedDeliveryDate },
      { header: 'Total estimado', value: (row) => row.totalEstimatedCost },
      { header: 'Quantidade pedida', value: (row) => this.orderedQuantity(row) },
      { header: 'Quantidade recebida', value: (row) => this.receivedQuantity(row) },
      { header: 'Quantidade pendente', value: (row) => this.pendingQuantity(row) },
      { header: 'Percentual recebido', value: (row) => `${this.receivedPercent(row).toFixed(2)}%` }
    ]);
  }

  protected orderedQuantity(order: PurchaseOrder): number {
    return order.items.reduce((total, item) => total + item.quantity, 0);
  }

  protected receivedQuantity(order: PurchaseOrder): number {
    return order.items.reduce((total, item) => total + item.receivedQuantity, 0);
  }

  protected pendingQuantity(order: PurchaseOrder): number {
    return order.items.reduce((total, item) => total + item.pendingQuantity, 0);
  }

  protected receivedPercent(order: PurchaseOrder): number {
    const orderedQuantity = this.orderedQuantity(order);

    if (!orderedQuantity) {
      return 0;
    }

    return (this.receivedQuantity(order) / orderedQuantity) * 100;
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
    return status === 'ACTIVE' ? 'Ativo' : 'Estornado';
  }

  protected receiptStatusSeverity(status: PurchaseOrderReceipt['status']): TagSeverity {
    return status === 'ACTIVE' ? 'success' : 'danger';
  }

  private loadReceipts(orderId: number): void {
    this.isLoadingReceipts.set(true);

    this.purchaseOrderService
      .listReceipts(orderId, 0, 20, 'receiptDate,desc')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (receiptsPage) => {
          this.receiptsPage.set(receiptsPage);
          this.isLoadingReceipts.set(false);
        },
        error: () => {
          this.receiptsPage.set(EMPTY_RECEIPTS_PAGE);
          this.isLoadingReceipts.set(false);
          this.showReceiptsLoadError();
        }
      });
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

  private isPendingOrder(order: PurchaseOrder): boolean {
    return order.status === 'DRAFT' || order.status === 'SENT' || order.status === 'PARTIALLY_RECEIVED';
  }

  private currencyValue(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
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
      summary: 'Erro ao carregar relatório de compras e recebimentos',
      detail: 'Não foi possível buscar os pedidos de compra. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showReceiptsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar recebimentos do pedido',
      detail: 'Não foi possível buscar o histórico de recebimentos deste pedido.',
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
      summary: 'Erro ao carregar fornecedores',
      detail: 'Não foi possível buscar os fornecedores para o filtro.',
      life: 5000
    });
  }
}
