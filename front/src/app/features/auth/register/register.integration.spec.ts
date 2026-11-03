import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationInitStatus } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { App } from '../../../app';
import { appConfig } from '../../../app.config';
import { Session } from '../../../core/session/session';

interface RegisterPage {
  fixture: ComponentFixture<App>;
  http: HttpTestingController;
  page: HTMLElement;
}

describe('Register page', () => {
  const account = {
    username: 'arthur.dent',
    email: 'arthur.dent@example.com',
    password: 'Dont-Panic-42',
  };

  async function openRegisterPage(): Promise<RegisterPage> {
    TestBed.configureTestingModule({
      providers: [...appConfig.providers, provideHttpClientTesting()],
    });
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/users/me').flush(null, { status: 401, statusText: 'Unauthorized' });
    await TestBed.inject(ApplicationInitStatus).donePromise;

    const fixture = TestBed.createComponent(App);
    await TestBed.inject(Router).navigateByUrl('/register');
    await fixture.whenStable();
    return { fixture, http, page: fixture.nativeElement as HTMLElement };
  }

  function type(page: HTMLElement, id: string, value: string): void {
    const input = page.querySelector(`#${id}`) as HTMLInputElement;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function fillIn(page: HTMLElement): void {
    type(page, 'username', account.username);
    type(page, 'email', account.email);
    type(page, 'password', account.password);
  }

  function submit(page: HTMLElement): void {
    (page.querySelector('button[type="submit"]') as HTMLButtonElement).click();
  }

  it('creates the account, logs the user in and opens the feed', async () => {
    // Arrange
    const { fixture, http, page } = await openRegisterPage();
    const user = { id: 42, username: account.username, email: account.email };
    fillIn(page);

    // Act
    submit(page);
    const request = http.expectOne('/api/auth/register');
    request.flush(user, { status: 201, statusText: 'Created' });
    await fixture.whenStable();

    // Assert
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(account);
    expect(TestBed.inject(Session).user()).toEqual(user);
    expect(TestBed.inject(Router).url).toBe('/feed');
  });

  it('explains what is wrong and sends nothing while a field is invalid', async () => {
    // Arrange
    const { fixture, http, page } = await openRegisterPage();
    fillIn(page);
    type(page, 'password', 'Deep-42');

    // Act
    submit(page);
    await fixture.whenStable();

    // Assert
    http.expectNone('/api/auth/register');
    const password = page.querySelector('#password') as HTMLInputElement;
    const error = password.closest('mat-form-field')?.querySelector('mat-error');
    expect(error?.textContent?.trim()).toBeTruthy();
    expect(password.getAttribute('aria-invalid')).toBe('true');
    expect(password.getAttribute('aria-describedby')).toContain(error?.id);
  });

  it('shows under the field a value that is already taken', async () => {
    // Arrange
    const { fixture, http, page } = await openRegisterPage();
    const message = 'Ce nom d’utilisateur est déjà pris.';
    fillIn(page);

    // Act
    submit(page);
    http
      .expectOne('/api/auth/register')
      .flush(
        { detail: message, errors: { username: message } },
        { status: 409, statusText: 'Conflict' },
      );
    await fixture.whenStable();

    // Assert
    const username = page.querySelector('#username') as HTMLInputElement;
    const error = username.closest('mat-form-field')?.querySelector('mat-error');
    expect(error?.textContent).toContain(message);
    expect(TestBed.inject(Router).url).toBe('/register');
  });

  it('sends a taken value again only once the user has changed it', async () => {
    // Arrange
    const { fixture, http, page } = await openRegisterPage();
    const message = 'Ce nom d’utilisateur est déjà pris.';
    fillIn(page);
    submit(page);
    http
      .expectOne('/api/auth/register')
      .flush(
        { detail: message, errors: { username: message } },
        { status: 409, statusText: 'Conflict' },
      );
    await fixture.whenStable();

    // Act: the same value again
    submit(page);
    await fixture.whenStable();

    // Assert
    http.expectNone('/api/auth/register');

    // Act: another value
    type(page, 'username', 'ford.prefect');
    submit(page);
    await fixture.whenStable();

    // Assert
    http.expectOne('/api/auth/register');
  });

  it('lets the user try again after a failure of the API', async () => {
    // Arrange
    const { fixture, http, page } = await openRegisterPage();
    fillIn(page);

    // Act: a first try
    submit(page);
    http
      .expectOne('/api/auth/register')
      .flush(null, { status: 500, statusText: 'Internal Server Error' });
    await fixture.whenStable();

    // Assert
    expect(page.querySelector('[role="alert"]')?.textContent?.trim()).toBeTruthy();

    // Act: a second try
    submit(page);
    await fixture.whenStable();

    // Assert
    http.expectOne('/api/auth/register');
  });
});
