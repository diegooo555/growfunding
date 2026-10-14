import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CampaignContribution,
  ContributeRequest,
  ContributionHistoryItem,
  ContributionReceipt,
} from '../domain/contribution.models';

@Injectable({ providedIn: 'root' })
export class ContributionsApi {
  private readonly http = inject(HttpClient);

  contribute(campaignId: string, request: ContributeRequest): Observable<ContributionReceipt> {
    return this.http.post<ContributionReceipt>(`/api/campaigns/${campaignId}/contributions`, request);
  }

  contributorsOf(campaignId: string): Observable<CampaignContribution[]> {
    return this.http.get<CampaignContribution[]>(`/api/campaigns/${campaignId}/contributors`);
  }

  mine(): Observable<ContributionHistoryItem[]> {
    return this.http.get<ContributionHistoryItem[]>('/api/contributions/me');
  }
}
