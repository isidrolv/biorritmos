// Package gfx implementa primitivas de dibujo sobre Gio equivalentes a
// lib/native_draw.rb (NativeDraw) del proyecto Ruby: rectángulos, líneas
// (con o sin patrón de trazo), círculos/anillos y texto alineado.
//
// Todas las coordenadas que reciben los métodos de Canvas son "unidades
// lógicas" -- los mismos números que usa el proyecto Ruby (p.ej. un
// gráfico de 760x340) -- y se escalan internamente a píxeles de
// dispositivo según el factor de esa ventana (gtx.Metric.PxPerDp), de
// forma que la app respeta el escalado de Windows igual que libui.
package gfx

import (
	"image"
	"image/color"
	"math"
	"strconv"
	"strings"

	"gioui.org/f32"
	"gioui.org/font"
	"gioui.org/layout"
	"gioui.org/op"
	"gioui.org/op/clip"
	"gioui.org/op/paint"
	"gioui.org/text"
	"gioui.org/unit"
	"gioui.org/widget"
)

// Align es la alineación horizontal del texto respecto al punto (x, y).
type Align int

const (
	AlignLeft Align = iota
	AlignCenter
	AlignRight
)

// Canvas envuelve un layout.Context para dibujar en coordenadas lógicas.
type Canvas struct {
	Gtx   layout.Context
	Fonts *Fonts
	Scale float32
}

// New crea un Canvas para el frame actual.
func New(gtx layout.Context, fonts *Fonts) *Canvas {
	return &Canvas{Gtx: gtx, Fonts: fonts, Scale: gtx.Metric.PxPerDp}
}

func (c *Canvas) ops() *op.Ops { return c.Gtx.Ops }

// Px convierte una coordenada lógica (misma unidad que usa el proyecto
// Ruby) a píxeles de dispositivo para este frame.
func (c *Canvas) Px(v float64) float32 { return float32(v) * c.Scale }

func (c *Canvas) px(v float64) float32 { return c.Px(v) }

// spFor calcula el tamaño en unit.Sp necesario para que, tras la
// conversión interna de Gio (gtx.Sp), el texto ocupe exactamente
// pxSize píxeles de dispositivo -- igual que el resto de la geometría.
func (c *Canvas) spFor(pxSize float32) unit.Sp {
	perSp := c.Gtx.Metric.PxPerSp
	if perSp == 0 {
		perSp = 1
	}
	return unit.Sp(pxSize / perSp)
}

// ParseDash convierte un patrón "7 4" (como en aspects.Aspect.Dash) a la
// lista de longitudes usada por StrokeLine/StrokePolyline. "0" o "" es
// una línea sólida (sin patrón).
func ParseDash(s string) []float64 {
	s = strings.TrimSpace(s)
	if s == "" || s == "0" {
		return nil
	}
	fields := strings.Fields(s)
	out := make([]float64, 0, len(fields))
	for _, f := range fields {
		v, err := strconv.ParseFloat(f, 64)
		if err != nil {
			continue
		}
		out = append(out, v)
	}
	return out
}

// FillRect rellena un rectángulo.
func (c *Canvas) FillRect(x, y, w, h float64, col color.NRGBA) {
	r := clip.Rect(image.Rect(int(c.px(x)), int(c.px(y)), int(c.px(x+w)), int(c.px(y+h))))
	paint.FillShape(c.ops(), col, r.Op())
}

// FillCircle rellena un círculo de radio r centrado en (cx, cy).
func (c *Canvas) FillCircle(cx, cy, r float64, col color.NRGBA) {
	x0, y0 := int(c.px(cx-r)), int(c.px(cy-r))
	x1, y1 := int(c.px(cx+r)), int(c.px(cy+r))
	e := clip.Ellipse{Min: image.Pt(x0, y0), Max: image.Pt(x1, y1)}
	paint.FillShape(c.ops(), col, e.Op(c.ops()))
}

