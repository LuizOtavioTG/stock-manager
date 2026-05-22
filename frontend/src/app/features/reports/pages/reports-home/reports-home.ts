import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';

import { InventoryAlertsSummary } from '../../../inventory/models/inventory-alerts-summary.model';
import { InventoryAlertsService } from '../../../inventory/services/inventory-alerts.service';

interface SummaryCardConfig {
  title: string;
  icon: string;
  severity: 'danger' | 'warn' | 'info' | 'secondary';
  value: (summary: InventoryAlertsSummary) => number;
}

interface ReportCard {
  title: string;
  description: string;
  icon: string;
  tag: string;
  tagSeverity: 'success' | 'danger' | 'warn' | 'info' | 'secondary';
  route: string;
}

const EMPTY_SUMMARY: InventoryAlertsSummary = {
  outOfStockCount: 0,
  lowStockCount: 0,
  reorderNeededCount: 0,
  overstockCount: 0
};

@Component({
  selector: 'app-reports-home',
  imports: [ButtonModule, CardModule, RouterLink, TagModule],
  templateUrl: './reports-home.html',
  styleUrl: './reports-home.scss'
})
export class ReportsHomeComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly inventoryAlertsService = inject(InventoryAlertsService);
  private readonly messageService = inject(MessageService);

  protected readonly isLoading = signal(true);
  protected readonly summary = signal<InventoryAlertsSummary>(EMPTY_SUMMARY);

  private readonly summaryConfigs: SummaryCardConfig[] = [
    {
      title: 'Sem estoque',
      icon: 'pi pi-times-circle',
      severity: 'danger',
      value: (summary) => summary.outOfStockCount
    },
    {
      title: 'Estoque baixo',
      icon: 'pi pi-exclamation-triangle',
      severity: 'warn',
      value: (summary) => summary.lowStockCount
    },
    {
      title: 'Reposição necessária',
      icon: 'pi pi-refresh',
      severity: 'info',
      value: (summary) => summary.reorderNeededCount
    },
    {
      title: 'Estoque excedente',
      icon: 'pi pi-arrow-up-right',
      severity: 'secondary',
      value: (summary) => summary.overstockCount
    }
  ];

  protected readonly summaryCards = computed(() =>
    this.summaryConfigs.map((item) => ({
      ...item,
      value: item.value(this.summary())
    }))
  );

  protected readonly reportCards: ReportCard[] = [
    {
      title: 'Estoque baixo',
      description: 'Itens abaixo do estoque mínimo configurado.',
      icon: 'pi pi-exclamation-triangle',
      tag: 'Atenção',
      tagSeverity: 'warn',
      route: '/estoque/alertas'
    },
    {
      title: 'Sem estoque',
      description: 'Itens sem saldo disponível para operação.',
      icon: 'pi pi-times-circle',
      tag: 'Crítico',
      tagSeverity: 'danger',
      route: '/estoque/alertas'
    },
    {
      title: 'Reposição necessária',
      description: 'Itens que chegaram ao ponto de reposição.',
      icon: 'pi pi-refresh',
      tag: 'Comprar',
      tagSeverity: 'info',
      route: '/estoque/alertas'
    },
    {
      title: 'Estoque excedente',
      description: 'Itens acima do limite máximo esperado.',
      icon: 'pi pi-arrow-up-right',
      tag: 'Revisar',
      tagSeverity: 'secondary',
      route: '/estoque/alertas'
    },
    {
      title: 'Movimentações',
      description: 'Histórico de entradas, saídas e ajustes de estoque.',
      icon: 'pi pi-arrow-right-arrow-left',
      tag: 'Histórico',
      tagSeverity: 'success',
      route: '/estoque/movimentacoes'
    },
    {
      title: 'Produtos cadastrados',
      description: 'Consulta dos produtos ativos e inativos do catálogo.',
      icon: 'pi pi-box',
      tag: 'Cadastro',
      tagSeverity: 'info',
      route: '/cadastros/produtos'
    }
  ];

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
      summary: 'Erro ao carregar resumo dos relatórios',
      detail: 'Não foi possível buscar o resumo dos relatórios. Verifique se a API está disponível.',
      life: 5000
    });
  }
}
