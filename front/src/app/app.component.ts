import {Component, ViewChild} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';
import {filter, Observable} from 'rxjs';
import {MatSidenav} from "@angular/material/sidenav";
import {AuthService} from "./features/auth/services/auth.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  @ViewChild('sidenav') sidenav!: MatSidenav;
  currentRoute:string = "";

  constructor(
    private router: Router,
    private authService: AuthService
  )
  {
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.currentRoute = event.url;
    });
  }
  public openSidenav(): void {
    this.sidenav.open();
  }
  public $isLogged(): Observable<boolean> {
    return this.authService.$isLogged();
  }

}
