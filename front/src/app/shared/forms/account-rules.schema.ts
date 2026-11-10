import { apply, email, maxLength, pattern, required, schema } from '@angular/forms/signals';

const USERNAME_PATTERN = /^[A-Za-z0-9._-]{3,30}$/;
const PASSWORD_PATTERN = /^(?=.*\d)(?=.*\p{Ll})(?=.*\p{Lu})(?=.*[^\p{L}\p{N}]).{8,64}$/u;

/**
 * From 3 to 30 unaccented letters, digits, dots, underscores or hyphens, as the API requires.
 */
export const usernameRules = schema<string>((path) => {
  required(path, { message: 'Le nom d’utilisateur est obligatoire.' });
  pattern(path, USERNAME_PATTERN, {
    message: 'De 3 à 30 caractères : lettres sans accent, chiffres, « . », « _ » ou « - ».',
  });
});

/**
 * A valid e-mail address of 254 characters at most, the limit of RFC 5321, as the API requires.
 */
export const emailRules = schema<string>((path) => {
  required(path, { message: 'L’adresse e-mail est obligatoire.' });
  email(path, { message: 'Cette adresse e-mail n’est pas valide.' });
  maxLength(path, 254, { message: 'L’adresse e-mail compte 254 caractères au plus.' });
});

/**
 * A password that must be given. The login checks only this: the other rules apply when a password is chosen.
 */
export const requiredPasswordRules = schema<string>((path) => {
  required(path, { message: 'Le mot de passe est obligatoire.' });
});

/**
 * From 8 to 64 characters, with a digit, a lowercase letter, an uppercase letter and a character
 * that is neither a letter nor a digit, as the API requires.
 */
export const passwordRules = schema<string>((path) => {
  apply(path, requiredPasswordRules);
  pattern(path, PASSWORD_PATTERN, {
    message:
      'De 8 à 64 caractères, avec un chiffre, une minuscule, une majuscule et un caractère spécial.',
  });
});
