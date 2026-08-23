package ui

import (
	"gioui.org/font"
	"gioui.org/widget"

	"biorritmo/internal/gfx"
	"biorritmo/internal/theme"
)

const (
	buttonPadX   = 14.0
	buttonPadY   = 6.0
	buttonBorder = 1.0
	buttonTextSz = 13.0
)

// Button es un botón plano simple (rectángulo con borde), parecido al
// estilo de los botones nativos de Windows que usa el proyecto Ruby.
type Button struct {
	Clickable widget.Clickable
	Text      string
}

// Measure devuelve el tamaño (ancho, alto) que ocupará el botón.
func (b *Button) Measure(c *gfx.Canvas) (w, h float64) {
	tw, th := c.MeasureText(b.Text, buttonTextSz, font.Normal)
	return tw + buttonPadX*2, th + buttonPadY*2
}

// Layout dibuja el botón en (x, y) y, si enabled, registra su zona
// clicable. Devuelve si fue pulsado en este frame.
func (b *Button) Layout(c *gfx.Canvas, x, y float64, colors theme.Colors, selected, enabled bool) bool {
	w, h := b.Measure(c)

	bg := colors.Bg
	textCol := colors.TextH
	borderCol := colors.MarkerLine
	if selected {
		bg = colors.Gridline
	}
	if !enabled {
		textCol = colors.LegendDim
		borderCol = colors.LegendDim
	}

	c.FillRect(x, y, w, h, bg)
	c.FillRect(x, y, w, buttonBorder, borderCol)
	c.FillRect(x, y+h-buttonBorder, w, buttonBorder, borderCol)
	c.FillRect(x, y, buttonBorder, h, borderCol)
	c.FillRect(x+w-buttonBorder, y, buttonBorder, h, borderCol)

	c.Text(b.Text, x+buttonPadX, y+buttonPadY, buttonTextSz, textCol, font.Normal, gfx.AlignLeft)

	clicked := false
	if enabled {
		clicked = b.Clickable.Clicked(c.Gtx)
		c.HitRegion(&b.Clickable, x, y, w, h)
	}
	return clicked
}
