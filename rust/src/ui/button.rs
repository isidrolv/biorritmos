//! Botón plano simple (rectángulo con borde), parecido al estilo de los
//! botones nativos de Windows que usa el proyecto Ruby. Replica button.go.

use crate::gfx::{Align, Canvas, Weight};
use crate::theme::Colors;

const PAD_X: f64 = 14.0;
const PAD_Y: f64 = 6.0;
const BORDER: f64 = 1.0;
const TEXT_SZ: f64 = 13.0;

pub struct Button {
    pub text: &'static str,
}

impl Button {
    pub fn new(text: &'static str) -> Self {
        Self { text }
    }

    /// Devuelve el tamaño (ancho, alto) que ocupará el botón.
    pub fn measure(&self, canvas: &Canvas) -> (f64, f64) {
        let (tw, th) = canvas.measure_text(self.text, TEXT_SZ, Weight::Normal);
        (tw + PAD_X * 2.0, th + PAD_Y * 2.0)
    }

    /// Dibuja el botón en (x, y) y, si enabled, registra su zona clicable.
    /// Devuelve si fue pulsado en este frame.
    #[allow(clippy::too_many_arguments)]
    pub fn show(
        &self,
        canvas: &Canvas,
        id_source: impl std::hash::Hash + std::fmt::Debug,
        x: f64,
        y: f64,
        colors: &Colors,
        selected: bool,
        enabled: bool,
    ) -> bool {
        let (w, h) = self.measure(canvas);

        let bg = if selected { colors.gridline } else { colors.bg };
        let text_col = if enabled {
            colors.text_h
        } else {
            colors.legend_dim
        };
        let border_col = if enabled {
            colors.marker_line
        } else {
            colors.legend_dim
        };

        canvas.fill_rect(x, y, w, h, bg);
        canvas.fill_rect(x, y, w, BORDER, border_col);
        canvas.fill_rect(x, y + h - BORDER, w, BORDER, border_col);
        canvas.fill_rect(x, y, BORDER, h, border_col);
        canvas.fill_rect(x + w - BORDER, y, BORDER, h, border_col);

        canvas.text(
            self.text,
            x + PAD_X,
            y + PAD_Y,
            TEXT_SZ,
            text_col,
            Weight::Normal,
            Align::Left,
        );

        if enabled {
            canvas.interact_click(x, y, w, h, id_source).clicked()
        } else {
            false
        }
    }
}
