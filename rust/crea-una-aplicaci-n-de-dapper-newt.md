# Port de la calculadora de biorritmo a Rust (carpeta `rust/`)

## Contexto

El repo ya tiene el proyecto original en Ruby (`../ruby`) y una versión ya portada en Go (`../golang`) que replica su apariencia y funcionalidad. La carpeta `rust/` está vacía (solo `.gitkeep`). El objetivo es crear ahí una app de escritorio en Rust con **exactamente la misma apariencia, colores y funcionalidad** que la versión Ruby: misma paleta claro/oscuro, mismos 7 ciclos (colores/periodos/dash), mismo layout (encabezado + gráfica + leyenda), mismos controles (fecha de nacimiento, fecha a analizar, navegación Anterior/Hoy/Siguiente, selector de tema).

**Hallazgo importante de la investigación**: en esta máquina no hay Rust instalado (ni `rustc`/`cargo`/`rustup` en PATH) ni tampoco un compilador C/C++ (ni `cl.exe` de MSVC ni `g++` de MinGW en PATH). Esto descarta usar bindings directos a `libui-ng` (el mismo motor nativo que usa la gema Ruby) como primera opción, porque el crate `libui-rs/libui` compila `libui-ng` desde C++ en cada build y exige un toolchain C++ completo (Visual Studio Build Tools o MinGW-w64) además de `git`. Como el usuario no respondió a la pregunta sobre qué enfoque prefiere, se sigue el patrón que **ya funcionó en la versión Go**: usar una librería GUI de modo inmediato 100% Rust (sin compilador C/C++), dibujando a mano cada control para igualar pixel a pixel el look de Ruby — igual que hizo `golang/internal/ui` con Gio. Esto minimiza fricción de instalación (solo hace falta `rustup`) y ya está probado que produce una apariencia fiel.

Se usará **egui + eframe** (crate GUI inmediata más popular en Rust, sin dependencias nativas de compilación) como equivalente directo de Gio. La traducción es casi 1:1 desde los archivos ya leídos de `golang/internal/{ui,gfx,theme,biorhythm,aspects}` y del Ruby original (`ruby/app.rb`, `ruby/lib/*.rb`), que ya están completamente auditados (colores exactos, fórmulas, tamaños, fuentes).

