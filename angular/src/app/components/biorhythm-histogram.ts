import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import {
  AspectKey,
  BiorhythmSeries,
  CHART_HEIGHT,
  CHART_WIDTH,
  MARGIN,
  yScale,
  xScale,
} from '../biorhythm';
import { BiorhythmSwitchBoard } from './biorhythm-switch-board';

@Component({
  selector: 'app-biorhythm-histogram',
  imports: [BiorhythmSwitchBoard],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg
      class="chart"
      [attr.viewBox]="viewBox"
      role="img"
      aria-label="Gráfico de biorritmo con ciclos físico, emocional, intelectual, espiritual, conciencia, intuición y estética"
    >
      @for (value of gridValues; track value) {
        <g>
          <line
            [attr.x1]="margin.left"
            [attr.x2]="chartWidth - margin.right"
            [attr.y1]="scaleY(value)"
            [attr.y2]="scaleY(value)"
            [attr.class]="value === 0 ? 'gridline gridline-zero' : 'gridline'"
          />
          <text
            [attr.x]="margin.left - 8"
            [attr.y]="scaleY(value)"
            class="axis-label"
            text-anchor="end"
            dy="0.32em"
          >
            {{ value }}
          </text>
        </g>
      }

      <line
        [attr.x1]="series().centerX"
        [attr.x2]="series().centerX"
        [attr.y1]="margin.top"
        [attr.y2]="chartHeight - margin.bottom"
        class="marker-line"
      />
      <text
        [attr.x]="series().centerX"
        [attr.y]="margin.top - 4"
        class="marker-label"
        text-anchor="middle"
      >
        {{ isToday() ? 'Hoy' : formatDate(selectedDate()) }}
      </text>

      @for (label of series().dateLabels; track label.offset) {
        <text
          [attr.x]="scaleX(label.index)"
          [attr.y]="chartHeight - margin.bottom + 18"
          class="axis-label"
          text-anchor="middle"
        >
          {{ formatDate(label.date) }}
        </text>
      }

      @for (line of series().lines; track line.aspect.key) {
        @if (visible()[line.aspect.key]) {
          <g>
            <path
              [attr.d]="line.path"
              fill="none"
              [attr.stroke]="line.aspect.color"
              stroke-width="2"
              [attr.stroke-dasharray]="line.aspect.dash"
              stroke-linejoin="round"
              stroke-linecap="round"
            />
            <circle
              [attr.cx]="line.markerX"
              [attr.cy]="line.markerY"
              r="5"
              [attr.fill]="line.aspect.color"
              stroke="#ffffff"
              stroke-width="2"
            >
              <title>{{ line.aspect.label }}: {{ rounded(line.currentValue) }}%</title>
            </circle>
          </g>
        }
      }
    </svg>

    <app-biorhythm-switch-board
      [lines]="series().lines"
      [visible]="visible()"
      (aspectToggle)="aspectToggle.emit($event)"
    />
  `,
})
export class BiorhythmHistogram {
  readonly series = input.required<BiorhythmSeries>();
  readonly isToday = input.required<boolean>();
  readonly selectedDate = input.required<Date>();
  readonly visible = input.required<Record<AspectKey, boolean>>();
  readonly aspectToggle = output<AspectKey>();

  readonly chartWidth = CHART_WIDTH;
  readonly chartHeight = CHART_HEIGHT;
  readonly margin = MARGIN;
  readonly viewBox = `0 0 ${CHART_WIDTH} ${CHART_HEIGHT}`;
  readonly gridValues = [100, 50, 0, -50, -100];
  readonly scaleX = xScale;
  readonly scaleY = yScale;

  formatDate(date: Date): string {
    return date.toLocaleDateString('es-ES', { day: '2-digit', month: 'short' });
  }

  rounded(value: number): number {
    return Math.round(value);
  }
}
