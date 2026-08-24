"""Aplicación de escritorio para calcular y visualizar el biorritmo."""

from __future__ import annotations

from datetime import date, timedelta
import tkinter as tk
from tkinter import ttk

from aspects import ASPECTS
from biorhythm import CHART_HEIGHT, CHART_WIDTH, MARGIN, series
from date_picker import DatePicker
from theme import colors

HEADER_HEIGHT = 92
LEGEND_WIDTH = CHART_WIDTH
LEGEND_COL_WIDTH = 340
LEGEND_COL_GAP = 40
LEGEND_LEFT = (LEGEND_WIDTH - (LEGEND_COL_WIDTH * 2 + LEGEND_COL_GAP)) / 2
LEGEND_TOP = 12
LEGEND_HEADER_H = 22
LEGEND_ROW_H = 26
LEGEND_HEIGHT = int(LEGEND_TOP + LEGEND_HEADER_H + 4 * LEGEND_ROW_H + 12)
CANVAS_GAP = 16
CHART_Y = HEADER_HEIGHT + CANVAS_GAP
LEGEND_Y = CHART_Y + CHART_HEIGHT + CANVAS_GAP
CANVAS_HEIGHT = LEGEND_Y + LEGEND_HEIGHT

BASIC = tuple(aspect for aspect in ASPECTS if aspect["group"] == "basico")
COMPLEMENTARY = tuple(aspect for aspect in ASPECTS if aspect["group"] == "complementario")
LEGEND_GROUPS = (
    (LEGEND_LEFT, "Aspectos básicos", BASIC),
    (LEGEND_LEFT + LEGEND_COL_WIDTH + LEGEND_COL_GAP, "Aspectos complementarios", COMPLEMENTARY),
)


