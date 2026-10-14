import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AppNotification } from '../domain/notification.models';

@Injectable({ providedIn: 'root' })
export class NotificationsApi {
  private readonly http = inject(HttpClient);

  list(userId: string): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`/api/users/${userId}/notifications`);
  }

  markAllRead(userId: string): Observable<{ updated: number }> {
    return this.http.post<{ updated: number }>(`/api/users/${userId}/notifications/read-all`, {});
  }
}
