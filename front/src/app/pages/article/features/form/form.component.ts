import { Component, OnInit } from '@angular/core';
import { Article } from '../../interface/article';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ArticleService } from '../../service/article.service';
import { SessionService } from 'src/app/services/session.service';
import { ThemeService } from 'src/app/pages/theme/service/theme.service';
import { Observable } from 'rxjs';
import { Theme } from 'src/app/pages/theme/interface/theme';
import { UserService } from 'src/app/pages/user/service/user.service';

@Component({
  selector: 'app-article',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.scss'],
})
export class ArticleComponent implements OnInit {

  public articleForm: FormGroup | undefined;

  public userId: string;

  // a faire seulement pour abonnements
  public themes$: Observable<Theme[]> = this.themeService.all();

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private matSnackBar: MatSnackBar,
    private articleService: ArticleService,
    private themeService: ThemeService,
    private sessionService: SessionService,
    private userService : UserService,
    private router: Router
  ) {
    this.userId = this.sessionService.sessionInformation!.id.toString();
   }

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(article?: Article): void {
    this.articleForm = this.fb.group({
      titre: [
        article ? article.titre : '',
        [Validators.required]
      ],
      theme_id: [
        article ? article.themeId : '',
        [Validators.required]
      ],
      contenu: [
        article ? article.contenu : '',
        [
          Validators.required,
          Validators.max(2000)
        ]
      ],
      user_id: [
        article ? article.user_id : this.userId ,
      ]
    });
  }
  public submit(): void {
    const article = this.articleForm?.value as Article;
      this.articleService
        .create(article)
        .subscribe((article: Article) => this.exitPage('Session created !'));
  }

  private exitPage(message: string): void {
    this.matSnackBar.open(message, 'Close', { duration: 3000 });
    this.router.navigate(['article/list']);
  }
}
