import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AppNotification, NotificationType } from '../domain/notification.models';

const ICONS: Record<NotificationType, string> = {
  NEW_CONTRIBUTION: '💸',
  GOAL_REACHED: '🎯',
  CLOSING_SOON: '⏳',
  CAMPAIGN_CLOSED: '🏁',
};

@Component({
  selector: 'app-notification-list',
  imports: [DatePipe, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ul class="list card">
      @for (n of notifications(); track n.id) {
        <li class="list__item notification" [class.notification--unread]="!n.read">
          <span class="notification__icon" aria-hidden="true">{{ icons[n.type] }}</span>
          <div class="notification__body">
            <strong>{{ n.title }}</strong>
            <p>{{ n.message }}</p>
            <small class="muted">{{ n.createdAt | date: 'dd/MM/yyyy HH:mm' }}</small>
          </div>
          @if (n.campaignId) {
            <a class="btn btn--small" [routerLink]="['/campaigns', n.campaignId]">Ver campaña</a>
          }
        </li>
      }
    </ul>
  `,
})
export class NotificationListComponent {
  readonly notifications = input.required<AppNotification[]>();
  protected readonly icons = ICONS;
}
