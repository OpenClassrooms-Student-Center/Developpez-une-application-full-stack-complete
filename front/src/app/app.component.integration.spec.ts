import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationInitStatus } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { App } from './app.component';
import { appConfig } from './app.config';
import { Session } from './core/session/session.service';
import { User } from './core/session/user.model';

describe('App', () => {
  const user: User = { id: 42, username: 'arthur.dent', email: 'arthur.dent@example.com' };

  /** Starts the application as a visitor (null) or as a logged-in user, and opens a URL. */
  async function open(url: string, currentUser: User | null): Promise<ComponentFixture<App>> {
    TestBed.configureTestingModule({
      providers: [...appConfig.providers, provideHttpClientTesting()],
    });
    const me = TestBed.inject(HttpTestingController).expectOne('/api/users/me');
    if (currentUser) {
      me.flush(currentUser);
    } else {
      me.flush(null, { status: 401, statusText: 'Unauthorized' });
    }
    await TestBed.inject(ApplicationInitStatus).donePromise;

    const fixture = TestBed.createComponent(App);
    await TestBed.inject(Router).navigateByUrl(url);
    await fixture.whenStable();
    return fixture;
  }

  it('shows the home page to a visitor on the root URL', async () => {
    // Act
    const fixture = await open('/', null);

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
      await open(url, user);

      // Assert
      expect(TestBed.inject(Router).url).toBe('/feed');
    },
  );

  it('logs the user out from the header and goes back home', async () => {
    // Arrange
    const fixture = await open('/feed', user);
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
    await open('/feed', null);

    // Assert
    expect(TestBed.inject(Router).url).toBe('/login');
  });
});
