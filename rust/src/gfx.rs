//! Primitivas de dibujo sobre un egui::Painter equivalentes a
//! lib/native_draw.rb (NativeDraw) del proyecto Ruby: rectángulos, líneas
//! (con o sin patrón de trazo), círculos/anillos y texto alineado.
//!
//! Todas las coordenadas que reciben los métodos de Canvas son las mismas
//! "unidades lógicas" que usa el proyecto Ruby (p.ej. un gráfico de
//! 760x340).

use egui::{Color32, FontFamily, FontId, Id, Painter, Pos2, Rect, Response, Sense, Stroke, Vec2};

use crate::theme;

#[derive(Clone, Copy, PartialEq)]
pub enum Align {
    Left,
    Center,
    Right,
}

#[derive(Clone, Copy, PartialEq)]
pub enum Weight {
    Normal,
    Medium,
    Bold,
}

pub const FONT_NORMAL: &str = "Segoe UI";
pub const FONT_MEDIUM: &str = "Segoe UI Medium";
pub const FONT_BOLD: &str = "Segoe UI Bold";

fn family_for(weight: Weight) -> FontFamily {
    match weight {
        Weight::Normal => FontFamily::Name(FONT_NORMAL.into()),
        Weight::Medium => FontFamily::Name(FONT_MEDIUM.into()),
        Weight::Bold => FontFamily::Name(FONT_BOLD.into()),
    }
}

/// Intenta cargar Segoe UI (la misma tipografía que usa el proyecto Ruby vía
/// NativeDraw::FONT_FAMILY) directamente de %WINDIR%\Fonts. Si no la
/// encuentra, las familias "Segoe UI"/"Segoe UI Medium"/"Segoe UI Bold"
/// quedan igualmente registradas apuntando a la tipografía por defecto de
/// egui, para que el resto del código nunca falle al pedirlas.
pub fn load_fonts(ctx: &egui::Context) {
    let mut fonts = egui::FontDefinitions::default();

    let win_dir = std::env::var("WINDIR").unwrap_or_else(|_| r"C:\Windows".to_string());
    let fonts_dir = std::path::Path::new(&win_dir).join("Fonts");

    let mut register = |key: &str, filename: &str, family_name: &str| {
        let family = FontFamily::Name(family_name.into());
        if let Ok(data) = std::fs::read(fonts_dir.join(filename)) {
            fonts.font_data.insert(key.to_string(), egui::FontData::from_owned(data).into());
            fonts.families.entry(family).or_default().insert(0, key.to_string());
        }
    };

    register("segoe-ui", "segoeui.ttf", FONT_NORMAL);
    register("segoe-ui-medium", "seguisb.ttf", FONT_MEDIUM);
    register("segoe-ui-bold", "segoeuib.ttf", FONT_BOLD);

    let fallback = fonts
        .families
        .get(&FontFamily::Proportional)
        .cloned()
        .unwrap_or_default();
    for name in [FONT_NORMAL, FONT_MEDIUM, FONT_BOLD] {
        let family = FontFamily::Name(name.into());
        let entry = fonts.families.entry(family).or_default();
        for f in &fallback {
            if !entry.contains(f) {
                entry.push(f.clone());
            }
        }
    }

    ctx.set_fonts(fonts);
}

/// Envuelve un egui::Ui para dibujar en coordenadas lógicas y detectar
/// clics sobre regiones arbitrarias -- el equivalente de AreaWidget +
/// NativeDraw del proyecto Ruby, ya que aquí header, gráfica y leyenda se
/// dibujan y se hacen clicables sobre el mismo lienzo.
pub struct Canvas<'a> {
    pub ui: &'a egui::Ui,
}

impl<'a> Canvas<'a> {
    pub fn new(ui: &'a egui::Ui) -> Self {
        Self { ui }
    }

    fn painter(&self) -> Painter {
        self.ui.painter().clone()
    }

    /// Registra una zona clicable en el rectángulo lógico (x, y, w, h),
    /// identificada por id_source (debe ser estable entre frames).
    pub fn interact_click(&self, x: f64, y: f64, w: f64, h: f64, id_source: impl std::hash::Hash) -> Response {
        let rect = Rect::from_min_size(Pos2::new(x as f32, y as f32), Vec2::new(w as f32, h as f32));
        self.ui.interact(rect, Id::new(id_source), Sense::click())
    }

    pub fn fill_rect(&self, x: f64, y: f64, w: f64, h: f64, color: Color32) {
        let rect = Rect::from_min_size(Pos2::new(x as f32, y as f32), Vec2::new(w as f32, h as f32));
        self.painter().rect_filled(rect, 0.0, color);
    }

    pub fn fill_circle(&self, cx: f64, cy: f64, r: f64, color: Color32) {
        self.painter().circle_filled(Pos2::new(cx as f32, cy as f32), r as f32, color);
    }

    pub fn stroke_circle(&self, cx: f64, cy: f64, r: f64, thickness: f64, color: Color32) {
        self.painter()
            .circle_stroke(Pos2::new(cx as f32, cy as f32), r as f32, Stroke::new(thickness as f32, color));
    }

    /// Círculo relleno del color de la línea con un anillo blanco encima,
    /// replicando el par fill_circle + stroke_circle('#ffffff') de Ruby
    /// para los marcadores de la gráfica.
    pub fn marker_circle(&self, cx: f64, cy: f64, r: f64, thickness: f64, fill_color: Color32) {
        self.fill_circle(cx, cy, r, fill_color);
        self.stroke_circle(cx, cy, r, thickness, theme::WHITE);
    }

