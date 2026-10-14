import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NotificationsStore } from '../../features/notifications/application/notifications.store';
import { NotificationBellComponent } from '../../features/notifications/ui/notification-bell.component';
import { CurrentUserService } from '../identity/current-user.service';

const ROLE_LABELS: Record<string, string> = { CREATOR: 'Creador', CONTRIBUTOR: 'Aportante', ADMIN: 'Admin' };

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, NotificationBellComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="topbar">
      <div class="topbar__inner">
        <a routerLink="/" class="brand"><span class="brand__logo">G7</span> Impulsa</a>
        <nav class="nav">
          <a routerLink="/" routerLinkActive="is-active" [routerLinkActiveOptions]="{ exact: true }">Campañas</a>
          @if (user.isCreator()) {
            <a routerLink="/campaigns/new" routerLinkActive="is-active">Nueva campaña</a>
            <a routerLink="/my/campaigns" routerLinkActive="is-active">Mis campañas</a>
          }
          <a routerLink="/my/contributions" routerLinkActive="is-active">Mis aportes</a>
          @if (user.isAdmin()) {
            <a routerLink="/admin" routerLinkActive="is-active">Admin</a>
          }
        </nav>
        <div class="topbar__right">
          <app-notification-bell [unread]="notifications.unreadCount()" />
          <label class="user-switcher">
            <span class="sr-only">Usuario actual</span>
            <select [value]="user.currentUserId() ?? ''" (change)="user.switchTo($any($event.target).value)">
              @for (u of user.users(); track u.id) {
                <option [value]="u.id" [selected]="u.id === user.currentUserId()">
                  {{ u.displayName }} · {{ rolesOf(u.roles) }}
                </option>
              }
            </select>
          </label>
        </div>
      </div>
    </header>
  `,
})
export class HeaderComponent {
  protected readonly user = inject(CurrentUserService);
  protected readonly notifications = inject(NotificationsStore);

  protected rolesOf(roles: string[]): string {
    return roles.map((r) => ROLE_LABELS[r] ?? r).join(', ');
  }
}
