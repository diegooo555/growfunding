import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CurrentUserService } from '../../../core/identity/current-user.service';
import { MyCampaignsStore } from '../application/my-campaigns.store';
import { CreateCampaignRequest } from '../domain/campaign.models';
import { CampaignFormComponent } from '../ui/campaign-form.component';

@Component({
  selector: 'app-new-campaign-page',
  imports: [CampaignFormComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="page-header">
      <div>
        <h1>Nueva campaña</h1>
        <p class="muted">Defina su meta y fecha límite. Podrá publicarla cuando esté lista.</p>
      </div>
    </header>
    @if (currentUser.isCreator()) {
      <app-campaign-form [submitting]="saving()" (save)="create($event)" />
    } @else {
      <p class="notice">El usuario actual no tiene el rol de creador. Cambie de usuario desde el encabezado.</p>
    }
  `,
})
export class NewCampaignPage {
  protected readonly currentUser = inject(CurrentUserService);
  private readonly store = inject(MyCampaignsStore);
  private readonly router = inject(Router);
  protected readonly saving = signal(false);

  protected async create(request: CreateCampaignRequest): Promise<void> {
    this.saving.set(true);
    const id = await this.store.create(request);
    this.saving.set(false);
    if (id) {
      await this.router.navigate(['/my/campaigns']);
    }
  }
}
