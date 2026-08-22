// Package theme define la paleta de colores claro/oscuro, replicando
// lib/theme.rb del proyecto Ruby.
package theme

import (
	"image/color"
)

// Mode es el modo de tema elegido por el usuario.
type Mode int

const (
	System Mode = iota
	Light
	Dark
)

// Colors es la paleta usada para dibujar el lienzo.
type Colors struct {
	Bg           color.NRGBA
	TextH        color.NRGBA
	Muted        color.NRGBA
	Label        color.NRGBA
	Gridline     color.NRGBA
	GridlineZero color.NRGBA
	MarkerLine   color.NRGBA
	AxisLabel    color.NRGBA
	LegendDim    color.NRGBA
}

// HexColor convierte un color hex "#rrggbb" (como aspects.Aspect.Color) a
// color.NRGBA con alfa opaco.
func HexColor(s string) color.NRGBA {
	return hex(s)
}

func hex(s string) color.NRGBA {
	if len(s) != 7 || s[0] != '#' {
		return color.NRGBA{A: 255}
	}
	return color.NRGBA{R: hexByte(s[1:3]), G: hexByte(s[3:5]), B: hexByte(s[5:7]), A: 255}
}

func hexByte(s string) uint8 {
	hi := hexNibble(s[0])
	lo := hexNibble(s[1])
	return hi<<4 | lo
}

func hexNibble(c byte) uint8 {
	switch {
	case c >= '0' && c <= '9':
		return c - '0'
	case c >= 'a' && c <= 'f':
		return c - 'a' + 10
	case c >= 'A' && c <= 'F':
		return c - 'A' + 10
	}
	return 0
}

var lightColors = Colors{
	Bg:           hex("#ffffff"),
	TextH:        hex("#1a1a1a"),
	Muted:        hex("#555555"),
	Label:        hex("#333333"),
	Gridline:     hex("#e5e4e7"),
	GridlineZero: hex("#b8b6bd"),
	MarkerLine:   hex("#999999"),
	AxisLabel:    hex("#777777"),
	LegendDim:    hex("#aaaaaa"),
}

var darkColors = Colors{
	Bg:           hex("#16171d"),
	TextH:        hex("#f3f4f6"),
	Muted:        hex("#9ca3af"),
	Label:        hex("#d1d5db"),
	Gridline:     hex("#2e303a"),
	GridlineZero: hex("#4b4d59"),
	MarkerLine:   hex("#6b6d78"),
	AxisLabel:    hex("#9ca3af"),
	LegendDim:    hex("#6b6d78"),
}

// White es el color usado para el anillo del marcador de valor actual.
var White = hex("#ffffff")

// For devuelve la paleta a usar para el modo indicado, consultando el tema
// del sistema operativo cuando mode es System.
func For(mode Mode) Colors {
	switch mode {
	case Dark:
		return darkColors
	case Light:
		return lightColors
	default:
		if systemIsDark() {
			return darkColors
		}
		return lightColors
	}
}
