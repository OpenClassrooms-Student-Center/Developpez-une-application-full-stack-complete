import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { fieldError, openApp, OpenedApp, submit, type } from '../../../testing/app-testing';
import { Session } from '../../core/session/session.service';
import { User } from '../../core/session/user.model';
import { Topic } from '../topics/topic.model';

describe('Profile page', () => {
  const user: User = { id: 42, username: 'arthur.dent', email: 'arthur.dent@example.com' };

  const topics: Topic[] = [
    { id: 1, title: 'Babel fish', description: 'Translates any language.', subscribed: false },
    { id: 2, title: 'Towels', description: 'The most useful thing to carry.', subscribed: true },
  ];

  // Opens the profile and answers the request for the topics that its Subscriptions section sends
  async function openProfile(answer: Topic[] = topics): Promise<OpenedApp> {
    const opened = await openApp('/profile', user);
    opened.http.expectOne('/api/topics').flush(answer);
    await opened.fixture.whenStable();
    return opened;
  }

  function valueOf(page: HTMLElement, id: string): string {
    return (page.querySelector(`#${id}`) as HTMLInputElement).value;
  }

  it('opens from the header, whose link then marks the current page', async () => {
    // Arrange
    const { fixture, http, page } = await openApp('/feed', user);
    const link = page.querySelector('header a[href="/profile"]') as HTMLAnchorElement;
    expect(link.getAttribute('aria-current')).toBeNull();

    // Act: once open, the page asks for the topics
    link.click();
    const request = await vi.waitFor(() => http.expectOne('/api/topics'));
    request.flush(topics);
    await fixture.whenStable();

    // Assert
    expect(TestBed.inject(Router).url).toBe('/profile');
    expect(link.getAttribute('aria-current')).toBe('page');
  });

  it('shows the username and the e-mail address', async () => {
    // Act
    const { page } = await openProfile();

    // Assert
    expect(page.querySelector('h1')?.textContent).toContain('Profil utilisateur');
    expect(valueOf(page, 'username')).toBe(user.username);
    expect(valueOf(page, 'email')).toBe(user.email);
  });

  it('saves the profile and says so', async () => {
    // Arrange
    const { fixture, http, page } = await openProfile();
    const saved = { ...user, username: 'ford.prefect' };
    type(page, 'username', saved.username);

    // Act
    submit(page);
    const request = http.expectOne('/api/users/me');
    request.flush(saved);
    await fixture.whenStable();

    // Assert
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ username: 'ford.prefect', email: user.email });
    expect(TestBed.inject(Session).user()).toEqual(saved);
    expect(document.querySelector('mat-snack-bar-container')?.textContent).toContain(
      'Profil mis à jour.',
    );
  });

  it('sends nothing while a field is invalid', async () => {
    // Arrange: one case, since the rules have their own unit test
    const { fixture, http, page } = await openProfile();
    type(page, 'email', 'arthur.dent');

    // Act
    submit(page);
    await fixture.whenStable();

    // Assert
    http.expectNone('/api/users/me');
    expect(fieldError(page, 'email')?.textContent?.trim()).toBeTruthy();
  });

  it('shows under the field a value that another user has taken', async () => {
    // Arrange
    const { fixture, http, page } = await openProfile();
    const message = 'Cette adresse e-mail est déjà utilisée.';
    type(page, 'email', 'ford.prefect@example.com');

    // Act
    submit(page);
    http
      .expectOne('/api/users/me')
      .flush(
        { detail: message, errors: { email: message } },
        { status: 409, statusText: 'Conflict' },
      );
    await fixture.whenStable();

    // Assert
    expect(fieldError(page, 'email')?.textContent).toContain(message);
    expect(TestBed.inject(Session).user()).toEqual(user);
  });

  it('lets the user try again after a failure of the API', async () => {
    // Arrange
    const { fixture, http, page } = await openProfile();

    // Act: a first try
    submit(page);
    http
      .expectOne('/api/users/me')
      .flush(null, { status: 500, statusText: 'Internal Server Error' });
    await fixture.whenStable();

    // Assert
    expect(page.querySelector('[role="alert"]')?.textContent?.trim()).toBeTruthy();

    // Act: a second try
    submit(page);
    await fixture.whenStable();

    // Assert
    http.expectOne('/api/users/me');
  });

  describe('account deletion', () => {
    function button(root: ParentNode, text: string): HTMLButtonElement {
      return [...root.querySelectorAll('button')].find((candidate) =>
        candidate.textContent?.includes(text),
      ) as HTMLButtonElement;
    }

    async function askForDeletion(): Promise<OpenedApp> {
      const opened = await openProfile();
      button(opened.page, 'Supprimer mon compte').click();
      await opened.fixture.whenStable();
      return opened;
    }

    it('deletes the account once confirmed, then goes back home', async () => {
      // Arrange
      const { fixture, http } = await askForDeletion();
      const dialog = document.querySelector('mat-dialog-container') as HTMLElement;

      // Act
      button(dialog, 'Supprimer').click();
      await fixture.whenStable();
      const request = http.expectOne('/api/users/me');
      request.flush(null, { status: 204, statusText: 'No Content' });
      // The first wait lets the deletion resume after the response, the second one waits for
      // the navigation it then starts
      await fixture.whenStable();
      await fixture.whenStable();

      // Assert
      expect(request.request.method).toBe('DELETE');
      expect(TestBed.inject(Session).user()).toBeNull();
      expect(TestBed.inject(Router).url).toBe('/');
    });

    it('keeps the user logged in and says why when the deletion fails', async () => {
      // Arrange
      const { fixture, http } = await askForDeletion();
      const dialog = document.querySelector('mat-dialog-container') as HTMLElement;

      // Act
      button(dialog, 'Supprimer').click();
      await fixture.whenStable();
      http
        .expectOne('/api/users/me')
        .flush(null, { status: 500, statusText: 'Internal Server Error' });
      await fixture.whenStable();
      await fixture.whenStable();

      // Assert
      expect(TestBed.inject(Session).user()).toEqual(user);
      expect(TestBed.inject(Router).url).toBe('/profile');
      expect(document.querySelector('mat-snack-bar-container')?.textContent?.trim()).toBeTruthy();
    });

    it('keeps the account when the user cancels', async () => {
      // Arrange
      const { fixture, http } = await askForDeletion();
      const dialog = document.querySelector('mat-dialog-container') as HTMLElement;

      // Act
      button(dialog, 'Annuler').click();
      await fixture.whenStable();

      // Assert
      http.expectNone('/api/users/me');
      expect(TestBed.inject(Router).url).toBe('/profile');
    });
  });

  describe('subscriptions', () => {
    function cards(page: HTMLElement): HTMLElement[] {
      return [...page.querySelectorAll('app-subscriptions app-topic-card')] as HTMLElement[];
    }

    it('lists the topics the user follows, each with a button to unsubscribe', async () => {
      // Act
      const { page } = await openProfile();

      // Assert
      expect(page.querySelector('app-subscriptions h2')?.textContent).toContain('Abonnements');
      const [towels, ...others] = cards(page);
      expect(others).toHaveLength(0);
      expect(towels.querySelector('h3')?.textContent).toContain('Towels');
      expect(towels.textContent).toContain('The most useful thing to carry.');
      expect(towels.querySelector('button')?.textContent).toContain('Se désabonner');
    });

    it('says so when the user follows no topic', async () => {
      // Act
      const { page } = await openProfile(topics.map((topic) => ({ ...topic, subscribed: false })));

      // Assert
      expect(cards(page)).toHaveLength(0);
      expect(page.querySelector('app-subscriptions p')?.textContent?.trim()).toBeTruthy();
    });

    it('says so when the subscriptions cannot be loaded', async () => {
      // Arrange
      const { fixture, http, page } = await openApp('/profile', user);

      // Act
      http
        .expectOne('/api/topics')
        .flush(null, { status: 500, statusText: 'Internal Server Error' });
      await fixture.whenStable();

      // Assert
      expect(
        page.querySelector('app-subscriptions [role="alert"]')?.textContent?.trim(),
      ).toBeTruthy();
    });

    it('unsubscribes the user, then takes the topic out of the list', async () => {
      // Arrange
      const { fixture, http, page } = await openProfile();

      // Act
      cards(page)[0].querySelector('button')?.click();
      const request = http.expectOne('/api/topics/2/subscription');
      request.flush(null, { status: 204, statusText: 'No Content' });
      await fixture.whenStable();

      // Assert
      expect(request.request.method).toBe('DELETE');
      expect(cards(page)).toHaveLength(0);
    });

    it('keeps the topic and says why when the unsubscription fails', async () => {
      // Arrange
      const { fixture, http, page } = await openProfile();

      // Act
      cards(page)[0].querySelector('button')?.click();
      http
        .expectOne('/api/topics/2/subscription')
        .flush(null, { status: 500, statusText: 'Internal Server Error' });
      await fixture.whenStable();

      // Assert
      expect(cards(page)).toHaveLength(1);
      expect(document.querySelector('mat-snack-bar-container')?.textContent?.trim()).toBeTruthy();
    });
  });

  describe('password form', () => {
    function passwordForm(page: HTMLElement): HTMLElement {
      return page.querySelector('app-password-form') as HTMLElement;
    }

    it('changes the password, empties the fields and says so', async () => {
      // Arrange
      const { fixture, http, page } = await openProfile();
      type(page, 'currentPassword', 'Dont-Panic-42');
      type(page, 'newPassword', 'So-Long-42');

      // Act
      submit(passwordForm(page));
      const request = http.expectOne('/api/users/me/password');
      request.flush(null, { status: 204, statusText: 'No Content' });
      await fixture.whenStable();

      // Assert
      expect(request.request.method).toBe('PUT');
      expect(request.request.body).toEqual({
        currentPassword: 'Dont-Panic-42',
        newPassword: 'So-Long-42',
      });
      expect(valueOf(page, 'currentPassword')).toBe('');
      expect(valueOf(page, 'newPassword')).toBe('');
      expect(fieldError(page, 'currentPassword')).toBeNull();
      expect(document.querySelector('mat-snack-bar-container')?.textContent).toContain(
        'Mot de passe modifié.',
      );
    });

    it('sends nothing while the new password is invalid', async () => {
      // Arrange
      const { fixture, http, page } = await openProfile();
      type(page, 'currentPassword', 'Dont-Panic-42');
      type(page, 'newPassword', 'Deep-42');

      // Act
      submit(passwordForm(page));
      await fixture.whenStable();

      // Assert
      http.expectNone('/api/users/me/password');
      expect(fieldError(page, 'newPassword')?.textContent?.trim()).toBeTruthy();
    });

    it('shows under the field a wrong current password', async () => {
      // Arrange
      const { fixture, http, page } = await openProfile();
      const message = 'Le mot de passe actuel est incorrect.';
      type(page, 'currentPassword', 'Wrong-Password-42');
      type(page, 'newPassword', 'So-Long-42');

      // Act
      submit(passwordForm(page));
      http
        .expectOne('/api/users/me/password')
        .flush(
          { detail: message, errors: { currentPassword: message } },
          { status: 400, statusText: 'Bad Request' },
        );
      await fixture.whenStable();

      // Assert
      expect(fieldError(page, 'currentPassword')?.textContent).toContain(message);
    });

    it('lets the user try again after a failure of the API', async () => {
      // Arrange
      const { fixture, http, page } = await openProfile();
      type(page, 'currentPassword', 'Dont-Panic-42');
      type(page, 'newPassword', 'So-Long-42');

      // Act: a first try
      submit(passwordForm(page));
      http
        .expectOne('/api/users/me/password')
        .flush(null, { status: 500, statusText: 'Internal Server Error' });
      await fixture.whenStable();

      // Assert
      expect(passwordForm(page).querySelector('[role="alert"]')?.textContent?.trim()).toBeTruthy();

      // Act: a second try
      submit(passwordForm(page));
      await fixture.whenStable();

      // Assert
      http.expectOne('/api/users/me/password');
    });
  });
});
