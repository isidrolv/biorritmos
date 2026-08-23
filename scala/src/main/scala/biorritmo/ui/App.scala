package biorritmo.ui

import java.time.LocalDate
import javafx.scene.canvas.{Canvas as FXCanvas}
import javafx.scene.input.MouseEvent
import javafx.scene.text.{FontWeight, TextAlignment}

import scala.collection.mutable

import biorritmo.{Aspect, Aspects, Biorhythm, Colors, Line, Mode, Series, Theme}
import biorritmo.gfx.{Canvas as GfxCanvas}

// Implementa la ventana completa de la aplicación, replicando app.rb del
// proyecto Ruby (y su misma reinterpretación en las versiones Go/Rust): una
// barra de tema, una fila de controles (fechas y navegación) y un lienzo con
// el encabezado, la gráfica y la leyenda -- todo dibujado a mano sobre un
// único Canvas de JavaFX.
class App(fxCanvas: FXCanvas) {
  import App.*

  private var themeMode: Mode = Mode.System
  private val visible: mutable.Map[String, Boolean] =
    mutable.Map.from(Aspects.all.map(_.key -> true))

  private val today = LocalDate.now()
  private var birthDate: LocalDate = {
    val day = math.min(today.getDayOfMonth, 28)
    LocalDate.of(today.getYear - 25, today.getMonth, day)
  }
  private var selectedDate: LocalDate = today

  private var series: Series = Biorhythm.series(birthDate, selectedDate)

  private val themeButtons = Array(new Button("Sistema"), new Button("Claro"), new Button("Oscuro"))
  private val themeModes = Array(Mode.System, Mode.Light, Mode.Dark)
  private val prevBtn = new Button("< Anterior")
  private val todayBtn = new Button("Hoy")
  private val nextBtn = new Button("Siguiente >")
  private val birthField = new DateField
  private val selectedField = new DateField

  private val legendGroups = Seq(
    (LegendLeft, "Aspectos básicos", Aspects.basico),
    (LegendLeft + LegendColWidth + LegendColGap, "Aspectos complementarios", Aspects.complementario),
  )

  private var gfx: GfxCanvas = new GfxCanvas(fxCanvas.getGraphicsContext2D)

  fxCanvas.setOnMousePressed((e: MouseEvent) => {
    if (gfx.dispatchClick(e.getX, e.getY)) draw()
  })

  private def recalc(): Unit = {
    series = Biorhythm.series(birthDate, selectedDate)
  }

  def draw(): Unit = {
    val c = new GfxCanvas(fxCanvas.getGraphicsContext2D)
    gfx = c
    c.clearHitRegions()

    val colors = Theme.colors(themeMode)
    val width = fxCanvas.getWidth
    val height = fxCanvas.getHeight
    c.fillRect(0, 0, width, height, colors.bg)

    val contentW = width - 2 * WindowMargin

    var y = WindowMargin
    y = drawTopbar(c, WindowMargin, y, contentW, colors) + RowGap
    y = drawControls(c, WindowMargin, y, contentW, colors) + RowGap
    drawCanvasArea(c, WindowMargin, y, contentW, height - WindowMargin - y, colors)
  }

  // -- barra de tema --------------------------------------------------------

  private def drawTopbar(c: GfxCanvas, ox: Double, oy: Double, width: Double, colors: Colors): Double = {
    val label = "Tema:"
    val gap = 8.0
    val labelSz = 13.0

    val (lw, lh) = c.measureText(label, labelSz)
    val sizes = themeButtons.map(_.measure(c))
    val btnH = sizes.map(_._2).max
    val contentW = lw + gap + sizes.map(_._1 + gap).sum
    val rowH = math.max(lh, btnH)
    val startX = math.max(width - contentW, 0.0)

    var cx = ox + startX
    c.text(label, cx, oy + (rowH - lh) / 2, labelSz, colors.textH)
    cx += lw + gap

    for (i <- themeButtons.indices) {
      val (w, h) = sizes(i)
      val selected = themeMode == themeModes(i)
      themeButtons(i).layout(c, cx, oy + (rowH - h) / 2, colors, selected, true) {
        themeMode = themeModes(i)
      }
      cx += w + gap
    }

    oy + rowH
  }

