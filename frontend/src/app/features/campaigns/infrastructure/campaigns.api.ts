import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Page } from '../../../shared/models/page';
import {
  CampaignDetail,
  CampaignFilters,
  CampaignStatus,
  CampaignSummary,
  CreateCampaignRequest,
} from '../domain/campaign.models';

/** HTTP adapter for the campaign endpoints (command + query sides). */
@Injectable({ providedIn: 'root' })
export class CampaignsApi {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/campaigns';

  search(filters: CampaignFilters): Observable<Page<CampaignSummary>> {
    let params = new HttpParams()
      .set('page', filters.page)
      .set('size', filters.size)
      .set('sort', filters.sort);
    if (filters.q.trim()) params = params.set('q', filters.q.trim());
    if (filters.category) params = params.set('category', filters.category);
    if (filters.minProgress) params = params.set('minProgress', filters.minProgress);
    if (filters.closingSoon) params = params.set('closingSoon', true);
    return this.http.get<Page<CampaignSummary>>(this.base, { params });
  }

  detail(id: string): Observable<CampaignDetail> {
    return this.http.get<CampaignDetail>(`${this.base}/${id}`);
  }

  mine(): Observable<CampaignSummary[]> {
    return this.http.get<CampaignSummary[]>(`${this.base}/mine`);
  }

  create(request: CreateCampaignRequest): Observable<{ id: string; status: CampaignStatus }> {
    return this.http.post<{ id: string; status: CampaignStatus }>(this.base, request);
  }

  publish(id: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/publish`, {});
  }

  close(id: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/close`, {});
  }

  cancel(id: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/cancel`, {});
  }
}
