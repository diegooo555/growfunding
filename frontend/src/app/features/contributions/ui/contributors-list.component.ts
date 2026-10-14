import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { CampaignContribution } from '../domain/contribution.models';

@Component({
  selector: 'app-contributors-list',
  imports: [MoneyPipe, DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="card">
      <h3>Aportantes</h3>
      @if (hiddenReason()) {
        <p class="notice">🔒 {{ hiddenReason() }}</p>
      } @else if (contributions().length === 0) {
        <p class="muted">Todavía no hay aportes. ¡Sea la primera persona en apoyar!</p>
      } @else {
        <ul class="list">
          @for (c of contributions(); track c.contributionId) {
            <li class="list__item">
              <div>
                <strong>{{ c.anonymous ? 'Anónimo' : c.contributorName }}</strong>
                @if (c.message) {
                  <p class="muted small">“{{ c.message }}”</p>
                }
              </div>
              <div class="right">
                <span class="amount">{{ c.amount | money: c.currency }}</span>
                <small class="muted">{{ c.createdAt | date: 'dd/MM/yyyy HH:mm' }}</small>
              </div>
            </li>
          }
        </ul>
      }
    </section>
  `,
})
export class ContributorsListComponent {
  readonly contributions = input.required<CampaignContribution[]>();
  readonly hiddenReason = input<string | null>(null);
}
