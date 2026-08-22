package biorritmo

import java.awt.Color

enum class ThemeMode(val displayName: String) {
    SYSTEM("Sistema"), LIGHT("Claro"), DARK("Oscuro")
}

data class ThemeColors(
    val background: Color, val heading: Color, val muted: Color, val label: Color,
    val gridline: Color, val zeroGridline: Color, val markerLine: Color,
    val axisLabel: Color, val legendDim: Color,
)

object Themes {
    val light = ThemeColors(
        color("#ffffff"), color("#1a1a1a"), color("#555555"), color("#333333"),
        color("#e5e4e7"), color("#b8b6bd"), color("#999999"), color("#777777"), color("#aaaaaa"),
    )
    val dark = ThemeColors(
        color("#16171d"), color("#f3f4f6"), color("#9ca3af"), color("#d1d5db"),
        color("#2e303a"), color("#4b4d59"), color("#6b6d78"), color("#9ca3af"), color("#6b6d78"),
    )

    fun colors(mode: ThemeMode): ThemeColors = when (mode) {
        ThemeMode.LIGHT -> light
        ThemeMode.DARK -> dark
        ThemeMode.SYSTEM -> if (systemIsDark()) dark else light
    }

    private fun systemIsDark(): Boolean {
        if (!System.getProperty("os.name", "").startsWith("Windows", ignoreCase = true)) return false
        return runCatching {
            val process = ProcessBuilder(
                "reg", "query", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v", "AppsUseLightTheme",
            ).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            Regex("AppsUseLightTheme\\s+REG_DWORD\\s+0x0(?:\\s|$)", RegexOption.IGNORE_CASE).containsMatchIn(output)
        }.getOrDefault(false)
    }

    private fun color(hex: String) = Color.decode(hex)
}
