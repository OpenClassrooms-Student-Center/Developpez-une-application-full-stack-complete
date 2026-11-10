import { HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { openApp } from '../testing/app-testing';
import { Session } from './core/session/session.service';
import { User } from './core/session/user.model';

describe('App', () => {
  const user: User = { id: 42, username: 'arthur.dent', email: 'arthur.dent@example.com' };

  it('shows the home page to a visitor on the root URL', async () => {
    // Act
    const { fixture } = await openApp('/', null);

    // Assert
    const page = fixture.nativeElement as HTMLElement;
    expect(page.querySelector('img')?.alt).toBe('MDD');
    expect(page.querySelector('a[href="/login"]')?.textContent).toContain('Se connecter');
    expect(page.querySelector('a[href="/register"]')?.textContent).toContain('S’inscrire');
  });

  it.each(['/', '/login', '/register'])(
    'sends a logged-in user from %s to the feed',
    async (url) => {
      // Act
      await openApp(url, user);

      // Assert
      expect(TestBed.inject(Router).url).toBe('/feed');
    },
  );

  it('logs the user out from the header and goes back home', async () => {
    // Arrange
    const { fixture } = await openApp('/feed', user);
    const page = fixture.nativeElement as HTMLElement;
    const logOut = [...page.querySelectorAll('header button')].find((button) =>
      button.textContent?.includes('Se déconnecter'),
    ) as HTMLButtonElement;

    // Act
    logOut.click();
    const request = TestBed.inject(HttpTestingController).expectOne('/api/auth/logout');
    request.flush(null, { status: 204, statusText: 'No Content' });
    await fixture.whenStable();

    // Assert
    expect(request.request.method).toBe('POST');
    expect(TestBed.inject(Session).user()).toBeNull();
    expect(TestBed.inject(Router).url).toBe('/');
  });

  it('sends a visitor from the feed to the login page', async () => {
    // Act
    await openApp('/feed', null);

    // Assert
    expect(TestBed.inject(Router).url).toBe('/login');
  });
});
