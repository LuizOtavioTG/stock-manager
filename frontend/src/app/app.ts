import { Component } from '@angular/core';

import { ToastModule } from 'primeng/toast';

import { AppLayoutComponent } from './core/layout/app-layout/app-layout';

@Component({
  selector: 'app-root',
  imports: [AppLayoutComponent, ToastModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {}
