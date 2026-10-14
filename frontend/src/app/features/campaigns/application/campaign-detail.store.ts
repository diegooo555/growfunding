import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { friendlyMessage, toApiError } from '../../../core/http/api-error';
import { CampaignContribution } from '../../contributions/domain/contribution.models';
import { ContributionsApi } from '../../contributions/infrastructure/contributions.api';
import { CampaignDetail } from '../domain/campaign.models';
import { CampaignsApi } from '../infrastructure/campaigns.api';

/** Detail page state: campaign, milestones and (HU-08) the contributors list when visible. */
@Injectable()
export class CampaignDetailStore {
  private readonly campaigns = inject(CampaignsApi);
  private readonly contributions = inject(ContributionsApi);

  readonly detail = signal<CampaignDetail | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly contributors = signal<CampaignContribution[]>([]);
  readonly contributorsHiddenReason = signal<string | null>(null);

  readonly summary = computed(() => this.detail()?.summary ?? null);

  async load(id: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      this.detail.set(await firstValueFrom(this.campaigns.detail(id)));
      await this.loadContributors(id);
    } catch (e) {
      this.error.set(friendlyMessage(toApiError(e)));
    } finally {
      this.loading.set(false);
    }
  }

  async refresh(): Promise<void> {
    const id = this.summary()?.id;
    if (!id) return;
    try {
      this.detail.set(await firstValueFrom(this.campaigns.detail(id)));
      await this.loadContributors(id);
    } catch {
      // keep the last known state; the next refresh will retry
    }
  }

  private async loadContributors(id: string): Promise<void> {
    try {
      this.contributors.set(await firstValueFrom(this.contributions.contributorsOf(id)));
      this.contributorsHiddenReason.set(null);
    } catch (e) {
      const error = toApiError(e);
      this.contributors.set([]);
      this.contributorsHiddenReason.set(
        error.status === 403 ? friendlyMessage(error) : 'No se pudo cargar la lista de aportantes.',
      );
    }
  }
}
