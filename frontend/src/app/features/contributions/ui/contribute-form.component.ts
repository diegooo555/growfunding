import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { inject } from '@angular/core';
import { ContributionDraft } from '../domain/contribution.models';

export function positiveAmount(control: AbstractControl): ValidationErrors | null {
  const value = Number(control.value);
  if (control.value === null || control.value === '' || Number.isNaN(value)) return null;
  return value > 0 ? null : { positive: true };
}

export function maxTwoDecimals(control: AbstractControl): ValidationErrors | null {
  if (control.value === null || control.value === '') return null;
  return /^\d+(\.\d{1,2})?$/.test(String(control.value)) ? null : { decimals: true };
}

@Component({
  selector: 'app-contribute-form',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form class="card form" [formGroup]="form" (ngSubmit)="submit()">
      <h3>Hacer un aporte</h3>
      @if (!enabled()) {
        <p class="muted">Esta campaña no está recibiendo aportes.</p>
      } @else {
        <label class="field">
          <span>Monto ({{ currency() }})</span>
          <input type="number" min="0.01" step="0.01" formControlName="amount" placeholder="Ej. 50" />
          @if (form.controls.amount.touched && form.controls.amount.errors; as errors) {
            <small class="field__error">
              @if (errors['required']) { Ingrese un monto. }
              @else if (errors['positive']) { El monto debe ser mayor a cero. }
              @else if (errors['decimals']) { Use como máximo dos decimales. }
            </small>
          }
        </label>
        <label class="field">
          <span>Mensaje (opcional)</span>
          <textarea rows="2" maxlength="500" formControlName="message" placeholder="Unas palabras de apoyo"></textarea>
        </label>
        <label class="checkbox">
          <input type="checkbox" formControlName="anonymous" />
          <span>Aportar de forma anónima</span>
        </label>
        <button type="submit" class="btn btn--primary" [disabled]="submitting()">
          {{ submitting() ? 'Procesando…' : 'Aportar' }}
        </button>
      }
    </form>
  `,
})
export class ContributeFormComponent {
  private readonly fb = inject(FormBuilder);

  readonly currency = input('USD');
  readonly enabled = input(true);
  readonly submitting = input(false);
  readonly contribute = output<ContributionDraft>();

  readonly form = this.fb.nonNullable.group({
    amount: this.fb.control<number | null>(null, [Validators.required, positiveAmount, maxTwoDecimals]),
    message: [''],
    anonymous: [false],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { amount, message, anonymous } = this.form.getRawValue();
    this.contribute.emit({ amount: Number(amount), message, anonymous });
  }

  reset(): void {
    this.form.reset({ amount: null, message: '', anonymous: false });
  }
}
