import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrl: './home.component.css',
    standalone: false
})
export class HomeComponent {
  
  constructor(private router:Router){

  }


  login() {
    console.log('login.....');
    this.router.navigate(['/auth/login']);

  }
  register() {
    console.log('register.....');
    this.router.navigate(['/auth/register']);

  }

}
