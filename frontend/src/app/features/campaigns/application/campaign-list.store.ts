import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { friendlyMessage, toApiError } from '../../../core/http/api-error';
import { Page } from '../../../shared/models/page';
import { CampaignFilters, CampaignSummary, DEFAULT_FILTERS } from '../domain/campaign.models';
import { CampaignsApi } from '../infrastructure/campaigns.api';

/** HU-03: state of the public campaign catalogue. */
@Injectable()
export class CampaignListStore {
  private readonly api = inject(CampaignsApi);
  private requestSeq = 0;

  readonly filters = signal<CampaignFilters>({ ...DEFAULT_FILTERS });
  readonly page = signal<Page<CampaignSummary> | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly campaigns = computed(() => this.page()?.content ?? []);
  readonly totalPages = computed(() => this.page()?.totalPages ?? 0);
  readonly hasResults = computed(() => this.campaigns().length > 0);

  async load(): Promise<void> {
    const seq = ++this.requestSeq;
    this.loading.set(true);
    this.error.set(null);
    try {
      const page = await firstValueFrom(this.api.search(this.filters()));
      if (seq === this.requestSeq) this.page.set(page);
    } catch (e) {
      if (seq === this.requestSeq) this.error.set(friendlyMessage(toApiError(e)));
    } finally {
      if (seq === this.requestSeq) this.loading.set(false);
    }
  }

  applyFilters(patch: Partial<CampaignFilters>): Promise<void> {
    this.filters.update((f) => ({ ...f, ...patch, page: 0 }));
    return this.load();
  }

  goToPage(page: number): Promise<void> {
    this.filters.update((f) => ({ ...f, page }));
    return this.load();
  }

  reset(): Promise<void> {
    this.filters.set({ ...DEFAULT_FILTERS });
    return this.load();
  }
}
