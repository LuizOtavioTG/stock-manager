import { Component } from '@angular/core';

import { StatCardComponent, StatCardSeverity } from '../../shared/components/stat-card/stat-card';

interface DashboardStat {
  title: string;
  value: number;
  description: string;
  icon: string;
  severity: StatCardSeverity;
  tag: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [StatCardComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent {
  protected readonly stats: DashboardStat[] = [
    {
      title: 'Sem estoque',
      value: 8,
      description: 'Itens sem saldo disponível para operação.',
      icon: 'pi pi-times-circle',
      severity: 'danger',
      tag: 'Crítico'
    },
    {
      title: 'Estoque baixo',
      value: 14,
      description: 'Itens abaixo do nível mínimo configurado.',
      icon: 'pi pi-exclamation-triangle',
      severity: 'warn',
      tag: 'Atenção'
    },
    {
      title: 'Reposição necessária',
      value: 21,
      description: 'Itens que já atingiram o ponto de reposição.',
      icon: 'pi pi-refresh',
      severity: 'info',
      tag: 'Comprar'
    },
    {
      title: 'Estoque excedente',
      value: 5,
      description: 'Itens acima do limite operacional esperado.',
      icon: 'pi pi-arrow-up-right',
      severity: 'secondary',
      tag: 'Revisar'
    }
  ];
}
