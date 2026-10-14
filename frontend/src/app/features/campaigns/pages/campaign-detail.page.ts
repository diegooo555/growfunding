import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, input, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ToastService } from '../../../core/feedback/toast.service';
import { CurrentUserService } from '../../../core/identity/current-user.service';
import { DaysLeftPipe } from '../../../shared/pipes/days-left.pipe';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { ProgressBarComponent } from '../../../shared/ui/progress-bar.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge.component';
import { ContributionFacade } from '../../contributions/application/contribution.facade';
import { ContributionDraft } from '../../contributions/domain/contribution.models';
import { ContributeFormComponent } from '../../contributions/ui/contribute-form.component';
import { ContributorsListComponent } from '../../contributions/ui/contributors-list.component';
import { NotificationsStore } from '../../notifications/application/notifications.store';
import { CampaignDetailStore } from '../application/campaign-detail.store';
import { CATEGORY_LABELS } from '../domain/campaign.models';
import { MilestoneListComponent } from '../ui/milestone-list.component';

@Component({
  selector: 'app-campaign-detail-page',
  imports: [
    RouterLink, DatePipe, MoneyPipe, DaysLeftPipe, ProgressBarComponent, StatusBadgeComponent,
    ContributeFormComponent, ContributorsListComponent, MilestoneListComponent,
  ],
  providers: [CampaignDetailStore, ContributionFacade],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <a routerLink="/" class="back">← Volver a campañas</a>
    @if (store.error()) {
      <p class="notice notice--error">{{ store.error() }}</p>
    } @else if (store.detail(); as detail) {
      <div class="detail">
        <section class="detail__main">
          <div class="card">
            <div class="detail__meta">
              <span class="chip">{{ categoryLabels[detail.summary.category] }}</span>
              <app-status-badge [status]="detail.summary.status" />
              @if (detail.summary.goalReached) {
                <span class="chip chip--success">Meta alcanzada</span>
              }
            </div>
            <h1>{{ detail.summary.title }}</h1>
            <p class="muted">Creada por <strong>{{ detail.summary.creatorName }}</strong></p>
            <app-progress-bar [percent]="detail.summary.progressPercent" />
            <div class="stats">
              <div><strong>{{ detail.summary.raisedAmount | money: detail.summary.currency }}</strong>
                <small class="muted">recaudado de {{ detail.summary.goalAmount | money: detail.summary.currency }}</small></div>
              <div><strong>{{ detail.summary.progressPercent }}%</strong><small class="muted">de la meta</small></div>
              <div><strong>{{ detail.summary.contributorsCount }}</strong><small class="muted">aportantes</small></div>
              <div><strong>{{ detail.summary.deadline | daysLeft }}</strong>
                <small class="muted">cierra el {{ detail.summary.deadline | date: 'dd/MM/yyyy HH:mm' }}</small></div>
            </div>
            <p class="description">{{ detail.description }}</p>
          </div>
          <app-contributors-list [contributions]="store.contributors()" [hiddenReason]="store.contributorsHiddenReason()" />
        </section>
        <aside class="detail__side">
          <app-contribute-form #contributeForm [currency]="detail.summary.currency"
                               [enabled]="detail.summary.acceptingContributions && !!currentUser.currentUserId()"
                               [submitting]="facade.submitting()" (contribute)="onContribute($event)" />
          <app-milestone-list [milestones]="detail.milestones" />
        </aside>
      </div>
    } @else {
      <p class="muted">Cargando…</p>
    }
  `,
})
export class CampaignDetailPage implements OnInit {
  /** Route parameter bound through withComponentInputBinding(). */
  readonly id = input.required<string>();

  protected readonly store = inject(CampaignDetailStore);
  protected readonly facade = inject(ContributionFacade);
  protected readonly currentUser = inject(CurrentUserService);
  protected readonly categoryLabels = CATEGORY_LABELS;
  private readonly toasts = inject(ToastService);
  private readonly notifications = inject(NotificationsStore);
  private readonly form = viewChild<ContributeFormComponent>('contributeForm');

  ngOnInit(): void {
    void this.store.load(this.id());
  }

  protected async onContribute(draft: ContributionDraft): Promise<void> {
    const outcome = await this.facade.contribute(this.id(), draft);
    if (outcome.ok) {
      this.toasts.success(
        outcome.receipt.campaignGoalReached
          ? '¡Gracias por su aporte! La campaña ya alcanzó su meta.'
          : '¡Gracias por su aporte!',
      );
      this.form()?.reset();
      await this.store.refresh();
      // Notifications are produced asynchronously by the outbox relay.
      setTimeout(() => void this.notifications.refresh(), 2500);
    } else {
      this.toasts.error(outcome.message);
    }
  }
}
