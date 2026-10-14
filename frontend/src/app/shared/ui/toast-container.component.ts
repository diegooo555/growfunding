import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ToastService } from '../../core/feedback/toast.service';

@Component({
  selector: 'app-toast-container',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toasts" aria-live="polite">
      @for (toast of toasts.toasts(); track toast.id) {
        <div class="toast" [class]="'toast toast--' + toast.kind">
          <span>{{ toast.message }}</span>
          <button type="button" class="toast__close" (click)="toasts.dismiss(toast.id)" aria-label="Cerrar">×</button>
        </div>
      }
    </div>
  `,
})
export class ToastContainerComponent {
  protected readonly toasts = inject(ToastService);
}
