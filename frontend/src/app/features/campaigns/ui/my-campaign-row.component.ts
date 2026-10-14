import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DaysLeftPipe } from '../../../shared/pipes/days-left.pipe';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { ProgressBarComponent } from '../../../shared/ui/progress-bar.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge.component';
import { CampaignSummary } from '../domain/campaign.models';

export type CampaignAction = 'publish' | 'close' | 'cancel';

@Component({
  selector: 'app-my-campaign-row',
  imports: [RouterLink, MoneyPipe, DaysLeftPipe, ProgressBarComponent, StatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <article class="card row-card">
      <div class="row-card__main">
        <div class="row-card__title">
          <a [routerLink]="['/campaigns', campaign().id]">{{ campaign().title }}</a>
          <app-status-badge [status]="campaign().status" />
        </div>
        <app-progress-bar [percent]="campaign().progressPercent" />
        <p class="muted small">
          {{ campaign().raisedAmount | money: campaign().currency }} de
          {{ campaign().goalAmount | money: campaign().currency }} ({{ campaign().progressPercent }}%) ·
          {{ campaign().contributionsCount }} aportes ·
          {{ campaign().status === 'ACTIVE' || campaign().status === 'DRAFT' ? (campaign().deadline | daysLeft) : 'Finalizada' }}
        </p>
      </div>
      <div class="row-card__actions">
        @if (campaign().status === 'DRAFT') {
          <button type="button" class="btn btn--primary" [disabled]="busy()" (click)="action.emit('publish')">Publicar</button>
        }
        @if (campaign().status === 'ACTIVE') {
          <button type="button" class="btn" [disabled]="busy()" (click)="confirm('close')">Cerrar</button>
        }
        @if (campaign().status === 'DRAFT' || campaign().status === 'ACTIVE') {
          <button type="button" class="btn btn--danger" [disabled]="busy() || !campaign().cancellable"
                  [title]="campaign().cancellable ? 'Cancelar campaña' : 'No se puede cancelar: ya tiene aportes. Puede cerrarla.'"
                  (click)="confirm('cancel')">Cancelar</button>
        }
      </div>
    </article>
  `,
})
export class MyCampaignRowComponent {
  readonly campaign = input.required<CampaignSummary>();
  readonly busy = input(false);
  readonly action = output<CampaignAction>();

  protected confirm(action: CampaignAction): void {
    const question =
      action === 'close'
        ? '¿Cerrar la campaña? No podrá volver a abrirse ni recibir aportes.'
        : '¿Cancelar la campaña? Esta acción no se puede deshacer.';
    if (window.confirm(question)) {
      this.action.emit(action);
    }
  }
}
