import { Routes } from '@angular/router';
import { PlaceholderPage } from './pages/placeholder-page/placeholder-page';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard'
  },
  {
    path: 'dashboard',
    component: PlaceholderPage,
    data: { title: 'Dashboard', section: 'Visão geral' }
  },
  {
    path: 'estoque/inventario',
    component: PlaceholderPage,
    data: { title: 'Inventário', section: 'Estoque' }
  },
  {
    path: 'estoque/movimentacoes',
    component: PlaceholderPage,
    data: { title: 'Movimentações', section: 'Estoque' }
  },
  {
    path: 'estoque/alertas',
    component: PlaceholderPage,
    data: { title: 'Alertas', section: 'Estoque' }
  },
  {
    path: 'cadastros/produtos',
    component: PlaceholderPage,
    data: { title: 'Produtos', section: 'Cadastros' }
  },
  {
    path: 'cadastros/categorias',
    component: PlaceholderPage,
    data: { title: 'Categorias', section: 'Cadastros' }
  },
  {
    path: 'cadastros/fornecedores',
    component: PlaceholderPage,
    data: { title: 'Fornecedores', section: 'Cadastros' }
  },
  {
    path: 'cadastros/locais-de-estoque',
    component: PlaceholderPage,
    data: { title: 'Locais de estoque', section: 'Cadastros' }
  },
  {
    path: 'relatorios',
    component: PlaceholderPage,
    data: { title: 'Relatórios', section: 'Análises' }
  }
];