**Prerrequisito para el usuario**: instalar Rust vía `rustup` (https://rustup.rs) antes de compilar — esto sí es indispensable y no tiene forma de evitarse. No hace falta ningún compilador C/C++ adicional con este enfoque.

## Especificación exacta a replicar (ya confirmada, ver Ruby/Go leídos)

- Ventana: título "Calculadora de biorritmo", tamaño inicial 900×840.
- Lienzo lógico de 760×614 centrado horizontalmente: header (92px) + gráfica (760×340, márgenes top16/right16/bottom34/left44) + leyenda (150px), con gap de 16px entre secciones.
- 7 aspectos (`fisico` #1656c9 sólido/23d, `emocional` #d32f2f sólido/28d, `intelectual` #1f9254 sólido/33d, `espiritual` #7c3aed dash "7 4"/53d, `conciencia` #0891b2 dash "1 4"/48d, `intuicion` #d97706 dash "9 3 2 3"/38d, `estetica` #a3195b dash "3 3"/43d), agrupados en "Aspectos básicos" / "Aspectos complementarios".
- Fórmula: `valor = sin(2π·días_desde_nacimiento/periodo)×100`; rango graficado = fecha seleccionada ±15 días (31 puntos); estado "Crítico" si `|valor|<3`, si no "Ascendente"/"Descendente" según el valor del día siguiente.
- Paletas claro/oscuro exactas (hex) de `theme.rb`/`theme.go`, con detección de tema del sistema vía registro `HKCU\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize\AppsUseLightTheme`.
- Tipografía: "Segoe UI" (normal/medium/bold) en los tamaños exactos ya documentados (22/12/11/12/14/13 según elemento).
- Interacción: click en una fila de la leyenda alterna la visibilidad de esa serie; botón "Hoy" deshabilitado cuando la fecha analizada ya es hoy; selectores de fecha día/mes/año con flechas (como en Go, ya que no hay date-picker nativo en egui).

## Estructura de archivos a crear

```
rust/
├── Cargo.toml                     (bin, deps: eframe, egui, winreg, chrono o time)
└── src/
    ├── main.rs                    (arranque eframe::run_native, título/tamaño ventana)
    ├── aspects.rs                 (port 1:1 de aspects.rb / aspects.go: struct Aspect, const LIST)
    ├── biorhythm.rs                (port 1:1 de biorhythm.rb/.go: value_at, phase_label, format_short, compute/series)
    ├── theme.rs                    (paletas Light/Dark, lectura de registro con crate `winreg`)
    ├── gfx.rs                      (helpers de dibujo sobre egui::Painter: fill_rect, stroke_line/polyline con dash manual, fill_circle, ring_circle, text alineado izq/centro/der, paragraph con wrap que retorna alto)
    └── ui/
        ├── mod.rs
        ├── app.rs                  (struct App = estado, impl eframe::App::update: topbar, controles, canvas — port de app.go/app.rb)
        ├── button.rs                (botón plano dibujado a mano, estilo Windows: rect + borde 1px + texto)
        └── datefield.rs             (selector día/mes/año con flechas arriba/abajo, como datefield.go)
```

## Detalles de implementación por archivo

- **`aspects.rs`**: `struct Aspect { key, label, period: f64, color: &'static str, dash: &'static str, group: &'static str }` y `const LIST: [Aspect; 7]` con los valores exactos de arriba. Funciones `basico()`/`complementario()` filtrando por grupo (usadas para las dos columnas de leyenda).

- **`biorhythm.rs`**: usar la crate `time` o `chrono` para fechas (recomendado `time::Date`, ligera, sin timezone). Traer literal: `RANGE_DAYS=15`, `CHART_WIDTH=760.0`, `CHART_HEIGHT=340.0`, márgenes `{top:16,right:16,bottom:34,left:44}`, `value_at`, `phase_label`, `format_short` (meses abreviados en español), y `compute(birth, selected) -> Series` con gridlines, center_x, marker_label, date_labels, lines (con puntos ya escalados a coordenadas del lienzo, current_value redondeado, status, marker_x/y).

- **`theme.rs`**: dos structs `Colors` (mismos 9 campos que Go: bg, text_h, muted, label, gridline, gridline_zero, marker_line, axis_label, legend_dim) con los hex exactos de Light/Dark. `enum ThemeMode { System, Light, Dark }`. `fn for_mode(mode) -> Colors` que en `System` llama a `system_is_dark()` leyendo el registro con el crate `winreg` (`RegKey::predef(HKEY_CURRENT_USER).open_subkey(...)`, leer `AppsUseLightTheme` como u32, dark si es 0; cualquier error → false/claro, igual que Ruby/Go). Un `hex_to_color32(&str) -> egui::Color32` helper.

- **`gfx.rs`** (equivalente a `native_draw.rb` / `canvas.go`): funciones que reciben `&egui::Painter` (o se agrupan en un `struct Canvas<'a>{ painter: &'a Painter, fonts_loaded: bool }`):
  - `fill_rect`, `stroke_line`/`stroke_polyline` (con soporte de patrón de dash **manual**, recorriendo segmentos y alternando trazo/hueco — igual algoritmo que `canvas.go` `StrokePolyline`, ya que egui no tiene stroke con dash nativo).
  - `fill_circle`, `ring_circle` (círculo relleno + anillo blanco encima, replicando el `fill_circle`+`stroke_circle blanco` de Ruby para los marcadores de la gráfica, y fill/hueco para los puntos de la leyenda).
  - `text(txt, pos, size, color, weight, align)` usando `painter.text(...)` con `FontId` (familia "Segoe UI" si se cargó, si no la default de egui) y anclas Left/Center/Right vía `egui::Align2`.
  - `paragraph(txt, x, y, width, size, color, weight) -> f32` (alto usado): construir un `egui::text::LayoutJob` con `wrap.max_width = width`, alineado centrado, medir con `ctx.fonts(|f| f.layout_job(job))`, dibujar con `painter.galley(...)`, devolver `galley.rect.height()` — replica `draw_layout`/`Paragraph`.
  - `fill_polygon` para las flechitas triangulares del selector de fecha.
  - Carga de fuente: en `main.rs`, al iniciar, intentar leer `%WINDIR%\Fonts\segoeui.ttf`, `seguisb.ttf`, `segoeuib.ttf` y registrarlas en `egui::FontDefinitions` bajo nombres propios ("Segoe UI"/"Segoe UI Semibold"/"Segoe UI Bold"), igual que `gfx/fonts.go`; si no existen, usar la fuente default de egui sin fallar.

- **`ui/button.rs`**: struct `Button { text: String }` con `fn measure(...) -> egui::Vec2` y `fn show(&self, canvas, pos, colors, selected, enabled) -> bool` (dibuja rect + borde 1px + texto, detecta click con `ui.interact` sobre un `Rect` en las coordenadas dadas), replicando `button.go` (fondo `colors.gridline` si `selected`, texto/borde atenuados con `legend_dim` si `!enabled`).

- **`ui/datefield.rs`**: struct `DateField` sin estado propio más que lo necesario; `fn show(canvas, pos, date, colors) -> (Date, bool)` dibujando 3 cajas (día/mes/año) cada una con flechas arriba/abajo que sub/suman día, mes (con clamp de día al fin de mes) o año — replica `datefield.go` con las mismas medidas (caja 26px alto, día/mes 30px, año 46px, flecha 16px).

- **`ui/app.rs`**: struct `App` con `theme_mode`, `visible: HashMap<&str,bool>`, `birth_date`, `selected_date`, `series: Series` recalculada tras cualquier cambio. `impl eframe::App for App { fn update(&mut self, ctx, frame) }`:
  - `egui::CentralPanel::default()` con fondo = `colors.bg` (via `Frame::fill`).
  - Barra superior: label "Tema:" + 3 `Button` (Sistema/Claro/Oscuro) alineados a la derecha.
  - Fila de controles: labels + `DateField`×2 + botones "< Anterior"/"Hoy"/"Siguiente >" centrados como en `app.go::layoutControls` (mismas medidas y gaps).
  - Área de canvas restante: dibuja header (título 22px medium + subtítulo 12px wrap, texto exacto: *"Ingresa tu fecha de nacimiento para graficar tus ciclos físico, emocional e intelectual, junto con los aspectos complementarios: espiritual, conciencia, intuición y estética."*), luego la gráfica (gridlines, línea vertical punteada del día marcado, etiquetas de fecha cada 5 días, las 7 series con su color/dash y marcador circular blanco+color), luego la leyenda en 2 columnas con nombre/porcentaje/estado y su punto de color (relleno si visible, contorno si oculto) — click en una fila alterna `visible[key]` vía `response.clicked()` sobre el `Rect` de esa fila.
  - Tras cualquier cambio de fecha o navegación, recalcular `series = biorhythm::compute(birth_date, selected_date)`.

- **`main.rs`**: `eframe::run_native("Calculadora de biorritmo", NativeOptions { viewport: ViewportBuilder::default().with_inner_size([900.0, 840.0]), ..Default::default() }, Box::new(|cc| { /* cargar fuentes */ Ok(Box::new(App::new())) }))`.

- **`Cargo.toml`**: dependencias `eframe`, `egui` (últimas versiones estables), `winreg` (lectura de registro en Windows), `time` (fechas, con feature `formatting`/`macros` si hace falta) — todas crates puramente Rust, sin build scripts nativos aparte de lo que ya trae eframe (winit + wgpu/glow, estándar y sin fricción en Windows).

## Verificación

1. `cd rust && cargo build` debe compilar sin errores (primera vez descargará/compilará winit/wgpu, tardará varios minutos).
2. `cargo run`: comprobar visualmente que la ventana abre a 900×840 con el título correcto, que la gráfica, colores y textos coinciden con capturas/lectura del proyecto Ruby (comparar valores concretos: p.ej. con fecha de nacimiento = hoy−25 años, día 1, revisar que el gráfico y los porcentajes de la leyenda tengan sentido).
3. Probar interacción: cambiar fecha de nacimiento y fecha a analizar con las flechas del `DateField`, navegar con "< Anterior"/"Siguiente >", verificar que "Hoy" se deshabilita al estar ya en la fecha actual, cambiar tema (Sistema/Claro/Oscuro) y confirmar que los colores cambian según lo esperado, y hacer click en filas de la leyenda para ocultar/mostrar series.
4. Redimensionar la ventana y confirmar que el contenido de 760px de ancho permanece centrado (igual que `max((width-760)/2, 0)` en Ruby/Go).
