import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { apply, form, Schema } from '@angular/forms/signals';

import {
  emailRules,
  passwordRules,
  requiredPasswordRules,
  usernameRules,
} from './account-rules.schema';

/**
 * Applies the rules to a value, in a one-field form, and returns the kinds of the broken rules.
 */
function brokenRules(rules: Schema<string>, value: string): string[] {
  return TestBed.runInInjectionContext(() => {
    const field = form(signal(value), (path) => apply(path, rules));
    return field()
      .errors()
      .map((error) => error.kind);
  });
}

describe('Account rules', () => {
  it.each([
    ['', ['required']],
    ['42', ['pattern']],
    ['zaphod.beeblebrox.the.president', ['pattern']],
    ['arthur.dent@home', ['pattern']],
    ['marvin.déprimé', ['pattern']],
  ])('refuses the username "%s"', (value, rules) => {
    expect(brokenRules(usernameRules, value)).toEqual(rules);
  });

  it.each([
    ['', ['required']],
    ['arthur.dent', ['email']],
    // Angular's email rule already stops at 254 characters, the limit of RFC 5321
    [`dent@${'vogon.'.repeat(39)}example.co.uk.fr`, ['email', 'maxLength']],
  ])('refuses the e-mail address "%s"', (value, rules) => {
    expect(brokenRules(emailRules, value)).toEqual(rules);
  });

  it.each([
    ['', ['required']],
    ['Deep-42', ['pattern']],
    ['Dont-Panic-42'.repeat(5), ['pattern']],
    ['dont-panic-42', ['pattern']],
    ['DONT-PANIC-42', ['pattern']],
    ['Dont-Panic', ['pattern']],
    ['DontPanic42', ['pattern']],
  ])('refuses the new password "%s"', (value, rules) => {
    expect(brokenRules(passwordRules, value)).toEqual(rules);
  });

  it.each([
    [usernameRules, 'ade'],
    [usernameRules, 'zaphod.beeblebrox_president-42'],
    [emailRules, 'arthur.dent@example.com'],
    [emailRules, `dent@${'vogon.'.repeat(39)}examples.com.fr`],
    [passwordRules, 'Deep-42!'],
    [passwordRules, `Aa1-${'x'.repeat(60)}`],
  ])('accepts a valid value even at the limits (%#)', (rules, value) => {
    expect(brokenRules(rules, value)).toEqual([]);
  });

  it('only asks for a password at login, whatever its format', () => {
    expect(brokenRules(requiredPasswordRules, '')).toEqual(['required']);
    expect(brokenRules(requiredPasswordRules, 'short')).toEqual([]);
  });
});
