import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Page } from '../../../shared/models/page';
import { CampaignFilters, CampaignSummary } from '../domain/campaign.models';
import { CampaignsApi } from '../infrastructure/campaigns.api';
import { CampaignListStore } from './campaign-list.store';

describe('CampaignListStore', () => {
  let lastFilters: CampaignFilters | null;
  let response: () => ReturnType<CampaignsApi['search']>;

  const page: Page<CampaignSummary> = {
    content: [{ id: '1', title: 'Huerta' } as CampaignSummary],
    page: 0,
    size: 9,
    totalElements: 1,
    totalPages: 1,
  };

  beforeEach(() => {
    lastFilters = null;
    response = () => of(page);
    TestBed.configureTestingModule({
      providers: [
        CampaignListStore,
        {
          provide: CampaignsApi,
          useValue: {
            search: (filters: CampaignFilters) => {
              lastFilters = filters;
              return response();
            },
          },
        },
      ],
    });
  });

  it('loads the first page of campaigns', async () => {
    const store = TestBed.inject(CampaignListStore);
    await store.load();
    expect(store.campaigns().length).toBe(1);
    expect(store.loading()).toBeFalse();
  });

  it('resets to the first page when filters change', async () => {
    const store = TestBed.inject(CampaignListStore);
    await store.goToPage(3);
    await store.applyFilters({ q: 'huerta', closingSoon: true });
    expect(lastFilters?.page).toBe(0);
    expect(lastFilters?.q).toBe('huerta');
    expect(lastFilters?.closingSoon).toBeTrue();
  });

  it('exposes a friendly error message when the API fails', async () => {
    response = () => throwError(() => ({ status: 403, title: 'Forbidden', detail: 'x', code: 'FORBIDDEN' }));
    const store = TestBed.inject(CampaignListStore);
    await store.load();
    expect(store.error()).toBeTruthy();
    expect(store.campaigns().length).toBe(0);
  });
});
