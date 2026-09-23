# Biorritmo en Go

Aplicación de escritorio en Go que replica la implementación Ruby ([`../ruby`](../ruby)): calcula y grafica los ciclos físico, emocional e intelectual junto con los aspectos complementarios (espiritual, conciencia, intuición y estética), a partir de una fecha de nacimiento. La GUI usa [Gio](https://gioui.org/).

## Ejecutar

```bash
go run .          # compila y abre la ventana (900x840)
go build -o biorritmo.exe .   # binario estático
```

## Arquitectura

```
┌──────────────────────────────────────── Biorritmo Go ────────────────────────────────────────┐
│                                                                                              │
│  ┌─────────────────────────────── main.go ────────────────────────────┐                     │
│  │  Punto de entrada · ventana Gio (app.Window) · loop de frames     │                     │
│  └──────────────────────────────────┬─────────────────────────────────┘                     │
│                                     │ FrameEvent → Layout                                       │
│  ┌──────────────────────────────▼──────────────────────────────────────┐                    │
│  │                          Capa UI · internal/ui                       │                   │
│  │  app.go · button.go · datefield.go                                   │                   │
│  │  topbar de tema · campos de fecha · navegación · leyenda            │                   │
│  └──────────┬──────────────────────┬──────────────────────┬────────────┘                   │
│             │ dibuja               │ paleta                │ calcula series                    │
│  ┌──────────▼──────┐   ┌───────────▼────────┐   ┌─────────▼──────────────┐             │
│  │ internal/gfx    │   │ internal/theme     │   │ internal/biorhythm     │             │
│  │ Canvas 760x340  │   │ claro/oscuro ·     │   │ Compute() · ValueAt ·  │             │
│  │ rect·línea·texto│   │ System/Light/Dark  │   │ PhaseLabel             │             │
│  └────┬────────────┘   └────────────────────┘   └─────────┬──────────────┘             │
│       │                                                    │ usa                          │
│  ┌────▼─────────────┐                          ┌───────────▼──────────────┐            │
│  │ Gio (gioui.org)  │                          │ internal/aspects          │            │
│  │ app · layout ·   │                          │ 7 ciclos (23–53 días)     │            │
│  │ op · paint       │                          │ color y trazo por aspecto │            │
│  └──────────────────┘                          └───────────────────────────┘            │
│                                                                                          │
│  ┌─────────────────────────────── OS · Windows ────────────────────────────┐            │
│  │  escalado DPI (PxPerDp)                                                │            │
│  └──────────────────────────────────────────────────────────────────────────┘            │
└──────────────────────────────────────────────────────────────────────────────────────────┘
```

Diagrama interactivo (tema oscuro, colores por capa): **[abrir `biorritmo-architecture.html`](biorritmo-architecture.html)**.

### Vista previa

| Capa | Paquetes | Responsabilidad |
| --- | --- | --- |
| **UI** | `main.go`, `internal/ui` | Ventana, controles de fechas, navegación, leyenda interactiva |
| **Motor** | `internal/biorhythm`, `internal/aspects` | Cálculo de las 7 series y sus fases |
| **Renderizado** | `internal/gfx`, `internal/theme` | Primitivas de dibujo y paleta claro/oscuro |

## Paquetes

| Paquete | Descripción |
| --- | --- |
| `internal/ui` | App y widgets: topbar de tema, campos de fecha, botones de navegación, leyenda clicable |
| `internal/biorhythm` | `Compute()` genera 31 días en torno a la fecha seleccionada, proyecta a coordenadas de gráfica y calcula valor actual y fase |
| `internal/aspects` | Define los 7 ciclos con su periodo, color y patrón de trazo |
| `internal/gfx` | Canvas en unidades lógicas (760x340) con escalado por DPI |
| `internal/theme` | Paleta de colores por modo de tema (Sistema / Claro / Oscuro) |

## Aspectos

Básicos: físico (23d), emocional (28d), intelectual (33d).
Complementarios: espiritual (53d), conciencia (48d), intuición (38d), estética (43d).

## Validación

```bash
go vet ./...
go build ./...
```
