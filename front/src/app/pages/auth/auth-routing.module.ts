import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegisterComponent } from './components/register/register/register.component';
import { LoginComponent } from './components/login/login/login.component';
import { UnauthGuard } from 'src/app/guards/unauth.guard';


const routes: Routes = [
  { title: 'Login', path: 'login', component: LoginComponent, canActivate: [UnauthGuard]  },
  { title: 'Register', path: 'register', component: RegisterComponent, canActivate: [UnauthGuard]  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuthRoutingModule { }
