//! Calculadora de biorritmo -- aplicación de escritorio en Rust que replica
//! la funcionalidad y apariencia del proyecto Ruby equivalente (../ruby):
//! calcula y grafica los ciclos físico, emocional e intelectual, junto con
//! los aspectos complementarios (espiritual, conciencia, intuición y
//! estética), a partir de una fecha de nacimiento.

mod aspects;
mod biorhythm;
mod gfx;
mod theme;
mod ui;

use ui::app::App;

fn main() -> eframe::Result {
    let options = eframe::NativeOptions {
        viewport: egui::ViewportBuilder::default().with_inner_size([900.0, 840.0]),
        ..Default::default()
    };

    eframe::run_native(
        "Calculadora de biorritmo",
        options,
        Box::new(|cc| {
            gfx::load_fonts(&cc.egui_ctx);
            Ok(Box::new(App::new()))
        }),
    )
}
