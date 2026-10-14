import { ChangeDetectionStrategy, Component, effect, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrentUserService } from '../../../core/identity/current-user.service';
import { EmptyStateComponent } from '../../../shared/ui/empty-state.component';
import { MyCampaignsStore } from '../application/my-campaigns.store';
import { CampaignAction, MyCampaignRowComponent } from '../ui/my-campaign-row.component';

@Component({
  selector: 'app-my-campaigns-page',
  imports: [MyCampaignRowComponent, EmptyStateComponent, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="page-header">
      <div>
        <h1>Mis campañas</h1>
        <p class="muted">Publique, cierre o cancele sus campañas.</p>
      </div>
      @if (currentUser.isCreator()) {
        <a routerLink="/campaigns/new" class="btn btn--primary">+ Nueva campaña</a>
      }
    </header>
    @if (store.loading() && store.campaigns().length === 0) {
      <p class="muted">Cargando…</p>
    } @else if (store.error()) {
      <p class="notice notice--error">{{ store.error() }}</p>
    } @else if (store.campaigns().length === 0) {
      <app-empty-state title="Todavía no creó campañas" hint="Cree su primera campaña para empezar a recaudar." />
    } @else {
      <div class="stack">
        @for (campaign of store.campaigns(); track campaign.id) {
          <app-my-campaign-row [campaign]="campaign" [busy]="store.busyId() === campaign.id"
                               (action)="run(campaign.id, $event)" />
        }
      </div>
    }
  `,
})
export class MyCampaignsPage {
  protected readonly store = inject(MyCampaignsStore);
  protected readonly currentUser = inject(CurrentUserService);

  constructor() {
    effect(() => {
      if (this.currentUser.currentUserId()) {
        void this.store.load();
      }
    });
  }

  protected run(id: string, action: CampaignAction): void {
    void this.store[action](id);
  }
}
