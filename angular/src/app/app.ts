import {
  ChangeDetectionStrategy,
  Component,
  computed,
  signal,
  ViewEncapsulation,
} from '@angular/core';
import {
  addDays,
  ASPECTS,
  AspectKey,
  biorhythmValue,
  BiorhythmSeries,
  daysBetween,
  fromInputValue,
  phaseLabel,
  RANGE_DAYS,
  toInputValue,
  xScale,
  yScale,
} from './biorhythm';
import { BiorhythmHeader } from './components/biorhythm-header';
import { BiorhythmHistogram } from './components/biorhythm-histogram';
import { BiorhythmInputForm } from './components/biorhythm-input-form';

type Theme = 'light' | 'night' | 'ocean' | 'aurora';

@Component({
  selector: 'app-root',
  imports: [BiorhythmHeader, BiorhythmInputForm, BiorhythmHistogram],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class App {
  readonly themes: { value: Theme; label: string }[] = [
    { value: 'light', label: 'Claro' },
    { value: 'night', label: 'Nocturno' },
    { value: 'ocean', label: 'Océano' },
    { value: 'aurora', label: 'Aurora' },
  ];
  readonly theme = signal<Theme>('light');
  readonly today = toInputValue(new Date());
  readonly birthDate = signal('');
  readonly selectedDate = signal(this.today);
  readonly visible = signal<Record<AspectKey, boolean>>(
    ASPECTS.reduce(
      (result, aspect) => ({ ...result, [aspect.key]: true }),
      {} as Record<AspectKey, boolean>,
    ),
  );

  readonly selectedDateValue = computed(() => fromInputValue(this.selectedDate()) ?? new Date());
  readonly isToday = computed(() => this.selectedDate() === this.today);
  readonly series = computed<BiorhythmSeries | null>(() => {
    const birthDate = fromInputValue(this.birthDate());
    if (!birthDate) return null;

    const selectedDate = this.selectedDateValue();
    const offsets = Array.from({ length: RANGE_DAYS * 2 + 1 }, (_, index) => index - RANGE_DAYS);
    const dates = offsets.map((offset) => addDays(selectedDate, offset));
    const daysSinceBirth = dates.map((date) => daysBetween(birthDate, date));

    const lines = ASPECTS.map((aspect) => {
      const values = daysSinceBirth.map((days) => biorhythmValue(days, aspect.period));
      const path = values
        .map(
          (value, index) =>
            `${index === 0 ? 'M' : 'L'} ${xScale(index).toFixed(2)} ${yScale(value).toFixed(2)}`,
        )
        .join(' ');
      const currentValue = values[RANGE_DAYS];
      const nextValue = biorhythmValue(daysSinceBirth[RANGE_DAYS] + 1, aspect.period);

      return {
        aspect,
        path,
        currentValue,
        status: phaseLabel(currentValue, nextValue),
        markerX: xScale(RANGE_DAYS),
        markerY: yScale(currentValue),
      };
    });

    const dateLabels = offsets
      .map((offset, index) => ({ offset, index, date: dates[index] }))
      .filter(({ offset }) => offset % 5 === 0);

    return { lines, dateLabels, centerX: xScale(RANGE_DAYS) };
  });

  shiftSelectedDate(amount: number): void {
    this.selectedDate.set(toInputValue(addDays(this.selectedDateValue(), amount)));
  }

  toggleAspect(key: AspectKey): void {
    this.visible.update((current) => ({ ...current, [key]: !current[key] }));
  }

  setTheme(event: Event): void {
    this.theme.set((event.target as HTMLSelectElement).value as Theme);
  }
}
