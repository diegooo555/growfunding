import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../feedback/toast.service';
import { toApiError } from './api-error';

/**
 * Normalizes HTTP failures into ApiError. Connectivity problems are surfaced globally;
 * business errors are left to the feature that triggered them.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toasts = inject(ToastService);
  return next(req).pipe(
    catchError((error) => {
      const apiError = toApiError(error);
      if (apiError.status === 0 || apiError.status >= 500) {
        toasts.error(apiError.status === 0 ? apiError.detail : 'Error del servidor. Intente más tarde.');
      }
      return throwError(() => apiError);
    }),
  );
};
