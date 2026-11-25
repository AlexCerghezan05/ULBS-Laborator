export const HOME = '';

export function safeRedirect(next: string | null): string {
  return next ?? '';
}

export function loginPath(_from: string, _expired: boolean): string {
  return '';
}
