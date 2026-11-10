import { Component, inject, TemplateRef, viewChild } from '@angular/core';
import { MatButton } from '@angular/material/button';
import {
  MatDialog,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { Session } from '../../../core/session/session.service';
import { failureMessage } from '../../../shared/forms/server-errors.util';
import { ProfileApi } from '../profile-api.service';

/**
 * The button that deletes the account, and the dialog that asks to confirm it first.
 */
@Component({
  imports: [MatButton, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle],
  selector: 'app-delete-account',
  styleUrl: './delete-account.component.scss',
  templateUrl: './delete-account.component.html',
})
export class DeleteAccount {
  private readonly profileApi = inject(ProfileApi);
  private readonly session = inject(Session);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  // The content of the dialog, in the template: it closes with true to delete
  private readonly confirmation = viewChild.required<TemplateRef<unknown>>('confirmation');

  /**
   * Deletes the account once the user has confirmed it, then goes back to the home page.
   */
  protected async deleteAccount(): Promise<void> {
    const dialogRef = this.dialog.open(this.confirmation());
    const confirmed = await firstValueFrom(dialogRef.afterClosed());

    if (!confirmed) {
      return;
    }

    try {
      await firstValueFrom(this.profileApi.deleteAccount());
    } catch (error) {
      this.snackBar.open(failureMessage(error), undefined, { duration: 3000 });
      return;
    }
    this.session.forget();
    await this.router.navigate(['/']);
  }
}
