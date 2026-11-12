export const password = 'Dont-Panic-42';

/**
 * Creates an account through the API and gives its e-mail address. The registration also logs the
 * browser in, with the token cookie.
 */
export function createAccount(username: string): string {
  const email = `${username}@example.com`;
  // A first request gets the CSRF cookie, whose token the registration sends back
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
