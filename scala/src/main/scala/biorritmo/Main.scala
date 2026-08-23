package biorritmo

// Calculadora de biorritmo -- aplicación de escritorio en Scala que replica
// la funcionalidad y apariencia del proyecto Ruby equivalente (../ruby):
// calcula y grafica los ciclos físico, emocional e intelectual, junto con
// los aspectos complementarios (espiritual, conciencia, intuición y
// estética), a partir de una fecha de nacimiento.

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane
import javafx.stage.Stage

import biorritmo.ui.App

class Main extends Application {
  override def start(stage: Stage): Unit = {
    val canvas = new Canvas(900, 840)
    val root = new Pane(canvas)
    canvas.widthProperty().bind(root.widthProperty())
    canvas.heightProperty().bind(root.heightProperty())

    val app = new App(canvas)
    canvas.widthProperty().addListener((_, _, _) => app.draw())
    canvas.heightProperty().addListener((_, _, _) => app.draw())

    val scene = new Scene(root, 900, 840)
    stage.setTitle("Calculadora de biorritmo")
    stage.setScene(scene)
    stage.show()

    app.draw()
  }
}

// Lanzador separado: si el método `main` viviera en la propia clase `Main`
// (que extiende Application), el manifest del jar apuntaría a una clase que
// hereda de javafx.application.Application, y `java -jar` exige entonces que
// JavaFX esté en el module-path en vez de en el classpath del jar sombreado,
// fallando con "faltan los componentes de JavaFX runtime" aunque las
// dependencias sí estén empaquetadas. Al mantener el `main` en una clase
// aparte que no extiende Application, ese chequeo no se activa.
object Launcher {
  def main(args: Array[String]): Unit = Application.launch(classOf[Main], args*)
}
