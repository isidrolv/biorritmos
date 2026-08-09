import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { AspectGroup, AspectKey, BiorhythmLine } from '../biorhythm';

@Component({
  selector: 'app-biorhythm-switch-board',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './biorhythm-switch-board.html',
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
