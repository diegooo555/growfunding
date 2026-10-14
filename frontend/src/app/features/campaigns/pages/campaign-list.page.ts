import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { EmptyStateComponent } from '../../../shared/ui/empty-state.component';
import { CampaignListStore } from '../application/campaign-list.store';
import { CampaignFilters } from '../domain/campaign.models';
import { CampaignCardComponent } from '../ui/campaign-card.component';
import { CampaignFiltersComponent } from '../ui/campaign-filters.component';

@Component({
  selector: 'app-campaign-list-page',
  imports: [CampaignCardComponent, CampaignFiltersComponent, EmptyStateComponent],
  providers: [CampaignListStore],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="page-header">
      <div>
        <h1>Campañas activas</h1>
        <p class="muted">Descubra proyectos y apoye los que le interesen.</p>
      </div>
      @if (store.page(); as page) {
        <span class="muted">{{ page.totalElements }} resultados</span>
      }
    </header>

    <app-campaign-filters [filters]="store.filters()" (changed)="onFilters($event)" />

    @if (store.error()) {
      <p class="notice notice--error">{{ store.error() }}</p>
    } @else if (store.loading() && !store.page()) {
      <p class="muted">Cargando campañas…</p>
    } @else if (!store.hasResults()) {
      <app-empty-state title="No hay campañas que coincidan" hint="Pruebe con otros filtros.">
        <button type="button" class="btn" (click)="store.reset()">Limpiar filtros</button>
      </app-empty-state>
    } @else {
      <div class="grid" [class.is-loading]="store.loading()">
        @for (campaign of store.campaigns(); track campaign.id) {
          <app-campaign-card [campaign]="campaign" />
        }
      </div>
      @if (store.totalPages() > 1) {
        <nav class="pagination" aria-label="Paginación">
          <button type="button" class="btn" [disabled]="store.filters().page === 0"
                  (click)="store.goToPage(store.filters().page - 1)">Anterior</button>
          <span>Página {{ store.filters().page + 1 }} de {{ store.totalPages() }}</span>
          <button type="button" class="btn" [disabled]="store.filters().page + 1 >= store.totalPages()"
                  (click)="store.goToPage(store.filters().page + 1)">Siguiente</button>
        </nav>
      }
    }
  `,
})
export class CampaignListPage implements OnInit {
  protected readonly store = inject(CampaignListStore);

  ngOnInit(): void {
    void this.store.load();
  }

  protected onFilters(patch: Partial<CampaignFilters>): void {
    void this.store.applyFilters(patch);
  }
}
