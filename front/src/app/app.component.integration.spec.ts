import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { App } from './app.component';
import { routes } from './app.routes';

describe('App', () => {
  it('shows the home page to a visitor on the root URL', async () => {
    // Arrange
    TestBed.configureTestingModule({ providers: [provideRouter(routes)] });
    const fixture = TestBed.createComponent(App);

    // Act
    await TestBed.inject(Router).navigateByUrl('/');
    await fixture.whenStable();

    // Assert
    const page = fixture.nativeElement as HTMLElement;
    expect(page.querySelector('img')?.alt).toBe('MDD');
    expect(page.querySelector('a[href="/login"]')?.textContent).toContain('Se connecter');
    expect(page.querySelector('a[href="/register"]')?.textContent).toContain('S’inscrire');
  });
});
