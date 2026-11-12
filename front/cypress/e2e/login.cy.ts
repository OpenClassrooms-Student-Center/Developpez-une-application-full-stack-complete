import { createAccount, password } from '../support/accounts';

describe('Login and logout', () => {
  // Creates the account, then logs the browser out, so that the test logs in by itself
  function createLoggedOutAccount(username: string): string {
    const email = createAccount(username);
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
    const email = createLoggedOutAccount(`trillian_${Date.now()}`);

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
    const email = createLoggedOutAccount(`trillian_${Date.now()}`);

    logIn(email, 'Wrong-Password-42');

    cy.get('[role="alert"]').should('be.visible');
    cy.location('pathname').should('eq', '/login');
    cy.getCookie('mdd_token').should('be.null');
  });
});
