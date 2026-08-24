"""Cálculos y geometría de la gráfica de biorritmo."""

from __future__ import annotations

from datetime import date, timedelta
import math

from aspects import ASPECTS

RANGE_DAYS = 15
CHART_WIDTH = 760
CHART_HEIGHT = 340
MARGIN = {"top": 16, "right": 16, "bottom": 34, "left": 44}
PLOT_WIDTH = CHART_WIDTH - MARGIN["left"] - MARGIN["right"]
PLOT_HEIGHT = CHART_HEIGHT - MARGIN["top"] - MARGIN["bottom"]
MONTHS_ES = ("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")


def value_at(days_since_birth: int, period: int) -> float:
    return math.sin((2 * math.pi * days_since_birth) / period) * 100


def phase_label(value: float, next_value: float) -> str:
    if abs(value) < 3:
        return "Crítico"
    return "Ascendente" if next_value > value else "Descendente"


def format_short(value: date) -> str:
    return f"{value.day:02d} {MONTHS_ES[value.month - 1]}"


def _round(value: float) -> int:
    """Redondeo aritmético, igual al usado por la interfaz."""
    return math.floor(value + 0.5) if value >= 0 else math.ceil(value - 0.5)


def series(birth_date: date, selected_date: date, today: date | None = None) -> dict:
    offsets = list(range(-RANGE_DAYS, RANGE_DAYS + 1))
    dates = [selected_date + timedelta(days=offset) for offset in offsets]
    elapsed = [(current - birth_date).days for current in dates]

    def x_scale(index: int) -> float:
        return MARGIN["left"] + index / (len(offsets) - 1) * PLOT_WIDTH

    def y_scale(value: float) -> float:
        return MARGIN["top"] + (1 - (value + 100) / 200) * PLOT_HEIGHT

    lines = []
    for aspect in ASPECTS:
        values = [value_at(days, aspect["period"]) for days in elapsed]
        current_value = values[RANGE_DAYS]
        next_value = value_at(elapsed[RANGE_DAYS] + 1, aspect["period"])
        lines.append(
            {
                **aspect,
                "points": [(x_scale(i), y_scale(value)) for i, value in enumerate(values)],
                "current_value": _round(current_value),
                "status": phase_label(current_value, next_value),
                "marker_x": x_scale(RANGE_DAYS),
                "marker_y": y_scale(current_value),
            }
        )

    date_labels = [
        {"x": round(x_scale(index), 2), "label": format_short(dates[index])}
        for index, offset in enumerate(offsets)
        if offset % 5 == 0
    ]
    gridlines = [
        {"value": value, "y": round(y_scale(value), 2), "zero": value == 0}
        for value in (100, 50, 0, -50, -100)
    ]
    actual_today = today or date.today()
    is_today = selected_date == actual_today
    return {
        "gridlines": gridlines,
        "center_x": x_scale(RANGE_DAYS),
        "marker_label": "Hoy" if is_today else format_short(selected_date),
        "date_labels": date_labels,
        "lines": lines,
        "is_today": is_today,
    }
