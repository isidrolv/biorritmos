// Package aspects define los siete aspectos del biorritmo, replicando
// lib/aspects.rb del proyecto Ruby (mismos periodos, colores y trazos).
package aspects

// Aspect describe un aspecto del biorritmo.
type Aspect struct {
	Key    string
	Label  string
	Period float64
	Color  string // hex, p.ej. "#1656c9"
	Dash   string // patrón de trazo, p.ej. "7 4" ("0" = sólido)
	Group  string // "basico" o "complementario"
}

// List contiene los siete aspectos en el mismo orden que el proyecto Ruby.
var List = []Aspect{
	{Key: "fisico", Label: "Físico", Period: 23, Color: "#1656c9", Dash: "0", Group: "basico"},
	{Key: "emocional", Label: "Emocional", Period: 28, Color: "#d32f2f", Dash: "0", Group: "basico"},
	{Key: "intelectual", Label: "Intelectual", Period: 33, Color: "#1f9254", Dash: "0", Group: "basico"},
	{Key: "espiritual", Label: "Espiritual", Period: 53, Color: "#7c3aed", Dash: "7 4", Group: "complementario"},
	{Key: "conciencia", Label: "Conciencia", Period: 48, Color: "#0891b2", Dash: "1 4", Group: "complementario"},
	{Key: "intuicion", Label: "Intuición", Period: 38, Color: "#d97706", Dash: "9 3 2 3", Group: "complementario"},
	{Key: "estetica", Label: "Estética", Period: 43, Color: "#a3195b", Dash: "3 3", Group: "complementario"},
}

// Basico son los tres aspectos básicos (físico, emocional, intelectual).
func Basico() []Aspect { return filterGroup("basico") }

// Complementario son los cuatro aspectos complementarios.
func Complementario() []Aspect { return filterGroup("complementario") }

func filterGroup(group string) []Aspect {
	out := make([]Aspect, 0, len(List))
	for _, a := range List {
		if a.Group == group {
			out = append(out, a)
		}
	}
	return out
}
