package biorritmo

import java.awt.Color
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

enum class AspectGroup { BASIC, COMPLEMENTARY }

data class Aspect(
    val key: String,
    val label: String,
    val period: Int,
    val color: Color,
    val dash: FloatArray?,
    val group: AspectGroup,
)

object Aspects {
    val all = listOf(
        Aspect("fisico", "Físico", 23, Color.decode("#1656c9"), null, AspectGroup.BASIC),
        Aspect("emocional", "Emocional", 28, Color.decode("#d32f2f"), null, AspectGroup.BASIC),
        Aspect("intelectual", "Intelectual", 33, Color.decode("#1f9254"), null, AspectGroup.BASIC),
        Aspect("espiritual", "Espiritual", 53, Color.decode("#7c3aed"), floatArrayOf(7f, 4f), AspectGroup.COMPLEMENTARY),
        Aspect("conciencia", "Conciencia", 48, Color.decode("#0891b2"), floatArrayOf(1f, 4f), AspectGroup.COMPLEMENTARY),
        Aspect("intuicion", "Intuición", 38, Color.decode("#d97706"), floatArrayOf(9f, 3f, 2f, 3f), AspectGroup.COMPLEMENTARY),
        Aspect("estetica", "Estética", 43, Color.decode("#a3195b"), floatArrayOf(3f, 3f), AspectGroup.COMPLEMENTARY),
    )
}

data class CycleResult(
    val aspect: Aspect,
    val values: List<Double>,
    val currentValue: Int,
    val status: String,
)

data class BiorhythmResult(
    val dates: List<LocalDate>,
    val cycles: List<CycleResult>,
)

object Biorhythm {
    const val RANGE_DAYS = 15

    fun daysBetween(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)

    fun valueAt(daysSinceBirth: Long, period: Int): Double =
        sin(2.0 * PI * daysSinceBirth / period.toDouble()) * 100.0

    fun phaseLabel(value: Double, nextValue: Double): String = when {
        abs(value) < 3.0 -> "Crítico"
        nextValue > value -> "Ascendente"
        else -> "Descendente"
    }

    fun calculate(birthDate: LocalDate, selectedDate: LocalDate): BiorhythmResult {
        val dates = (-RANGE_DAYS..RANGE_DAYS).map { selectedDate.plusDays(it.toLong()) }
        val days = dates.map { daysBetween(birthDate, it) }
        val cycles = Aspects.all.map { aspect ->
            val values = days.map { valueAt(it, aspect.period) }
            val current = values[RANGE_DAYS]
            CycleResult(
                aspect = aspect,
                values = values,
                currentValue = current.roundToInt(),
                status = phaseLabel(current, valueAt(days[RANGE_DAYS] + 1, aspect.period)),
            )
        }
        return BiorhythmResult(dates, cycles)
    }
}
