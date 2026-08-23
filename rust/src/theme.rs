//! Define la paleta de colores claro/oscuro, replicando lib/theme.rb del
//! proyecto Ruby.

use egui::Color32;

#[derive(Clone, Copy, PartialEq, Eq, Debug)]
pub enum Mode {
    System,
    Light,
    Dark,
}

pub struct Colors {
    pub bg: Color32,
    pub text_h: Color32,
    pub muted: Color32,
    pub label: Color32,
    pub gridline: Color32,
    pub gridline_zero: Color32,
    pub marker_line: Color32,
    pub axis_label: Color32,
    pub legend_dim: Color32,
}

pub fn hex(s: &str) -> Color32 {
    if s.len() != 7 || !s.starts_with('#') {
        return Color32::BLACK;
    }
    let r = u8::from_str_radix(&s[1..3], 16).unwrap_or(0);
    let g = u8::from_str_radix(&s[3..5], 16).unwrap_or(0);
    let b = u8::from_str_radix(&s[5..7], 16).unwrap_or(0);
    Color32::from_rgb(r, g, b)
}

pub const WHITE: Color32 = Color32::from_rgb(0xff, 0xff, 0xff);

fn light_colors() -> Colors {
    Colors {
        bg: hex("#ffffff"),
        text_h: hex("#1a1a1a"),
        muted: hex("#555555"),
        label: hex("#333333"),
        gridline: hex("#e5e4e7"),
        gridline_zero: hex("#b8b6bd"),
        marker_line: hex("#999999"),
        axis_label: hex("#777777"),
        legend_dim: hex("#aaaaaa"),
    }
}

fn dark_colors() -> Colors {
    Colors {
        bg: hex("#16171d"),
        text_h: hex("#f3f4f6"),
        muted: hex("#9ca3af"),
        label: hex("#d1d5db"),
        gridline: hex("#2e303a"),
        gridline_zero: hex("#4b4d59"),
        marker_line: hex("#6b6d78"),
        axis_label: hex("#9ca3af"),
        legend_dim: hex("#6b6d78"),
    }
}

#[cfg(windows)]
fn system_is_dark() -> bool {
    use winreg::enums::HKEY_CURRENT_USER;
    use winreg::RegKey;

    let hkcu = RegKey::predef(HKEY_CURRENT_USER);
    let result = hkcu
        .open_subkey(r"Software\Microsoft\Windows\CurrentVersion\Themes\Personalize")
        .and_then(|key| key.get_value::<u32, _>("AppsUseLightTheme"));

    match result {
        Ok(v) => v == 0,
        Err(_) => false,
    }
}

#[cfg(not(windows))]
fn system_is_dark() -> bool {
    false
}

pub fn for_mode(mode: Mode) -> Colors {
    match mode {
        Mode::Dark => dark_colors(),
        Mode::Light => light_colors(),
        Mode::System => {
            if system_is_dark() {
                dark_colors()
            } else {
                light_colors()
            }
        }
    }
}
