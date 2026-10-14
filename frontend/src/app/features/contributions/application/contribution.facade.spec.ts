import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ContributeRequest, ContributionReceipt } from '../domain/contribution.models';
import { ContributionsApi } from '../infrastructure/contributions.api';
import { ContributionFacade } from './contribution.facade';

describe('ContributionFacade', () => {
  const keys: string[] = [];
  let fail: { status: number } | null;

  beforeEach(() => {
    keys.length = 0;
    fail = null;
    TestBed.configureTestingModule({
      providers: [
        ContributionFacade,
        {
          provide: ContributionsApi,
          useValue: {
            contribute: (_id: string, request: ContributeRequest) => {
              keys.push(request.idempotencyKey);
              return fail
                ? throwError(() => ({ ...fail, title: 'x', detail: 'x' }))
                : of({ contributionId: 'c1' } as ContributionReceipt);
            },
          },
        },
      ],
    });
  });

  const draft = { amount: 10, anonymous: false, message: '' };

  it('reuses the idempotency key when retrying after a transient failure', async () => {
    const facade = TestBed.inject(ContributionFacade);
    fail = { status: 0 };
    await facade.contribute('camp', draft);
    fail = null;
    const outcome = await facade.contribute('camp', draft);
    expect(outcome.ok).toBeTrue();
    expect(keys[0]).toBe(keys[1]);
  });

  it('uses a new key for each successful submission', async () => {
    const facade = TestBed.inject(ContributionFacade);
    await facade.contribute('camp', draft);
    await facade.contribute('camp', draft);
    expect(keys[0]).not.toBe(keys[1]);
  });
});
