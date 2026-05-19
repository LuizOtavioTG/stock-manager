import { NgClass } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { AppSidebarComponent } from '../app-sidebar/app-sidebar';
import { AppTopbarComponent } from '../app-topbar/app-topbar';
import { LayoutService } from '../service/layout.service';

@Component({
  selector: 'app-layout',
  imports: [AppSidebarComponent, AppTopbarComponent, NgClass, RouterOutlet],
  templateUrl: './app-layout.html',
  styleUrl: './app-layout.scss'
})
export class AppLayoutComponent {
  protected readonly layoutService = inject(LayoutService);

  protected readonly containerClass = computed(() => {
    const config = this.layoutService.layoutConfig();
    const state = this.layoutService.layoutState();

    return {
      'layout-overlay': config.menuMode === 'overlay',
      'layout-static': config.menuMode === 'static',
      'layout-static-inactive': state.staticMenuDesktopInactive && config.menuMode === 'static',
      'layout-overlay-active': state.overlayMenuActive,
      'layout-mobile-active': state.mobileMenuActive
    };
  });
}
