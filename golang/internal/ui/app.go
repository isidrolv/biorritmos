// Package ui implementa la ventana de la aplicación, replicando app.rb
// del proyecto Ruby: una barra de tema, una fila de controles (fechas y
// navegación) y un lienzo con el encabezado, la gráfica y la leyenda.
package ui

import (
	"fmt"
	"image"
	"time"

	"gioui.org/font"
	"gioui.org/layout"
	"gioui.org/op/clip"
	"gioui.org/op/paint"
	"gioui.org/unit"
	"gioui.org/widget"

	"biorritmo/internal/aspects"
	"biorritmo/internal/biorhythm"
	"biorritmo/internal/gfx"
	"biorritmo/internal/theme"
)

const (
	chartWidth   = biorhythm.ChartWidth
	chartHeight  = biorhythm.ChartHeight
	headerHeight = 92.0

	legendWidth    = chartWidth
	legendColWidth = 340.0
	legendColGap   = 40.0
	legendTop      = 12.0
	legendHeaderH  = 22.0
	legendRowH     = 26.0

	canvasGap = 16.0

	windowMargin = 16.0
	rowGap       = 10.0
)

var (
	legendLeft   = (legendWidth - (legendColWidth*2 + legendColGap)) / 2.0
	legendHeight = legendTop + legendHeaderH + 4*legendRowH + 12
	chartY       = headerHeight + canvasGap
	legendY      = chartY + chartHeight + canvasGap
)

type legendGroup struct {
	colX    float64
	title   string
	aspects []aspects.Aspect
}

var legendGroups = []legendGroup{
	{legendLeft, "Aspectos básicos", aspects.Basico()},
	{legendLeft + legendColWidth + legendColGap, "Aspectos complementarios", aspects.Complementario()},
}

// App es el estado completo de la aplicación.
type App struct {
	fonts *gfx.Fonts

	themeMode theme.Mode
	visible   map[string]bool

	birthDate    time.Time
	selectedDate time.Time
	series       biorhythm.Series

	themeButtons [3]Button
	prevBtn      Button
	todayBtn     Button
	nextBtn      Button

	birthField    DateField
	selectedField DateField

	legendClicks map[string]*widget.Clickable
}

// NewApp construye el estado inicial, igual que BiorritmoApp#initialize.
func NewApp() *App {
	today := todayMidnight()
	birthDay := today.Day()
	if birthDay > 28 {
		birthDay = 28
	}
	birth := time.Date(today.Year()-25, today.Month(), birthDay, 0, 0, 0, 0, time.UTC)

	a := &App{
		fonts:        gfx.LoadFonts(),
		themeMode:    theme.System,
		visible:      make(map[string]bool, len(aspects.List)),
		birthDate:    birth,
		selectedDate: today,
		legendClicks: make(map[string]*widget.Clickable, len(aspects.List)),
	}
	for _, asp := range aspects.List {
		a.visible[asp.Key] = true
		a.legendClicks[asp.Key] = new(widget.Clickable)
	}
	a.themeButtons = [3]Button{{Text: "Sistema"}, {Text: "Claro"}, {Text: "Oscuro"}}
	a.prevBtn = Button{Text: "< Anterior"}
	a.todayBtn = Button{Text: "Hoy"}
	a.nextBtn = Button{Text: "Siguiente >"}
	a.recalc()
	return a
}

func (a *App) recalc() {
	a.series = biorhythm.Compute(a.birthDate, a.selectedDate)
}

func todayMidnight() time.Time {
	t := time.Now()
	return time.Date(t.Year(), t.Month(), t.Day(), 0, 0, 0, 0, time.UTC)
}

func unitDp(v float64) unit.Dp { return unit.Dp(v) }

func sameDate(a, b time.Time) bool {
	ay, am, ad := a.Date()
	by, bm, bd := b.Date()
	return ay == by && am == bm && ad == bd
}

// Layout dibuja toda la ventana para este frame.
func (a *App) Layout(gtx layout.Context) layout.Dimensions {
	colors := theme.For(a.themeMode)
	paint.FillShape(gtx.Ops, colors.Bg, clip.Rect(image.Rectangle{Max: gtx.Constraints.Max}).Op())

	return layout.Flex{Axis: layout.Vertical}.Layout(gtx,
		layout.Rigid(func(gtx layout.Context) layout.Dimensions {
			return layout.Inset{Left: unitDp(windowMargin), Right: unitDp(windowMargin), Top: unitDp(windowMargin)}.
				Layout(gtx, func(gtx layout.Context) layout.Dimensions {
					return a.layoutTopbar(gtx, colors)
				})
		}),
		layout.Rigid(layout.Spacer{Height: unitDp(rowGap)}.Layout),
		layout.Rigid(func(gtx layout.Context) layout.Dimensions {
			return layout.Inset{Left: unitDp(windowMargin), Right: unitDp(windowMargin)}.
				Layout(gtx, func(gtx layout.Context) layout.Dimensions {
					return a.layoutControls(gtx, colors)
				})
		}),
		layout.Rigid(layout.Spacer{Height: unitDp(rowGap)}.Layout),
		layout.Flexed(1, func(gtx layout.Context) layout.Dimensions {
			return a.layoutCanvas(gtx, colors)
		}),
	)
}

