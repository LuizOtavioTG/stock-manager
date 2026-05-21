import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';

import { InventoryAlertsSummary } from '../../features/inventory/models/inventory-alerts-summary.model';
import { InventoryAlertsService } from '../../features/inventory/services/inventory-alerts.service';
import { StatCardComponent, StatCardSeverity } from '../../shared/components/stat-card/stat-card';

interface DashboardStatConfig {
  title: string;
  description: string;
  icon: string;
  severity: StatCardSeverity;
  tag: string;
  value: (summary: InventoryAlertsSummary) => number;
}

interface DashboardStat {
  title: string;
  value: number;
  description: string;
  icon: string;
  severity: StatCardSeverity;
  tag: string;
}

const EMPTY_SUMMARY: InventoryAlertsSummary = {
  outOfStockCount: 0,
  lowStockCount: 0,
  reorderNeededCount: 0,
  overstockCount: 0
};

@Component({
  selector: 'app-dashboard',
  imports: [StatCardComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly inventoryAlertsService = inject(InventoryAlertsService);
  private readonly messageService = inject(MessageService);

  protected readonly isLoading = signal(true);
  protected readonly summary = signal<InventoryAlertsSummary>(EMPTY_SUMMARY);

  private readonly statConfigs: DashboardStatConfig[] = [
    {
      title: 'Sem estoque',
      description: 'Itens sem saldo disponível para operação.',
      icon: 'pi pi-times-circle',
      severity: 'danger',
      tag: 'Crítico',
      value: (summary) => summary.outOfStockCount
    },
    {
      title: 'Estoque baixo',
      description: 'Itens abaixo do nível mínimo configurado.',
      icon: 'pi pi-exclamation-triangle',
      severity: 'warn',
      tag: 'Atenção',
      value: (summary) => summary.lowStockCount
    },
    {
      title: 'Reposição necessária',
      description: 'Itens que já atingiram o ponto de reposição.',
      icon: 'pi pi-refresh',
      severity: 'info',
      tag: 'Comprar',
      value: (summary) => summary.reorderNeededCount
    },
    {
      title: 'Estoque excedente',
      description: 'Itens acima do limite operacional esperado.',
      icon: 'pi pi-arrow-up-right',
      severity: 'secondary',
      tag: 'Revisar',
      value: (summary) => summary.overstockCount
    }
  ];

  protected readonly stats = computed<DashboardStat[]>(() =>
    this.statConfigs.map((stat) => ({
      ...stat,
      value: stat.value(this.summary())
    }))
  );

  ngOnInit(): void {
    this.loadSummary();
  }

  protected loadSummary(): void {
    this.isLoading.set(true);

    this.inventoryAlertsService
      .getSummary()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (summary) => {
          this.summary.set(summary);
          this.isLoading.set(false);
        },
        error: () => {
          this.summary.set(EMPTY_SUMMARY);
          this.isLoading.set(false);
          this.showLoadError();
        }
      });
  }

  private showLoadError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erro ao carregar alertas',
      detail: 'Não foi possível buscar o resumo do inventário. Verifique se a API está disponível.',
      life: 5000
    });
  }
}
