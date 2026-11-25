import { describe, expect, it } from 'vitest';
import { HOME, loginPath, safeRedirect } from './safeRedirect';

// D-034: după login, doar o cale internă a aplicației; altfel lista de facturi.
describe('safeRedirect', () => {
  it('acceptă o cale internă, cu query și fragment', () => {
    expect(safeRedirect('/facturi')).toBe('/facturi');
    expect(safeRedirect('/facturi/5?tab=linii#total')).toBe('/facturi/5?tab=linii#total');
  });

  it.each([
    ['lipsă', null],
    ['gol', ''],
    ['protocol-relativ', '//evil.com'],
    ['protocol-relativ cu cale', '//evil.com/facturi'],
    ['backslash', '/\\evil.com'],
    ['backslash dublu', '\\\\evil.com'],
    ['URL absolut', 'https://evil.com/facturi'],
    ['javascript:', 'javascript:alert(1)'],
    ['fără slash la început', 'facturi'],
    ['tab în protocol-relativ', '/\t/evil.com'],
    ['newline în protocol-relativ', '/\n/evil.com'],
    ['pagina de login (buclă)', '/login'],
    ['login cu query', '/login?next=/facturi'],
  ])('respinge %s', (_name, next) => {
    expect(safeRedirect(next)).toBe(HOME);
  });

  it('lista de facturi e destinația implicită', () => {
    expect(HOME).toBe('/facturi');
  });
});

describe('loginPath', () => {
  it('păstrează pagina de unde a plecat utilizatorul', () => {
    expect(loginPath('/facturi/5?tab=linii', false)).toBe('/login?next=%2Ffacturi%2F5%3Ftab%3Dlinii');
  });

  it('marchează sesiunea expirată', () => {
    expect(loginPath('/facturi', true)).toBe('/login?next=%2Ffacturi&expired=1');
  });

  it('fără next pentru pagina implicită', () => {
    expect(loginPath(HOME, false)).toBe('/login');
  });
});
