// Calculadora de biorritmo -- aplicación de escritorio en Go que replica
// la funcionalidad y apariencia del proyecto Ruby equivalente (../ruby):
// calcula y grafica los ciclos físico, emocional e intelectual, junto con
// los aspectos complementarios (espiritual, conciencia, intuición y
// estética), a partir de una fecha de nacimiento.
package main

import (
	"os"

	"gioui.org/app"
	"gioui.org/op"

	"biorritmo/internal/ui"
)

func main() {
	go func() {
		w := new(app.Window)
		w.Option(app.Title("Calculadora de biorritmo"), app.Size(900, 840))
		if err := run(w); err != nil {
			os.Exit(1)
		}
		os.Exit(0)
	}()
	app.Main()
}

func run(w *app.Window) error {
	a := ui.NewApp()
	var ops op.Ops
	for {
		switch e := w.Event().(type) {
		case app.DestroyEvent:
			return e.Err
		case app.FrameEvent:
			gtx := app.NewContext(&ops, e)
			a.Layout(gtx)
			e.Frame(gtx.Ops)
		}
	}
}
