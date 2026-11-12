import { createAccount } from '../support/accounts';

describe('Topics', () => {
  it('subscribes to a topic, which the profile then lists until the user unsubscribes', () => {
    createAccount(`marvin_${Date.now()}`);
    cy.visit('/feed');

    cy.get('header').contains('a', 'Thèmes').click();
    cy.location('pathname').should('eq', '/topics');
    cy.get('app-topic-card').first().as('topic').find('h2').invoke('text').as('title');
    cy.get('@topic').contains('button', 'S’abonner').click();
    cy.get('@topic').contains('button', 'Déjà abonné').should('be.disabled');

    cy.get('header a[aria-label="Profil"]').click();
    cy.get('@title').then((title) =>
      cy.get('app-subscriptions app-topic-card').should('have.length', 1).and('contain', title),
    );
    cy.get('app-subscriptions').contains('button', 'Se désabonner').click();
    cy.get('app-subscriptions app-topic-card').should('not.exist');

    cy.get('header').contains('a', 'Thèmes').click();
    cy.location('pathname').should('eq', '/topics');
    cy.get('@title').then((title) =>
      cy.contains('app-topic-card', title).contains('button', 'S’abonner').should('be.enabled'),
    );
  });
});
