import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge.component';
import { CampaignSummary } from '../../campaigns/domain/campaign.models';

@Component({
  selector: 'app-admin-campaign-table',
  imports: [DatePipe, MoneyPipe, RouterLink, StatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="table-wrap card">
      <table class="table">
        <thead>
          <tr>
            <th>Campaña</th>
            <th>Creador</th>
            <th>Estado</th>
            <th class="right">Recaudado</th>
            <th>Fecha límite</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          @for (c of campaigns(); track c.id) {
            <tr>
              <td><a [routerLink]="['/campaigns', c.id]">{{ c.title }}</a></td>
              <td>{{ c.creatorName }}</td>
              <td><app-status-badge [status]="c.status" /></td>
              <td class="right">{{ c.raisedAmount | money: c.currency }} ({{ c.progressPercent }}%)</td>
              <td>{{ c.deadline | date: 'dd/MM/yyyy' }}</td>
              <td class="right">
                @if (c.status === 'ACTIVE' || c.status === 'DRAFT') {
                  <button type="button" class="btn btn--danger btn--small" (click)="confirmSuspend(c)">Suspender</button>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class AdminCampaignTableComponent {
  readonly campaigns = input.required<CampaignSummary[]>();
  readonly suspend = output<string>();

  protected confirmSuspend(campaign: CampaignSummary): void {
    if (window.confirm(`¿Suspender "${campaign.title}"? Dejará de recibir aportes.`)) {
      this.suspend.emit(campaign.id);
    }
  }
}
