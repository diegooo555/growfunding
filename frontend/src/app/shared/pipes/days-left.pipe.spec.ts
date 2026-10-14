import { describeTimeLeft } from './days-left.pipe';

describe('describeTimeLeft', () => {
  const now = new Date('2026-01-01T00:00:00Z');

  it('says finished when the deadline passed', () => {
    expect(describeTimeLeft('2025-12-31T23:00:00Z', now)).toBe('Finalizada');
  });

  it('shows hours when less than a day is left', () => {
    expect(describeTimeLeft('2026-01-01T05:30:00Z', now)).toBe('Quedan 5 h');
  });

  it('shows days otherwise', () => {
    expect(describeTimeLeft('2026-01-04T00:00:00Z', now)).toBe('Quedan 3 días');
    expect(describeTimeLeft('2026-01-02T01:00:00Z', now)).toBe('Queda 1 día');
  });
});
