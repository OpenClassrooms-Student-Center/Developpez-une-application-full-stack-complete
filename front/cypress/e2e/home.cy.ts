describe('Home page', () => {
  it('offers a visitor to log in or register', () => {
    cy.visit('/');

    cy.get('img[alt="MDD"]').should('be.visible').its('0.naturalWidth').should('be.greaterThan', 0);
    cy.contains('a', 'Se connecter').should('be.visible').and('have.attr', 'href', '/login');
    cy.contains('a', 'S’inscrire').should('be.visible').and('have.attr', 'href', '/register');
  });

  it('leads to the registration page', () => {
    cy.visit('/');

    cy.contains('a', 'S’inscrire').click();

    cy.location('pathname').should('eq', '/register');
    cy.contains('h1', 'Inscription').should('be.visible');
  });
});
