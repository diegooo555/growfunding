import { ChangeDetectionStrategy, Component, inject, input, output } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { positiveAmount } from '../../contributions/ui/contribute-form.component';
import { CATEGORIES, CATEGORY_LABELS, CreateCampaignRequest } from '../domain/campaign.models';

export function futureDate(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  return new Date(control.value).getTime() > Date.now() ? null : { future: true };
}

@Component({
  selector: 'app-campaign-form',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form class="card form form--wide" [formGroup]="form" (ngSubmit)="submit()">
      <label class="field">
        <span>Título</span>
        <input type="text" maxlength="150" formControlName="title" placeholder="Nombre de la campaña" />
        @if (invalid('title')) { <small class="field__error">El título es obligatorio.</small> }
      </label>
      <label class="field">
        <span>Descripción</span>
        <textarea rows="5" maxlength="4000" formControlName="description"
                  placeholder="Cuente qué quiere lograr y cómo usará los fondos"></textarea>
        @if (invalid('description')) { <small class="field__error">La descripción es obligatoria.</small> }
      </label>
      <div class="form__row">
        <label class="field">
          <span>Meta (USD)</span>
          <input type="number" min="1" step="0.01" formControlName="goalAmount" placeholder="Ej. 5000" />
          @if (invalid('goalAmount')) { <small class="field__error">La meta debe ser mayor a cero.</small> }
        </label>
        <label class="field">
          <span>Fecha límite</span>
          <input type="datetime-local" formControlName="deadline" />
          @if (invalid('deadline')) { <small class="field__error">La fecha límite debe ser futura.</small> }
        </label>
      </div>
      <div class="form__row">
        <label class="field">
          <span>Categoría</span>
          <select formControlName="category">
            @for (c of categories; track c) {
              <option [value]="c">{{ labels[c] }}</option>
            }
          </select>
        </label>
        <label class="field">
          <span>Visibilidad de aportantes</span>
          <select formControlName="contributorsVisibility">
            <option value="PUBLIC">Pública: cualquiera ve la lista</option>
            <option value="PRIVATE">Privada: solo usted la ve</option>
          </select>
        </label>
      </div>
      <p class="muted small">La campaña se crea como borrador. Podrá publicarla desde "Mis campañas".</p>
      <button type="submit" class="btn btn--primary" [disabled]="submitting()">
        {{ submitting() ? 'Guardando…' : 'Crear campaña' }}
      </button>
    </form>
  `,
})
export class CampaignFormComponent {
  private readonly fb = inject(FormBuilder);

  readonly submitting = input(false);
  readonly save = output<CreateCampaignRequest>();

  protected readonly categories = CATEGORIES;
  protected readonly labels = CATEGORY_LABELS;

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.required, Validators.maxLength(4000)]],
    goalAmount: this.fb.control<number | null>(null, [Validators.required, positiveAmount]),
    deadline: ['', [Validators.required, futureDate]],
    category: this.fb.nonNullable.control<CreateCampaignRequest['category']>('COMMUNITY'),
    contributorsVisibility: this.fb.nonNullable.control<CreateCampaignRequest['contributorsVisibility']>('PUBLIC'),
  });

  protected invalid(name: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.save.emit({
      title: v.title.trim(),
      description: v.description.trim(),
      goalAmount: Number(v.goalAmount),
      currency: 'USD',
      deadline: new Date(v.deadline).toISOString(),
      category: v.category,
      contributorsVisibility: v.contributorsVisibility,
    });
  }
}
