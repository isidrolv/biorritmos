package ui

import (
	"fmt"
	"time"

	"gioui.org/font"
	"gioui.org/widget"

	"biorritmo/internal/gfx"
	"biorritmo/internal/theme"
)

// DateField es un selector de fecha compacto (día/mes/año), cada unidad
// con flechas arriba/abajo para incrementar o decrementar -- el
// equivalente funcional del uiDatePicker nativo que usa el proyecto Ruby.
type DateField struct {
	dayUp, dayDown     widget.Clickable
	monthUp, monthDown widget.Clickable
	yearUp, yearDown   widget.Clickable
}

const (
	dfBoxH    = 26.0
	dfDayW    = 30.0
	dfMonthW  = 30.0
	dfYearW   = 46.0
	dfArrowW  = 16.0
	dfUnitGap = 3.0
	dfBorder  = 1.0
	dfTextSz  = 13.0
)

// Width es el ancho total que ocupa el control.
func (d *DateField) Width() float64 {
	unit := dfArrowW
	return (dfDayW + unit) + dfUnitGap + (dfMonthW + unit) + dfUnitGap + (dfYearW + unit)
}

// Height es el alto del control.
func (d *DateField) Height() float64 { return dfBoxH }

// Layout dibuja el selector en (x, y) para date y devuelve la fecha
// resultante y si cambió en este frame.
func (d *DateField) Layout(c *gfx.Canvas, x, y float64, date time.Time, colors theme.Colors) (time.Time, bool) {
	yy, mm, dd := date.Date()
	result := date
	changed := false

	cx := x
	if d.dayUp.Clicked(c.Gtx) {
		result = addDays(result, 1)
		changed = true
	}
	if d.dayDown.Clicked(c.Gtx) {
		result = addDays(result, -1)
		changed = true
	}
	d.drawUnit(c, cx, y, dfDayW, fmt.Sprintf("%02d", dd), colors, &d.dayUp, &d.dayDown)
	cx += dfDayW + dfArrowW + dfUnitGap

	if d.monthUp.Clicked(c.Gtx) {
		result = addMonths(result, 1)
		changed = true
	}
	if d.monthDown.Clicked(c.Gtx) {
		result = addMonths(result, -1)
		changed = true
	}
	d.drawUnit(c, cx, y, dfMonthW, fmt.Sprintf("%02d", int(mm)), colors, &d.monthUp, &d.monthDown)
	cx += dfMonthW + dfArrowW + dfUnitGap

	if d.yearUp.Clicked(c.Gtx) {
		result = addYears(result, 1)
		changed = true
	}
	if d.yearDown.Clicked(c.Gtx) {
		result = addYears(result, -1)
		changed = true
	}
	d.drawUnit(c, cx, y, dfYearW, fmt.Sprintf("%04d", yy), colors, &d.yearUp, &d.yearDown)

	return result, changed
}

func (d *DateField) drawUnit(c *gfx.Canvas, x, y, boxW float64, text string, colors theme.Colors, up, down *widget.Clickable) {
	borderCol := colors.MarkerLine

	c.FillRect(x, y, boxW, dfBoxH, colors.Bg)
	c.FillRect(x, y, boxW, dfBorder, borderCol)
	c.FillRect(x, y+dfBoxH-dfBorder, boxW, dfBorder, borderCol)
	c.FillRect(x, y, dfBorder, dfBoxH, borderCol)
	c.FillRect(x+boxW-dfBorder, y, dfBorder, dfBoxH, borderCol)

	tw, th := c.MeasureText(text, dfTextSz, font.Normal)
	c.Text(text, x+(boxW-tw)/2, y+(dfBoxH-th)/2, dfTextSz, colors.TextH, font.Normal, gfx.AlignLeft)

	ax := x + boxW
	arrowH := dfBoxH / 2
	c.FillRect(ax, y, dfArrowW, dfBoxH, colors.Bg)
	c.FillRect(ax, y, dfArrowW, dfBorder, borderCol)
	c.FillRect(ax, y+dfBoxH-dfBorder, dfArrowW, dfBorder, borderCol)
	c.FillRect(ax, y, dfBorder, dfBoxH, borderCol)
	c.FillRect(ax+dfArrowW-dfBorder, y, dfBorder, dfBoxH, borderCol)
	c.FillRect(ax, y+arrowH-dfBorder/2, dfArrowW, dfBorder, borderCol)

	triCol := colors.Label
	mx := ax + dfArrowW/2
	c.FillPolygon([][2]float64{{mx - 4, y + arrowH - 5}, {mx + 4, y + arrowH - 5}, {mx, y + arrowH - 11}}, triCol)
	c.FillPolygon([][2]float64{{mx - 4, y + arrowH + 5}, {mx + 4, y + arrowH + 5}, {mx, y + arrowH + 11}}, triCol)

	c.HitRegion(up, ax, y, dfArrowW, arrowH)
	c.HitRegion(down, ax, y+arrowH, dfArrowW, arrowH)
}

func daysInMonth(y int, m time.Month) int {
	return time.Date(y, m+1, 0, 0, 0, 0, 0, time.UTC).Day()
}

func addDays(d time.Time, delta int) time.Time {
	return d.AddDate(0, 0, delta)
}

func addMonths(d time.Time, delta int) time.Time {
	y, m, day := d.Date()
	total := int(m) - 1 + delta
	y += total / 12
	m2 := total % 12
	if m2 < 0 {
		m2 += 12
		y--
	}
	newMonth := time.Month(m2 + 1)
	if dim := daysInMonth(y, newMonth); day > dim {
		day = dim
	}
	return time.Date(y, newMonth, day, 0, 0, 0, 0, time.UTC)
}

func addYears(d time.Time, delta int) time.Time {
	y, m, day := d.Date()
	y += delta
	if dim := daysInMonth(y, m); day > dim {
		day = dim
	}
	return time.Date(y, m, day, 0, 0, 0, 0, time.UTC)
}
