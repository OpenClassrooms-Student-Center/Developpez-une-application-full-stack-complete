import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationInitStatus } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { App } from '../../../app.component';
import { appConfig } from '../../../app.config';
import { Session } from '../../../core/session/session.service';

interface LoginPage {
  fixture: ComponentFixture<App>;
  http: HttpTestingController;
  page: HTMLElement;
}

describe('Login page', () => {
  const credentials = { email: 'arthur.dent@example.com', password: 'Dont-Panic-42' };

  async function openLoginPage(): Promise<LoginPage> {
    TestBed.configureTestingModule({
      providers: [...appConfig.providers, provideHttpClientTesting()],
    });
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/users/me').flush(null, { status: 401, statusText: 'Unauthorized' });
    await TestBed.inject(ApplicationInitStatus).donePromise;

    const fixture = TestBed.createComponent(App);
    await TestBed.inject(Router).navigateByUrl('/login');
    await fixture.whenStable();
    return { fixture, http, page: fixture.nativeElement as HTMLElement };
  }

  function type(page: HTMLElement, id: string, value: string): void {
    const input = page.querySelector(`#${id}`) as HTMLInputElement;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function fillIn(page: HTMLElement): void {
    type(page, 'email', credentials.email);
    type(page, 'password', credentials.password);
  }

  function submit(page: HTMLElement): void {
    (page.querySelector('button[type="submit"]') as HTMLButtonElement).click();
  }

  it('logs the user in and opens the feed', async () => {
    // Arrange
    const { fixture, http, page } = await openLoginPage();
    const user = { id: 42, username: 'arthur.dent', email: credentials.email };
    fillIn(page);

    // Act
    submit(page);
    const request = http.expectOne('/api/auth/login');
    request.flush(user);
    await fixture.whenStable();

    // Assert
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(credentials);
    expect(TestBed.inject(Session).user()).toEqual(user);
    expect(TestBed.inject(Router).url).toBe('/feed');
  });

  it('sends nothing while a field is empty', async () => {
    // Arrange
    const { fixture, http, page } = await openLoginPage();
    type(page, 'email', credentials.email);

    // Act
    submit(page);
    await fixture.whenStable();

    // Assert
    http.expectNone('/api/auth/login');
    const password = page.querySelector('#password') as HTMLInputElement;
    const error = password.closest('mat-form-field')?.querySelector('mat-error');
    expect(error?.textContent?.trim()).toBeTruthy();
    expect(password.getAttribute('aria-describedby')).toContain(error?.id);
  });

  it('says the credentials are wrong and lets the user try again', async () => {
    // Arrange
    const { fixture, http, page } = await openLoginPage();
    fillIn(page);

    // Act: a first try
    submit(page);
    http
      .expectOne('/api/auth/login')
      .flush(
        { detail: 'E-mail ou mot de passe incorrect.' },
        { status: 401, statusText: 'Unauthorized' },
      );
    await fixture.whenStable();

    // Assert
    expect(page.querySelector('[role="alert"]')?.textContent).toContain(
      'E-mail ou mot de passe incorrect.',
    );
    expect(TestBed.inject(Router).url).toBe('/login');

    // Act: a second try
    submit(page);
    await fixture.whenStable();

    // Assert
    http.expectOne('/api/auth/login');
  });
});
