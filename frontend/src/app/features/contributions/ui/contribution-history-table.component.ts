import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge.component';
import { ContributionHistoryItem } from '../domain/contribution.models';

@Component({
  selector: 'app-contribution-history-table',
  imports: [MoneyPipe, DatePipe, RouterLink, StatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="table-wrap card">
      <table class="table">
        <thead>
          <tr>
            <th>Fecha</th>
            <th>Campaña</th>
            <th>Estado</th>
            <th class="right">Monto</th>
            <th>Detalle</th>
          </tr>
        </thead>
        <tbody>
          @for (item of items(); track item.contributionId) {
            <tr>
              <td>{{ item.createdAt | date: 'dd/MM/yyyy HH:mm' }}</td>
              <td><a [routerLink]="['/campaigns', item.campaignId]">{{ item.campaignTitle }}</a></td>
              <td><app-status-badge [status]="item.campaignStatus" /></td>
              <td class="right amount">{{ item.amount | money: item.currency }}</td>
              <td class="muted small">
                {{ item.anonymous ? 'Anónimo' : 'Público' }}@if (item.message) { · “{{ item.message }}” }
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class ContributionHistoryTableComponent {
  readonly items = input.required<ContributionHistoryItem[]>();
}
