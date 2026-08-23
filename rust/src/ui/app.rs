//! Implementa la ventana de la aplicación, replicando app.rb del proyecto
//! Ruby: una barra de tema, una fila de controles (fechas y navegación) y
//! un lienzo con el encabezado, la gráfica y la leyenda.

use std::collections::HashMap;

use egui::{Sense, Vec2};
use time::{Date, OffsetDateTime};

use crate::aspects;
use crate::biorhythm::{self, Series};
use crate::gfx::{Align, Canvas, Weight};
use crate::theme::{self, Colors, Mode};
use crate::ui::button::Button;
use crate::ui::datefield::DateField;

const CHART_WIDTH: f64 = biorhythm::CHART_WIDTH;
const CHART_HEIGHT: f64 = biorhythm::CHART_HEIGHT;
const HEADER_HEIGHT: f64 = 92.0;

const LEGEND_WIDTH: f64 = CHART_WIDTH;
const LEGEND_COL_WIDTH: f64 = 340.0;
const LEGEND_COL_GAP: f64 = 40.0;
const LEGEND_TOP: f64 = 12.0;
const LEGEND_HEADER_H: f64 = 22.0;
const LEGEND_ROW_H: f64 = 26.0;

const CANVAS_GAP: f64 = 16.0;
const ROW_GAP: f32 = 10.0;
const WINDOW_MARGIN: f32 = 16.0;

fn legend_left() -> f64 {
    (LEGEND_WIDTH - (LEGEND_COL_WIDTH * 2.0 + LEGEND_COL_GAP)) / 2.0
}

fn chart_y() -> f64 {
    HEADER_HEIGHT + CANVAS_GAP
}

fn legend_y() -> f64 {
    chart_y() + CHART_HEIGHT + CANVAS_GAP
}

pub fn today_date() -> Date {
    OffsetDateTime::now_local().unwrap_or_else(|_| OffsetDateTime::now_utc()).date()
}

pub struct App {
    theme_mode: Mode,
    visible: HashMap<&'static str, bool>,
    birth_date: Date,
    selected_date: Date,
    series: Series,

    theme_buttons: [Button; 3],
    prev_btn: Button,
    today_btn: Button,
    next_btn: Button,
}

impl App {
    pub fn new() -> Self {
        let today = today_date();
        let mut birth_day = today.day();
        if birth_day > 28 {
            birth_day = 28;
        }
        let birth = Date::from_calendar_date(today.year() - 25, today.month(), birth_day).unwrap_or(today);

        let mut visible = HashMap::with_capacity(aspects::LIST.len());
        for a in aspects::LIST.iter() {
            visible.insert(a.key, true);
        }

        let series = biorhythm::compute(birth, today, today);

        Self {
            theme_mode: Mode::System,
            visible,
            birth_date: birth,
            selected_date: today,
            series,
            theme_buttons: [Button::new("Sistema"), Button::new("Claro"), Button::new("Oscuro")],
            prev_btn: Button::new("< Anterior"),
            today_btn: Button::new("Hoy"),
            next_btn: Button::new("Siguiente >"),
        }
    }

    fn recalc(&mut self) {
        self.series = biorhythm::compute(self.birth_date, self.selected_date, today_date());
    }