// RingCircle dibuja un anillo (círculo con centro hueco) de radio r y
// grosor thickness, replicando NativeDraw.stroke_circle: se logra
// rellenando un círculo exterior con ringColor y tapando el centro con
// holeColor (el color de fondo sobre el que se dibuja el anillo).
func (c *Canvas) RingCircle(cx, cy, r, thickness float64, ringColor, holeColor color.NRGBA) {
	c.FillCircle(cx, cy, r+thickness/2, ringColor)
	c.FillCircle(cx, cy, r-thickness/2, holeColor)
}

func (c *Canvas) strokeSegPx(a, b f32.Point, col color.NRGBA, thicknessPx float32) {
	var p clip.Path
	p.Begin(c.ops())
	p.MoveTo(a)
	p.LineTo(b)
	spec := p.End()
	paint.FillShape(c.ops(), col, clip.Stroke{Path: spec, Width: thicknessPx}.Op())
}

// StrokeLine dibuja una línea recta, opcionalmente punteada.
func (c *Canvas) StrokeLine(x1, y1, x2, y2 float64, col color.NRGBA, thickness float64, dash []float64) {
	c.StrokePolyline([][2]float64{{x1, y1}, {x2, y2}}, col, thickness, dash)
}

// StrokePolyline dibuja una polilínea, opcionalmente punteada según
// dash (longitudes alternas trazo/hueco, en unidades lógicas).
func (c *Canvas) StrokePolyline(pts [][2]float64, col color.NRGBA, thickness float64, dash []float64) {
	if len(pts) < 2 {
		return
	}
	fpts := make([]f32.Point, len(pts))
	for i, p := range pts {
		fpts[i] = f32.Pt(c.px(p[0]), c.px(p[1]))
	}
	thicknessPx := c.px(thickness)

	if len(dash) == 0 {
		var p clip.Path
		p.Begin(c.ops())
		p.MoveTo(fpts[0])
		for _, pt := range fpts[1:] {
			p.LineTo(pt)
		}
		spec := p.End()
		paint.FillShape(c.ops(), col, clip.Stroke{Path: spec, Width: thicknessPx}.Op())
		return
	}

	dashPx := make([]float32, len(dash))
	for i, d := range dash {
		dashPx[i] = c.px(d)
	}

	dashIdx := 0
	on := true
	remaining := dashPx[0]
	for i := 0; i+1 < len(fpts); i++ {
		a, b := fpts[i], fpts[i+1]
		dx, dy := b.X-a.X, b.Y-a.Y
		segLen := float32(math.Hypot(float64(dx), float64(dy)))
		if segLen == 0 {
			continue
		}
		dir := f32.Pt(dx/segLen, dy/segLen)
		pos := float32(0)
		cur := a
		for pos < segLen {
			step := remaining
			if segLen-pos < step {
				step = segLen - pos
			}
			next := f32.Pt(cur.X+dir.X*step, cur.Y+dir.Y*step)
			if on {
				c.strokeSegPx(cur, next, col, thicknessPx)
			}
			cur = next
			pos += step
			remaining -= step
			if remaining <= 0.001 {
				dashIdx = (dashIdx + 1) % len(dashPx)
				remaining = dashPx[dashIdx]
				on = !on
			}
		}
	}
}

// MeasureText devuelve el ancho y alto (en unidades lógicas) que ocuparía
// txt en una sola línea, sin dibujarlo.
func (c *Canvas) MeasureText(txt string, size float64, weight font.Weight) (w, h float64) {
	gtx := c.Gtx
	gtx.Constraints = layout.Constraints{Max: image.Pt(1<<20, 1<<20)}
	fnt := font.Font{Typeface: c.Fonts.Face, Weight: weight}
	sp := c.spFor(c.px(size))

	m := op.Record(c.ops())
	dims := (widget.Label{}).Layout(gtx, c.Fonts.Shaper, fnt, sp, txt, op.CallOp{})
	m.Stop()

	return float64(dims.Size.X) / float64(c.Scale), float64(dims.Size.Y) / float64(c.Scale)
}

