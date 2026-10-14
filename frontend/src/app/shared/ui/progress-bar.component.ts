import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-progress-bar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="progress" role="progressbar" [attr.aria-valuenow]="percent()" aria-valuemin="0"
         aria-valuemax="100" [class.progress--done]="percent() >= 100">
      <div class="progress__fill" [style.width.%]="width()"></div>
    </div>
  `,
})
export class ProgressBarComponent {
  readonly percent = input.required<number>();
  protected readonly width = computed(() => Math.min(100, Math.max(0, this.percent())));
}
