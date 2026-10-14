import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { CurrentUserService } from '../identity/current-user.service';

export const USER_HEADER = 'X-User-Id';

/** Adds the simulated identity header to every API call. */
export const userIdInterceptor: HttpInterceptorFn = (req, next) => {
  const userId = inject(CurrentUserService).currentUserId();
  if (!userId || !req.url.startsWith('/api') || req.headers.has(USER_HEADER)) {
    return next(req);
  }
  return next(req.clone({ setHeaders: { [USER_HEADER]: userId } }));
};
