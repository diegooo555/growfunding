import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DaysLeftPipe } from '../../../shared/pipes/days-left.pipe';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { ProgressBarComponent } from '../../../shared/ui/progress-bar.component';
import { CATEGORY_LABELS, CampaignSummary } from '../domain/campaign.models';

@Component({
  selector: 'app-campaign-card',
  imports: [RouterLink, MoneyPipe, DaysLeftPipe, ProgressBarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <a class="card campaign-card" [routerLink]="['/campaigns', campaign().id]">
      <div class="campaign-card__top">
        <span class="chip">{{ categoryLabels[campaign().category] }}</span>
        @if (campaign().goalReached) {
          <span class="chip chip--success">Meta alcanzada</span>
        } @else if (campaign().closingSoon) {
          <span class="chip chip--warning">Cierra pronto</span>
        }
      </div>
      <h3 class="campaign-card__title">{{ campaign().title }}</h3>
      <p class="campaign-card__summary">{{ campaign().summary }}</p>
      <app-progress-bar [percent]="campaign().progressPercent" />
      <div class="campaign-card__numbers">
        <div>
          <strong>{{ campaign().raisedAmount | money: campaign().currency }}</strong>
          <small class="muted"> de {{ campaign().goalAmount | money: campaign().currency }}</small>
        </div>
        <strong class="percent">{{ campaign().progressPercent }}%</strong>
      </div>
      <div class="campaign-card__footer muted small">
        <span>{{ campaign().contributorsCount }} aportantes</span>
        <span>{{ campaign().deadline | daysLeft }}</span>
      </div>
    </a>
  `,
})
export class CampaignCardComponent {
  readonly campaign = input.required<CampaignSummary>();
  protected readonly categoryLabels = CATEGORY_LABELS;
}
