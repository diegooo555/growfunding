import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { friendlyMessage, toApiError } from '../../../core/http/api-error';
import { ContributionHistoryItem } from '../domain/contribution.models';
import { ContributionsApi } from '../infrastructure/contributions.api';

/** HU-07: contribution history of the current user. */
@Injectable()
export class ContributionHistoryStore {
  private readonly api = inject(ContributionsApi);

  readonly items = signal<ContributionHistoryItem[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly totalsByCurrency = computed(() => {
    const totals = new Map<string, number>();
    for (const item of this.items()) {
      totals.set(item.currency, (totals.get(item.currency) ?? 0) + Number(item.amount));
    }
    return [...totals.entries()].map(([currency, amount]) => ({ currency, amount }));
  });

  async load(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      this.items.set(await firstValueFrom(this.api.mine()));
    } catch (e) {
      this.error.set(friendlyMessage(toApiError(e)));
    } finally {
      this.loading.set(false);
    }
  }
}
