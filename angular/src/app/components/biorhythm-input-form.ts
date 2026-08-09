import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-biorhythm-input-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="controls">
      <label>
        Fecha de nacimiento
        <input
          type="date"
          [value]="birthDate()"
          [max]="today()"
          (input)="birthDateChange.emit(valueOf($event))"
        />
      </label>
      <label>
        Fecha a analizar
        <input
          type="date"
          [value]="selectedDate()"
          (input)="selectedDateChange.emit(valueOf($event))"
        />
      </label>
      <div class="date-nav">
        <button type="button" (click)="previousDay.emit()" aria-label="Día anterior">←</button>
        <button type="button" (click)="goToToday.emit()" [disabled]="isToday()">Hoy</button>
        <button type="button" (click)="nextDay.emit()" aria-label="Día siguiente">→</button>
      </div>
    </div>
  `,
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
