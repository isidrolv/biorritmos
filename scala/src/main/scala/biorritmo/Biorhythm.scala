package biorritmo

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.math.{Pi, abs, sin}

// Calcula las series del biorritmo, replicando lib/biorhythm.rb del
// proyecto Ruby.

case class Margin(top: Double, right: Double, bottom: Double, left: Double)

case class Gridline(value: Int, y: Double, zero: Boolean)
case class DateLabel(x: Double, label: String)

case class Line(
  key: String,
  label: String,
  color: String,
  dash: String,
  group: String,
  points: Seq[(Double, Double)],
  currentValue: Int,
  status: String,
  markerX: Double,
  markerY: Double,
)

case class Series(
  gridlines: Seq[Gridline],
  centerX: Double,
  markerLabel: String,
  dateLabels: Seq[DateLabel],
  lines: Seq[Line],
  isToday: Boolean,
)

object Biorhythm {
  val RangeDays = 15
  val ChartWidth = 760.0
  val ChartHeight = 340.0
  val MarginV: Margin = Margin(top = 16, right = 16, bottom = 34, left = 44)
  val PlotWidth: Double = ChartWidth - MarginV.left - MarginV.right
  val PlotHeight: Double = ChartHeight - MarginV.top - MarginV.bottom

  private val MonthsEs =
    Array("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

  def daysBetween(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)

  def valueAt(daysSinceBirth: Long, period: Double): Double =
    sin(2 * Pi * daysSinceBirth.toDouble / period) * 100

  def phaseLabel(value: Double, nextValue: Double): String =
    if (abs(value) < 3) "Crítico"
    else if (nextValue > value) "Ascendente"
    else "Descendente"

  def formatShort(date: LocalDate): String =
    f"${date.getDayOfMonth}%02d ${MonthsEs(date.getMonthValue - 1)}"

  // Redondeo "half away from zero", igual que Float#round de Ruby y el
  // helper `round` de las versiones Go/Rust (Math.round de Java redondea
  // distinto para negativos exactos en .5).
  private def roundHalfAwayFromZero(v: Double): Int =
    if (v >= 0) (v + 0.5).toInt else (v - 0.5).toInt

  def series(birthDate: LocalDate, selectedDate: LocalDate): Series = {
    val offsets = (-RangeDays to RangeDays).toVector
    val dates = offsets.map(selectedDate.plusDays(_))
    val daysSinceBirth = dates.map(d => daysBetween(birthDate, d))

    def xScale(index: Int): Double =
      MarginV.left + (index.toDouble / (offsets.length - 1)) * PlotWidth
    def yScale(v: Double): Double =
      MarginV.top + (1 - (v + 100) / 200.0) * PlotHeight

    val lines = Aspects.all.map { aspect =>
      val values = daysSinceBirth.map(d => valueAt(d, aspect.period))
      val points = values.zipWithIndex.map { case (v, i) => (xScale(i), yScale(v)) }
      val currentValue = values(RangeDays)
      val nextValue = valueAt(daysSinceBirth(RangeDays) + 1, aspect.period)

      Line(
        key = aspect.key,
        label = aspect.label,
        color = aspect.color,
        dash = aspect.dash,
        group = aspect.group,
        points = points,
        currentValue = roundHalfAwayFromZero(currentValue),
        status = phaseLabel(currentValue, nextValue),
        markerX = xScale(RangeDays),
        markerY = yScale(currentValue),
      )
    }

    val dateLabels = offsets.zipWithIndex.collect {
      case (o, i) if o % 5 == 0 => DateLabel(xScale(i), formatShort(dates(i)))
    }

    val gridlines = Seq(100, 50, 0, -50, -100).map(v => Gridline(v, yScale(v.toDouble), v == 0))

    val today = LocalDate.now()
    val isToday = selectedDate == today
    val markerLabel = if (isToday) "Hoy" else formatShort(selectedDate)

    Series(
      gridlines = gridlines,
      centerX = xScale(RangeDays),
      markerLabel = markerLabel,
      dateLabels = dateLabels,
      lines = lines,
      isToday = isToday,
    )
  }
}
