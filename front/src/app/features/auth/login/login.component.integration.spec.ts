import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { fieldError, openApp, submit, type } from '../../../../testing/app-testing';
import { Session } from '../../../core/session/session.service';

describe('Login page', () => {
  const credentials = { email: 'arthur.dent@example.com', password: 'Dont-Panic-42' };

  function fillIn(page: HTMLElement): void {
    type(page, 'email', credentials.email);
    type(page, 'password', credentials.password);
  }

  it('logs the user in and opens the feed', async () => {
    // Arrange
    const { fixture, http, page } = await openApp('/login', null);
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
    const { fixture, http, page } = await openApp('/login', null);
    type(page, 'email', credentials.email);

    // Act
    submit(page);
    await fixture.whenStable();

    // Assert
    http.expectNone('/api/auth/login');
    const password = page.querySelector('#password') as HTMLInputElement;
    const error = fieldError(page, 'password');
    expect(error?.textContent?.trim()).toBeTruthy();
    expect(password.getAttribute('aria-describedby')).toContain(error?.id);
  });

  it('says the credentials are wrong and lets the user try again', async () => {
    // Arrange
    const { fixture, http, page } = await openApp('/login', null);
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
