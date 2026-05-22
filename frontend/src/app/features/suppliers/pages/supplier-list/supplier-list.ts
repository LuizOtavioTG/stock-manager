import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';
import { ToggleSwitchModule } from 'primeng/toggleswitch';

import { Page } from '../../../../models/page.model';
import {
  PaginatedListActionsDirective,
  PaginatedListBodyDirective,
  PaginatedListColumn,
  PaginatedListComponent,
  PaginatedListEmptyDirective
} from '../../../../shared/components/paginated-list/paginated-list';
import { Supplier } from '../../models/supplier.model';
import { SupplierCreateRequest, SupplierUpdateRequest } from '../../models/supplier-request.model';
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
    ConfirmDialogModule,
    DatePipe,
    DialogModule,
    InputTextModule,
    PaginatedListActionsDirective,
    PaginatedListBodyDirective,
    PaginatedListComponent,
    PaginatedListEmptyDirective,
    ReactiveFormsModule,
    TableModule,
    TagModule,
    TextareaModule,
    ToggleSwitchModule
  ],
  templateUrl: './supplier-list.html',
  styleUrl: './supplier-list.scss'
})
export class SupplierListComponent implements OnInit {
  private readonly confirmationService = inject(ConfirmationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
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
  protected readonly isFormDialogVisible = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly supplierBeingEdited = signal<Supplier | null>(null);

  protected readonly suppliers = computed(() => this.suppliersPage().content);
  protected readonly totalElements = computed(() => this.suppliersPage().totalElements);
  protected readonly first = computed(() => this.suppliersPage().number * this.suppliersPage().size);
  protected readonly formTitle = computed(() =>
    this.supplierBeingEdited() ? 'Editar fornecedor' : 'Novo fornecedor'
  );

  protected readonly supplierForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(255), Validators.pattern(/\S/)]],
    contactName: this.formBuilder.control<string | null>(null, [Validators.maxLength(255)]),
    phoneNumber: this.formBuilder.control<string | null>(null, [Validators.maxLength(20)]),
    email: this.formBuilder.control<string | null>(null, [Validators.email, Validators.maxLength(255)]),
    address: this.formBuilder.control<string | null>(null, [Validators.maxLength(500)]),
    active: this.formBuilder.control<boolean | null>(true)
  });

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

  protected openCreateDialog(): void {
    this.supplierBeingEdited.set(null);
    this.supplierForm.reset({
      name: '',
      contactName: null,
      phoneNumber: null,
      email: null,
      address: null,
      active: true
    });
    this.supplierForm.markAsPristine();
    this.supplierForm.markAsUntouched();
    this.isFormDialogVisible.set(true);
  }

  protected openEditDialog(supplier: Supplier): void {
    this.supplierService
      .getSupplierById(supplier.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (supplierDetail) => {
          this.supplierBeingEdited.set(supplierDetail);
          this.supplierForm.reset({
            name: supplierDetail.name,
            contactName: supplierDetail.contactName,
            phoneNumber: supplierDetail.phoneNumber,
            email: supplierDetail.email,
            address: supplierDetail.address,
            active: supplierDetail.active
          });
          this.supplierForm.markAsPristine();
          this.supplierForm.markAsUntouched();
          this.isFormDialogVisible.set(true);
        },
        error: () => this.showDetailsLoadError()
      });
  }

  protected saveSupplier(): void {
    if (this.supplierForm.invalid) {
      this.supplierForm.markAllAsTouched();
      return;
    }

    const editingSupplier = this.supplierBeingEdited();
    const request = editingSupplier
      ? this.supplierService.updateSupplier(editingSupplier.id, this.supplierUpdatePayload())
      : this.supplierService.createSupplier(this.supplierCreatePayload());

    this.isSaving.set(true);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.isFormDialogVisible.set(false);
        this.showSuccess(editingSupplier ? 'Fornecedor atualizado com sucesso.' : 'Fornecedor criado com sucesso.');
        this.loadSuppliers(this.suppliersPage().number, this.pageSize(), this.sort());
      },
      error: () => {
        this.isSaving.set(false);
        this.showSaveError();
      }
    });
  }

  protected confirmDeleteSupplier(supplier: Supplier): void {
    this.confirmationService.confirm({
      header: 'Excluir fornecedor',
      message: `Deseja excluir o fornecedor "${supplier.name}"?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deleteSupplier(supplier)
    });
  }

  protected shouldShowError(controlName: 'name' | 'contactName' | 'phoneNumber' | 'email' | 'address'): boolean {
    const control = this.supplierForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
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

  private resolveSort(sortField?: string | string[] | null, sortOrder?: number | null): string {
    if (!sortField || Array.isArray(sortField)) {
      return this.sort();
    }

    return `${sortField},${sortOrder === -1 ? 'desc' : 'asc'}`;
  }

  private supplierCreatePayload(): SupplierCreateRequest {
    const value = this.supplierForm.getRawValue();

    return {
      name: value.name.trim(),
      contactName: this.optionalText(value.contactName),
      phoneNumber: this.optionalText(value.phoneNumber),
      email: this.optionalText(value.email),
      address: this.optionalText(value.address)
    };
  }

  private supplierUpdatePayload(): SupplierUpdateRequest {
    return {
      ...this.supplierCreatePayload(),
      active: this.supplierForm.controls.active.value ?? true
    };
  }

  private optionalText(value: string | null): string | null {
    return value?.trim() || null;
  }

  private deleteSupplier(supplier: Supplier): void {
    this.supplierService
      .deleteSupplier(supplier.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.showSuccess('Fornecedor excluído com sucesso.');
          const currentPage = this.suppliersPage();
          const shouldGoBack = currentPage.content.length === 1 && currentPage.number > 0;
          const nextPage = shouldGoBack ? currentPage.number - 1 : currentPage.number;

          this.loadSuppliers(nextPage, this.pageSize(), this.sort());
        },
        error: () => this.showDeleteError()
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

  private showSaveError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao salvar fornecedor',
      detail: 'Não foi possível salvar o fornecedor. Verifique os dados e tente novamente.',
      life: 5000
    });
  }

  private showDeleteError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao excluir fornecedor',
      detail: 'Não foi possível excluir o fornecedor. Verifique se ele não está em uso.',
      life: 5000
    });
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
