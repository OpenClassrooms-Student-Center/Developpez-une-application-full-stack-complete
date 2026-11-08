import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Session } from './session.service';

describe('Session', () => {
  function setUp(): { session: Session; http: HttpTestingController } {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    return { session: TestBed.inject(Session), http: TestBed.inject(HttpTestingController) };
  }

  it('remembers the user the API recognises', () => {
    // Arrange
    const { session, http } = setUp();
    const user = { id: 42, username: 'arthur.dent', email: 'arthur.dent@example.com' };

    // Act
    session.restore().subscribe();
    http.expectOne('/api/users/me').flush(user);

    // Assert
    expect(session.user()).toEqual(user);
  });

  it('treats a visitor as logged out', () => {
    // Arrange
    const { session, http } = setUp();

    // Act
    session.restore().subscribe();
    http.expectOne('/api/users/me').flush(null, { status: 401, statusText: 'Unauthorized' });

    // Assert
    expect(session.user()).toBeNull();
  });
});
