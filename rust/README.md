# Biorritmo (Rust)

Aplicación de escritorio nativa desarrollada con Rust, `eframe` y `egui`. Reproduce
la funcionalidad, distribución, textos, temas y colores de la implementación Ruby:

- calcula los siete ciclos de biorritmo;
- grafica los 15 días anteriores y posteriores a la fecha analizada;
- permite cambiar las fechas y navegar por día;
- muestra valores y fases (`Crítico`, `Ascendente`, `Descendente`);
- permite ocultar o mostrar cada aspecto;
- ofrece los temas Sistema, Claro y Oscuro.

## Requisitos

- Rust estable y Cargo.
- En Windows usa Segoe UI cuando está disponible y respeta el tema configurado
  para las aplicaciones del sistema.

## Ejecutar

```powershell
cargo run
```

## Validar

```powershell
cargo test
cargo clippy --all-targets -- -D warnings
cargo fmt -- --check
```

## Compilar para distribución

```powershell
cargo build --release
```

El ejecutable se genera en `target\release\biorritmo.exe` en Windows.
