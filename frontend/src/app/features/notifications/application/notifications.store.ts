import { DestroyRef, Injectable, computed, effect, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { CurrentUserService } from '../../../core/identity/current-user.service';
import { AppNotification } from '../domain/notification.models';
import { NotificationsApi } from '../infrastructure/notifications.api';

const POLL_MS = 20_000;

/**
 * In-app notifications of the current user. Failures are silent on purpose: the notification
 * feature must never break campaign browsing.
 */
@Injectable({ providedIn: 'root' })
export class NotificationsStore {
  private readonly api = inject(NotificationsApi);
  private readonly currentUser = inject(CurrentUserService);

  readonly items = signal<AppNotification[]>([]);
  readonly unavailable = signal(false);
  readonly unreadCount = computed(() => this.items().filter((n) => !n.read).length);

  constructor() {
    effect(() => {
      const userId = this.currentUser.currentUserId();
      this.items.set([]);
      if (userId) void this.refresh();
    });
    const timer = setInterval(() => void this.refresh(), POLL_MS);
    inject(DestroyRef).onDestroy(() => clearInterval(timer));
  }

  async refresh(): Promise<void> {
    const userId = this.currentUser.currentUserId();
    if (!userId) return;
    try {
      const items = await firstValueFrom(this.api.list(userId));
      if (userId === this.currentUser.currentUserId()) {
        this.items.set(items);
        this.unavailable.set(false);
      }
    } catch {
      this.unavailable.set(true);
    }
  }

  async markAllRead(): Promise<void> {
    const userId = this.currentUser.currentUserId();
    if (!userId || this.unreadCount() === 0) return;
    this.items.update((list) => list.map((n) => ({ ...n, read: true })));
    try {
      await firstValueFrom(this.api.markAllRead(userId));
    } catch {
      await this.refresh();
    }
  }
}
