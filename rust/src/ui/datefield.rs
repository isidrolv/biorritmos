//! Selector de fecha compacto (día/mes/año), cada unidad con flechas
//! arriba/abajo para incrementar o decrementar -- el equivalente funcional
//! del uiDatePicker nativo que usa el proyecto Ruby. Replica datefield.go
//! de la versión Go.

use time::{Date, Duration, Month};

use crate::gfx::{Align, Canvas, Weight};
use crate::theme::Colors;

const BOX_H: f64 = 26.0;
const DAY_W: f64 = 30.0;
const MONTH_W: f64 = 30.0;
const YEAR_W: f64 = 46.0;
const ARROW_W: f64 = 16.0;
const UNIT_GAP: f64 = 3.0;
const BORDER: f64 = 1.0;
const TEXT_SZ: f64 = 13.0;

pub struct DateField;

impl DateField {
    /// Ancho total que ocupa el control.
    pub fn width() -> f64 {
        (DAY_W + ARROW_W) + UNIT_GAP + (MONTH_W + ARROW_W) + UNIT_GAP + (YEAR_W + ARROW_W)
    }

    /// Alto del control.
    pub fn height() -> f64 {
        BOX_H
    }

    /// Dibuja el selector en (x, y) para date y devuelve la fecha resultante
    /// y si cambió en este frame. `id_prefix` debe ser único por instancia
    /// (p.ej. "birth" o "selected") para que las zonas clicables no choquen.
    pub fn show(
        canvas: &Canvas,
        id_prefix: &str,
        x: f64,
        y: f64,
        date: Date,
        colors: &Colors,
    ) -> (Date, bool) {
        let mut result = date;
        let mut changed = false;
        let mut cx = x;

        let day_text = format!("{:02}", date.day());
        if let Some(delta) = Self::draw_unit(
            canvas,
            &format!("{id_prefix}-day"),
            cx,
            y,
            DAY_W,
            &day_text,
            colors,
        ) {
            result = add_days(result, delta);
            changed = true;
        }
        cx += DAY_W + ARROW_W + UNIT_GAP;

        let month_text = format!("{:02}", u8::from(date.month()));
        if let Some(delta) = Self::draw_unit(
            canvas,
            &format!("{id_prefix}-month"),
            cx,
            y,
            MONTH_W,
            &month_text,
            colors,
        ) {
            result = add_months(result, delta as i32);
            changed = true;
        }
        cx += MONTH_W + ARROW_W + UNIT_GAP;

        let year_text = format!("{:04}", date.year());
        if let Some(delta) = Self::draw_unit(
            canvas,
            &format!("{id_prefix}-year"),
            cx,
            y,
            YEAR_W,
            &year_text,
            colors,
        ) {
            result = add_years(result, delta as i32);
            changed = true;
        }

        (result, changed)
    }

    fn draw_unit(
        canvas: &Canvas,
        id_prefix: &str,
        x: f64,
        y: f64,
        box_w: f64,
        text: &str,
        colors: &Colors,
    ) -> Option<i64> {
        let border_col = colors.marker_line;

        canvas.fill_rect(x, y, box_w, BOX_H, colors.bg);
        canvas.fill_rect(x, y, box_w, BORDER, border_col);
        canvas.fill_rect(x, y + BOX_H - BORDER, box_w, BORDER, border_col);
        canvas.fill_rect(x, y, BORDER, BOX_H, border_col);
        canvas.fill_rect(x + box_w - BORDER, y, BORDER, BOX_H, border_col);

        let (tw, th) = canvas.measure_text(text, TEXT_SZ, Weight::Normal);
        canvas.text(
            text,
            x + (box_w - tw) / 2.0,
            y + (BOX_H - th) / 2.0,
            TEXT_SZ,
            colors.text_h,
            Weight::Normal,
            Align::Left,
        );

        let ax = x + box_w;
        let arrow_h = BOX_H / 2.0;
        canvas.fill_rect(ax, y, ARROW_W, BOX_H, colors.bg);
        canvas.fill_rect(ax, y, ARROW_W, BORDER, border_col);
        canvas.fill_rect(ax, y + BOX_H - BORDER, ARROW_W, BORDER, border_col);
        canvas.fill_rect(ax, y, BORDER, BOX_H, border_col);
        canvas.fill_rect(ax + ARROW_W - BORDER, y, BORDER, BOX_H, border_col);
        canvas.fill_rect(ax, y + arrow_h - BORDER / 2.0, ARROW_W, BORDER, border_col);

        let tri_col = colors.label;
        let mx = ax + ARROW_W / 2.0;
        canvas.fill_polygon(
            &[
                (mx - 4.0, y + arrow_h - 5.0),
                (mx + 4.0, y + arrow_h - 5.0),
                (mx, y + arrow_h - 11.0),
            ],
            tri_col,
        );
        canvas.fill_polygon(
            &[
                (mx - 4.0, y + arrow_h + 5.0),
                (mx + 4.0, y + arrow_h + 5.0),
                (mx, y + arrow_h + 11.0),
            ],
            tri_col,
        );

        let up_clicked = canvas
            .interact_click(ax, y, ARROW_W, arrow_h, format!("{id_prefix}-up"))
            .clicked();
        let down_clicked = canvas
            .interact_click(
                ax,
                y + arrow_h,
                ARROW_W,
                arrow_h,
                format!("{id_prefix}-down"),
            )
            .clicked();

        if up_clicked {
            Some(1)
        } else if down_clicked {
            Some(-1)
        } else {
            None
        }
    }
}

fn days_in_month(year: i32, month: Month) -> u8 {
    let (next_year, next_month) = if month == Month::December {
        (year + 1, Month::January)
    } else {
        (year, month.next())
    };
    let first_of_next = Date::from_calendar_date(next_year, next_month, 1).expect("fecha válida");
    (first_of_next - Duration::days(1)).day()
}

fn add_days(d: Date, delta: i64) -> Date {
    d + Duration::days(delta)
}

fn add_months(d: Date, delta: i32) -> Date {
    let day = d.day();
    let month_index = i32::from(u8::from(d.month())) - 1;
    let total = month_index + delta;
    let year = d.year() + total.div_euclid(12);
    let month = Month::try_from((total.rem_euclid(12) + 1) as u8).expect("mes válido");
    let dim = days_in_month(year, month);
    Date::from_calendar_date(year, month, day.min(dim)).expect("fecha válida")
}

fn add_years(d: Date, delta: i32) -> Date {
    let year = d.year() + delta;
    let month = d.month();
    let dim = days_in_month(year, month);
    Date::from_calendar_date(year, month, d.day().min(dim)).expect("fecha válida")
}
