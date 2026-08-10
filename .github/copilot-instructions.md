# Repository overview

This repository contains equivalent biorhythm calculators implemented independently by
technology. The functional applications are:

- `angular/`: Angular web application using standalone components and signals.
- `react/`: React/TypeScript web application built with Vite.
- `electron/`: Electron desktop application whose renderer mirrors the React application.

There is no root package manifest or shared source package. Run commands in the relevant
technology directory (or use `npm --prefix <directory> ...`). The other language directories
currently contain placeholders only.

## Build, test, and lint

Install each implementation's dependencies independently with `npm ci` in its directory.
Node requirements differ: React and Electron require Node `^20.19.0` or `>=22.12.0`; Angular
requires a version supported by Angular 22 (documented in `angular/README.md`).

From the repository root:

| Implementation | Development | Build | Tests | Lint |
| --- | --- | --- | --- | --- |
| Angular | `npm --prefix angular start` | `npm --prefix angular run build` | `npm --prefix angular test -- --watch=false` | No lint script |
| React | `npm --prefix react run dev` | `npm --prefix react run build` | No test suite | `npm --prefix react run lint` |
| Electron | `npm --prefix electron run dev` | `npm --prefix electron run build` | `npm --prefix electron test` | `npm --prefix electron run lint` |

Run one test file:

- Angular: `npm --prefix angular test -- --watch=false --include src/app/biorhythm.spec.ts`
- Electron: `npm --prefix electron test -- src/utils.spec.ts`

Run one named test with `--filter <regexp>` for Angular or `-t <regexp>` for Electron.
Electron's `npm start` performs a production build and then opens the desktop application.

## Architecture

All implementations model each cycle as
`sin(2 * PI * daysSinceBirth / period) * 100`. The shared product behavior includes seven
aspects, a chart spanning 15 days before and after the selected date, five-day date labels,
per-aspect visibility, and phase labels (`Crítico`, `Ascendente`, `Descendente`).

- Angular keeps domain types, aspect metadata, date helpers, chart constants, and scale
  functions in `angular/src/app/biorhythm.ts`. `app.ts` owns writable signals and derives the
  chart series with `computed`; standalone child components communicate through signal inputs
  and outputs.
- React keeps aspect metadata in `react/src/setup-datasets.ts`, chart constants in
  `react/src/variables.ts`, and date/calculation helpers in `react/src/utils.ts`. `App.tsx`
  owns state and memoizes the complete SVG series, while components render controls, the chart,
  and the aspect switch board.
- Electron has two processes. `electron/electron/main.ts` owns only the native window lifecycle
  and navigation policy. `electron/src/` is a local React renderer with the same structure and
  behavior as `react/src/`; no preload bridge or native API is currently required.

The implementations do not import from each other. When changing calculation rules, aspect
metadata, chart behavior, or user-visible behavior, inspect and update every functional
implementation unless the task is explicitly scoped to one platform.

## Repository-specific conventions

- Keep all user-facing text in Spanish and format chart dates with the `es-ES` locale.
- Treat date input values as local calendar dates: parse `YYYY-MM-DD` with
  `new Date(year, month - 1, day)`, normalize dates to midnight for day differences, and avoid
  UTC parsing that can shift the displayed date.
- Preserve the canonical aspect keys, periods, colors, dash patterns, and
  `basico`/`complementario` grouping across implementations.
- Preserve the critical-phase threshold: values with absolute magnitude below 3 are
  `Crítico`; otherwise compare the current value with the following day's value.
- Angular code uses standalone components, `ChangeDetectionStrategy.OnPush`, signal-based
  `input()`/`output()`, and signals/computed state rather than NgModules or mutable component
  state. Angular formatting is defined by `angular/.editorconfig` and `.prettierrc`.
- React and Electron use function components, controlled date inputs, and immutable state
  updates. Keep pure date and biorhythm calculations outside rendering components.
- Keep Electron's renderer isolated: retain `contextIsolation: true`, `sandbox: true`, and
  `nodeIntegration: false`. Open external links through `shell.openExternal`, block in-window
  navigation away from the application, and keep Vite's `base: './'` so packaged `file://`
  assets resolve correctly.
- If an implementation becomes functional or its status changes, update the status table in
  the root `README.md`; keep implementation-specific setup and commands in that
  implementation's README.
