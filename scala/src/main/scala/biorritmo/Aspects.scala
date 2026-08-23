package biorritmo

// Define los siete aspectos del biorritmo, replicando lib/aspects.rb del
// proyecto Ruby (mismos periodos, colores y trazos).

case class Aspect(key: String, label: String, period: Double, color: String, dash: String, group: String)

object Aspects {
  val all: Seq[Aspect] = Seq(
    Aspect("fisico", "Físico", 23, "#1656c9", "0", "basico"),
    Aspect("emocional", "Emocional", 28, "#d32f2f", "0", "basico"),
    Aspect("intelectual", "Intelectual", 33, "#1f9254", "0", "basico"),
    Aspect("espiritual", "Espiritual", 53, "#7c3aed", "7 4", "complementario"),
    Aspect("conciencia", "Conciencia", 48, "#0891b2", "1 4", "complementario"),
    Aspect("intuicion", "Intuición", 38, "#d97706", "9 3 2 3", "complementario"),
    Aspect("estetica", "Estética", 43, "#a3195b", "3 3", "complementario"),
  )

  def basico: Seq[Aspect] = all.filter(_.group == "basico")
  def complementario: Seq[Aspect] = all.filter(_.group == "complementario")
}
