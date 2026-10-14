import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

const LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  ACTIVE: 'Activa',
  CLOSED_BY_DEADLINE: 'Cerrada por fecha',
  CLOSED_MANUALLY: 'Cerrada',
  CANCELLED: 'Cancelada',
  SUSPENDED: 'Suspendida',
};

@Component({
  selector: 'app-status-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="badge" [class]="'badge badge--' + status().toLowerCase()">{{ label() }}</span>`,
})
export class StatusBadgeComponent {
  readonly status = input.required<string>();
  protected readonly label = computed(() => LABELS[this.status()] ?? this.status());
}