// FillPolygon rellena un polígono cerrado (usado para las flechas de los
// selectores de fecha).
func (c *Canvas) FillPolygon(pts [][2]float64, col color.NRGBA) {
	if len(pts) < 3 {
		return
	}
	var p clip.Path
	p.Begin(c.ops())
	p.MoveTo(f32.Pt(c.px(pts[0][0]), c.px(pts[0][1])))
	for _, pt := range pts[1:] {
		p.LineTo(f32.Pt(c.px(pt[0]), c.px(pt[1])))
	}
	p.Close()
	spec := p.End()
	paint.FillShape(c.ops(), col, clip.Outline{Path: spec}.Op())
}

// HitRegion registra una zona clicable en el rectángulo lógico (x, y, w, h)
// asociada al widget.Clickable dado, para que Clicked(gtx) lo detecte en
// frames siguientes. Debe llamarse en cada frame para mantener viva la
// zona interactiva.
func (c *Canvas) HitRegion(cl *widget.Clickable, x, y, w, h float64) {
	size := image.Pt(int(c.px(w)), int(c.px(h)))
	stack := op.Offset(image.Pt(int(c.px(x)), int(c.px(y)))).Push(c.ops())
	cl.Layout(c.Gtx, func(gtx layout.Context) layout.Dimensions {
		return layout.Dimensions{Size: size}
	})
	stack.Pop()
}

func (c *Canvas) textColor(col color.NRGBA) op.CallOp {
	m := op.Record(c.ops())
	paint.ColorOp{Color: col}.Add(c.ops())
	return m.Stop()
}

// Text dibuja una línea de texto en (x, y) -- esquina superior según align.
func (c *Canvas) Text(txt string, x, y, size float64, col color.NRGBA, weight font.Weight, align Align) {
	gtx := c.Gtx
	gtx.Constraints = layout.Constraints{Max: image.Pt(1<<20, 1<<20)}
	fnt := font.Font{Typeface: c.Fonts.Face, Weight: weight}
	sp := c.spFor(c.px(size))
	textMaterial := c.textColor(col)

	m := op.Record(c.ops())
	dims := (widget.Label{}).Layout(gtx, c.Fonts.Shaper, fnt, sp, txt, textMaterial)
	call := m.Stop()

	ox := c.px(x)
	switch align {
	case AlignCenter:
		ox -= float32(dims.Size.X) / 2
	case AlignRight:
		ox -= float32(dims.Size.X)
	}

	stack := op.Offset(image.Pt(int(ox), int(c.px(y)))).Push(c.ops())
	call.Add(c.ops())
	stack.Pop()
}

// Paragraph dibuja un párrafo centrado y ajustado (wrap) dentro de width,
// como NativeDraw.draw_layout. Devuelve la altura ocupada, en unidades
// lógicas, para poder posicionar contenido debajo.
func (c *Canvas) Paragraph(txt string, x, y, width, size float64, col color.NRGBA, weight font.Weight) float64 {
	gtx := c.Gtx
	wpx := int(c.px(width))
	gtx.Constraints = layout.Constraints{Min: image.Pt(wpx, 0), Max: image.Pt(wpx, 1 << 20)}
	fnt := font.Font{Typeface: c.Fonts.Face, Weight: weight}
	sp := c.spFor(c.px(size))
	textMaterial := c.textColor(col)

	m := op.Record(c.ops())
	lbl := widget.Label{Alignment: text.Middle}
	dims := lbl.Layout(gtx, c.Fonts.Shaper, fnt, sp, txt, textMaterial)
	call := m.Stop()

	stack := op.Offset(image.Pt(int(c.px(x)), int(c.px(y)))).Push(c.ops())
	call.Add(c.ops())
	stack.Pop()

	return float64(dims.Size.Y) / float64(c.Scale)
}
