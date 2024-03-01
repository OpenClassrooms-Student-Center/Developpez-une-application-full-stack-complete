import { Component, inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/common/layout/header/header.component';
import { filter, take } from 'rxjs';
import { Store } from '@ngrx/store';
import { toSignal } from '@angular/core/rxjs-interop';
import { UserBasicInfo, UserEntity } from '@core/types/user.type';
import { UserService } from '@core/services/user/user.service';
import { setInfo } from '@mdd-global-state-ngrx/actions/user-info.actions';

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  imports: [RouterOutlet, HeaderComponent],
})
export class AppComponent {
  title = 'mdd-app';

  private store = inject(Store);

  public userService = inject(UserService);

  public userInfo = toSignal<UserBasicInfo>(this.store.select('userInfo'));

  // * Instance properties
  public showHeader = true;

  // * Dependency injections
  public router = inject(Router);

  // * Observables
  private navigationEndEvents$ = this.router.events.pipe(
    filter((event): event is NavigationEnd => {
      return event instanceof NavigationEnd;
    })
  );

  ngOnInit() {
    if (this.router.url !== 'login' && this.router.url !== 'register') {
      this.userService.getUser().pipe(take(1));
      // .subscribe((userInfo) => {
      //   const { id, email, username } = userInfo;
      //   this.store.dispatch(setInfo({ id, email, username }));
      // });
    }

    this.navigationEndEvents$.subscribe(() => {
      this.showHeader = this.router.url !== '/home';
    });
  }
}
