import { MoneyPipe, formatMoney } from './money.pipe';

describe('MoneyPipe', () => {
  const pipe = new MoneyPipe();

  it('formats amounts with the currency', () => {
    const text = pipe.transform(1500, 'USD');
    expect(text.replace(/\D/g, '')).toBe('1500');
    expect(text).toMatch(/US\$|USD|\$/);
  });

  it('returns a dash for missing values', () => {
    expect(formatMoney(null)).toBe('—');
    expect(formatMoney(undefined)).toBe('—');
  });
});
