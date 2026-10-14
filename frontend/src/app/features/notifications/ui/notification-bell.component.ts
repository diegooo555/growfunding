import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-notification-bell',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <a routerLink="/notifications" class="bell" [attr.aria-label]="'Notificaciones: ' + unread() + ' sin leer'">
      <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
        <path fill="currentColor"
              d="M12 22a2.5 2.5 0 0 0 2.45-2h-4.9A2.5 2.5 0 0 0 12 22Zm7-6V11a7 7 0 0 0-5.5-6.84V3a1.5 1.5 0 0 0-3 0v1.16A7 7 0 0 0 5 11v5l-2 2v1h18v-1Z" />
      </svg>
      @if (unread() > 0) {
        <span class="bell__count">{{ unread() > 9 ? '9+' : unread() }}</span>
      }
    </a>
  `,
})
export class NotificationBellComponent {
  readonly unread = input(0);
}