  // -- fila de controles (fechas y navegación) -------------------------------

  private def drawControls(c: GfxCanvas, ox: Double, oy: Double, width: Double, colors: Colors): Double = {
    val labelSz = 12.0
    val fieldGap = 4.0
    val colGap = 28.0
    val navBtnGap = 6.0
    val vPad = 8.0

    val birthLabel = "Fecha de nacimiento"
    val selLabel = "Fecha a analizar"
    val (birthLabelW, labelH) = c.measureText(birthLabel, labelSz)
    val (selLabelW, _) = c.measureText(selLabel, labelSz)
    val fieldW = birthField.width

    val col1W = math.max(birthLabelW, fieldW)
    val col2W = math.max(selLabelW, fieldW)

    val (prevW, prevH) = prevBtn.measure(c)
    val (todayW, todayH) = todayBtn.measure(c)
    val (nextW, nextH) = nextBtn.measure(c)
    val navW = prevW + navBtnGap + todayW + navBtnGap + nextW
    val navH = Seq(prevH, todayH, nextH).max
    val col3W = navW

    val contentW = col1W + colGap + col2W + colGap + col3W
    val startX = math.max((width - contentW) / 2.0, 0.0)

    val fieldH = birthField.height
    val rowH = labelH + fieldGap + math.max(fieldH, navH)

    val labelY = oy + vPad
    val fieldY = labelY + labelH + fieldGap
    val navY = fieldY + (fieldH - navH) / 2.0

    var cx = ox + startX
    c.text(birthLabel, cx, labelY, labelSz, colors.label)
    birthField.layout(c, cx, fieldY, birthDate, colors) { d =>
      birthDate = d
      recalc()
    }
    cx += col1W + colGap

    c.text(selLabel, cx, labelY, labelSz, colors.label)
    selectedField.layout(c, cx, fieldY, selectedDate, colors) { d =>
      selectedDate = d
      recalc()
    }
    cx += col2W + colGap

    var nx = cx
    prevBtn.layout(c, nx, navY, colors, false, true) {
      selectedDate = selectedDate.minusDays(1)
      recalc()
    }
    nx += prevW + navBtnGap

    val todayEnabled = selectedDate != LocalDate.now()
    todayBtn.layout(c, nx, navY, colors, false, todayEnabled) {
      selectedDate = LocalDate.now()
      recalc()
    }
    nx += todayW + navBtnGap

    nextBtn.layout(c, nx, navY, colors, false, true) {
      selectedDate = selectedDate.plusDays(1)
      recalc()
    }

    oy + rowH + 2 * vPad
  }

  // -- lienzo: encabezado + gráfica + leyenda --------------------------------

  private def drawCanvasArea(c: GfxCanvas, ox: Double, oy: Double, width: Double, height: Double, colors: Colors): Unit = {
    c.fillRect(ox, oy, width, height, colors.bg)

    val innerOx = ox + math.max((width - ChartWidth) / 2.0, 0.0)

    drawHeader(c, innerOx, oy, colors)
    drawChart(c, innerOx, oy + ChartY, colors)
    drawLegend(c, innerOx, oy + LegendY, colors)
  }

  private def drawHeader(c: GfxCanvas, ox: Double, oy: Double, colors: Colors): Unit = {
    val titleH = c.paragraph("Calculadora de biorritmo", ox, oy + 6, ChartWidth, 22, colors.textH, FontWeight.MEDIUM)

    val subtitle = "Ingresa tu fecha de nacimiento para graficar tus ciclos físico, emocional e intelectual, " +
      "junto con los aspectos complementarios: espiritual, conciencia, intuición y estética."
    c.paragraph(subtitle, ox, oy + 6 + titleH + 6, ChartWidth, 12, colors.muted)
  }

