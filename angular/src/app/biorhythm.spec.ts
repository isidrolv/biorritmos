import {
  addDays,
  biorhythmValue,
  daysBetween,
  fromInputValue,
  phaseLabel,
  toInputValue,
} from './biorhythm';

describe('biorhythm utilities', () => {
  it('converts dates to and from input values', () => {
    const date = fromInputValue('1990-05-17');

    expect(date).not.toBeNull();
    expect(toInputValue(date!)).toBe('1990-05-17');
  });

  it('calculates whole calendar days without changing the source date', () => {
    const birthDate = new Date(2000, 0, 1, 18);
    const selectedDate = new Date(2000, 0, 24, 7);

    expect(daysBetween(birthDate, selectedDate)).toBe(23);
    expect(birthDate.getHours()).toBe(18);
  });

  it('completes a physical cycle every 23 days', () => {
    expect(biorhythmValue(0, 23)).toBeCloseTo(0);
    expect(biorhythmValue(23, 23)).toBeCloseTo(0);
  });

  it('adds days and labels cycle phases', () => {
    expect(toInputValue(addDays(new Date(2026, 7, 8), 1))).toBe('2026-08-09');
    expect(phaseLabel(0, 10)).toBe('Crítico');
    expect(phaseLabel(50, 60)).toBe('Ascendente');
    expect(phaseLabel(50, 40)).toBe('Descendente');
  });
});
