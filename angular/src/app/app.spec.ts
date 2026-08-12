import { App } from './app';
import { ASPECTS } from './biorhythm';

describe('App component', () => {
  it('should initialize with default values', () => {
    const app = new App();
    expect(app.theme()).toBe('light');
    expect(app.birthDate()).toBe('');
    expect(app.selectedDate()).toBe(app.today);
    expect(app.isToday()).toBe(true);
    expect(app.series()).toBeNull();

    // Check all aspects are visible by default
    const visible = app.visible();
    ASPECTS.forEach(aspect => {
      expect(visible[aspect.key]).toBe(true);
    });
  });

  it('should set theme', () => {
    const app = new App();
    const event = {
      target: { value: 'night' }
    } as unknown as Event;

    app.setTheme(event);
    expect(app.theme()).toBe('night');
  });

  it('should toggle aspect visibility', () => {
    const app = new App();
    const aspectKey = ASPECTS[0].key;

    expect(app.visible()[aspectKey]).toBe(true);
    app.toggleAspect(aspectKey);
    expect(app.visible()[aspectKey]).toBe(false);
    app.toggleAspect(aspectKey);
    expect(app.visible()[aspectKey]).toBe(true);
  });

  it('should shift selected date', () => {
    const app = new App();
    const initialDate = app.selectedDate();

    app.shiftSelectedDate(1);
    expect(app.selectedDate()).not.toBe(initialDate);

    app.shiftSelectedDate(-1);
    expect(app.selectedDate()).toBe(initialDate);
  });

  it('should compute series when birthDate is set', () => {
    const app = new App();
    app.birthDate.set('1990-01-01');

    const series = app.series();
    expect(series).not.toBeNull();
    expect(series?.lines.length).toBe(ASPECTS.length);
    expect(series?.dateLabels.length).toBeGreaterThan(0);
  });

  it('should update isToday when date changes', () => {
    const app = new App();
    expect(app.isToday()).toBe(true);

    app.shiftSelectedDate(1);
    expect(app.isToday()).toBe(false);

    app.selectedDate.set(app.today);
    expect(app.isToday()).toBe(true);
  });
});
