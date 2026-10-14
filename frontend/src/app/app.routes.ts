import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    title: 'Campañas activas',
    loadComponent: () => import('./features/campaigns/pages/campaign-list.page').then((m) => m.CampaignListPage),
  },
  {
    path: 'campaigns/new',
    title: 'Nueva campaña',
    loadComponent: () => import('./features/campaigns/pages/new-campaign.page').then((m) => m.NewCampaignPage),
  },
  {
    path: 'campaigns/:id',
    title: 'Detalle de campaña',
    loadComponent: () =>
      import('./features/campaigns/pages/campaign-detail.page').then((m) => m.CampaignDetailPage),
  },
  {
    path: 'my/campaigns',
    title: 'Mis campañas',
    loadComponent: () => import('./features/campaigns/pages/my-campaigns.page').then((m) => m.MyCampaignsPage),
  },
  {
    path: 'my/contributions',
    title: 'Mis aportes',
    loadComponent: () =>
      import('./features/contributions/pages/my-contributions.page').then((m) => m.MyContributionsPage),
  },
  {
    path: 'notifications',
    title: 'Notificaciones',
    loadComponent: () =>
      import('./features/notifications/pages/notifications.page').then((m) => m.NotificationsPage),
  },
  {
    path: 'admin',
    title: 'Administración',
    loadComponent: () => import('./features/admin/pages/admin.page').then((m) => m.AdminPage),
  },
  { path: '**', redirectTo: '' },
];
