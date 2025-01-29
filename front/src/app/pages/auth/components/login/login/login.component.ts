import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { LoginRequest } from '../../../interfaces/interfaces/loginRequest.interface';
import { AuthSuccess } from '../../../interfaces/interfaces/authSuccess.interface';
import { UserService } from 'src/app/pages/user/service/user.service';
import { User } from 'src/app/pages/user/interface/user.interface';
import { SessionService } from 'src/app/services/session.service';
import { SessionInformation } from 'src/app/interfaces/sessionInformation.interface';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  public hide = true;
  public onError = false;

  public form = this.fb.group({
    emailOrFirstName: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.min(8)]]
  });

  constructor(private authService: AuthService, 
    private fb: FormBuilder, 
    private router: Router,
    private sessionService: SessionService) { }

  public submit(): void {
    const loginRequest = this.form.value as LoginRequest;
    this.authService.login(loginRequest).subscribe({
      next: (response: SessionInformation) => {
        this.sessionService.logIn(response);
        console.log(this.sessionService.sessionInformation?.token);
        this.router.navigate(['article/list']);
      },
      error: error => {this.onError = true,
      console.log(error)}
    });
  }

}
