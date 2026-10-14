import { Injectable, inject, signal } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { ToastService } from '../../../core/feedback/toast.service';
import { friendlyMessage, toApiError } from '../../../core/http/api-error';
import { CampaignSummary, CreateCampaignRequest } from '../domain/campaign.models';
import { CampaignsApi } from '../infrastructure/campaigns.api';

/** Creator workspace: HU-01, HU-02, manual close and HU-09. */
@Injectable({ providedIn: 'root' })
export class MyCampaignsStore {
  private readonly api = inject(CampaignsApi);
  private readonly toasts = inject(ToastService);

  readonly campaigns = signal<CampaignSummary[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly busyId = signal<string | null>(null);

  async load(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      this.campaigns.set(await firstValueFrom(this.api.mine()));
    } catch (e) {
      this.error.set(friendlyMessage(toApiError(e)));
    } finally {
      this.loading.set(false);
    }
  }

  /** @returns the new campaign id, or null when creation failed (error already surfaced). */
  async create(request: CreateCampaignRequest): Promise<string | null> {
    try {
      const created = await firstValueFrom(this.api.create(request));
      this.toasts.success('Campaña creada como borrador.');
      return created.id;
    } catch (e) {
      this.toasts.error(friendlyMessage(toApiError(e)));
      return null;
    }
  }

  publish(id: string): Promise<void> {
    return this.run(id, this.api.publish(id), 'Campaña publicada. Ya es visible para todos.');
  }

  close(id: string): Promise<void> {
    return this.run(id, this.api.close(id), 'Campaña cerrada.');
  }

  cancel(id: string): Promise<void> {
    return this.run(id, this.api.cancel(id), 'Campaña cancelada.');
  }

  private async run(id: string, action: Observable<void>, successMessage: string): Promise<void> {
    this.busyId.set(id);
    try {
      await firstValueFrom(action, { defaultValue: undefined });
      this.toasts.success(successMessage);
      await this.load();
    } catch (e) {
      this.toasts.error(friendlyMessage(toApiError(e)));
    } finally {
      this.busyId.set(null);
    }
  }
}
