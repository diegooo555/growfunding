import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { EmptyStateComponent } from '../../../shared/ui/empty-state.component';
import { NotificationsStore } from '../application/notifications.store';
import { NotificationListComponent } from '../ui/notification-list.component';

@Component({
  selector: 'app-notifications-page',
  imports: [NotificationListComponent, EmptyStateComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="page-header">
      <div>
        <h1>Notificaciones</h1>
        <p class="muted">Avisos automáticos sobre las campañas que creó o apoyó.</p>
      </div>
      <button type="button" class="btn" [disabled]="store.unreadCount() === 0" (click)="store.markAllRead()">
        Marcar todas como leídas
      </button>
    </header>
    @if (store.unavailable()) {
      <p class="notice">Las notificaciones no están disponibles en este momento. El resto de la plataforma sigue funcionando.</p>
    }
    @if (store.items().length === 0) {
      <app-empty-state title="No tiene notificaciones" hint="Le avisaremos cuando una campaña alcance su meta o esté por cerrar." />
    } @else {
      <app-notification-list [notifications]="store.items()" />
    }
  `,
})
export class NotificationsPage implements OnInit {
  protected readonly store = inject(NotificationsStore);

  ngOnInit(): void {
    void this.store.refresh();
  }
}
