package biorritmo.gfx

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.{StrokeLineCap, StrokeLineJoin}
import javafx.scene.text.{Font, FontWeight, Text, TextAlignment}

import scala.collection.mutable.ArrayBuffer

// Primitivas de dibujo sobre un GraphicsContext de JavaFX, equivalentes a
// lib/native_draw.rb (NativeDraw) del proyecto Ruby: rectángulos, líneas
// (con o sin patrón de trazo), círculos/anillos y texto alineado -- más el
// registro de zonas clicables (el equivalente de AreaWidget#redraw +
// canvas_mouse_down, y de HitRegion/interact_click en las versiones Go/Rust).
//
// Todas las coordenadas son las mismas "unidades lógicas" que usa el
// proyecto Ruby (p.ej. un gráfico de 760x340); JavaFX ya escala la ventana
// completa según el DPI de Windows, así que no hace falta escalar a mano.
class Canvas(gc: GraphicsContext) {
  val FontFamily = "Segoe UI"

  private val measurer = new Text()
  private val hitRegions = ArrayBuffer.empty[(Double, Double, Double, Double, () => Unit)]

  def clearHitRegions(): Unit = hitRegions.clear()

  def hitRegion(x: Double, y: Double, w: Double, h: Double)(onClick: => Unit): Unit =
    hitRegions += ((x, y, w, h, () => onClick))

  // Busca la zona clicable (registrada en el último draw()) bajo (px, py) y
  // ejecuta su callback. Devuelve si hubo alguna coincidencia.
  def dispatchClick(px: Double, py: Double): Boolean =
    hitRegions.find { case (x, y, w, h, _) => px >= x && px <= x + w && py >= y && py <= y + h } match {
      case Some((_, _, _, _, cb)) => cb(); true
      case None                   => false
    }

  def measureText(text: String, size: Double, weight: FontWeight = FontWeight.NORMAL): (Double, Double) = {
    measurer.setText(text)
    measurer.setFont(Font.font(FontFamily, weight, size))
    val b = measurer.getLayoutBounds
    (b.getWidth, b.getHeight)
  }

  def fillRect(x: Double, y: Double, w: Double, h: Double, color: Color): Unit = {
    gc.setFill(color)
    gc.fillRect(x, y, w, h)
  }

  def strokeRectBorder(x: Double, y: Double, w: Double, h: Double, thickness: Double, color: Color): Unit = {
    gc.setStroke(color)
    gc.setLineWidth(thickness)
    gc.strokeRect(x, y, w, h)
  }

  def fillCircle(cx: Double, cy: Double, r: Double, color: Color): Unit = {
    gc.setFill(color)
    gc.fillOval(cx - r, cy - r, r * 2, r * 2)
  }

  def strokeCircle(cx: Double, cy: Double, r: Double, thickness: Double, color: Color): Unit = {
    gc.setStroke(color)
    gc.setLineWidth(thickness)
    gc.strokeOval(cx - r, cy - r, r * 2, r * 2)
  }

  def strokeLine(
    x1: Double, y1: Double, x2: Double, y2: Double,
    color: Color, thickness: Double = 1.0, dashes: Seq[Double] = Nil,
  ): Unit =
    strokePolyline(Seq((x1, y1), (x2, y2)), color, thickness, dashes)

  def strokePolyline(
    points: Seq[(Double, Double)], color: Color,
    thickness: Double = 2.0, dashes: Seq[Double] = Nil,
  ): Unit = {
    if (points.size < 2) return

    gc.setStroke(color)
    gc.setLineWidth(thickness)
    gc.setLineCap(StrokeLineCap.ROUND)
    gc.setLineJoin(StrokeLineJoin.ROUND)
    if (dashes.nonEmpty) gc.setLineDashes(dashes.toArray*) else gc.setLineDashes()

    gc.beginPath()
    gc.moveTo(points.head._1, points.head._2)
    points.tail.foreach { case (x, y) => gc.lineTo(x, y) }
    gc.stroke()
    gc.setLineDashes()
  }

  def fillPolygon(points: Seq[(Double, Double)], color: Color): Unit = {
    if (points.size < 3) return
    gc.setFill(color)
    gc.fillPolygon(points.map(_._1).toArray, points.map(_._2).toArray, points.size)
  }

  // Convierte un patrón "7 4" (como en Aspect.dash) a la lista de longitudes
  // usada por strokeLine/strokePolyline. "0" o "" es una línea sólida.
  def parseDash(dashStr: String): Seq[Double] = {
    val s = dashStr.trim
    if (s.isEmpty || s == "0") Nil
    else s.split("\\s+").toSeq.flatMap(v => scala.util.Try(v.toDouble).toOption)
  }

  // Dibuja una línea de texto en (x, y) -- esquina superior según align.
  def text(
    text: String, x: Double, y: Double, size: Double, color: Color,
    weight: FontWeight = FontWeight.NORMAL, align: TextAlignment = TextAlignment.LEFT,
  ): Unit = {
    gc.setFont(Font.font(FontFamily, weight, size))
    gc.setFill(color)
    gc.setTextAlign(align)
    gc.setTextBaseline(VPos.TOP)
    gc.fillText(text, x, y)
  }

  // Dibuja un párrafo centrado y ajustado (wrap) dentro de width, como
  // NativeDraw.draw_layout. Devuelve la altura ocupada, para poder
  // posicionar contenido debajo.
  def paragraph(
    text: String, x: Double, y: Double, width: Double, size: Double, color: Color,
    weight: FontWeight = FontWeight.NORMAL,
  ): Double = {
    val words = text.split(" ").toSeq
    var lines = Vector.empty[String]
    var current = ""
    for (w <- words) {
      val candidate = if (current.isEmpty) w else s"$current $w"
      val (cw, _) = measureText(candidate, size, weight)
      if (cw > width && current.nonEmpty) {
        lines :+= current
        current = w
      } else current = candidate
    }
    if (current.nonEmpty) lines :+= current
    if (lines.isEmpty) lines :+= text

    val (_, lineH) = measureText(lines.head, size, weight)
    val lineHeight = lineH * 1.3
    val cx = x + width / 2.0
    var cy = y
    for (line <- lines) {
      this.text(line, cx, cy, size, color, weight, TextAlignment.CENTER)
      cy += lineHeight
    }
    cy - y
  }
}
