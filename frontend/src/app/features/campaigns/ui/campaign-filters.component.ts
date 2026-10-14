import { ChangeDetectionStrategy, Component, OnDestroy, input, output } from '@angular/core';
import { CATEGORIES, CATEGORY_LABELS, CampaignFilters, CampaignSort, Category } from '../domain/campaign.models';

@Component({
  selector: 'app-campaign-filters',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form class="card filters" (submit)="$event.preventDefault()">
      <label class="field filters__search">
        <span>Buscar</span>
        <input type="search" placeholder="Título o descripción" [value]="filters().q"
               (input)="onSearch($any($event.target).value)" />
      </label>
      <label class="field">
        <span>Categoría</span>
        <select [value]="filters().category ?? ''" (change)="changed.emit({ category: toCategory($any($event.target).value) })">
          <option value="">Todas</option>
          @for (c of categories; track c) {
            <option [value]="c">{{ labels[c] }}</option>
          }
        </select>
      </label>
      <label class="field">
        <span>Progreso mínimo</span>
        <select [value]="filters().minProgress ?? ''" (change)="changed.emit({ minProgress: toNumber($any($event.target).value) })">
          <option value="">Cualquiera</option>
          <option value="25">25% o más</option>
          <option value="50">50% o más</option>
          <option value="75">75% o más</option>
          <option value="100">Meta alcanzada</option>
        </select>
      </label>
      <label class="field">
        <span>Ordenar por</span>
        <select [value]="filters().sort" (change)="changed.emit({ sort: $any($event.target).value })">
          @for (s of sorts; track s.value) {
            <option [value]="s.value">{{ s.label }}</option>
          }
        </select>
      </label>
      <label class="checkbox filters__toggle">
        <input type="checkbox" [checked]="filters().closingSoon"
               (change)="changed.emit({ closingSoon: $any($event.target).checked })" />
        <span>Cierran pronto</span>
      </label>
    </form>
  `,
})
export class CampaignFiltersComponent implements OnDestroy {
  readonly filters = input.required<CampaignFilters>();
  readonly changed = output<Partial<CampaignFilters>>();

  protected readonly categories = CATEGORIES;
  protected readonly labels = CATEGORY_LABELS;
  protected readonly sorts: { value: CampaignSort; label: string }[] = [
    { value: 'deadline', label: 'Próximas a cerrar' },
    { value: 'newest', label: 'Más recientes' },
    { value: 'progress', label: 'Mayor progreso' },
    { value: 'raised', label: 'Más recaudado' },
  ];

  private debounce?: ReturnType<typeof setTimeout>;

  protected onSearch(value: string): void {
    clearTimeout(this.debounce);
    this.debounce = setTimeout(() => this.changed.emit({ q: value }), 350);
  }

  protected toCategory(value: string): Category | null {
    return value ? (value as Category) : null;
  }

  protected toNumber(value: string): number | null {
    return value ? Number(value) : null;
  }

  ngOnDestroy(): void {
    clearTimeout(this.debounce);
  }
}
