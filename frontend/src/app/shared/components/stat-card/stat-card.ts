import { Component, computed, input } from '@angular/core';

import { CardModule } from 'primeng/card';
import { SkeletonModule } from 'primeng/skeleton';
import { TagModule } from 'primeng/tag';

export type StatCardSeverity = 'danger' | 'warn' | 'info' | 'success' | 'secondary' | 'contrast';

@Component({
  selector: 'app-stat-card',
  imports: [CardModule, SkeletonModule, TagModule],
  templateUrl: './stat-card.html',
  styleUrl: './stat-card.scss'
})
export class StatCardComponent {
  readonly title = input.required<string>();
  readonly value = input.required<number>();
  readonly description = input.required<string>();
  readonly icon = input.required<string>();
  readonly severity = input<StatCardSeverity>('info');
  readonly tag = input<string>();
  readonly loading = input(false);

  protected readonly cardClass = computed(() => `stat-card stat-card--${this.severity()}`);
}
