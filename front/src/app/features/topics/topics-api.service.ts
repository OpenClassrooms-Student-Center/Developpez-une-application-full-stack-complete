import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Topic } from './topic.model';

@Injectable({ providedIn: 'root' })
export class TopicsApi {
  private readonly http = inject(HttpClient);

  /**
   * Lists every topic, sorted by title, and says which ones the user follows.
   */
  list(): Observable<Topic[]> {
    return this.http.get<Topic[]>('/api/topics');
  }

  /**
   * Follows a topic. Following it again changes nothing.
   */
  subscribe(id: number): Observable<void> {
    return this.http.put<void>(`/api/topics/${id}/subscription`, null);
  }

  /**
   * Stops following a topic. Nothing changes if the user did not follow it.
   */
  unsubscribe(id: number): Observable<void> {
    return this.http.delete<void>(`/api/topics/${id}/subscription`);
  }
}
