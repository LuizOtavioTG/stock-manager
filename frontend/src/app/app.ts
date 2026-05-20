import { Component } from '@angular/core';

import { AppLayoutComponent } from './core/layout/app-layout/app-layout';

@Component({
  selector: 'app-root',
  imports: [AppLayoutComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {}
