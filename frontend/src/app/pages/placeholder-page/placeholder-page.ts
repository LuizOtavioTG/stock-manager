import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-placeholder-page',
  templateUrl: './placeholder-page.html',
  styleUrl: './placeholder-page.scss'
})
export class PlaceholderPage {
  private readonly route = inject(ActivatedRoute);
  private readonly routeData = toSignal(this.route.data, { initialValue: this.route.snapshot.data });

  protected readonly title = computed(() => this.routeData()['title'] ?? 'Dashboard');
  protected readonly eyebrow = computed(() => this.routeData()['section'] ?? 'Stock Manager');
}
