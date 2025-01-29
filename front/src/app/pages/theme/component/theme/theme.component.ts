import { Component, OnInit } from '@angular/core';
import { Theme } from '../../interface/theme';
import { Observable, mergeMap, of, take } from 'rxjs';
import { ThemeService } from '../../service/theme.service';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from 'src/app/pages/user/service/user.service';
import { SessionInformation } from 'src/app/interfaces/sessionInformation.interface';
import { SessionService } from 'src/app/services/session.service';
import { User } from 'src/app/pages/user/interface/user.interface';

@Component({
  selector: 'app-theme',
  templateUrl: './theme.component.html',
  styleUrls: ['./theme.component.scss']
})
export class ThemeComponent implements OnInit {

  public themes  = this.themeService.all();

  private userId: string;

  constructor(
    private themeService : ThemeService,
    private route: ActivatedRoute, 
    private userService : UserService,
    private sessionService : SessionService
    ) { 
      this.userId = this.sessionService.sessionInformation!.id.toString();
    }
  ngOnInit(): void {

  }

    public follow(themeId : any): void {
      this.userService.follow(themeId, this.userId).subscribe(_ => this.themes= this.themeService.all());
    }
  
    public unFollow(themeId : any): void {
      this.userService.unFollow(themeId, this.userId).subscribe(_ =>  this.themes= this.themeService.all());
    }

}
