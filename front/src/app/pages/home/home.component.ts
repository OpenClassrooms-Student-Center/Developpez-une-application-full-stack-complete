
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  constructor(private router: Router) {}

  ngOnInit(): void {}

  connexion() {
    this.router.navigate(['/login'])
  }

  enregistrement() {
    this.router.navigate(['/register'])
  }

  theme() {
    this.router.navigate(['/theme'])
  }

  user() {
    this.router.navigate(['/detail'])
  }
  edit() {
    this.router.navigate(['/edit'])
  }
}
