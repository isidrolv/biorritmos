import { describe, expect, it } from 'vitest'
import { addDays, biorhythmValue, fromInputValue, phaseLabel, toInputValue } from './utils'

describe('biorhythm utilities', () => {
  it('converts input dates without changing the calendar day', () => {
    const date = fromInputValue('1990-05-17')
    expect(date).not.toBeNull()
    expect(toInputValue(date!)).toBe('1990-05-17')
  })

  it('completes each cycle at its configured period', () => {
    expect(biorhythmValue(0, 23)).toBeCloseTo(0)
    expect(biorhythmValue(23, 23)).toBeCloseTo(0)
    expect(biorhythmValue(28, 28)).toBeCloseTo(0)
    expect(biorhythmValue(33, 33)).toBeCloseTo(0)
  })

  it('adds days and identifies cycle phases', () => {
    expect(toInputValue(addDays(new Date(2026, 7, 8), 1))).toBe('2026-08-09')
    expect(phaseLabel(0, 10)).toBe('Crítico')
    expect(phaseLabel(50, 60)).toBe('Ascendente')
    expect(phaseLabel(50, 40)).toBe('Descendente')
  })
})