    fn layout_topbar(&mut self, ui: &mut egui::Ui, colors: &Colors) {
        let width = ui.available_width() as f64;
        const GAP: f64 = 8.0;
        const LABEL_SZ: f64 = 13.0;
        let label = "Tema:";

        let (lw, lh, btn_sizes) = {
            let canvas = Canvas::new(ui);
            let (lw, lh) = canvas.measure_text(label, LABEL_SZ, Weight::Normal);
            let sizes: Vec<(f64, f64)> = self.theme_buttons.iter().map(|b| b.measure(&canvas)).collect();
            (lw, lh, sizes)
        };

        let mut content_w = lw + GAP;
        let mut btn_h = 0.0;
        for &(w, h) in &btn_sizes {
            content_w += w + GAP;
            if h > btn_h {
                btn_h = h;
            }
        }
        let row_h = lh.max(btn_h);
        let start_x = (width - content_w).max(0.0);

        let (rect, _resp) = ui.allocate_exact_size(Vec2::new(width as f32, row_h as f32), Sense::hover());

        let canvas = Canvas::new(ui);
        let ox = rect.min.x as f64;
        let oy = rect.min.y as f64;

        let mut cx = ox + start_x;
        canvas.text(label, cx, oy + (row_h - lh) / 2.0, LABEL_SZ, colors.text_h, Weight::Normal, Align::Left);
        cx += lw + GAP;

        let mut clicked_idx: Option<usize> = None;
        for (i, btn) in self.theme_buttons.iter().enumerate() {
            let (w, h) = btn_sizes[i];
            let selected = self.theme_mode as usize == i;
            if btn.show(&canvas, ("theme-btn", i), cx, oy + (row_h - h) / 2.0, colors, selected, true) {
                clicked_idx = Some(i);
            }
            cx += w + GAP;
        }

        if let Some(i) = clicked_idx {
            self.theme_mode = match i {
                0 => Mode::System,
                1 => Mode::Light,
                _ => Mode::Dark,
            };
        }
    }

    fn layout_controls(&mut self, ui: &mut egui::Ui, colors: &Colors) {
        let width = ui.available_width() as f64;

        const LABEL_SZ: f64 = 12.0;
        const FIELD_GAP: f64 = 4.0;
        const COL_GAP: f64 = 28.0;
        const NAV_BTN_GAP: f64 = 6.0;
        const V_PAD: f64 = 8.0;

        let birth_label = "Fecha de nacimiento";
        let sel_label = "Fecha a analizar";

        let (birth_label_w, label_h, sel_label_w, field_w, prev_size, today_size, next_size) = {
            let canvas = Canvas::new(ui);
            let (blw, lh) = canvas.measure_text(birth_label, LABEL_SZ, Weight::Normal);
            let (slw, _) = canvas.measure_text(sel_label, LABEL_SZ, Weight::Normal);
            let fw = DateField::width();
            let prev = self.prev_btn.measure(&canvas);
            let today = self.today_btn.measure(&canvas);
            let next = self.next_btn.measure(&canvas);
            (blw, lh, slw, fw, prev, today, next)
        };

        let col1_w = birth_label_w.max(field_w);
        let col2_w = sel_label_w.max(field_w);
        let nav_w = prev_size.0 + NAV_BTN_GAP + today_size.0 + NAV_BTN_GAP + next_size.0;
        let nav_h = prev_size.1.max(today_size.1).max(next_size.1);
        let col3_w = nav_w;

        let content_w = col1_w + COL_GAP + col2_w + COL_GAP + col3_w;
        let start_x = ((width - content_w) / 2.0).max(0.0);

        let field_h = DateField::height();
        let row_h = label_h + FIELD_GAP + field_h.max(nav_h);

        let (rect, _resp) =
            ui.allocate_exact_size(Vec2::new(width as f32, (row_h + 2.0 * V_PAD) as f32), Sense::hover());
        let ox = rect.min.x as f64;
        let oy = rect.min.y as f64;

        let label_y = oy + V_PAD;
        let field_y = label_y + label_h + FIELD_GAP;
        let nav_y = field_y + (field_h - nav_h) / 2.0;

        let canvas = Canvas::new(ui);

        let mut cx = ox + start_x;
        canvas.text(birth_label, cx, label_y, LABEL_SZ, colors.label, Weight::Normal, Align::Left);
        let (new_birth, birth_changed) = DateField::show(&canvas, "birth", cx, field_y, self.birth_date, colors);
        cx += col1_w + COL_GAP;

        canvas.text(sel_label, cx, label_y, LABEL_SZ, colors.label, Weight::Normal, Align::Left);
        let (new_selected, sel_changed) = DateField::show(&canvas, "selected", cx, field_y, self.selected_date, colors);
        cx += col2_w + COL_GAP;

        let mut nx = cx;
        let prev_clicked = self.prev_btn.show(&canvas, "prev-btn", nx, nav_y, colors, false, true);
        nx += prev_size.0 + NAV_BTN_GAP;
        let today_enabled = self.selected_date != today_date();
        let today_clicked = self.today_btn.show(&canvas, "today-btn", nx, nav_y, colors, false, today_enabled);
        nx += today_size.0 + NAV_BTN_GAP;
        let next_clicked = self.next_btn.show(&canvas, "next-btn", nx, nav_y, colors, false, true);

        let mut changed = false;
        if birth_changed {
            self.birth_date = new_birth;
            changed = true;
        }
        if sel_changed {
            self.selected_date = new_selected;
            changed = true;
        }
        if prev_clicked {
            self.selected_date = self.selected_date - time::Duration::days(1);
            changed = true;
        }
        if today_clicked && today_enabled {
            self.selected_date = today_date();
            changed = true;
        }
        if next_clicked {
            self.selected_date = self.selected_date + time::Duration::days(1);
            changed = true;
        }

        if changed {
            self.recalc();
        }
    }

