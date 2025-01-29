import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Comment } from '../interface/comment.interface';

@Injectable({
  providedIn: 'root'
})
export class CommentService {

  private pathService = 'api/comment';

  constructor(private httpClient : HttpClient) { 

  }

  public create(comment : Comment): Observable<Comment> {
    return this.httpClient.post<Comment>(`${this.pathService}`, comment);
  }

  public getByArticleId(id: string): Observable<Comment> {
    return this.httpClient.get<Comment>(`${this.pathService}/${id}`);
  }
}