func (a *App) layoutTopbar(gtx layout.Context, colors theme.Colors) layout.Dimensions {
	c := gfx.New(gtx, a.fonts)
	widthPx := gtx.Constraints.Max.X
	width := float64(widthPx) / float64(c.Scale)

	label := "Tema:"
	lw, lh := c.MeasureText(label, 13, font.Normal)

	const gap = 8.0
	btnW := make([]float64, len(a.themeButtons))
	btnH := 0.0
	for i := range a.themeButtons {
		w, h := a.themeButtons[i].Measure(c)
		btnW[i] = w
		btnH = max(btnH, h)
	}

	contentW := lw + gap
	for _, w := range btnW {
		contentW += w + gap
	}
	rowH := max(lh, btnH)
	startX := width - contentW
	if startX < 0 {
		startX = 0
	}

	cx := startX
	c.Text(label, cx, (rowH-lh)/2, 13, colors.TextH, font.Normal, gfx.AlignLeft)
	cx += lw + gap

	for i := range a.themeButtons {
		selected := int(a.themeMode) == i
		if a.themeButtons[i].Layout(c, cx, (rowH-btnH)/2, colors, selected, true) {
			a.themeMode = theme.Mode(i)
		}
		cx += btnW[i] + gap
	}

	return layout.Dimensions{Size: image.Pt(widthPx, int(c.Px(rowH)))}
}

func (a *App) layoutControls(gtx layout.Context, colors theme.Colors) layout.Dimensions {
	c := gfx.New(gtx, a.fonts)
	widthPx := gtx.Constraints.Max.X
	width := float64(widthPx) / float64(c.Scale)

	const (
		labelSz    = 12.0
		fieldGap   = 4.0
		colGap     = 28.0
		navBtnGap  = 6.0
	)

	birthLabel := "Fecha de nacimiento"
	selLabel := "Fecha a analizar"
	birthLabelW, labelH := c.MeasureText(birthLabel, labelSz, font.Normal)
	selLabelW, _ := c.MeasureText(selLabel, labelSz, font.Normal)

	col1W := max(birthLabelW, a.birthField.Width())
	col2W := max(selLabelW, a.selectedField.Width())

	prevW, prevH := a.prevBtn.Measure(c)
	todayW, todayH := a.todayBtn.Measure(c)
	nextW, nextH := a.nextBtn.Measure(c)
	navW := prevW + navBtnGap + todayW + navBtnGap + nextW
	navH := max(prevH, max(todayH, nextH))
	col3W := navW

	contentW := col1W + colGap + col2W + colGap + col3W
	startX := (width - contentW) / 2
	if startX < 0 {
		startX = 0
	}

	fieldH := a.birthField.Height()
	rowH := labelH + fieldGap + max(fieldH, navH)
	const vPad = 8.0

	labelY := vPad
	fieldY := labelY + labelH + fieldGap
	navY := fieldY + (fieldH-navH)/2

	cx := startX
	c.Text(birthLabel, cx, labelY, labelSz, colors.Label, font.Normal, gfx.AlignLeft)
	newBirth, birthChanged := a.birthField.Layout(c, cx, fieldY, a.birthDate, colors)
	cx += col1W + colGap

	c.Text(selLabel, cx, labelY, labelSz, colors.Label, font.Normal, gfx.AlignLeft)
	newSelected, selChanged := a.selectedField.Layout(c, cx, fieldY, a.selectedDate, colors)
	cx += col2W + colGap

	nx := cx
	prevClicked := a.prevBtn.Layout(c, nx, navY, colors, false, true)
	nx += prevW + navBtnGap
	todayEnabled := !sameDate(a.selectedDate, todayMidnight())
	todayClicked := a.todayBtn.Layout(c, nx, navY, colors, false, todayEnabled)
	nx += todayW + navBtnGap
	nextClicked := a.nextBtn.Layout(c, nx, navY, colors, false, true)

	changed := false
	if birthChanged {
		a.birthDate = newBirth
		changed = true
	}
	if selChanged {
		a.selectedDate = newSelected
		changed = true
	}
	if prevClicked {
		a.selectedDate = addDays(a.selectedDate, -1)
		changed = true
	}
	if todayClicked && todayEnabled {
		a.selectedDate = todayMidnight()
		changed = true
	}
	if nextClicked {
		a.selectedDate = addDays(a.selectedDate, 1)
		changed = true
	}
	if changed {
		a.recalc()
	}

	return layout.Dimensions{Size: image.Pt(widthPx, int(c.Px(rowH+2*vPad)))}
}

