import { Component } from '@angular/core';

import { MenuItem } from 'primeng/api';

import { AppMenuItemComponent } from '../app-menuitem/app-menuitem';

@Component({
  selector: 'app-menu',
  imports: [AppMenuItemComponent],
  templateUrl: './app-menu.html'
})
export class AppMenuComponent {
  protected readonly model: MenuItem[] = [
    {
      label: 'Home',
      items: [{ label: 'Dashboard', icon: 'pi pi-fw pi-home', routerLink: ['/dashboard'] }]
    },
    {
      label: 'Estoque',
      items: [
        { label: 'Inventário', icon: 'pi pi-fw pi-list-check', routerLink: ['/estoque/inventario'] },
        { label: 'Movimentações', icon: 'pi pi-fw pi-arrow-right-arrow-left', routerLink: ['/estoque/movimentacoes'] },
        { label: 'Alertas', icon: 'pi pi-fw pi-bell', routerLink: ['/estoque/alertas'] }
      ]
    },
    {
      label: 'Cadastros',
      items: [
        { label: 'Produtos', icon: 'pi pi-fw pi-box', routerLink: ['/cadastros/produtos'] },
        { label: 'Categorias', icon: 'pi pi-fw pi-tags', routerLink: ['/cadastros/categorias'] },
        { label: 'Fornecedores', icon: 'pi pi-fw pi-truck', routerLink: ['/cadastros/fornecedores'] },
        { label: 'Locais de estoque', icon: 'pi pi-fw pi-map-marker', routerLink: ['/cadastros/locais-de-estoque'] }
      ]
    },
    {
      label: 'Análises',
      items: [{ label: 'Relatórios', icon: 'pi pi-fw pi-chart-bar', routerLink: ['/relatorios'] }]
    }
  ];
}
