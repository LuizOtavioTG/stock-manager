import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard';
import { PlaceholderPage } from './pages/placeholder-page/placeholder-page';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard'
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    data: { title: 'Dashboard', section: 'Visão geral' }
  },
  {
    path: 'estoque/inventario',
    loadComponent: () =>
      import('./features/inventory/pages/inventory-list/inventory-list').then((m) => m.InventoryListComponent),
    data: { title: 'Inventário', section: 'Estoque' }
  },
  {
    path: 'estoque/movimentacoes',
    loadComponent: () =>
      import('./features/stock-movements/pages/stock-movement-list/stock-movement-list').then(
        (m) => m.StockMovementListComponent
      ),
    data: { title: 'Movimentações', section: 'Estoque' }
  },
  {
    path: 'estoque/alertas',
    loadComponent: () =>
      import('./features/inventory/pages/inventory-alerts/inventory-alerts').then((m) => m.InventoryAlertsComponent),
    data: { title: 'Alertas', section: 'Estoque' }
  },
  {
    path: 'cadastros/produtos',
    loadComponent: () =>
      import('./features/products/pages/product-list/product-list').then((m) => m.ProductListComponent),
    data: { title: 'Produtos', section: 'Cadastros' }
  },
  {
    path: 'cadastros/categorias',
    loadComponent: () =>
      import('./features/categories/pages/category-list/category-list').then((m) => m.CategoryListComponent),
    data: { title: 'Categorias', section: 'Cadastros' }
  },
  {
    path: 'cadastros/fornecedores',
    loadComponent: () =>
      import('./features/suppliers/pages/supplier-list/supplier-list').then((m) => m.SupplierListComponent),
    data: { title: 'Fornecedores', section: 'Cadastros' }
  },
  {
    path: 'cadastros/locais-de-estoque',
    loadComponent: () =>
      import('./features/storage-locations/pages/storage-location-list/storage-location-list').then(
        (m) => m.StorageLocationListComponent
      ),
    data: { title: 'Locais de estoque', section: 'Cadastros' }
  },
  {
    path: 'compras/pedidos',
    loadComponent: () =>
      import('./features/purchase-orders/pages/purchase-order-list/purchase-order-list').then(
        (m) => m.PurchaseOrderListComponent
      ),
    data: { title: 'Pedidos de compra', section: 'Compras' }
  },
  {
    path: 'relatorios',
    loadComponent: () =>
      import('./features/reports/pages/reports-home/reports-home').then((m) => m.ReportsHomeComponent),
    data: { title: 'Relatórios', section: 'Análises' }
  },
  {
    path: 'relatorios/reposicao-sugerida',
    loadComponent: () =>
      import('./features/reports/pages/reorder-suggestion-report/reorder-suggestion-report').then(
        (m) => m.ReorderSuggestionReportComponent
      ),
    data: { title: 'Reposição sugerida', section: 'Relatórios' }
  },
  {
    path: 'relatorios/reposicao-por-fornecedor',
    loadComponent: () =>
      import('./features/reports/pages/reorder-by-supplier-report/reorder-by-supplier-report').then(
        (m) => m.ReorderBySupplierReportComponent
      ),
    data: { title: 'Reposição por fornecedor', section: 'Relatórios' }
  },
  {
    path: 'relatorios/movimentacoes',
    loadComponent: () =>
      import('./features/reports/pages/stock-movement-report/stock-movement-report').then(
        (m) => m.StockMovementReportComponent
      ),
    data: { title: 'Relatório de movimentações', section: 'Relatórios' }
  }
];
