import { ChangeDetectionStrategy, Component, effect, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrentUserService } from '../../../core/identity/current-user.service';
import { MoneyPipe } from '../../../shared/pipes/money.pipe';
import { EmptyStateComponent } from '../../../shared/ui/empty-state.component';
import { ContributionHistoryStore } from '../application/contribution-history.store';
import { ContributionHistoryTableComponent } from '../ui/contribution-history-table.component';

@Component({
  selector: 'app-my-contributions-page',
  imports: [ContributionHistoryTableComponent, EmptyStateComponent, MoneyPipe, RouterLink],
  providers: [ContributionHistoryStore],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="page-header">
      <div>
        <h1>Mis aportes</h1>
        <p class="muted">Historial de todos los aportes realizados.</p>
      </div>
      @if (store.items().length) {
        <div class="stat">
          @for (t of store.totalsByCurrency(); track t.currency) {
            <span>Total aportado: <strong>{{ t.amount | money: t.currency }}</strong></span>
          }
        </div>
      }
    </header>
    @if (store.loading()) {
      <p class="muted">Cargando…</p>
    } @else if (store.error()) {
      <p class="notice notice--error">{{ store.error() }}</p>
    } @else if (store.items().length === 0) {
      <app-empty-state title="Todavía no realizó aportes" hint="Explore las campañas activas y apoye un proyecto.">
        <a routerLink="/" class="btn btn--primary">Ver campañas</a>
      </app-empty-state>
    } @else {
      <app-contribution-history-table [items]="store.items()" />
    }
  `,
})
export class MyContributionsPage {
  protected readonly store = inject(ContributionHistoryStore);
  private readonly currentUser = inject(CurrentUserService);

  constructor() {
    effect(() => {
      if (this.currentUser.currentUserId()) {
        void this.store.load();
      }
    });
  }
}
