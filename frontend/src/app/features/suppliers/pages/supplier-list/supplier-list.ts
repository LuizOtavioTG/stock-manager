import { DatePipe } from '@angular/common';
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
import { Supplier } from '../../models/supplier.model';
import { SupplierService } from '../../services/supplier.service';

const EMPTY_PAGE: Page<Supplier> = {
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
  selector: 'app-supplier-list',
  standalone: true,
  imports: [
    ButtonModule,
    DatePipe,
    DialogModule,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    TableModule,
    TagModule
  ],
  templateUrl: './supplier-list.html',
  styleUrl: './supplier-list.scss'
})
export class SupplierListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);
  private readonly supplierService = inject(SupplierService);

  protected readonly columns: PaginatedListColumn[] = [
    { label: 'Nome', field: 'name' },
    { label: 'Contato', field: 'contactName' },
    { label: 'Telefone', field: 'phoneNumber' },
    { label: 'E-mail', field: 'email' },
    { label: 'Endereço', field: 'address' },
    { label: 'Status', field: 'active' },
    { label: 'Ações', sortable: false, styleClass: 'actions-column' }
  ];

  protected readonly isLoading = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('name,asc');
  protected readonly suppliersPage = signal<Page<Supplier>>(EMPTY_PAGE);
  protected readonly selectedSupplier = signal<Supplier | null>(null);
  protected readonly isDetailsDialogVisible = signal(false);
  protected readonly isDetailsLoading = signal(false);

  protected readonly suppliers = computed(() => this.suppliersPage().content);
  protected readonly totalElements = computed(() => this.suppliersPage().totalElements);
  protected readonly first = computed(() => this.suppliersPage().number * this.suppliersPage().size);

  ngOnInit(): void {
    this.loadSuppliers(0, this.pageSize(), this.sort());
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null; sortField?: string | string[] | null; sortOrder?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);
    const sort = this.resolveSort(event.sortField, event.sortOrder);

    this.pageSize.set(rows);
    this.sort.set(sort);
    this.loadSuppliers(page, rows, sort);
  }

  protected loadSuppliers(page: number, size: number, sort = this.sort()): void {
    this.isLoading.set(true);

    this.supplierService
      .listSuppliers(page, size, sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (suppliersPage) => {
          this.suppliersPage.set(suppliersPage);
          this.isLoading.set(false);
        },
        error: () => {
          this.suppliersPage.set({ ...EMPTY_PAGE, size, number: page });
          this.isLoading.set(false);
          this.showLoadError();
        }
      });
  }

  protected openSupplierDetails(supplier: Supplier): void {
    this.isDetailsDialogVisible.set(true);
    this.isDetailsLoading.set(true);
    this.selectedSupplier.set(null);

    this.supplierService
      .getSupplierById(supplier.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (supplierDetail) => {
          this.selectedSupplier.set(supplierDetail);
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
      summary: 'Erro ao carregar fornecedores',
      detail: 'Não foi possível buscar os fornecedores cadastrados. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showDetailsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar fornecedor',
      detail: 'Não foi possível buscar os detalhes do fornecedor. Verifique se a API está disponível.',
      life: 5000
    });
  }
}
