export type AspectKey =
  'fisico' | 'emocional' | 'intelectual' | 'espiritual' | 'conciencia' | 'intuicion' | 'estetica';

export type AspectGroup = 'basico' | 'complementario';

export interface Aspect {
  key: AspectKey;
  label: string;
  period: number;
  color: string;
  dash: string;
  group: AspectGroup;
}

export interface BiorhythmLine {
  aspect: Aspect;
  path: string;
  currentValue: number;
  status: string;
  markerX: number;
  markerY: number;
}

export interface DateLabel {
  offset: number;
  index: number;
  date: Date;
}

export interface BiorhythmSeries {
  lines: BiorhythmLine[];
  dateLabels: DateLabel[];
  centerX: number;
}

export const ASPECTS: Aspect[] = [
  { key: 'fisico', label: 'Físico', period: 23, color: '#1656c9', dash: '0', group: 'basico' },
  {
    key: 'emocional',
    label: 'Emocional',
    period: 28,
    color: '#d32f2f',
    dash: '0',
    group: 'basico',
  },
  {
    key: 'intelectual',
    label: 'Intelectual',
    period: 33,
    color: '#1f9254',
    dash: '0',
    group: 'basico',
  },
  {
    key: 'espiritual',
    label: 'Espiritual',
    period: 53,
    color: '#7c3aed',
    dash: '7 4',
    group: 'complementario',
  },
  {
    key: 'conciencia',
    label: 'Conciencia',
    period: 48,
    color: '#0891b2',
    dash: '1 4',
    group: 'complementario',
  },
  {
    key: 'intuicion',
    label: 'Intuición',
    period: 38,
    color: '#d97706',
    dash: '9 3 2 3',
    group: 'complementario',
  },
  {
    key: 'estetica',
    label: 'Estética',
    period: 43,
    color: '#a3195b',
    dash: '3 3',
    group: 'complementario',
  },
];

export const RANGE_DAYS = 15;
export const CHART_WIDTH = 760;
export const CHART_HEIGHT = 340;
export const MARGIN = { top: 16, right: 16, bottom: 34, left: 44 };
export const PLOT_WIDTH = CHART_WIDTH - MARGIN.left - MARGIN.right;
export const PLOT_HEIGHT = CHART_HEIGHT - MARGIN.top - MARGIN.bottom;

const pad2 = (value: number): string => (value < 10 ? `0${value}` : `${value}`);

export const toInputValue = (date: Date): string =>
  `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;

export const fromInputValue = (value: string): Date | null => {
  if (!value) return null;
  const [year, month, day] = value.split('-').map(Number);
  if (!year || !month || !day) return null;
  return new Date(year, month - 1, day);
};

export const addDays = (date: Date, amount: number): Date => {
  const result = new Date(date);
  result.setDate(result.getDate() + amount);
  return result;
};

export const daysBetween = (from: Date, to: Date): number => {
  const toMidnight = new Date(to).setHours(0, 0, 0, 0);
  const fromMidnight = new Date(from).setHours(0, 0, 0, 0);
  return Math.round((toMidnight - fromMidnight) / 86_400_000);
};

export const biorhythmValue = (daysSinceBirth: number, period: number): number =>
  Math.sin((2 * Math.PI * daysSinceBirth) / period) * 100;

export const phaseLabel = (value: number, nextValue: number): string => {
  if (Math.abs(value) < 3) return 'Crítico';
  return nextValue > value ? 'Ascendente' : 'Descendente';
};

export const xScale = (index: number): number =>
  MARGIN.left + (index / (RANGE_DAYS * 2)) * PLOT_WIDTH;

export const yScale = (value: number): number =>
  MARGIN.top + (1 - (value + 100) / 200) * PLOT_HEIGHT;
