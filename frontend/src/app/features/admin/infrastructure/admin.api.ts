import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../../../shared/models/page';
import { CampaignStatus, CampaignSummary } from '../../campaigns/domain/campaign.models';

@Injectable({ providedIn: 'root' })
export class AdminApi {
  private readonly http = inject(HttpClient);

  campaigns(status: CampaignStatus | null, q: string, page: number): Observable<Page<CampaignSummary>> {
    let params = new HttpParams().set('page', page).set('size', 20);
    if (status) params = params.set('status', status);
    if (q.trim()) params = params.set('q', q.trim());
    return this.http.get<Page<CampaignSummary>>('/api/admin/campaigns', { params });
  }

  suspend(id: string): Observable<void> {
    return this.http.post<void>(`/api/admin/campaigns/${id}/suspend`, {});
  }
}
