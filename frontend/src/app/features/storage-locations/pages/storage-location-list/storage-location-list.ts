import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
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
import { StorageLocation } from '../../models/storage-location.model';
import {
  StorageLocationCreateRequest,
  StorageLocationUpdateRequest
} from '../../models/storage-location-request.model';
import { StorageLocationService } from '../../services/storage-location.service';

const EMPTY_PAGE: Page<StorageLocation> = {
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
  selector: 'app-storage-location-list',
  standalone: true,
  imports: [
    ButtonModule,
    ConfirmDialogModule,
    DatePipe,
    DecimalPipe,
    DialogModule,
    InputNumberModule,
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
  templateUrl: './storage-location-list.html',
  styleUrl: './storage-location-list.scss'
})
export class StorageLocationListComponent implements OnInit {
  private readonly confirmationService = inject(ConfirmationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly messageService = inject(MessageService);
  private readonly storageLocationService = inject(StorageLocationService);

  protected readonly columns: PaginatedListColumn[] = [
    { label: 'Nome', field: 'name' },
    { label: 'Tipo', field: 'type' },
    { label: 'Responsável', field: 'responsibleName' },
    { label: 'Capacidade', field: 'capacity', styleClass: 'numeric-column' },
    { label: 'Local padrão', field: 'defaultLocation' },
    { label: 'Contato', sortable: false },
    { label: 'Ações', sortable: false, styleClass: 'actions-column' }
  ];

  protected readonly isLoading = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('id,asc');
  protected readonly storageLocationsPage = signal<Page<StorageLocation>>(EMPTY_PAGE);
  protected readonly selectedStorageLocation = signal<StorageLocation | null>(null);
  protected readonly isDetailsDialogVisible = signal(false);
  protected readonly isDetailsLoading = signal(false);
  protected readonly isFormDialogVisible = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly storageLocationBeingEdited = signal<StorageLocation | null>(null);

  protected readonly storageLocations = computed(() => this.storageLocationsPage().content);
  protected readonly totalElements = computed(() => this.storageLocationsPage().totalElements);
  protected readonly first = computed(() => this.storageLocationsPage().number * this.storageLocationsPage().size);
  protected readonly formTitle = computed(() =>
    this.storageLocationBeingEdited() ? 'Editar local de estoque' : 'Novo local de estoque'
  );

  protected readonly storageLocationForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/\S/)]],
    type: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/\S/)]],
    address: this.formBuilder.control<string | null>(null, [Validators.maxLength(255)]),
    phoneNumber: this.formBuilder.control<string | null>(null, [Validators.maxLength(20)]),
    email: this.formBuilder.control<string | null>(null, [Validators.email, Validators.maxLength(100)]),
    responsibleName: this.formBuilder.control<string | null>(null, [Validators.maxLength(100)]),
    notes: this.formBuilder.control<string | null>(null, [Validators.maxLength(500)]),
    capacity: this.formBuilder.control<number | null>(null, [Validators.min(0)]),
    defaultLocation: this.formBuilder.control<boolean>(false, {
      nonNullable: true,
      validators: [Validators.required]
    }),
    latitude: this.formBuilder.control<number | null>(null, [Validators.min(-90), Validators.max(90)]),
    longitude: this.formBuilder.control<number | null>(null, [Validators.min(-180), Validators.max(180)])
  });

  ngOnInit(): void {
    this.loadStorageLocations(0, this.pageSize(), this.sort());
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null; sortField?: string | string[] | null; sortOrder?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);
    const sort = this.resolveSort(event.sortField, event.sortOrder);

    this.pageSize.set(rows);
    this.sort.set(sort);
    this.loadStorageLocations(page, rows, sort);
  }

  protected loadStorageLocations(page: number, size: number, sort = this.sort()): void {
    this.isLoading.set(true);

    this.storageLocationService
      .listStorageLocations(page, size, sort)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (storageLocationsPage) => {
          this.storageLocationsPage.set(storageLocationsPage);
          this.isLoading.set(false);
        },
        error: () => {
          this.storageLocationsPage.set({ ...EMPTY_PAGE, size, number: page });
          this.isLoading.set(false);
          this.showLoadError();
        }
      });
  }

  protected openCreateDialog(): void {
    this.storageLocationBeingEdited.set(null);
    this.storageLocationForm.reset({
      name: '',
      type: '',
      address: null,
      phoneNumber: null,
      email: null,
      responsibleName: null,
      notes: null,
      capacity: null,
      defaultLocation: false,
      latitude: null,
      longitude: null
    });
    this.storageLocationForm.markAsPristine();
    this.storageLocationForm.markAsUntouched();
    this.isFormDialogVisible.set(true);
  }

  protected openEditDialog(storageLocation: StorageLocation): void {
    this.storageLocationService
      .getStorageLocationById(storageLocation.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (storageLocationDetail) => {
          this.storageLocationBeingEdited.set(storageLocationDetail);
          this.storageLocationForm.reset({
            name: storageLocationDetail.name,
            type: storageLocationDetail.type ?? '',
            address: storageLocationDetail.address,
            phoneNumber: storageLocationDetail.phoneNumber,
            email: storageLocationDetail.email,
            responsibleName: storageLocationDetail.responsibleName,
            notes: storageLocationDetail.notes,
            capacity: storageLocationDetail.capacity,
            defaultLocation: storageLocationDetail.defaultLocation,
            latitude: storageLocationDetail.latitude,
            longitude: storageLocationDetail.longitude
          });
          this.storageLocationForm.markAsPristine();
          this.storageLocationForm.markAsUntouched();
          this.isFormDialogVisible.set(true);
        },
        error: () => this.showDetailsLoadError()
      });
  }

  protected saveStorageLocation(): void {
    if (this.storageLocationForm.invalid) {
      this.storageLocationForm.markAllAsTouched();
      return;
    }

    const editingStorageLocation = this.storageLocationBeingEdited();
    const payload = this.storageLocationPayload();
    const request = editingStorageLocation
      ? this.storageLocationService.updateStorageLocation(editingStorageLocation.id, payload)
      : this.storageLocationService.createStorageLocation(payload);

    this.isSaving.set(true);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.isFormDialogVisible.set(false);
        this.showSuccess(
          editingStorageLocation
            ? 'Local de estoque atualizado com sucesso.'
            : 'Local de estoque criado com sucesso.'
        );
        this.loadStorageLocations(this.storageLocationsPage().number, this.pageSize(), this.sort());
      },
      error: () => {
        this.isSaving.set(false);
        this.showSaveError();
      }
    });
  }

  protected confirmDeleteStorageLocation(storageLocation: StorageLocation): void {
    this.confirmationService.confirm({
      header: 'Excluir local de estoque',
      message: `Deseja excluir o local "${storageLocation.name}"?`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deleteStorageLocation(storageLocation)
    });
  }

  protected shouldShowError(
    controlName:
      | 'name'
      | 'type'
      | 'address'
      | 'phoneNumber'
      | 'email'
      | 'responsibleName'
      | 'notes'
      | 'capacity'
      | 'latitude'
      | 'longitude'
  ): boolean {
    const control = this.storageLocationForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  protected openStorageLocationDetails(storageLocation: StorageLocation): void {
    this.isDetailsDialogVisible.set(true);
    this.isDetailsLoading.set(true);
    this.selectedStorageLocation.set(null);

    this.storageLocationService
      .getStorageLocationById(storageLocation.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (storageLocationDetail) => {
          this.selectedStorageLocation.set(storageLocationDetail);
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

  private storageLocationPayload(): StorageLocationCreateRequest & StorageLocationUpdateRequest {
    const value = this.storageLocationForm.getRawValue();

    return {
      name: value.name.trim(),
      type: value.type.trim(),
      address: this.optionalText(value.address),
      phoneNumber: this.optionalText(value.phoneNumber),
      email: this.optionalText(value.email),
      responsibleName: this.optionalText(value.responsibleName),
      notes: this.optionalText(value.notes),
      capacity: value.capacity ?? null,
      defaultLocation: value.defaultLocation,
      latitude: value.latitude ?? null,
      longitude: value.longitude ?? null
    };
  }

  private optionalText(value: string | null): string | null {
    return value?.trim() || null;
  }

  private deleteStorageLocation(storageLocation: StorageLocation): void {
    this.storageLocationService
      .deleteStorageLocation(storageLocation.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.showSuccess('Local de estoque excluído com sucesso.');
          const currentPage = this.storageLocationsPage();
          const shouldGoBack = currentPage.content.length === 1 && currentPage.number > 0;
          const nextPage = shouldGoBack ? currentPage.number - 1 : currentPage.number;

          this.loadStorageLocations(nextPage, this.pageSize(), this.sort());
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
      summary: 'Erro ao salvar local de estoque',
      detail: 'Não foi possível salvar o local de estoque. Verifique os dados e tente novamente.',
      life: 5000
    });
  }

  private showDeleteError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao excluir local de estoque',
      detail: 'Não foi possível excluir o local de estoque. Verifique se ele não está em uso.',
      life: 5000
    });
  }

  private showLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar locais de estoque',
      detail: 'Não foi possível buscar os locais cadastrados. Verifique se a API está disponível.',
      life: 5000
    });
  }

  private showDetailsLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar local de estoque',
      detail: 'Não foi possível buscar os detalhes do local de estoque. Verifique se a API está disponível.',
      life: 5000
    });
  }
}
