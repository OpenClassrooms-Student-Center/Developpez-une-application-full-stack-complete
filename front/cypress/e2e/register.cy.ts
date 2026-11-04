describe('Registration', () => {
  const password = 'Dont-Panic-42';

  function register(username: string, email: string): void {
    cy.visit('/register');
    cy.get('#username').type(username);
    cy.get('#email').type(email);
    cy.get('#password').type(password);
    cy.contains('button', 'S’inscrire').click();
  }

  it('creates an account and logs the new user in', () => {
    const username = `zaphod_${Date.now()}`;

    register(username, `${username}@example.com`);

    cy.location('pathname').should('eq', '/feed');
    cy.getCookie('mdd_token').should('include', { httpOnly: true, secure: true });
    cy.request('/api/users/me').its('body.username').should('eq', username);
  });

  it('refuses a username that is already taken', () => {
    const username = `zaphod_${Date.now()}`;
    register(username, `${username}@example.com`);
    cy.location('pathname').should('eq', '/feed');
    cy.clearCookies();

    register(username, `ford_${username}@example.com`);

    cy.contains('mat-error', 'Ce nom d’utilisateur est déjà pris.').should('be.visible');
    cy.location('pathname').should('eq', '/register');
  });
});
