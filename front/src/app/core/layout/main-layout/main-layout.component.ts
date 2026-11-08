import { Component, inject } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { Session } from '../../session/session.service';

@Component({
  imports: [MatButton, RouterLink, RouterOutlet],
  selector: 'app-main-layout',
  styleUrl: './main-layout.component.scss',
  templateUrl: './main-layout.component.html',
})
export class MainLayout {
  private readonly session = inject(Session);
  private readonly router = inject(Router);

  protected async logOut(): Promise<void> {
    await firstValueFrom(this.session.logOut());
    await this.router.navigateByUrl('/');
  }
}
