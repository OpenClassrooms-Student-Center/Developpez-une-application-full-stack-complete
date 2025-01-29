import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService } from 'src/app/services/session.service';
import { UserService } from '../../service/user.service';
import { User } from '../../interface/user.interface';
import { Theme } from 'src/app/pages/theme/interface/theme';

@Component({
  selector: 'app-edit',
  templateUrl: './edit.component.html',
  styleUrls: ['./edit.component.scss']
})
export class EditComponent implements OnInit {

  private id: string | undefined;

  public editUserForm: FormGroup | undefined;

  public user: User | undefined;

  public themes$: Theme[] | undefined;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private matSnackBar: MatSnackBar,
    private userService: UserService,
    private sessionService : SessionService,
    private router: Router
  ) { }

  ngOnInit(): void {

    this.id = this.route.snapshot.paramMap.get('id')!;
    this.userService
      .getById(this.sessionService.sessionInformation!.id.toString())
      .subscribe((user: User) => this.initForm(user));

    this.userService
      .getById(this.sessionService.sessionInformation!.id.toString())
      .subscribe((user: User) => {
        this.user = user, 
        this.themes$ =  this.user?.themes
      });
  }

  public back(): void {
    window.history.back();
  }
  
  public submit(): void {
    const user = this.editUserForm?.value as User;

      this.userService
        .update(this.sessionService.sessionInformation!.id.toString(), user)
        .subscribe((_: User) => this.exitPage('Vos informations personnelles ont été actualisés !'));
    }

    private exitPage(message: string): void {
      this.matSnackBar.open(message, 'Close', { duration: 3000 });
      this.router.navigate(['home']);
    }

    private initForm(user?: User): void {
      this.editUserForm = this.fb.group({
        firstName: [
          user ? user.firstName : '',
          [Validators.required,
            Validators.min(3),
            Validators.max(40)]
        ],
        email: [
          user ? user.email : '',
          [Validators.required,
            Validators.email]
        ],
        password: [
          user ? user.password : '',
          [
            Validators.required,
            Validators.min(3),
            Validators.max(40)
          ]
        ],
      });
    }
    public logout(): void {
      this.sessionService.logOut();
      this.router.navigate(['/home'])
    }
}
