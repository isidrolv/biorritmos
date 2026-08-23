//! Calcula las series del biorritmo, replicando lib/biorhythm.rb del
//! proyecto Ruby.

use std::f64::consts::PI;
use time::Date;

use crate::aspects;

pub const RANGE_DAYS: i64 = 15;
pub const CHART_WIDTH: f64 = 760.0;
pub const CHART_HEIGHT: f64 = 340.0;

pub struct Margin {
    pub top: f64,
    pub right: f64,
    pub bottom: f64,
    pub left: f64,
}

pub const MARGIN: Margin = Margin { top: 16.0, right: 16.0, bottom: 34.0, left: 44.0 };

pub const PLOT_WIDTH: f64 = CHART_WIDTH - MARGIN.left - MARGIN.right;
pub const PLOT_HEIGHT: f64 = CHART_HEIGHT - MARGIN.top - MARGIN.bottom;

const MONTHS_ES: [&str; 12] =
    ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"];

pub fn days_between(from: Date, to: Date) -> i64 {
    (to - from).whole_days()
}

pub fn value_at(days_since_birth: i64, period: f64) -> f64 {
    (2.0 * PI * days_since_birth as f64 / period).sin() * 100.0
}

pub fn phase_label(value: f64, next_value: f64) -> &'static str {
    if value.abs() < 3.0 {
        "Crítico"
    } else if next_value > value {
        "Ascendente"
    } else {
        "Descendente"
    }
}

pub fn format_short(date: Date) -> String {
    format!("{:02} {}", date.day(), MONTHS_ES[date.month() as usize - 1])
}

pub struct Gridline {
    pub value: i32,
    pub y: f64,
    pub zero: bool,
}

pub struct DateLabel {
    pub x: f64,
    pub label: String,
}

pub struct Line {
    pub key: &'static str,
    pub label: &'static str,
    pub color: &'static str,
    pub dash: &'static str,
    pub points: Vec<(f64, f64)>,
    pub current_value: i32,
    pub status: &'static str,
    pub marker_x: f64,
    pub marker_y: f64,
}

pub struct Series {
    pub gridlines: Vec<Gridline>,
    pub center_x: f64,
    pub marker_label: String,
    pub date_labels: Vec<DateLabel>,
    pub lines: Vec<Line>,
}

fn round(v: f64) -> i32 {
    if v >= 0.0 {
        (v + 0.5) as i32
    } else {
        (v - 0.5) as i32
    }
}

pub fn compute(birth_date: Date, selected_date: Date, today: Date) -> Series {
    let offsets: Vec<i64> = (-RANGE_DAYS..=RANGE_DAYS).collect();
    let n = offsets.len();

    let dates: Vec<Date> = offsets.iter().map(|&o| selected_date + time::Duration::days(o)).collect();
    let days_since_birth: Vec<i64> = dates.iter().map(|&d| days_between(birth_date, d)).collect();

    let x_scale = |index: usize| -> f64 {
        MARGIN.left + (index as f64 / (n - 1) as f64) * PLOT_WIDTH
    };
    let y_scale = |val: f64| -> f64 { MARGIN.top + (1.0 - (val + 100.0) / 200.0) * PLOT_HEIGHT };

    let range_idx = RANGE_DAYS as usize;

    let mut lines = Vec::with_capacity(aspects::LIST.len());
    for aspect in aspects::LIST.iter() {
        let values: Vec<f64> = days_since_birth.iter().map(|&d| value_at(d, aspect.period)).collect();
        let points: Vec<(f64, f64)> =
            values.iter().enumerate().map(|(i, &v)| (x_scale(i), y_scale(v))).collect();
        let current_value = values[range_idx];
        let next_value = value_at(days_since_birth[range_idx] + 1, aspect.period);

        lines.push(Line {
            key: aspect.key,
            label: aspect.label,
            color: aspect.color,
            dash: aspect.dash,
            points,
            current_value: round(current_value),
            status: phase_label(current_value, next_value),
            marker_x: x_scale(range_idx),
            marker_y: y_scale(current_value),
        });
    }

    let date_labels = offsets
        .iter()
        .enumerate()
        .filter(|(_, &o)| o.rem_euclid(5) == 0)
        .map(|(i, _)| DateLabel { x: x_scale(i), label: format_short(dates[i]) })
        .collect();

    let gridlines = [100, 50, 0, -50, -100]
        .into_iter()
        .map(|val| Gridline { value: val, y: y_scale(val as f64), zero: val == 0 })
        .collect();

    let is_today = selected_date == today;
    let marker_label = if is_today { "Hoy".to_string() } else { format_short(selected_date) };

    Series { gridlines, center_x: x_scale(range_idx), marker_label, date_labels, lines }
}
