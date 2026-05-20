import { NgClass } from '@angular/common';
import { Component, computed, inject, input, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs';

import { MenuItem } from 'primeng/api';

import { LayoutService } from '../service/layout.service';

@Component({
  selector: '[app-menuitem]',
  imports: [NgClass, RouterLink, RouterLinkActive],
  templateUrl: './app-menuitem.html',
  host: {
    '[class.active-menuitem]': 'isActive()',
    '[class.layout-root-menuitem]': 'root()'
  }
})
export class AppMenuItemComponent {
  private readonly layoutService = inject(LayoutService);
  private readonly router = inject(Router);

  readonly item = input.required<MenuItem>();
  readonly root = input(false);
  readonly parentPath = input<string | null>(null);

  protected readonly initialized = signal(false);

  protected readonly isVisible = computed(() => this.item().visible !== false);
  protected readonly hasChildren = computed(() => !!this.item().items?.length);
  protected readonly hasRouterLink = computed(() => !!this.item().routerLink);

  protected readonly fullPath = computed(() => {
    const itemPath = this.item()['path'] as string | undefined;

    if (!itemPath) {
      return this.parentPath();
    }

    const parent = this.parentPath();
    return parent && !itemPath.startsWith(parent) ? `${parent}${itemPath}` : itemPath;
  });

  protected readonly isActive = computed(() => {
    const activePath = this.layoutService.layoutState().activePath;
    const itemPath = this.fullPath();
    return !!itemPath && !!activePath?.startsWith(itemPath);
  });

  constructor() {
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
      if (this.item().routerLink) {
        this.updateActiveStateFromRoute();
      }
    });
  }

  ngOnInit(): void {
    if (this.item().routerLink) {
      this.updateActiveStateFromRoute();
    }
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.initialized.set(true));
  }

  protected itemClick(event: Event): void {
    const item = this.item();

    if (item.disabled) {
      event.preventDefault();
      return;
    }

    if (item.command) {
      item.command({ originalEvent: event, item });
    }

    if (this.hasChildren()) {
      this.layoutService.setActivePath(this.isActive() ? this.parentPath() : this.fullPath());
    } else {
      this.layoutService.closeMenu();
    }
  }

  private updateActiveStateFromRoute(): void {
    const routerLink = this.item().routerLink;

    if (!routerLink) {
      return;
    }

    const firstLink = Array.isArray(routerLink) ? routerLink[0] : routerLink;

    if (this.router.isActive(firstLink, { paths: 'exact', queryParams: 'ignored', matrixParams: 'ignored', fragment: 'ignored' })) {
      this.layoutService.setActivePath(this.parentPath());
    }
  }
}
