package biorritmo

import org.junit.jupiter.api.Test
import java.time.LocalDate

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class BiorhythmTest {
    @Test
    void 'todos los ciclos comienzan en cero el día del nacimiento'() {
        Aspects.ALL.each { Aspect aspect ->
            assertTrue(Math.abs(Biorhythm.valueAt(0, aspect.period)) < 0.000001d)
        }
    }

    @Test
    void 'el cálculo produce el mismo rango de 31 días que Ruby'() {
        LocalDate selected = LocalDate.of(2026, 8, 21)
        BiorhythmResult result = Biorhythm.calculate(LocalDate.of(2000, 1, 1), selected)
        assertEquals(31, result.dates.size())
        assertEquals(selected, result.dates[Biorhythm.RANGE_DAYS])
        assertEquals(7, result.cycles.size())
    }

    @Test
    void 'las etiquetas de fase respetan los umbrales de Ruby'() {
        assertEquals('Crítico', Biorhythm.phaseLabel(2.999d, 4d))
        assertEquals('Ascendente', Biorhythm.phaseLabel(4d, 5d))
        assertEquals('Descendente', Biorhythm.phaseLabel(4d, 3d))
    }

    @Test
    void 'los siete aspectos conservan periodos colores y grupos de Ruby'() {
        assertEquals([23, 28, 33, 53, 48, 38, 43], Aspects.ALL*.period)
        assertEquals(3, Aspects.ALL.count { it.group == AspectGroup.BASIC })
        assertEquals(4, Aspects.ALL.count { it.group == AspectGroup.COMPLEMENTARY })
    }
}