class BiorritmoApp:
    def __init__(self, root: tk.Tk | None = None):
        self.root = root or tk.Tk()
        self.root.title("Calculadora de biorritmo")
        self.root.geometry("900x840")
        self.root.minsize(790, 690)
        self.theme_mode = "system"
        self.visible = {aspect["key"]: True for aspect in ASPECTS}
        today = date.today()
        self.birth_date = date(today.year - 25, today.month, min(today.day, 28))
        self.selected_date = today
        self.data: dict | None = None
        self.canvas_offset_x = 0.0
        self._build_window()
        self.recalculate()

    def _build_window(self) -> None:
        self.root.columnconfigure(0, weight=1)
        self.root.rowconfigure(2, weight=1)
        self.style = ttk.Style(self.root)

        topbar = ttk.Frame(self.root, padding=(12, 8, 12, 2))
        topbar.grid(row=0, column=0, sticky="ew")
        topbar.columnconfigure(0, weight=1)
        ttk.Label(topbar, text="Tema:").grid(row=0, column=1, padx=(0, 7))
        self.theme_combo = ttk.Combobox(topbar, values=("Sistema", "Claro", "Oscuro"), width=9, state="readonly")
        self.theme_combo.current(0)
        self.theme_combo.grid(row=0, column=2)
        self.theme_combo.bind("<<ComboboxSelected>>", self._theme_changed)

        controls = ttk.Frame(self.root, padding=(12, 6, 12, 8))
        controls.grid(row=1, column=0)
        birth_column = ttk.Frame(controls)
        birth_column.grid(row=0, column=0, padx=7, sticky="n")
        ttk.Label(birth_column, text="Fecha de nacimiento").pack(anchor="w", pady=(0, 6))
        self.birth_picker = DatePicker(birth_column, self.birth_date, self._birth_changed)
        self.birth_picker.pack(anchor="w")

        selected_column = ttk.Frame(controls)
        selected_column.grid(row=0, column=1, padx=7, sticky="n")
        ttk.Label(selected_column, text="Fecha a analizar").pack(anchor="w", pady=(0, 6))
        self.selected_picker = DatePicker(selected_column, self.selected_date, self._selected_changed)
        self.selected_picker.pack(anchor="w")

        navigation_column = ttk.Frame(controls)
        navigation_column.grid(row=0, column=2, padx=7, sticky="s")
        ttk.Label(navigation_column, text="").pack(anchor="w", pady=(0, 6))
        navigation = ttk.Frame(navigation_column)
        navigation.pack()
        ttk.Button(navigation, text="< Anterior", command=lambda: self.shift_selected(-1)).pack(side="left", padx=3)
        self.today_button = ttk.Button(navigation, text="Hoy", command=lambda: self.set_selected(date.today()))
        self.today_button.pack(side="left", padx=3)
        ttk.Button(navigation, text="Siguiente >", command=lambda: self.shift_selected(1)).pack(side="left", padx=3)

        self.canvas = tk.Canvas(self.root, highlightthickness=0, borderwidth=0, width=CHART_WIDTH, height=CANVAS_HEIGHT)
        self.canvas.grid(row=2, column=0, sticky="nsew")
        self.canvas.bind("<Configure>", lambda _event: self.redraw())
        self.canvas.bind("<Button-1>", self._canvas_clicked)

    def _theme_changed(self, _event=None) -> None:
        self.theme_mode = ("system", "light", "dark")[self.theme_combo.current()]
        self.redraw()

    def _birth_changed(self, value: date) -> None:
        self.birth_date = value
        self.recalculate()

    def _selected_changed(self, value: date) -> None:
        self.selected_date = value
        self.recalculate()

    def shift_selected(self, amount: int) -> None:
        self.set_selected(self.selected_date + timedelta(days=amount))

    def set_selected(self, value: date) -> None:
        self.selected_date = value
        self.selected_picker.set(value, notify=False)
        self.recalculate()

    def recalculate(self) -> None:
        self.data = series(self.birth_date, self.selected_date)
        self.today_button.configure(state="disabled" if self.selected_date == date.today() else "normal")
        self.redraw()

    def redraw(self) -> None:
        if not self.data or not self.canvas.winfo_exists():
            return
        palette = colors(self.theme_mode)
        width = max(self.canvas.winfo_width(), CHART_WIDTH)
        height = max(self.canvas.winfo_height(), CANVAS_HEIGHT)
        self.canvas.configure(background=palette["bg"])
        self.canvas.delete("all")
        self.canvas.create_rectangle(0, 0, width, height, fill=palette["bg"], outline="")
        self.canvas_offset_x = max((width - CHART_WIDTH) / 2, 0)
        self._draw_header(palette)
        self._draw_chart(palette)
        self._draw_legend(palette)

    def _draw_header(self, palette: dict[str, str]) -> None:
        ox = self.canvas_offset_x
        self.canvas.create_text(
            ox + CHART_WIDTH / 2, 6, text="Calculadora de biorritmo", anchor="n", fill=palette["text_h"],
            font=("Segoe UI", 22, "normal"),
        )
        subtitle = (
            "Ingresa tu fecha de nacimiento para graficar tus ciclos físico, emocional e intelectual, "
            "junto con los aspectos complementarios: espiritual, conciencia, intuición y estética."
        )
        self.canvas.create_text(
            ox + CHART_WIDTH / 2, 45, text=subtitle, anchor="n", justify="center", width=CHART_WIDTH,
            fill=palette["muted"], font=("Segoe UI", 12),
        )

    def _draw_chart(self, palette: dict[str, str]) -> None:
        assert self.data is not None
        ox = self.canvas_offset_x
        oy = CHART_Y
        left = ox + MARGIN["left"]
        right = ox + CHART_WIDTH - MARGIN["right"]
        top = oy + MARGIN["top"]
        bottom = oy + CHART_HEIGHT - MARGIN["bottom"]

        for gridline in self.data["gridlines"]:
            y = oy + gridline["y"]
            color = palette["gridline_zero"] if gridline["zero"] else palette["gridline"]
            self.canvas.create_line(left, y, right, y, fill=color, width=1)
            self.canvas.create_text(
                left - 8, y, text=str(gridline["value"]), anchor="e", fill=palette["axis_label"],
                font=("Segoe UI", 11),
            )

        center_x = ox + self.data["center_x"]
        self.canvas.create_line(center_x, top, center_x, bottom, fill=palette["marker_line"], width=1, dash=(3, 3))
        self.canvas.create_text(
            center_x, top - 18, text=self.data["marker_label"], anchor="center", fill=palette["label"],
            font=("Segoe UI", 12, "bold"),
        )
        for item in self.data["date_labels"]:
            self.canvas.create_text(
                ox + item["x"], bottom + 13, text=item["label"], anchor="center", fill=palette["axis_label"],
                font=("Segoe UI", 11),
            )

        for line in self.data["lines"]:
            if not self.visible[line["key"]]:
                continue
            coordinates = [coordinate for x, y in line["points"] for coordinate in (ox + x, oy + y)]
            options = {"fill": line["color"], "width": 2}
            if line["dash"]:
                options["dash"] = line["dash"]
            self.canvas.create_line(*coordinates, **options)
            marker_x = ox + line["marker_x"]
            marker_y = oy + line["marker_y"]
            self.canvas.create_oval(
                marker_x - 5, marker_y - 5, marker_x + 5, marker_y + 5,
                fill=line["color"], outline="#ffffff", width=2,
            )

    def _draw_legend(self, palette: dict[str, str]) -> None:
        assert self.data is not None
        ox = self.canvas_offset_x
        values = {line["key"]: line for line in self.data["lines"]}
        for column_x, title, aspects in LEGEND_GROUPS:
            x = ox + column_x
            self.canvas.create_text(
                x, LEGEND_Y + LEGEND_TOP, text=title, anchor="nw", fill=palette["label"],
                font=("Segoe UI", 14, "bold"),
            )
            for index, aspect in enumerate(aspects):
                row_y = LEGEND_Y + LEGEND_TOP + LEGEND_HEADER_H + index * LEGEND_ROW_H
                self._draw_legend_row(x, row_y, aspect, values[aspect["key"]], palette)

    def _draw_legend_row(self, x: float, y: float, aspect: dict, line: dict, palette: dict[str, str]) -> None:
        visible = self.visible[aspect["key"]]
        center_y = y + LEGEND_ROW_H / 2
        self.canvas.create_oval(
            x + 2, center_y - 6, x + 14, center_y + 6,
            fill=aspect["color"] if visible else palette["bg"], outline=aspect["color"], width=2 if not visible else 0,
        )
        text_color = palette["text_h"] if visible else palette["legend_dim"]
        self.canvas.create_text(x + 24, y + 4, text=aspect["label"], anchor="nw", fill=text_color, font=("Segoe UI", 13))
        self.canvas.create_text(
            x + LEGEND_COL_WIDTH - 90, y + 4, text=f'{line["current_value"]}%', anchor="ne",
            fill=text_color, font=("Segoe UI", 13),
        )
        self.canvas.create_text(
            x + LEGEND_COL_WIDTH, y + 4, text=line["status"], anchor="ne", fill=palette["axis_label"],
            font=("Segoe UI", 12),
        )

    def _canvas_clicked(self, event: tk.Event) -> None:
        local_x = event.x - self.canvas_offset_x
        local_y = event.y - LEGEND_Y
        for column_x, _title, aspects in LEGEND_GROUPS:
            if not column_x <= local_x <= column_x + LEGEND_COL_WIDTH:
                continue
            for index, aspect in enumerate(aspects):
                row_y = LEGEND_TOP + LEGEND_HEADER_H + index * LEGEND_ROW_H
                if row_y <= local_y <= row_y + LEGEND_ROW_H:
                    self.visible[aspect["key"]] = not self.visible[aspect["key"]]
                    self.redraw()
                    return

    def run(self) -> None:
        self.root.mainloop()


def main() -> None:
    BiorritmoApp().run()


if __name__ == "__main__":
    main()
