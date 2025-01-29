import { Component, OnInit } from '@angular/core';
import { Article } from '../../../interface/article';
import { Observable } from 'rxjs';
import { ArticleService } from '../../../service/article.service';
import { ActivatedRoute, Router } from '@angular/router';
import { CommentService } from 'src/app/pages/comment/service/comment.service';
import { Comment } from 'src/app/pages/comment/interface/comment.interface';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {

  public articles$: Observable<Article[]> = this.articleService.allDesc();

  public articlesAsc$: Observable<Article[]> = this.articleService.allAsc();

  public commentForm: FormGroup | undefined;

  public ascending : boolean | undefined;

  constructor(
    private articleService : ArticleService,
    private router : Router
  ) { }

  ngOnInit(): void {
  }

  create() {
    this.router.navigateByUrl('article/create');
  }

  onDownClick() {
    this.ascending=true;
  }

  onUpClick() {
    this.ascending=false;
  }
}
