import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
@Component({
  selector: 'app-article-page',
  templateUrl: './article-page.component.html',
  styleUrls: ['./article-page.component.scss']
})
export class ArticlePageComponent implements OnInit {
  articles: any[] = [];
  indexArray: number[] = [];

  constructor(private http: HttpClient) { }
  ngOnInit(): void {
    this.fetchArticles();
  }
  fetchArticles(): void {
    this.http.get<any>('http://localhost:8080/api/articles')
      .subscribe((response) => {
        this.articles = response.articles;
        for (let i = 0; i < this.articles.length / 2; i++) {
          this.indexArray.push(i);
        }      }, (error) => {
        console.error('Erreur lors de la récupération des articles :', error);
      });
  }
}