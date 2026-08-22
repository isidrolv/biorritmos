package gfx

import (
	"os"
	"path/filepath"

	"gioui.org/font"
	"gioui.org/font/gofont"
	"gioui.org/font/opentype"
	"gioui.org/text"
)

// Fonts agrupa el shaper de texto de Gio y la tipografía preferida.
type Fonts struct {
	Shaper *text.Shaper
	Face   font.Typeface
}

const segoeUI font.Typeface = "Segoe UI"

// LoadFonts intenta cargar Segoe UI (la misma tipografía que usa el
// proyecto Ruby vía NativeDraw::FONT_FAMILY) directamente de
// C:\Windows\Fonts. Si no la encuentra, recurre a la tipografía Go
// incluida en Gio.
func LoadFonts() *Fonts {
	var faces []font.FontFace
	if f, ok := loadFace(font.Normal, "segoeui.ttf"); ok {
		faces = append(faces, f)
	}
	if f, ok := loadFace(font.Medium, "seguisb.ttf"); ok {
		faces = append(faces, f)
	}
	if f, ok := loadFace(font.Bold, "segoeuib.ttf"); ok {
		faces = append(faces, f)
	}

	face := segoeUI
	if len(faces) == 0 {
		faces = gofont.Collection()
		face = ""
	}

	return &Fonts{
		Shaper: text.NewShaper(text.WithCollection(faces)),
		Face:   face,
	}
}

func loadFace(weight font.Weight, filename string) (font.FontFace, bool) {
	winDir := os.Getenv("WINDIR")
	if winDir == "" {
		winDir = `C:\Windows`
	}
	data, err := os.ReadFile(filepath.Join(winDir, "Fonts", filename))
	if err != nil {
		return font.FontFace{}, false
	}
	face, err := opentype.Parse(data)
	if err != nil {
		return font.FontFace{}, false
	}
	return font.FontFace{
		Font: font.Font{Typeface: segoeUI, Weight: weight},
		Face: face,
	}, true
}
