import { Injectable, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ToastService } from '../../../core/feedback/toast.service';
import { friendlyMessage, toApiError } from '../../../core/http/api-error';
import { Page } from '../../../shared/models/page';
import { CampaignStatus, CampaignSummary } from '../../campaigns/domain/campaign.models';
import { AdminApi } from '../infrastructure/admin.api';

@Injectable()
export class AdminStore {
  private readonly api = inject(AdminApi);
  private readonly toasts = inject(ToastService);

  readonly status = signal<CampaignStatus | null>(null);
  readonly query = signal('');
  readonly pageNumber = signal(0);
  readonly page = signal<Page<CampaignSummary> | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  async load(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      this.page.set(await firstValueFrom(this.api.campaigns(this.status(), this.query(), this.pageNumber())));
    } catch (e) {
      this.error.set(friendlyMessage(toApiError(e)));
    } finally {
      this.loading.set(false);
    }
  }

  filter(status: CampaignStatus | null, query: string): Promise<void> {
    this.status.set(status);
    this.query.set(query);
    this.pageNumber.set(0);
    return this.load();
  }

  goToPage(page: number): Promise<void> {
    this.pageNumber.set(page);
    return this.load();
  }

  async suspend(id: string): Promise<void> {
    try {
      await firstValueFrom(this.api.suspend(id), { defaultValue: undefined });
      this.toasts.success('Campaña suspendida.');
      await this.load();
    } catch (e) {
      this.toasts.error(friendlyMessage(toApiError(e)));
    }
  }
}
