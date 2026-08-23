package biorritmo.ui

import java.time.LocalDate
import javafx.scene.text.{FontWeight, TextAlignment}

import biorritmo.Colors
import biorritmo.gfx.Canvas

// Selector de fecha compacto (día/mes/año), cada unidad con flechas
// arriba/abajo para incrementar o decrementar -- el equivalente funcional
// del uiDatePicker nativo que usa el proyecto Ruby. Replica datefield.go /
// datefield.rs.
class DateField {
  private val BoxH = 26.0
  private val DayW = 30.0
  private val MonthW = 30.0
  private val YearW = 46.0
  private val ArrowW = 16.0
  private val UnitGap = 3.0
  private val Border = 1.0
  private val TextSz = 13.0

  def width: Double = (DayW + ArrowW) + UnitGap + (MonthW + ArrowW) + UnitGap + (YearW + ArrowW)
  def height: Double = BoxH

  // Dibuja el selector en (x, y) para date; onChange se invoca con la nueva
  // fecha cuando el usuario pulsa una flecha.
  def layout(c: Canvas, x: Double, y: Double, date: LocalDate, colors: Colors)(onChange: LocalDate => Unit): Unit = {
    var cx = x

    drawUnit(c, cx, y, DayW, f"${date.getDayOfMonth}%02d", colors,
      () => onChange(date.plusDays(1)), () => onChange(date.plusDays(-1)))
    cx += DayW + ArrowW + UnitGap

    drawUnit(c, cx, y, MonthW, f"${date.getMonthValue}%02d", colors,
      () => onChange(date.plusMonths(1)), () => onChange(date.plusMonths(-1)))
    cx += MonthW + ArrowW + UnitGap

    drawUnit(c, cx, y, YearW, f"${date.getYear}%04d", colors,
      () => onChange(date.plusYears(1)), () => onChange(date.plusYears(-1)))
  }

  private def drawUnit(
    c: Canvas, x: Double, y: Double, boxW: Double, text: String, colors: Colors,
    onUp: () => Unit, onDown: () => Unit,
  ): Unit = {
    val borderCol = colors.markerLine

    c.fillRect(x, y, boxW, BoxH, colors.bg)
    c.strokeRectBorder(x, y, boxW, BoxH, Border, borderCol)

    val (tw, th) = c.measureText(text, TextSz, FontWeight.NORMAL)
    c.text(text, x + (boxW - tw) / 2, y + (BoxH - th) / 2, TextSz, colors.textH, FontWeight.NORMAL, TextAlignment.LEFT)

    val ax = x + boxW
    val arrowH = BoxH / 2
    c.fillRect(ax, y, ArrowW, BoxH, colors.bg)
    c.strokeRectBorder(ax, y, ArrowW, BoxH, Border, borderCol)
    c.strokeLine(ax, y + arrowH, ax + ArrowW, y + arrowH, borderCol, Border)

    val triCol = colors.label
    val mx = ax + ArrowW / 2
    c.fillPolygon(Seq((mx - 4, y + arrowH - 5), (mx + 4, y + arrowH - 5), (mx, y + arrowH - 11)), triCol)
    c.fillPolygon(Seq((mx - 4, y + arrowH + 5), (mx + 4, y + arrowH + 5), (mx, y + arrowH + 11)), triCol)

    c.hitRegion(ax, y, ArrowW, arrowH)(onUp())
    c.hitRegion(ax, y + arrowH, ArrowW, arrowH)(onDown())
  }
}
