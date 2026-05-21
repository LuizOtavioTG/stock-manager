import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';

import { Page } from '../../../../models/page.model';
import { InventoryAlertItem, StockStatus } from '../../models/inventory-alert-item.model';
import { InventoryAlertType, InventoryAlertsService } from '../../services/inventory-alerts.service';

interface AlertFilterOption {
  label: string;
  description: string;
  type: InventoryAlertType;
  icon: string;
  severity: 'danger' | 'warn' | 'info' | 'secondary';
}

const EMPTY_PAGE: Page<InventoryAlertItem> = {
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
  selector: 'app-inventory-alerts',
  imports: [ButtonModule, CardModule, TableModule, TagModule],
  templateUrl: './inventory-alerts.html',
  styleUrl: './inventory-alerts.scss'
})
export class InventoryAlertsComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly inventoryAlertsService = inject(InventoryAlertsService);
  private readonly messageService = inject(MessageService);

  protected readonly alertOptions: AlertFilterOption[] = [
    {
      label: 'Sem estoque',
      description: 'Itens sem saldo disponível.',
      type: 'out-of-stock',
      icon: 'pi pi-times-circle',
      severity: 'danger'
    },
    {
      label: 'Estoque baixo',
      description: 'Itens abaixo do estoque mínimo.',
      type: 'low-stock',
      icon: 'pi pi-exclamation-triangle',
      severity: 'warn'
    },
    {
      label: 'Reposição necessária',
      description: 'Itens no ponto de reposição.',
      type: 'reorder-needed',
      icon: 'pi pi-refresh',
      severity: 'info'
    },
    {
      label: 'Estoque excedente',
      description: 'Itens acima do limite máximo.',
      type: 'overstock',
      icon: 'pi pi-arrow-up-right',
      severity: 'secondary'
    }
  ];

  protected readonly selectedType = signal<InventoryAlertType>('out-of-stock');
  protected readonly isLoading = signal(false);
  protected readonly pageSize = signal(10);
  protected readonly alertsPage = signal<Page<InventoryAlertItem>>(EMPTY_PAGE);

  protected readonly selectedOption = computed(() =>
    this.alertOptions.find((option) => option.type === this.selectedType()) ?? this.alertOptions[0]
  );
  protected readonly items = computed(() => this.alertsPage().content);
  protected readonly totalElements = computed(() => this.alertsPage().totalElements);
  protected readonly first = computed(() => this.alertsPage().number * this.alertsPage().size);

  ngOnInit(): void {
    this.loadAlerts(this.selectedType(), 0, this.pageSize());
  }

  protected selectAlertType(type: InventoryAlertType): void {
    if (this.selectedType() === type) {
      return;
    }

    this.selectedType.set(type);
    this.loadAlerts(type, 0, this.pageSize());
  }

  protected onPageChange(event: { first?: number | null; rows?: number | null }): void {
    const rows = event.rows ?? this.pageSize();
    const first = event.first ?? 0;
    const page = Math.floor(first / rows);

    this.pageSize.set(rows);
    this.loadAlerts(this.selectedType(), page, rows);
  }

  protected statusLabel(status: StockStatus): string {
    const labels: Record<StockStatus, string> = {
      OUT_OF_STOCK: 'Sem estoque',
      LOW_STOCK: 'Estoque baixo',
      REORDER_NEEDED: 'Reposição necessária',
      NORMAL: 'Normal',
      OVERSTOCK: 'Excedente'
    };

    return labels[status];
  }

  protected statusSeverity(status: StockStatus): 'danger' | 'warn' | 'info' | 'success' | 'secondary' {
    const severities: Record<StockStatus, 'danger' | 'warn' | 'info' | 'success' | 'secondary'> = {
      OUT_OF_STOCK: 'danger',
      LOW_STOCK: 'warn',
      REORDER_NEEDED: 'info',
      NORMAL: 'success',
      OVERSTOCK: 'secondary'
    };

    return severities[status];
  }

  protected loadAlerts(type: InventoryAlertType, page: number, size: number): void {
    this.isLoading.set(true);

    this.inventoryAlertsService
      .getAlerts(type, { page, size })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (alertsPage) => {
          this.alertsPage.set(alertsPage);
          this.isLoading.set(false);
        },
        error: () => {
          this.alertsPage.set({ ...EMPTY_PAGE, size, number: page });
          this.isLoading.set(false);
          this.showLoadError();
        }
      });
  }

  private showLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar alertas',
      detail: 'Não foi possível buscar os alertas de estoque. Verifique se a API está disponível.',
      life: 5000
    });
  }
}
