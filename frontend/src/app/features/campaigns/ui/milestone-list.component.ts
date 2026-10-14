import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { Milestone } from '../domain/campaign.models';

const LABELS: Record<Milestone['type'], string> = {
  PUBLISHED: 'Campaña publicada',
  GOAL_REACHED: 'Meta alcanzada',
  CLOSING_SOON: 'Aviso de cierre próximo',
  CLOSED: 'Campaña cerrada',
};

@Component({
  selector: 'app-milestone-list',
  imports: [DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="card">
      <h3>Hitos</h3>
      @if (milestones().length === 0) {
        <p class="muted">Sin hitos todavía.</p>
      } @else {
        <ol class="timeline">
          @for (m of milestones(); track m.type) {
            <li class="timeline__item" [class]="'timeline__item timeline__item--' + m.type.toLowerCase()">
              <strong>{{ labels[m.type] }}</strong>
              <small class="muted">{{ m.reachedAt | date: 'dd/MM/yyyy HH:mm' }}</small>
            </li>
          }
        </ol>
      }
    </section>
  `,
})
export class MilestoneListComponent {
  readonly milestones = input.required<Milestone[]>();
  protected readonly labels = LABELS;
}
