import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationInitStatus } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { App } from '../app/app.component';
import { appConfig } from '../app/app.config';
import { User } from '../app/core/session/user.model';

export interface OpenedApp {
  fixture: ComponentFixture<App>;
  http: HttpTestingController;
  page: HTMLElement;
}

/**
 * Starts the real application, with its routes, guards and services, as a visitor (null) or as
 * a logged-in user, then opens a URL. Only the HTTP back end is simulated.
 */
export async function openApp(url: string, user: User | null): Promise<OpenedApp> {
  TestBed.configureTestingModule({
    providers: [...appConfig.providers, provideHttpClientTesting()],
  });
  const http = TestBed.inject(HttpTestingController);
  const me = http.expectOne('/api/users/me');
  if (user) {
    me.flush(user);
  } else {
    me.flush(null, { status: 401, statusText: 'Unauthorized' });
  }
  await TestBed.inject(ApplicationInitStatus).donePromise;

  const fixture = TestBed.createComponent(App);
  await TestBed.inject(Router).navigateByUrl(url);
  await fixture.whenStable();
  return { fixture, http, page: fixture.nativeElement as HTMLElement };
}

/** Types a value in the field with this id, as a user would. */
export function type(page: HTMLElement, id: string, value: string): void {
  const input = page.querySelector(`#${id}`) as HTMLInputElement;
  input.value = value;
  input.dispatchEvent(new Event('input'));
}

/** Clicks the submit button of a page, or of one of its forms when the page has several. */
export function submit(root: ParentNode): void {
  (root.querySelector('button[type="submit"]') as HTMLButtonElement).click();
}

/** Finds the error shown under the field with this id. */
export function fieldError(page: HTMLElement, id: string): HTMLElement | null {
  return (
    page.querySelector(`#${id}`)?.closest('mat-form-field')?.querySelector('mat-error') ?? null
  );
}
