import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import {
  AspectKey,
  BiorhythmSeries,
  CHART_HEIGHT,
  CHART_WIDTH,
  MARGIN,
  xScale,
  yScale,
} from '../biorhythm';
import { BiorhythmHistogram } from './biorhythm-histogram';
import { BiorhythmSwitchBoard } from './biorhythm-switch-board';

describe('BiorhythmHistogram', () => {
  let fixture: ComponentFixture<BiorhythmHistogram>;
  let component: BiorhythmHistogram;

  const selectedDate = new Date(2026, 4, 17);
  const series: BiorhythmSeries = {
    centerX: 380,
    dateLabels: [{ offset: 0, index: 15, date: selectedDate }],
    lines: [
      {
        aspect: {
          key: 'fisico',
          label: 'Físico',
          period: 23,
          color: '#1656c9',
          dash: '0',
          group: 'basico',
        },
        path: 'M 0 10 L 20 30',
        currentValue: 49.6,
        status: 'Ascendente',
        markerX: 380,
        markerY: 90,
      },
      {
        aspect: {
          key: 'emocional',
          label: 'Emocional',
          period: 28,
          color: '#d32f2f',
          dash: '0',
          group: 'basico',
        },
        path: 'M 0 30 L 20 10',
        currentValue: -25.4,
        status: 'Descendente',
        markerX: 380,
        markerY: 210,
      },
    ],
  };

  const visible: Record<AspectKey, boolean> = {
    fisico: true,
    emocional: false,
    intelectual: false,
    espiritual: false,
    conciencia: false,
    intuicion: false,
    estetica: false,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BiorhythmHistogram],
    }).compileComponents();

    fixture = TestBed.createComponent(BiorhythmHistogram);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('series', series);
    fixture.componentRef.setInput('isToday', false);
    fixture.componentRef.setInput('selectedDate', selectedDate);
    fixture.componentRef.setInput('visible', visible);
    fixture.detectChanges();
  });

  it('expone la configuración y las escalas del gráfico', () => {
    expect(component.chartWidth).toBe(CHART_WIDTH);
    expect(component.chartHeight).toBe(CHART_HEIGHT);
    expect(component.margin).toBe(MARGIN);
    expect(component.viewBox).toBe(`0 0 ${CHART_WIDTH} ${CHART_HEIGHT}`);
    expect(component.gridValues).toEqual([100, 50, 0, -50, -100]);
    expect(component.scaleX).toBe(xScale);
    expect(component.scaleY).toBe(yScale);
  });

  it('formatea fechas en español y redondea valores', () => {
    expect(component.formatDate(selectedDate)).toBe(
      selectedDate.toLocaleDateString('es-ES', { day: '2-digit', month: 'short' }),
    );
    expect(component.rounded(49.6)).toBe(50);
    expect(component.rounded(-25.4)).toBe(-25);
  });

  it('muestra la fecha seleccionada cuando no corresponde a hoy', () => {
    const marker = fixture.nativeElement.querySelector('.marker-label') as SVGTextElement;

    expect(marker.textContent?.trim()).toBe(component.formatDate(selectedDate));
  });

  it('muestra "Hoy" cuando la fecha corresponde a hoy', () => {
    fixture.componentRef.setInput('isToday', true);
    fixture.detectChanges();

    const marker = fixture.nativeElement.querySelector('.marker-label') as SVGTextElement;
    expect(marker.textContent?.trim()).toBe('Hoy');
  });

  it('dibuja únicamente las líneas visibles con sus datos', () => {
    const paths = fixture.nativeElement.querySelectorAll('path');
    const circles = fixture.nativeElement.querySelectorAll('circle');

    expect(paths).toHaveLength(1);
    expect(paths[0].getAttribute('d')).toBe(series.lines[0].path);
    expect(paths[0].getAttribute('stroke')).toBe(series.lines[0].aspect.color);
    expect(circles).toHaveLength(1);
    expect(circles[0].querySelector('title')?.textContent).toBe('Físico: 50%');
  });

  it('propaga el aspecto emitido por el tablero de interruptores', () => {
    const emitted: AspectKey[] = [];
    component.aspectToggle.subscribe((key) => emitted.push(key));
    const switchBoard = fixture.debugElement.query(By.directive(BiorhythmSwitchBoard))
      .componentInstance as BiorhythmSwitchBoard;

    switchBoard.aspectToggle.emit('fisico');

    expect(emitted).toEqual(['fisico']);
  });
});
