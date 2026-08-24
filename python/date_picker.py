"""Selector de fecha compacto con calendario desplegable."""

from __future__ import annotations

import calendar
from datetime import date
import tkinter as tk
from tkinter import ttk
from typing import Callable

MONTH_NAMES = (
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)
WEEKDAYS = ("Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do")


class DatePicker(ttk.Frame):
    def __init__(self, master, value: date, command: Callable[[date], None] | None = None):
        super().__init__(master, style="App.TFrame")
        self._value = value
        self._command = command
        self._popup: tk.Toplevel | None = None
        self._view_year = value.year
        self._view_month = value.month
        self.variable = tk.StringVar()
        self.entry = ttk.Entry(self, width=12, textvariable=self.variable, style="Date.TEntry")
        self.entry.pack(side="left")
        self.button = ttk.Button(self, text="▾", width=2, command=self._toggle_popup, style="Small.TButton")
        self.button.pack(side="left", padx=(2, 0))
        self.entry.bind("<Return>", self._accept_text)
        self.entry.bind("<FocusOut>", self._accept_text)
        self.set(value, notify=False)

    def get(self) -> date:
        return self._value

    def set(self, value: date, notify: bool = True) -> None:
        changed = value != self._value
        self._value = value
        self._view_year, self._view_month = value.year, value.month
        self.variable.set(value.strftime("%d/%m/%Y"))
        if notify and changed and self._command:
            self._command(value)

    def _accept_text(self, _event=None) -> None:
        try:
            day, month, year = (int(part) for part in self.variable.get().strip().split("/"))
            self.set(date(year, month, day))
        except (TypeError, ValueError):
            self.variable.set(self._value.strftime("%d/%m/%Y"))

    def _toggle_popup(self) -> None:
        if self._popup and self._popup.winfo_exists():
            self._close_popup()
            return
        self._popup = tk.Toplevel(self)
        self._popup.wm_overrideredirect(True)
        self._popup.transient(self.winfo_toplevel())
        self._popup.configure(background="#888888")
        x = self.winfo_rootx()
        y = self.winfo_rooty() + self.winfo_height() + 2
        self._popup.geometry(f"+{x}+{y}")
        self._popup.bind("<Escape>", lambda _e: self._close_popup())
        self._draw_calendar()
        self._popup.focus_set()

    def _close_popup(self) -> None:
        if self._popup and self._popup.winfo_exists():
            self._popup.destroy()
        self._popup = None

    def _change_month(self, amount: int) -> None:
        month = self._view_month + amount
        self._view_year += (month - 1) // 12
        self._view_month = (month - 1) % 12 + 1
        self._draw_calendar()

    def _draw_calendar(self) -> None:
        if not self._popup:
            return
        for child in self._popup.winfo_children():
            child.destroy()
        panel = ttk.Frame(self._popup, padding=7, style="Calendar.TFrame")
        panel.pack(padx=1, pady=1)
        ttk.Button(panel, text="‹", width=2, style="Calendar.TButton", command=lambda: self._change_month(-1)).grid(row=0, column=0)
        ttk.Label(panel, text=f"{MONTH_NAMES[self._view_month - 1].capitalize()} {self._view_year}", width=20,
                  anchor="center", style="CalendarHeader.TLabel").grid(row=0, column=1, columnspan=5)
        ttk.Button(panel, text="›", width=2, style="Calendar.TButton", command=lambda: self._change_month(1)).grid(row=0, column=6)
        for column, label in enumerate(WEEKDAYS):
            ttk.Label(panel, text=label, width=3, anchor="center", style="CalendarWeek.TLabel").grid(row=1, column=column, pady=(5, 2))
        weeks = calendar.Calendar(firstweekday=0).monthdayscalendar(self._view_year, self._view_month)
        for row, week in enumerate(weeks, start=2):
            for column, day in enumerate(week):
                if not day:
                    ttk.Label(panel, text="", width=3, style="Calendar.TLabel").grid(row=row, column=column)
                    continue
                candidate = date(self._view_year, self._view_month, day)
                style = "CalendarSelected.TButton" if candidate == self._value else "Calendar.TButton"
                ttk.Button(panel, text=str(day), width=3, style=style,
                           command=lambda chosen=candidate: self._choose(chosen)).grid(row=row, column=column)

    def _choose(self, value: date) -> None:
        self.set(value)
        self._close_popup()
