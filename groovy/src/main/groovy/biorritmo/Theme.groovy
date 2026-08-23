package biorritmo

import java.awt.Color

enum ThemeMode {
    SYSTEM('Sistema'), LIGHT('Claro'), DARK('Oscuro')

    final String displayName
    ThemeMode(String displayName) { this.displayName = displayName }
}

class ThemeColors {
    final Color background
    final Color heading
    final Color muted
    final Color label
    final Color gridline
    final Color zeroGridline
    final Color markerLine
    final Color axisLabel
    final Color legendDim

    ThemeColors(String background, String heading, String muted, String label,
                String gridline, String zeroGridline, String markerLine,
                String axisLabel, String legendDim) {
        this.background = Color.decode(background)
        this.heading = Color.decode(heading)
        this.muted = Color.decode(muted)
        this.label = Color.decode(label)
        this.gridline = Color.decode(gridline)
        this.zeroGridline = Color.decode(zeroGridline)
        this.markerLine = Color.decode(markerLine)
        this.axisLabel = Color.decode(axisLabel)
        this.legendDim = Color.decode(legendDim)
    }
}

class Themes {
    static final ThemeColors LIGHT = new ThemeColors(
        '#ffffff', '#1a1a1a', '#555555', '#333333', '#e5e4e7',
        '#b8b6bd', '#999999', '#777777', '#aaaaaa'
    )
    static final ThemeColors DARK = new ThemeColors(
        '#16171d', '#f3f4f6', '#9ca3af', '#d1d5db', '#2e303a',
        '#4b4d59', '#6b6d78', '#9ca3af', '#6b6d78'
    )

    static ThemeColors colors(ThemeMode mode) {
        switch (mode) {
            case ThemeMode.LIGHT: return LIGHT
            case ThemeMode.DARK: return DARK
            default: return systemIsDark() ? DARK : LIGHT
        }
    }

    private static boolean systemIsDark() {
        if (!System.getProperty('os.name', '').toLowerCase(Locale.ROOT).startsWith('windows')) return false
        try {
            Process process = new ProcessBuilder(
                'reg', 'query', 'HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize',
                '/v', 'AppsUseLightTheme'
            ).redirectErrorStream(true).start()
            String output = process.inputStream.getText('UTF-8')
            process.waitFor()
            output =~ /(?i)AppsUseLightTheme\s+REG_DWORD\s+0x0(?:\s|$)/
        } catch (Exception ignored) {
            false
        }
    }
}
