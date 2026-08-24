"""Paleta y detección del tema de Windows."""

from __future__ import annotations

LIGHT = {
    "bg": "#ffffff", "text_h": "#1a1a1a", "muted": "#555555", "label": "#333333",
    "gridline": "#e5e4e7", "gridline_zero": "#b8b6bd", "marker_line": "#999999",
    "axis_label": "#777777", "legend_dim": "#aaaaaa", "field": "#ffffff",
    "border": "#a9a9a9", "button": "#f5f5f5", "button_active": "#e8e8e8",
}

DARK = {
    "bg": "#16171d", "text_h": "#f3f4f6", "muted": "#9ca3af", "label": "#d1d5db",
    "gridline": "#2e303a", "gridline_zero": "#4b4d59", "marker_line": "#6b6d78",
    "axis_label": "#9ca3af", "legend_dim": "#6b6d78", "field": "#25262e",
    "border": "#555762", "button": "#2d2f38", "button_active": "#393b46",
}


def system_is_dark() -> bool:
    try:
        import winreg

        path = r"Software\Microsoft\Windows\CurrentVersion\Themes\Personalize"
        with winreg.OpenKey(winreg.HKEY_CURRENT_USER, path) as key:
            value, _ = winreg.QueryValueEx(key, "AppsUseLightTheme")
            return int(value) == 0
    except (ImportError, OSError, ValueError):
        return False


def colors(mode: str) -> dict[str, str]:
    if mode == "dark":
        return DARK
    if mode == "light":
        return LIGHT
    return DARK if system_is_dark() else LIGHT
