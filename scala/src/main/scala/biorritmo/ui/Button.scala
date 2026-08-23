package biorritmo.ui

import javafx.scene.text.{FontWeight, TextAlignment}

import biorritmo.Colors
import biorritmo.gfx.Canvas

// Botón plano simple (rectángulo con borde), parecido al estilo de los
// botones nativos de Windows que usa el proyecto Ruby. Replica button.go /
// button.rs.
class Button(val text: String) {
  private val PadX = 14.0
  private val PadY = 6.0
  private val Border = 1.0
  private val TextSz = 13.0

  // Devuelve el tamaño (ancho, alto) que ocupará el botón.
  def measure(c: Canvas): (Double, Double) = {
    val (tw, th) = c.measureText(text, TextSz, FontWeight.NORMAL)
    (tw + PadX * 2, th + PadY * 2)
  }

  // Dibuja el botón en (x, y) y, si enabled, registra su zona clicable.
  def layout(
    c: Canvas, x: Double, y: Double, colors: Colors, selected: Boolean, enabled: Boolean,
  )(onClick: => Unit): Unit = {
    val (w, h) = measure(c)

    val bg = if (selected) colors.gridline else colors.bg
    val textCol = if (enabled) colors.textH else colors.legendDim
    val borderCol = if (enabled) colors.markerLine else colors.legendDim

    c.fillRect(x, y, w, h, bg)
    c.strokeRectBorder(x, y, w, h, Border, borderCol)
    c.text(text, x + PadX, y + PadY, TextSz, textCol, FontWeight.NORMAL, TextAlignment.LEFT)

    if (enabled) c.hitRegion(x, y, w, h)(onClick)
  }
}
