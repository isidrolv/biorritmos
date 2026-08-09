import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { AspectGroup, AspectKey, BiorhythmLine } from '../biorhythm';

@Component({
  selector: 'app-biorhythm-switch-board',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="legend">
      @for (group of groups; track group.key) {
        <div class="legend-group">
          <h2>{{ group.label }}</h2>
          <ul>
            @for (line of linesFor(group.key); track line.aspect.key) {
              <li>
                <button
                  type="button"
                  class="legend-item"
                  [attr.aria-pressed]="visible()[line.aspect.key]"
                  (click)="aspectToggle.emit(line.aspect.key)"
                >
                  <span
                    class="swatch"
                    [style.background]="
                      visible()[line.aspect.key] ? line.aspect.color : 'transparent'
                    "
                    [style.border-color]="line.aspect.color"
                  ></span>
                  <span class="legend-text">{{ line.aspect.label }}</span>
                  <span class="legend-value">{{ rounded(line.currentValue) }}%</span>
                  <span class="legend-status">{{ line.status }}</span>
                </button>
              </li>
            }
          </ul>
        </div>
      }
    </div>
  `,
})
export class BiorhythmSwitchBoard {
  readonly lines = input.required<BiorhythmLine[]>();
  readonly visible = input.required<Record<AspectKey, boolean>>();
  readonly aspectToggle = output<AspectKey>();

  readonly groups: { key: AspectGroup; label: string }[] = [
    { key: 'basico', label: 'Aspectos básicos' },
    { key: 'complementario', label: 'Aspectos complementarios' },
  ];

  linesFor(group: AspectGroup): BiorhythmLine[] {
    return this.lines().filter((line) => line.aspect.group === group);
  }

  rounded(value: number): number {
    return Math.round(value);
  }
}
