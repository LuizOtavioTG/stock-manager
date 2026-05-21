import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';

import { Page } from '../../../../models/page.model';
import { StorageLocation } from '../../models/storage-location.model';
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
  imports: [ButtonModule, CardModule, DatePipe, DecimalPipe, DialogModule, TableModule, TagModule],
  templateUrl: './storage-location-list.html',
  styleUrl: './storage-location-list.scss'
})
export class StorageLocationListComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);
  private readonly storageLocationService = inject(StorageLocationService);

  protected readonly isLoading = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly sort = signal('id,asc');
  protected readonly storageLocationsPage = signal<Page<StorageLocation>>(EMPTY_PAGE);
  protected readonly selectedStorageLocation = signal<StorageLocation | null>(null);
  protected readonly isDetailsDialogVisible = signal(false);
  protected readonly isDetailsLoading = signal(false);

  protected readonly storageLocations = computed(() => this.storageLocationsPage().content);
  protected readonly totalElements = computed(() => this.storageLocationsPage().totalElements);
  protected readonly first = computed(() => this.storageLocationsPage().number * this.storageLocationsPage().size);

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
