import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-biorhythm-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './biorhythm-header.html',
})
export class BiorhythmHeader {}
