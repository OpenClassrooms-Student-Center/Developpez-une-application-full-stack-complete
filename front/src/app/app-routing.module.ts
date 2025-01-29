import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { UnauthGuard } from './guards/unauth.guard';
import { ThemeComponent } from './pages/theme/component/theme/theme.component';
import { EditComponent } from './pages/user/components/edit/edit.component';
import { AuthGuard } from './guards/auth.guard';
import { ArticleComponent } from './pages/article/features/form/form.component';
import { ListComponent } from './pages/article/features/list/list/list.component';

// consider a guard combined with canLoad / canActivate route option
// to manage unauthenticated user to access private routes
const routes: Routes = [
  { path: 'home', 
  canActivate: [UnauthGuard], 
  component: HomeComponent 
  },
  {
    path: '',
    canActivate: [UnauthGuard],
    loadChildren: () => import('./pages/auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: 'theme',
    canActivate: [AuthGuard],
    component: ThemeComponent
  },
  {
    path: 'edit',
    canActivate: [AuthGuard],
    component: EditComponent
  },
  {
    path: 'article',
    canActivate: [AuthGuard],
    loadChildren: () => import('./pages/article/articles.module').then(m => m.ArticleModule)
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {

}