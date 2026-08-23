// Package biorhythm calcula las series del biorritmo, replicando
// lib/biorhythm.rb del proyecto Ruby.
package biorhythm

import (
	"fmt"
	"math"
	"time"

	"biorritmo/internal/aspects"
)

const (
	RangeDays   = 15
	ChartWidth  = 760.0
	ChartHeight = 340.0
)

// Margin son los márgenes del área de gráfica (igual que MARGIN en Ruby).
var Margin = struct{ Top, Right, Bottom, Left float64 }{Top: 16, Right: 16, Bottom: 34, Left: 44}

var (
	PlotWidth  = ChartWidth - Margin.Left - Margin.Right
	PlotHeight = ChartHeight - Margin.Top - Margin.Bottom
)

var monthsEs = [12]string{"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"}

// DaysBetween es el número de días (con signo) entre dos fechas.
func DaysBetween(from, to time.Time) int {
	return int(to.Sub(from).Hours() / 24)
}

// ValueAt es el valor del ciclo (−100..100) en el día indicado.
func ValueAt(daysSinceBirth int, period float64) float64 {
	return math.Sin(2*math.Pi*float64(daysSinceBirth)/period) * 100
}

// PhaseLabel describe la fase del ciclo: Crítico, Ascendente o Descendente.
func PhaseLabel(value, nextValue float64) string {
	if math.Abs(value) < 3 {
		return "Crítico"
	}
	if nextValue > value {
		return "Ascendente"
	}
	return "Descendente"
}

// FormatShort da formato "DD mes" en español, p.ej. "05 ago".
func FormatShort(date time.Time) string {
	return fmt.Sprintf("%02d %s", date.Day(), monthsEs[date.Month()-1])
}

// Gridline es una línea horizontal de referencia (100, 50, 0, −50, −100).
type Gridline struct {
	Value int
	Y     float64
	Zero  bool
}

// DateLabel es una etiqueta de fecha bajo el eje horizontal.
type DateLabel struct {
	X     float64
	Label string
}

// Line es la serie de un aspecto ya proyectada a coordenadas de gráfica.
type Line struct {
	Key, Label, Color, Dash, Group string
	Points                         [][2]float64
	CurrentValue                   int
	Status                         string
	MarkerX, MarkerY               float64
}

// Series contiene todo lo necesario para dibujar la gráfica y la leyenda.
type Series struct {
	Gridlines   []Gridline
	CenterX     float64
	MarkerLabel string
	DateLabels  []DateLabel
	Lines       []Line
	IsToday     bool
}

func round(v float64) int {
	if v >= 0 {
		return int(v + 0.5)
	}
	return int(v - 0.5)
}

// Compute calcula la serie completa para la fecha de nacimiento y la fecha
// seleccionada, igual que Biorhythm.series en Ruby.
func Compute(birthDate, selectedDate time.Time) Series {
	offsets := make([]int, 0, 2*RangeDays+1)
	for o := -RangeDays; o <= RangeDays; o++ {
		offsets = append(offsets, o)
	}

	dates := make([]time.Time, len(offsets))
	daysSinceBirth := make([]int, len(offsets))
	for i, o := range offsets {
		dates[i] = selectedDate.AddDate(0, 0, o)
		daysSinceBirth[i] = DaysBetween(birthDate, dates[i])
	}

	xScale := func(index int) float64 {
		return Margin.Left + (float64(index)/float64(len(offsets)-1))*PlotWidth
	}
	yScale := func(val float64) float64 {
		return Margin.Top + (1-(val+100)/200.0)*PlotHeight
	}

	lines := make([]Line, 0, len(aspects.List))
	for _, aspect := range aspects.List {
		values := make([]float64, len(offsets))
		points := make([][2]float64, len(offsets))
		for i, days := range daysSinceBirth {
			v := ValueAt(days, aspect.Period)
			values[i] = v
			points[i] = [2]float64{xScale(i), yScale(v)}
		}
		currentValue := values[RangeDays]
		nextValue := ValueAt(daysSinceBirth[RangeDays]+1, aspect.Period)

		lines = append(lines, Line{
			Key:          aspect.Key,
			Label:        aspect.Label,
			Color:        aspect.Color,
			Dash:         aspect.Dash,
			Group:        aspect.Group,
			Points:       points,
			CurrentValue: round(currentValue),
			Status:       PhaseLabel(currentValue, nextValue),
			MarkerX:      xScale(RangeDays),
			MarkerY:      yScale(currentValue),
		})
	}

	dateLabels := make([]DateLabel, 0)
	for i, o := range offsets {
		if o%5 == 0 {
			dateLabels = append(dateLabels, DateLabel{X: xScale(i), Label: FormatShort(dates[i])})
		}
	}

	gridlines := make([]Gridline, 0, 5)
	for _, val := range []int{100, 50, 0, -50, -100} {
		gridlines = append(gridlines, Gridline{Value: val, Y: yScale(float64(val)), Zero: val == 0})
	}

	today := time.Now()
	isToday := sameDate(selectedDate, today)
	markerLabel := FormatShort(selectedDate)
	if isToday {
		markerLabel = "Hoy"
	}

	return Series{
		Gridlines:   gridlines,
		CenterX:     xScale(RangeDays),
		MarkerLabel: markerLabel,
		DateLabels:  dateLabels,
		Lines:       lines,
		IsToday:     isToday,
	}
}

func sameDate(a, b time.Time) bool {
	ay, am, ad := a.Date()
	by, bm, bd := b.Date()
	return ay == by && am == bm && ad == bd
}