  private def drawChart(c: GfxCanvas, ox: Double, oy: Double, colors: Colors): Unit = {
    val s = series
    val left = ox + Biorhythm.MarginV.left
    val rightEdge = ox + ChartWidth - Biorhythm.MarginV.right
    val bottom = oy + ChartHeight - Biorhythm.MarginV.bottom
    val top = oy + Biorhythm.MarginV.top

    for (g <- s.gridlines) {
      val col = if (g.zero) colors.gridlineZero else colors.gridline
      c.strokeLine(left, oy + g.y, rightEdge, oy + g.y, col, 1)
      c.text(g.value.toString, left - 8, oy + g.y - 6, 11, colors.axisLabel, FontWeight.NORMAL, TextAlignment.RIGHT)
    }

    val cx = ox + s.centerX
    c.strokeLine(cx, top, cx, bottom, colors.markerLine, 1, Seq(3, 3))
    c.text(s.markerLabel, cx, top - 18, 12, colors.label, FontWeight.BOLD, TextAlignment.CENTER)

    for (dl <- s.dateLabels)
      c.text(dl.label, ox + dl.x, bottom + 6, 11, colors.axisLabel, FontWeight.NORMAL, TextAlignment.CENTER)

    for (line <- s.lines if visible(line.key)) {
      val col = Theme.hex(line.color)
      val dash = c.parseDash(line.dash)
      val points = line.points.map { case (x, y) => (ox + x, oy + y) }
      c.strokePolyline(points, col, 2, dash)

      val mx = ox + line.markerX
      val my = oy + line.markerY
      c.fillCircle(mx, my, 5, col)
      c.strokeCircle(mx, my, 5, 2, Theme.White)
    }
  }

  private def drawLegend(c: GfxCanvas, ox: Double, oy: Double, colors: Colors): Unit = {
    val valuesByKey = series.lines.map(l => l.key -> l).toMap

    for ((colX, title, asps) <- legendGroups) {
      c.text(title, ox + colX, oy + LegendTop, 14, colors.label, FontWeight.BOLD)

      for ((asp, i) <- asps.zipWithIndex) {
        val rowY = oy + LegendTop + LegendHeaderH + i * LegendRowH
        drawLegendRow(c, ox + colX, rowY, asp, valuesByKey(asp.key), colors)
        c.hitRegion(ox + colX, rowY, LegendColWidth, LegendRowH) {
          visible(asp.key) = !visible(asp.key)
        }
      }
    }
  }

  private def drawLegendRow(c: GfxCanvas, colX: Double, rowY: Double, asp: Aspect, line: Line, colors: Colors): Unit = {
    val vis = visible(asp.key)
    val cy = rowY + LegendRowH / 2.0
    val col = Theme.hex(asp.color)

    if (vis) c.fillCircle(colX + 8, cy, 6, col)
    else c.strokeCircle(colX + 8, cy, 6, 2, col)

    val textCol = if (vis) colors.textH else colors.legendDim
    c.text(asp.label, colX + 24, rowY + 4, 13, textCol)
    c.text(s"${line.currentValue}%", colX + LegendColWidth - 90, rowY + 4, 13, textCol, FontWeight.NORMAL, TextAlignment.RIGHT)
    c.text(line.status, colX + LegendColWidth, rowY + 4, 12, colors.axisLabel, FontWeight.NORMAL, TextAlignment.RIGHT)
  }
}

object App {
  private val WindowMargin = 16.0
  private val RowGap = 10.0

  private val ChartWidth = Biorhythm.ChartWidth
  private val ChartHeight = Biorhythm.ChartHeight
  private val HeaderHeight = 92.0

  private val LegendWidth = ChartWidth
  private val LegendColWidth = 340.0
  private val LegendColGap = 40.0
  private val LegendLeft = (LegendWidth - (LegendColWidth * 2 + LegendColGap)) / 2.0
  private val LegendTop = 12.0
  private val LegendHeaderH = 22.0
  private val LegendRowH = 26.0

  private val CanvasGap = 16.0
  private val ChartY = HeaderHeight + CanvasGap
  private val LegendY = ChartY + ChartHeight + CanvasGap
}
