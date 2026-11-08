import { Component, input } from '@angular/core';
import { MatIconAnchor } from '@angular/material/button';
import { RouterLink } from '@angular/router';

@Component({
  imports: [MatIconAnchor, RouterLink],
  selector: 'app-auth-layout',
  styleUrl: './auth-layout.component.scss',
  templateUrl: './auth-layout.component.html',
})
export class AuthLayout {
  readonly heading = input.required<string>();
}
