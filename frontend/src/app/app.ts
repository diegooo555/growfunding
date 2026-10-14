import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './core/layout/header.component';
import { ToastContainerComponent } from './shared/ui/toast-container.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent, ToastContainerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-header />
    <main class="container">
      <router-outlet />
    </main>
    <footer class="footer muted small">G7 Impulsa · Plataforma de crowdfunding (proyecto académico)</footer>
    <app-toast-container />
  `,
})
export class App {}