    fn layout_canvas(&mut self, ui: &mut egui::Ui, colors: &Colors) {
        let width = ui.available_width() as f64;
        let height = ui.available_height() as f64;

        let (rect, _resp) = ui.allocate_exact_size(Vec2::new(width as f32, height as f32), Sense::hover());

        let ox_avail = rect.min.x as f64;
        let oy_avail = rect.min.y as f64;

        let canvas = Canvas::new(ui);
        canvas.fill_rect(ox_avail, oy_avail, width, height, colors.bg);

        let ox = ox_avail + (width - CHART_WIDTH).max(0.0) / 2.0;
        let oy = oy_avail;

        self.draw_header(&canvas, ox, oy, colors);
        self.draw_chart(&canvas, ox, oy + chart_y(), colors);

        let mut toggled: Option<&'static str> = None;
        self.draw_legend(&canvas, ox, oy + legend_y(), colors, &mut toggled);

        if let Some(key) = toggled {
            if let Some(v) = self.visible.get_mut(key) {
                *v = !*v;
            }
        }
    }

    fn draw_header(&self, canvas: &Canvas, ox: f64, oy: f64, colors: &Colors) {
        let title_h =
            canvas.paragraph("Calculadora de biorritmo", ox, oy + 6.0, CHART_WIDTH, 22.0, colors.text_h, Weight::Medium);

        let subtitle = "Ingresa tu fecha de nacimiento para graficar tus ciclos físico, emocional e intelectual, \
                         junto con los aspectos complementarios: espiritual, conciencia, intuición y estética.";
        canvas.paragraph(subtitle, ox, oy + 6.0 + title_h + 6.0, CHART_WIDTH, 12.0, colors.muted, Weight::Normal);
    }

    fn draw_chart(&self, canvas: &Canvas, ox: f64, oy: f64, colors: &Colors) {
        let s = &self.series;
        let left = ox + biorhythm::MARGIN.left;
        let right_edge = ox + CHART_WIDTH - biorhythm::MARGIN.right;
        let bottom = oy + CHART_HEIGHT - biorhythm::MARGIN.bottom;
        let top = oy + biorhythm::MARGIN.top;

        for g in &s.gridlines {
            let col = if g.zero { colors.gridline_zero } else { colors.gridline };
            canvas.stroke_line(left, oy + g.y, right_edge, oy + g.y, col, 1.0, &[]);
            canvas.text(&g.value.to_string(), left - 8.0, oy + g.y - 6.0, 11.0, colors.axis_label, Weight::Normal, Align::Right);
        }

        let cx = ox + s.center_x;
        let dash_33 = Canvas::parse_dash("3 3");
        canvas.stroke_line(cx, top, cx, bottom, colors.marker_line, 1.0, &dash_33);
        canvas.text(&s.marker_label, cx, top - 18.0, 12.0, colors.label, Weight::Bold, Align::Center);

        for dl in &s.date_labels {
            canvas.text(&dl.label, ox + dl.x, bottom + 6.0, 11.0, colors.axis_label, Weight::Normal, Align::Center);
        }

        for line in &s.lines {
            if !*self.visible.get(line.key).unwrap_or(&true) {
                continue;
            }

            let col = theme::hex(line.color);
            let dash = Canvas::parse_dash(line.dash);

            let points: Vec<(f64, f64)> = line.points.iter().map(|&(x, y)| (ox + x, oy + y)).collect();
            canvas.stroke_polyline(&points, col, 2.0, &dash);

            let mx = ox + line.marker_x;
            let my = oy + line.marker_y;
            canvas.marker_circle(mx, my, 5.0, 2.0, col);
        }
    }

