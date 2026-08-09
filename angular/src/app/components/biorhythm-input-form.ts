import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-biorhythm-input-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './biorhythm-input-form.html',
})
export class BiorhythmInputForm {
  readonly today = input.required<string>();
  readonly birthDate = input.required<string>();
  readonly selectedDate = input.required<string>();
  readonly isToday = input.required<boolean>();

  readonly birthDateChange = output<string>();
  readonly selectedDateChange = output<string>();
  readonly previousDay = output<void>();
  readonly goToToday = output<void>();
  readonly nextDay = output<void>();

  valueOf(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }
}
