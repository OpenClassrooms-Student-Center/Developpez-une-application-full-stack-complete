describe('Login and logout', () => {
  const password = 'Dont-Panic-42';

  function createAccount(username: string): string {
    const email = `${username}@example.com`;
    cy.request({ url: '/api/users/me', failOnStatusCode: false });
    cy.getCookie('XSRF-TOKEN').then((csrf) =>
      cy.request({
        method: 'POST',
        url: '/api/auth/register',
        headers: { 'X-XSRF-TOKEN': csrf?.value ?? '' },
        body: { username, email, password },
      }),
    );
    cy.clearCookies();
    return email;
  }

  function logIn(email: string, typedPassword: string): void {
    cy.visit('/login');
    cy.get('#email').type(email);
    cy.get('#password').type(typedPassword);
    cy.contains('button', 'Se connecter').click();
  }

  it('logs the user in, keeps them logged in and logs them out', () => {
    const email = createAccount(`trillian_${Date.now()}`);

    logIn(email, password);
    cy.location('pathname').should('eq', '/feed');

    cy.reload();
    cy.location('pathname').should('eq', '/feed');
    cy.visit('/');
    cy.location('pathname').should('eq', '/feed');

    cy.contains('Se déconnecter').click();
    cy.location('pathname').should('eq', '/');
    cy.getCookie('mdd_token').should('be.null');
    cy.visit('/feed');
    cy.location('pathname').should('eq', '/login');
  });

  it('refuses a wrong password', () => {
    const email = createAccount(`trillian_${Date.now()}`);

    logIn(email, 'Wrong-Password-42');

    cy.get('[role="alert"]').should('be.visible');
    cy.location('pathname').should('eq', '/login');
    cy.getCookie('mdd_token').should('be.null');
  });
});
