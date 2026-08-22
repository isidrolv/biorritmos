package biorritmo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.math.abs

class BiorhythmTest {
    @Test
    fun `all cycles begin at zero on birth date`() {
        Aspects.all.forEach { assertTrue(abs(Biorhythm.valueAt(0, it.period)) < 0.000001) }
    }

    @Test
    fun `calculation produces the same 31 day range as Ruby`() {
        val selected = LocalDate.of(2026, 8, 21)
        val result = Biorhythm.calculate(LocalDate.of(2000, 1, 1), selected)
        assertEquals(31, result.dates.size)
        assertEquals(selected, result.dates[Biorhythm.RANGE_DAYS])
        assertEquals(7, result.cycles.size)
    }

    @Test
    fun `phase labels match Ruby thresholds`() {
        assertEquals("Crítico", Biorhythm.phaseLabel(2.999, 4.0))
        assertEquals("Ascendente", Biorhythm.phaseLabel(4.0, 5.0))
        assertEquals("Descendente", Biorhythm.phaseLabel(4.0, 3.0))
    }
}