    fn draw_legend(&self, canvas: &Canvas, ox: f64, oy: f64, colors: &Colors, toggled: &mut Option<&'static str>) {
        let mut values_by_key: HashMap<&str, &biorhythm::Line> = HashMap::new();
        for l in &self.series.lines {
            values_by_key.insert(l.key, l);
        }

        let groups: [(f64, &str, Vec<&aspects::Aspect>); 2] = [
            (legend_left(), "Aspectos básicos", aspects::basico()),
            (legend_left() + LEGEND_COL_WIDTH + LEGEND_COL_GAP, "Aspectos complementarios", aspects::complementario()),
        ];

        for (col_x, title, asps) in groups.iter() {
            canvas.text(title, ox + col_x, oy + LEGEND_TOP, 14.0, colors.label, Weight::Bold, Align::Left);

            for (i, asp) in asps.iter().enumerate() {
                let row_y = oy + LEGEND_TOP + LEGEND_HEADER_H + i as f64 * LEGEND_ROW_H;
                self.draw_legend_row(canvas, ox + col_x, row_y, asp, values_by_key.get(asp.key).copied(), colors);

                let resp = canvas.interact_click(ox + col_x, row_y, LEGEND_COL_WIDTH, LEGEND_ROW_H, ("legend-row", asp.key));
                if resp.clicked() {
                    *toggled = Some(asp.key);
                }
            }
        }
    }

    fn draw_legend_row(
        &self,
        canvas: &Canvas,
        col_x: f64,
        row_y: f64,
        asp: &aspects::Aspect,
        line: Option<&biorhythm::Line>,
        colors: &Colors,
    ) {
        let visible = *self.visible.get(asp.key).unwrap_or(&true);
        let cy = row_y + LEGEND_ROW_H / 2.0;
        let col = theme::hex(asp.color);

        if visible {
            canvas.fill_circle(col_x + 8.0, cy, 6.0, col);
        } else {
            canvas.stroke_circle(col_x + 8.0, cy, 6.0, 2.0, col);
        }

        let text_col = if visible { colors.text_h } else { colors.legend_dim };

        canvas.text(asp.label, col_x + 24.0, row_y + 4.0, 13.0, text_col, Weight::Normal, Align::Left);
        if let Some(l) = line {
            canvas.text(
                &format!("{}%", l.current_value),
                col_x + LEGEND_COL_WIDTH - 90.0,
                row_y + 4.0,
                13.0,
                text_col,
                Weight::Normal,
                Align::Right,
            );
            canvas.text(l.status, col_x + LEGEND_COL_WIDTH, row_y + 4.0, 12.0, colors.axis_label, Weight::Normal, Align::Right);
        }
    }
}

impl eframe::App for App {
    fn ui(&mut self, ui: &mut egui::Ui, _frame: &mut eframe::Frame) {
        let colors = theme::for_mode(self.theme_mode);

        egui::Frame::NONE.fill(colors.bg).inner_margin(WINDOW_MARGIN).show(ui, |ui| {
            self.layout_topbar(ui, &colors);
            ui.add_space(ROW_GAP);
            self.layout_controls(ui, &colors);
            ui.add_space(ROW_GAP);
            self.layout_canvas(ui, &colors);
        });
    }
}
