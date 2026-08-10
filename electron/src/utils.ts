export const pad2 = (n: number) => n < 10 ? `0${n}` : `${n}`;

export const toInputValue = (date: Date): string =>
    `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;


export const fromInputValue = (value: string): Date | null => {
    if (!value) return null
    const [y, m, d] = value.split('-').map(Number)
    if (!y || !m || !d) return null
    return new Date(y, m - 1, d)
};

export const addDays = (date: Date, amount: number): Date => {
    const result = new Date(date)
    result.setDate(result.getDate() + amount)
    return result
};

export const daysBetween = (from: Date, to: Date): number => {
    const ms = to.setHours(0, 0, 0, 0) - new Date(from).setHours(0, 0, 0, 0)
    return Math.round(ms / 86400000)
};

export const biorhythmValue = (daysSinceBirth: number, period: number): number => {
    return Math.sin((2 * Math.PI * daysSinceBirth) / period) * 100
};

export const phaseLabel = (value: number, nextValue: number): string => {
    if (Math.abs(value) < 3) return 'Crítico'
    return nextValue > value ? 'Ascendente' : 'Descendente'
};
