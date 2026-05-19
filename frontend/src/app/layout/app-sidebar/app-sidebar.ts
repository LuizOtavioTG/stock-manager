import { Component, ElementRef, inject } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

import { AppMenuComponent } from '../app-menu/app-menu';
import { LayoutService } from '../service/layout.service';

@Component({
  selector: 'app-sidebar',
  imports: [AppMenuComponent],
  templateUrl: './app-sidebar.html',
  styleUrl: './app-sidebar.scss'
})
export class AppSidebarComponent {
  private readonly layoutService = inject(LayoutService);
  private readonly router = inject(Router);
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  private outsideClickListener: ((event: MouseEvent) => void) | null = null;

  constructor() {
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe((event) => {
      this.onRouteChange(event.urlAfterRedirects);
    });
  }

  ngOnInit(): void {
    this.onRouteChange(this.router.url);
    document.addEventListener('click', this.handleDocumentClick);
  }

  ngOnDestroy(): void {
    document.removeEventListener('click', this.handleDocumentClick);
    this.unbindOutsideClickListener();
  }

  private readonly handleDocumentClick = (): void => {
    if (this.layoutService.isSidebarActive()) {
      this.bindOutsideClickListener();
    } else {
      this.unbindOutsideClickListener();
    }
  };

  private onRouteChange(path: string): void {
    this.layoutService.layoutState.update((state) => ({
      ...state,
      activePath: path,
      overlayMenuActive: false,
      mobileMenuActive: false
    }));
  }

  private bindOutsideClickListener(): void {
    if (this.outsideClickListener) {
      return;
    }

    this.outsideClickListener = (event: MouseEvent) => {
      if (this.isOutsideClicked(event)) {
        this.layoutService.closeMenu();
      }
    };

    document.addEventListener('click', this.outsideClickListener);
  }

  private unbindOutsideClickListener(): void {
    if (!this.outsideClickListener) {
      return;
    }

    document.removeEventListener('click', this.outsideClickListener);
    this.outsideClickListener = null;
  }

  private isOutsideClicked(event: MouseEvent): boolean {
    const topbarButtonEl = document.querySelector('.layout-menu-button');
    const sidebarEl = this.elementRef.nativeElement;
    const target = event.target as Node;

    return !(sidebarEl.isSameNode(target) || sidebarEl.contains(target) || topbarButtonEl?.isSameNode(target) || topbarButtonEl?.contains(target));
  }
}
