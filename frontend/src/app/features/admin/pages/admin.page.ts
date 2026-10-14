import { ChangeDetectionStrategy, Component, effect, inject } from '@angular/core';
import { CurrentUserService } from '../../../core/identity/current-user.service';
import { CampaignStatus } from '../../campaigns/domain/campaign.models';
import { AdminStore } from '../application/admin.store';
import { AdminCampaignTableComponent } from '../ui/admin-campaign-table.component';

@Component({
  selector: 'app-admin-page',
  imports: [AdminCampaignTableComponent],
  providers: [AdminStore],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="page-header">
      <div>
        <h1>Administración</h1>
        <p class="muted">Todas las campañas de la plataforma, en cualquier estado.</p>
      </div>
    </header>
    @if (!currentUser.isAdmin()) {
      <p class="notice">Esta sección requiere el rol de administrador. Cambie de usuario desde el encabezado.</p>
    } @else {
      <form class="card filters" (submit)="$event.preventDefault(); apply(q.value, st.value)">
        <label class="field filters__search"><span>Buscar</span><input #q type="search" placeholder="Título o descripción" /></label>
        <label class="field">
          <span>Estado</span>
          <select #st (change)="apply(q.value, st.value)">
            <option value="">Todos</option>
            @for (s of statuses; track s.value) {
              <option [value]="s.value">{{ s.label }}</option>
            }
          </select>
        </label>
        <button type="submit" class="btn">Filtrar</button>
      </form>
      @if (store.error()) {
        <p class="notice notice--error">{{ store.error() }}</p>
      } @else if (store.page(); as page) {
        <app-admin-campaign-table [campaigns]="page.content" (suspend)="store.suspend($event)" />
        @if (page.totalPages > 1) {
          <nav class="pagination">
            <button type="button" class="btn" [disabled]="page.page === 0" (click)="store.goToPage(page.page - 1)">Anterior</button>
            <span>Página {{ page.page + 1 }} de {{ page.totalPages }}</span>
            <button type="button" class="btn" [disabled]="page.page + 1 >= page.totalPages" (click)="store.goToPage(page.page + 1)">Siguiente</button>
          </nav>
        }
      }
    }
  `,
})
export class AdminPage {
  protected readonly store = inject(AdminStore);
  protected readonly currentUser = inject(CurrentUserService);
  protected readonly statuses: { value: CampaignStatus; label: string }[] = [
    { value: 'DRAFT', label: 'Borrador' },
    { value: 'ACTIVE', label: 'Activa' },
    { value: 'CLOSED_BY_DEADLINE', label: 'Cerrada por fecha' },
    { value: 'CLOSED_MANUALLY', label: 'Cerrada' },
    { value: 'CANCELLED', label: 'Cancelada' },
    { value: 'SUSPENDED', label: 'Suspendida' },
  ];

  constructor() {
    effect(() => {
      if (this.currentUser.isAdmin()) void this.store.load();
    });
  }

  protected apply(query: string, status: string): void {
    void this.store.filter((status || null) as CampaignStatus | null, query);
  }
}
