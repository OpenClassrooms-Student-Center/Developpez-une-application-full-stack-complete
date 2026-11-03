import { Component, input } from '@angular/core';
import { MatIconAnchor } from '@angular/material/button';
import { RouterLink } from '@angular/router';

@Component({
  imports: [MatIconAnchor, RouterLink],
  selector: 'app-auth-layout',
  styleUrl: './auth-layout.scss',
  templateUrl: './auth-layout.html',
})
export class AuthLayout {
  readonly heading = input.required<string>();
}
