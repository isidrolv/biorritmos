package biorritmo

import java.awt.Color
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum AspectGroup { BASIC, COMPLEMENTARY }

class Aspect {
    final String key
    final String label
    final int period
    final Color color
    final float[] dash
    final AspectGroup group

    Aspect(String key, String label, int period, String color, float[] dash, AspectGroup group) {
        this.key = key
        this.label = label
        this.period = period
        this.color = Color.decode(color)
        this.dash = dash
        this.group = group
    }
}

class Aspects {
    static final List<Aspect> ALL = Collections.unmodifiableList([
        new Aspect('fisico', 'Físico', 23, '#1656c9', null, AspectGroup.BASIC),
        new Aspect('emocional', 'Emocional', 28, '#d32f2f', null, AspectGroup.BASIC),
        new Aspect('intelectual', 'Intelectual', 33, '#1f9254', null, AspectGroup.BASIC),
        new Aspect('espiritual', 'Espiritual', 53, '#7c3aed', [7f, 4f] as float[], AspectGroup.COMPLEMENTARY),
        new Aspect('conciencia', 'Conciencia', 48, '#0891b2', [1f, 4f] as float[], AspectGroup.COMPLEMENTARY),
        new Aspect('intuicion', 'Intuición', 38, '#d97706', [9f, 3f, 2f, 3f] as float[], AspectGroup.COMPLEMENTARY),
        new Aspect('estetica', 'Estética', 43, '#a3195b', [3f, 3f] as float[], AspectGroup.COMPLEMENTARY)
    ])
}

class CycleResult {
    final Aspect aspect
    final List<Double> values
    final int currentValue
    final String status

    CycleResult(Aspect aspect, List<Double> values, int currentValue, String status) {
        this.aspect = aspect
        this.values = values
        this.currentValue = currentValue
        this.status = status
    }
}

class BiorhythmResult {
    final List<LocalDate> dates
    final List<CycleResult> cycles

    BiorhythmResult(List<LocalDate> dates, List<CycleResult> cycles) {
        this.dates = dates
        this.cycles = cycles
    }
}

class Biorhythm {
    static final int RANGE_DAYS = 15

    static long daysBetween(LocalDate from, LocalDate to) {
        ChronoUnit.DAYS.between(from, to)
    }

    static double valueAt(long daysSinceBirth, int period) {
        Math.sin(2d * Math.PI * daysSinceBirth / period) * 100d
    }

    static String phaseLabel(double value, double nextValue) {
        if (Math.abs(value) < 3d) return 'Crítico'
        nextValue > value ? 'Ascendente' : 'Descendente'
    }

    static BiorhythmResult calculate(LocalDate birthDate, LocalDate selectedDate) {
        List<LocalDate> dates = (-RANGE_DAYS..RANGE_DAYS).collect { int offset ->
            selectedDate.plusDays(offset)
        }
        List<Long> days = dates.collect { LocalDate date -> daysBetween(birthDate, date) }
        List<CycleResult> cycles = Aspects.ALL.collect { Aspect aspect ->
            List<Double> values = days.collect { long day -> valueAt(day, aspect.period) }
            double current = values[RANGE_DAYS]
            new CycleResult(
                aspect,
                values,
                Math.round(current) as int,
                phaseLabel(current, valueAt(days[RANGE_DAYS] + 1L, aspect.period))
            )
        }
        new BiorhythmResult(dates, cycles)
    }
}
