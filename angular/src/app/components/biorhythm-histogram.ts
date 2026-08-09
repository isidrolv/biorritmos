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
  templateUrl: './biorhythm-histogram.html',
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