    pub fn stroke_line(&self, x1: f64, y1: f64, x2: f64, y2: f64, color: Color32, thickness: f64, dash: &[f64]) {
        self.stroke_polyline(&[(x1, y1), (x2, y2)], color, thickness, dash);
    }

    /// Dibuja una polilínea, opcionalmente punteada según dash (longitudes
    /// alternas trazo/hueco, en unidades lógicas) -- egui no soporta stroke
    /// punteado nativo, así que el patrón se recorre a mano igual que en la
    /// versión Go (canvas.go StrokePolyline).
    pub fn stroke_polyline(&self, pts: &[(f64, f64)], color: Color32, thickness: f64, dash: &[f64]) {
        if pts.len() < 2 {
            return;
        }
        let stroke = Stroke::new(thickness as f32, color);

        let painter = self.painter();

        if dash.is_empty() {
            for w in pts.windows(2) {
                painter.line_segment(
                    [Pos2::new(w[0].0 as f32, w[0].1 as f32), Pos2::new(w[1].0 as f32, w[1].1 as f32)],
                    stroke,
                );
            }
            return;
        }

        let mut dash_idx = 0usize;
        let mut on = true;
        let mut remaining = dash[0];
        for w in pts.windows(2) {
            let (ax, ay) = w[0];
            let (bx, by) = w[1];
            let dx = bx - ax;
            let dy = by - ay;
            let seg_len = (dx * dx + dy * dy).sqrt();
            if seg_len == 0.0 {
                continue;
            }
            let dir = (dx / seg_len, dy / seg_len);
            let mut pos = 0.0;
            let mut cur = (ax, ay);
            while pos < seg_len {
                let mut step = remaining;
                if seg_len - pos < step {
                    step = seg_len - pos;
                }
                let next = (cur.0 + dir.0 * step, cur.1 + dir.1 * step);
                if on {
                    painter.line_segment(
                        [Pos2::new(cur.0 as f32, cur.1 as f32), Pos2::new(next.0 as f32, next.1 as f32)],
                        stroke,
                    );
                }
                cur = next;
                pos += step;
                remaining -= step;
                if remaining <= 0.001 {
                    dash_idx = (dash_idx + 1) % dash.len();
                    remaining = dash[dash_idx];
                    on = !on;
                }
            }
        }
    }

    /// Convierte un patrón "7 4" (como en aspects::Aspect.dash) a la lista
    /// de longitudes usada por stroke_line/stroke_polyline. "0" o "" es una
    /// línea sólida (sin patrón).
    pub fn parse_dash(s: &str) -> Vec<f64> {
        let s = s.trim();
        if s.is_empty() || s == "0" {
            return Vec::new();
        }
        s.split_whitespace().filter_map(|f| f.parse::<f64>().ok()).collect()
    }

    /// Rellena un polígono cerrado (usado para las flechas de los
    /// selectores de fecha).
    pub fn fill_polygon(&self, pts: &[(f64, f64)], color: Color32) {
        if pts.len() < 3 {
            return;
        }
        let points: Vec<Pos2> = pts.iter().map(|&(x, y)| Pos2::new(x as f32, y as f32)).collect();
        self.painter()
            .add(egui::Shape::convex_polygon(points, color, Stroke::NONE));
    }

    /// Devuelve el ancho y alto (en unidades lógicas) que ocuparía txt en
    /// una sola línea, sin dibujarlo.
    pub fn measure_text(&self, text: &str, size: f64, weight: Weight) -> (f64, f64) {
        let font_id = FontId::new(size as f32, family_for(weight));
        let galley = self
            .ui
            .ctx()
            .fonts_mut(|f| f.layout_no_wrap(text.to_string(), font_id, Color32::WHITE));
        (galley.size().x as f64, galley.size().y as f64)
    }

    /// Dibuja una línea de texto en (x, y) -- esquina superior según align.
    pub fn text(&self, text: &str, x: f64, y: f64, size: f64, color: Color32, weight: Weight, align: Align) {
        let font_id = FontId::new(size as f32, family_for(weight));
        let anchor = match align {
            Align::Left => egui::Align2::LEFT_TOP,
            Align::Center => egui::Align2::CENTER_TOP,
            Align::Right => egui::Align2::RIGHT_TOP,
        };
        self.painter().text(Pos2::new(x as f32, y as f32), anchor, text, font_id, color);
    }

    /// Dibuja un párrafo centrado y ajustado (wrap) dentro de width, como
    /// NativeDraw.draw_layout. Devuelve la altura ocupada, en unidades
    /// lógicas, para poder posicionar contenido debajo.
    pub fn paragraph(&self, text: &str, x: f64, y: f64, width: f64, size: f64, color: Color32, weight: Weight) -> f64 {
        let font_id = FontId::new(size as f32, family_for(weight));
        let mut job = egui::text::LayoutJob::single_section(
            text.to_string(),
            egui::TextFormat { font_id, color, ..Default::default() },
        );
        job.wrap.max_width = width as f32;
        job.halign = egui::Align::Center;

        let galley = self.ui.ctx().fonts_mut(|f| f.layout_job(job));
        let height = galley.size().y as f64;
        self.painter().galley(Pos2::new(x as f32, y as f32), galley, color);
        height
    }
}
