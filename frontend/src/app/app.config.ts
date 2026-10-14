import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { errorInterceptor } from './core/http/error.interceptor';
import { userIdInterceptor } from './core/http/user-id.interceptor';
import { CurrentUserService } from './core/identity/current-user.service';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([userIdInterceptor, errorInterceptor])),
    provideAppInitializer(() => inject(CurrentUserService).load()),
  ],
};
