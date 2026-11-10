import { HttpErrorResponse } from '@angular/common/http';
import { WritableSignal } from '@angular/core';
import { FieldTree, ValidationError } from '@angular/forms/signals';

interface Problem {
  detail?: string;
  errors?: Record<string, string>;
}

function problemOf(error: unknown): Problem | null {
  return error instanceof HttpErrorResponse ? error.error : null;
}

/**
 * Attaches to each field the message the API gives for it, in the `errors` property of the
 * Problem Details. Signal Forms clears such an error when the user changes the field.
 */
export function fieldErrors<T>(
  error: unknown,
  form: FieldTree<T>,
): ValidationError.WithOptionalFieldTree[] {
  return Object.entries(problemOf(error)?.errors ?? {}).map(([field, message]) => ({
    kind: 'server',
    fieldTree: form[field as keyof FieldTree<T>] as FieldTree<unknown>,
    message,
  }));
}

/**
 * Gives a message for an error that concerns no field. It must not become a form error: nothing
 * would clear it, and Signal Forms refuses to submit a form that still has errors.
 */
export function failureMessage(error: unknown): string {
  return problemOf(error)?.detail ?? 'Une erreur est survenue. Veuillez réessayer.';
}

/**
 * Turns a failed submission into what Signal Forms expects: the API's messages under their fields, or,
 * when the API names no field, a general message in `failure`.
 */
export function submissionErrors<T>(
  error: unknown,
  form: FieldTree<T>,
  failure: WritableSignal<string>,
): ValidationError.WithOptionalFieldTree[] {
  const errors = fieldErrors(error, form);
  failure.set(errors.length > 0 ? '' : failureMessage(error));
  return errors;
}
