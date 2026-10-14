import { Injectable, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiError, friendlyMessage, toApiError } from '../../../core/http/api-error';
import { newIdempotencyKey } from '../../../shared/utils/idempotency';
import { ContributionDraft, ContributionReceipt } from '../domain/contribution.models';
import { ContributionsApi } from '../infrastructure/contributions.api';

export type ContributionOutcome =
  | { ok: true; receipt: ContributionReceipt }
  | { ok: false; message: string };

/**
 * HU-04. Keeps one idempotency key per logical submission: if the request fails for a
 * transient reason (network, 5xx, conflict) the retry reuses the key so the backend never
 * registers the same contribution twice. A new key is generated after success or after a
 * definitive business rejection.
 */
@Injectable()
export class ContributionFacade {
  private readonly api = inject(ContributionsApi);
  private pendingKey: string | null = null;

  readonly submitting = signal(false);

  async contribute(campaignId: string, draft: ContributionDraft): Promise<ContributionOutcome> {
    this.pendingKey ??= newIdempotencyKey();
    this.submitting.set(true);
    try {
      const receipt = await firstValueFrom(
        this.api.contribute(campaignId, {
          amount: draft.amount,
          anonymous: draft.anonymous,
          message: draft.message?.trim() ? draft.message.trim() : null,
          idempotencyKey: this.pendingKey,
        }),
      );
      this.pendingKey = null;
      return { ok: true, receipt };
    } catch (e) {
      const error = toApiError(e);
      if (!isTransient(error)) {
        this.pendingKey = null;
      }
      return { ok: false, message: friendlyMessage(error) };
    } finally {
      this.submitting.set(false);
    }
  }
}

function isTransient(error: ApiError): boolean {
  return error.status === 0 || error.status === 409 || error.status >= 500;
}
