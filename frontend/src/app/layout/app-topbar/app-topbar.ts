import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { LayoutService } from '../service/layout.service';

@Component({
  selector: 'app-topbar',
  imports: [RouterLink],
  templateUrl: './app-topbar.html',
  styleUrl: './app-topbar.scss'
})
export class AppTopbarComponent {
  protected readonly layoutService = inject(LayoutService);

  protected toggleMenu(): void {
    this.layoutService.onMenuToggle();
  }
}
