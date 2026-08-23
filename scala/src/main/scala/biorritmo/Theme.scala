package biorritmo

import javafx.scene.paint.Color

// Define la paleta de colores claro/oscuro, replicando lib/theme.rb del
// proyecto Ruby.

enum Mode:
  case System, Light, Dark

case class Colors(
  bg: Color,
  textH: Color,
  muted: Color,
  label: Color,
  gridline: Color,
  gridlineZero: Color,
  markerLine: Color,
  axisLabel: Color,
  legendDim: Color,
)

object Theme {
  def hex(s: String): Color = Color.web(s)

  val White: Color = hex("#ffffff")

  val Light: Colors = Colors(
    bg = hex("#ffffff"),
    textH = hex("#1a1a1a"),
    muted = hex("#555555"),
    label = hex("#333333"),
    gridline = hex("#e5e4e7"),
    gridlineZero = hex("#b8b6bd"),
    markerLine = hex("#999999"),
    axisLabel = hex("#777777"),
    legendDim = hex("#aaaaaa"),
  )

  val Dark: Colors = Colors(
    bg = hex("#16171d"),
    textH = hex("#f3f4f6"),
    muted = hex("#9ca3af"),
    label = hex("#d1d5db"),
    gridline = hex("#2e303a"),
    gridlineZero = hex("#4b4d59"),
    markerLine = hex("#6b6d78"),
    axisLabel = hex("#9ca3af"),
    legendDim = hex("#6b6d78"),
  )

  // Consulta el registro de Windows (mismo valor que lib/theme.rb via
  // win32/registry) para saber si el tema del sistema es oscuro.
  def systemIsDark: Boolean =
    try {
      val pb = new ProcessBuilder(
        "reg", "query",
        "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
        "/v", "AppsUseLightTheme",
      )
      pb.redirectErrorStream(true)
      val proc = pb.start()
      val output = new String(proc.getInputStream.readAllBytes())
      proc.waitFor()
      "0x([0-9a-fA-F]+)".r.findFirstMatchIn(output).exists(m => Integer.parseInt(m.group(1), 16) == 0)
    } catch {
      case _: Throwable => false
    }

  def colors(mode: Mode): Colors = mode match {
    case Mode.Dark   => Dark
    case Mode.Light  => Light
    case Mode.System => if (systemIsDark) Dark else Light
  }
}
