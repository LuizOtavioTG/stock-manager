import { TestBed } from '@angular/core/testing';

import { LayoutService } from './layout.service';

describe('LayoutService', () => {
  let service: LayoutService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LayoutService);
  });

  it('should toggle and close the static desktop menu', () => {
    spyOn(service, 'isDesktop').and.returnValue(true);

    service.onMenuToggle();
    expect(service.layoutState().staticMenuDesktopInactive).toBeTrue();

    service.onMenuToggle();
    expect(service.layoutState().staticMenuDesktopInactive).toBeFalse();
  });

  it('should close mobile and overlay menu states', () => {
    service.layoutState.update((state) => ({
      ...state,
      mobileMenuActive: true,
      overlayMenuActive: true
    }));

    service.closeMenu();

    expect(service.layoutState().mobileMenuActive).toBeFalse();
    expect(service.layoutState().overlayMenuActive).toBeFalse();
  });
});
