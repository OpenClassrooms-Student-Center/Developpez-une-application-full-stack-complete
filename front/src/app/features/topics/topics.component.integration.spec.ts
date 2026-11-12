import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { openApp } from '../../../testing/app-testing';
import { User } from '../../core/session/user.model';
import { Topic } from './topic.model';

describe('Topics page', () => {
  const user: User = { id: 42, username: 'arthur.dent', email: 'arthur.dent@example.com' };
  const topics: Topic[] = [
    { id: 1, title: 'Babel fish', description: 'Translates any language.', subscribed: false },
    { id: 2, title: 'Towels', description: 'The most useful thing to carry.', subscribed: true },
  ];

  function cards(page: HTMLElement): HTMLElement[] {
    return [...page.querySelectorAll('app-topic-card')] as HTMLElement[];
  }

  function buttonOf(card: HTMLElement): HTMLButtonElement {
    return card.querySelector('button') as HTMLButtonElement;
  }

  it('opens from the header, whose link then marks the current page', async () => {
    // Arrange
    const { fixture, http, page } = await openApp('/feed', user);
    const link = page.querySelector('header a[href="/topics"]') as HTMLAnchorElement;
    expect(link.getAttribute('aria-current')).toBeNull();

    // Act: once open, the page asks for the topics
    link.click();
    const request = await vi.waitFor(() => http.expectOne('/api/topics'));
    request.flush(topics);
    await fixture.whenStable();

    // Assert
    expect(TestBed.inject(Router).url).toBe('/topics');
    expect(link.getAttribute('aria-current')).toBe('page');
  });

  it('lists every topic and says which ones the user already follows', async () => {
    // Arrange
    const { fixture, http, page } = await openApp('/topics', user);

    // Act
    http.expectOne('/api/topics').flush(topics);
    await fixture.whenStable();

    // Assert
    const [babelFish, towels] = cards(page);
    expect(babelFish.querySelector('h2')?.textContent).toContain('Babel fish');
    expect(babelFish.textContent).toContain('Translates any language.');
    expect(buttonOf(babelFish).textContent).toContain('S’abonner');
    expect(buttonOf(babelFish).disabled).toBe(false);
    expect(buttonOf(towels).textContent).toContain('Déjà abonné');
    expect(buttonOf(towels).disabled).toBe(true);
  });

  it('says so when the topics cannot be loaded', async () => {
    // Arrange
    const { fixture, http, page } = await openApp('/topics', user);

    // Act
    http.expectOne('/api/topics').flush(null, { status: 500, statusText: 'Internal Server Error' });
    await fixture.whenStable();

    // Assert
    expect(page.querySelector('[role="alert"]')?.textContent?.trim()).toBeTruthy();
    expect(cards(page)).toHaveLength(0);
  });
});
