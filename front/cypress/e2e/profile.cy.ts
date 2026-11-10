describe('Profile', () => {
  const password = 'Dont-Panic-42';

  function createAccountAndLogIn(username: string): string {
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
    return email;
  }

  it('changes the username and keeps it after a reload', () => {
    const username = `marvin_${Date.now()}`;
    createAccountAndLogIn(username);
    cy.visit('/feed');

    cy.get('header a[aria-label="Profil"]').click();
    cy.location('pathname').should('eq', '/profile');
    cy.get('#username').should('have.value', username).clear().type(`${username}_2`);
    cy.contains('button', 'Sauvegarder').click();
    cy.contains('Profil mis à jour.').should('be.visible');

    cy.reload();
    cy.get('#username').should('have.value', `${username}_2`);
  });

  it('changes the password, which then opens the account', () => {
    const email = createAccountAndLogIn(`marvin_${Date.now()}`);
    cy.visit('/profile');

    cy.get('#currentPassword').type(password);
    cy.get('#newPassword').type('So-Long-42');
    cy.contains('button', 'Modifier').click();
    cy.contains('Mot de passe modifié.').should('be.visible');

    cy.contains('Se déconnecter').click();
    logIn(email, 'So-Long-42');
    cy.location('pathname').should('eq', '/feed');
  });

  it('deletes the account once confirmed, so that it can no longer log in', () => {
    const email = createAccountAndLogIn(`marvin_${Date.now()}`);
    cy.visit('/profile');

    cy.contains('button', 'Supprimer mon compte').click();
    cy.get('mat-dialog-container').contains('button', 'Supprimer').click();
    cy.location('pathname').should('eq', '/');
    cy.getCookie('mdd_token').should('be.null');

    logIn(email, password);
    cy.get('[role="alert"]').should('be.visible');
  });

  function logIn(email: string, typedPassword: string): void {
    cy.visit('/login');
    cy.get('#email').type(email);
    cy.get('#password').type(typedPassword);
    cy.contains('button', 'Se connecter').click();
  }
});
