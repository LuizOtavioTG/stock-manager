import { computed, effect, Injectable, signal } from '@angular/core';

type MenuMode = 'static' | 'overlay';

interface LayoutConfig {
  menuMode: MenuMode;
}

interface LayoutState {
  staticMenuDesktopInactive: boolean;
  overlayMenuActive: boolean;
  mobileMenuActive: boolean;
  activePath: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class LayoutService {
  readonly layoutConfig = signal<LayoutConfig>({
    menuMode: 'static'
  });

  readonly layoutState = signal<LayoutState>({
    staticMenuDesktopInactive: false,
    overlayMenuActive: false,
    mobileMenuActive: false,
    activePath: null
  });

  readonly isSidebarActive = computed(() => this.layoutState().overlayMenuActive || this.layoutState().mobileMenuActive);

  constructor() {
    effect(() => {
      if (this.layoutState().mobileMenuActive) {
        document.body.classList.add('blocked-scroll');
      } else {
        document.body.classList.remove('blocked-scroll');
      }
    });
  }

  onMenuToggle(): void {
    if (this.layoutConfig().menuMode === 'overlay') {
      this.layoutState.update((state) => ({ ...state, overlayMenuActive: !state.overlayMenuActive }));
      return;
    }

    if (this.isDesktop()) {
      this.layoutState.update((state) => ({ ...state, staticMenuDesktopInactive: !state.staticMenuDesktopInactive }));
    } else {
      this.layoutState.update((state) => ({ ...state, mobileMenuActive: !state.mobileMenuActive }));
    }
  }

  closeMenu(): void {
    this.layoutState.update((state) => ({
      ...state,
      overlayMenuActive: false,
      mobileMenuActive: false
    }));
  }

  setActivePath(path: string | null): void {
    this.layoutState.update((state) => ({ ...state, activePath: path }));
  }

  isDesktop(): boolean {
    return window.innerWidth > 991;
  }
}