func (a *App) layoutCanvas(gtx layout.Context, colors theme.Colors) layout.Dimensions {
	c := gfx.New(gtx, a.fonts)
	widthPx := gtx.Constraints.Max.X
	heightPx := gtx.Constraints.Max.Y
	width := float64(widthPx) / float64(c.Scale)
	height := float64(heightPx) / float64(c.Scale)

	c.FillRect(0, 0, width, height, colors.Bg)

	ox := max((width-chartWidth)/2.0, 0)

	a.drawHeader(c, ox, 0, colors)
	a.drawChart(c, ox, chartY, colors)
	a.drawLegend(c, ox, legendY, colors)

	return layout.Dimensions{Size: image.Pt(widthPx, heightPx)}
}

func (a *App) drawHeader(c *gfx.Canvas, ox, oy float64, colors theme.Colors) {
	titleH := c.Paragraph("Calculadora de biorritmo", ox, oy+6, chartWidth, 22, colors.TextH, font.Medium)

	subtitle := "Ingresa tu fecha de nacimiento para graficar tus ciclos físico, emocional e intelectual, " +
		"junto con los aspectos complementarios: espiritual, conciencia, intuición y estética."
	c.Paragraph(subtitle, ox, oy+6+titleH+6, chartWidth, 12, colors.Muted, font.Normal)
}

func (a *App) drawChart(c *gfx.Canvas, ox, oy float64, colors theme.Colors) {
	s := a.series
	left := ox + biorhythm.Margin.Left
	rightEdge := ox + chartWidth - biorhythm.Margin.Right
	bottom := oy + chartHeight - biorhythm.Margin.Bottom
	top := oy + biorhythm.Margin.Top

	for _, g := range s.Gridlines {
		col := colors.Gridline
		if g.Zero {
			col = colors.GridlineZero
		}
		c.StrokeLine(left, oy+g.Y, rightEdge, oy+g.Y, col, 1, nil)
		c.Text(fmt.Sprintf("%d", g.Value), left-8, oy+g.Y-6, 11, colors.AxisLabel, font.Normal, gfx.AlignRight)
	}

	cx := ox + s.CenterX
	c.StrokeLine(cx, top, cx, bottom, colors.MarkerLine, 1, gfx.ParseDash("3 3"))
	c.Text(s.MarkerLabel, cx, top-18, 12, colors.Label, font.Bold, gfx.AlignCenter)

	for _, dl := range s.DateLabels {
		c.Text(dl.Label, ox+dl.X, bottom+6, 11, colors.AxisLabel, font.Normal, gfx.AlignCenter)
	}

	for _, line := range s.Lines {
		if !a.visible[line.Key] {
			continue
		}
		col := theme.HexColor(line.Color)
		dash := gfx.ParseDash(line.Dash)

		points := make([][2]float64, len(line.Points))
		for i, p := range line.Points {
			points[i] = [2]float64{ox + p[0], oy + p[1]}
		}
		c.StrokePolyline(points, col, 2, dash)

		mx, my := ox+line.MarkerX, oy+line.MarkerY
		c.RingCircle(mx, my, 5, 2, theme.White, col)
	}
}

func (a *App) drawLegend(c *gfx.Canvas, ox, oy float64, colors theme.Colors) {
	valuesByKey := make(map[string]biorhythm.Line, len(a.series.Lines))
	for _, l := range a.series.Lines {
		valuesByKey[l.Key] = l
	}

	for _, group := range legendGroups {
		c.Text(group.title, ox+group.colX, oy+legendTop, 14, colors.Label, font.Bold, gfx.AlignLeft)

		for i, asp := range group.aspects {
			rowY := oy + legendTop + legendHeaderH + float64(i)*legendRowH
			a.drawLegendRow(c, ox+group.colX, rowY, asp, valuesByKey[asp.Key], colors)

			cl := a.legendClicks[asp.Key]
			if cl.Clicked(c.Gtx) {
				a.visible[asp.Key] = !a.visible[asp.Key]
			}
			c.HitRegion(cl, ox+group.colX, rowY, legendColWidth, legendRowH)
		}
	}
}

func (a *App) drawLegendRow(c *gfx.Canvas, colX, rowY float64, asp aspects.Aspect, line biorhythm.Line, colors theme.Colors) {
	visible := a.visible[asp.Key]
	cy := rowY + legendRowH/2.0
	col := theme.HexColor(asp.Color)

	if visible {
		c.FillCircle(colX+8, cy, 6, col)
	} else {
		c.RingCircle(colX+8, cy, 6, 2, col, colors.Bg)
	}

	textCol := colors.TextH
	if !visible {
		textCol = colors.LegendDim
	}

	c.Text(asp.Label, colX+24, rowY+4, 13, textCol, font.Normal, gfx.AlignLeft)
	c.Text(fmt.Sprintf("%d%%", line.CurrentValue), colX+legendColWidth-90, rowY+4, 13, textCol, font.Normal, gfx.AlignRight)
	c.Text(line.Status, colX+legendColWidth, rowY+4, 12, colors.AxisLabel, font.Normal, gfx.AlignRight)
}
