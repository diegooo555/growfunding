import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { User } from './user.model';
import { UsersApi } from './users.api';

const STORAGE_KEY = 'g7.currentUserId';

/**
 * Simulated identity: there is no real login, the user picks who they act as.
 * The selected id is sent in the X-User-Id header by the interceptor.
 */
@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  private readonly api = inject(UsersApi);

  readonly users = signal<User[]>([]);
  readonly currentUserId = signal<string | null>(readStoredId());
  readonly currentUser = computed(
    () => this.users().find((u) => u.id === this.currentUserId()) ?? null,
  );
  readonly isAdmin = computed(() => this.currentUser()?.roles.includes('ADMIN') ?? false);
  readonly isCreator = computed(() => this.currentUser()?.roles.includes('CREATOR') ?? false);
  readonly isContributor = computed(
    () => this.currentUser()?.roles.includes('CONTRIBUTOR') ?? false,
  );

  async load(): Promise<void> {
    try {
      const users = await firstValueFrom(this.api.list());
      this.users.set(users);
      const stored = this.currentUserId();
      if (!stored || !users.some((u) => u.id === stored)) {
        const fallback = users.find((u) => u.roles.includes('CREATOR')) ?? users[0];
        this.switchTo(fallback?.id ?? null);
      }
    } catch {
      // Backend unavailable: the header shows no user and pages display their own errors.
    }
  }

  switchTo(userId: string | null): void {
    this.currentUserId.set(userId);
    try {
      if (userId) {
        localStorage.setItem(STORAGE_KEY, userId);
      } else {
        localStorage.removeItem(STORAGE_KEY);
      }
    } catch {
      // Storage may be unavailable (private mode); identity still works for this session.
    }
  }
}

function readStoredId(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}
