# Biorritmos con React

Base del frontend de la calculadora de biorritmos, preparada con React, TypeScript y Vite.

> [!IMPORTANT]
> La configuración del proyecto está creada, pero el repositorio todavía no incluye el directorio `src/`. Por ello, los comandos de desarrollo y compilación no producirán una aplicación funcional hasta que se añada, como mínimo, el punto de entrada `src/main.tsx` referenciado por `index.html`.

## Tecnologías

- React 19
- TypeScript 6
- Vite 8 con `@vitejs/plugin-react`
- ESLint 10
- npm y archivo de bloqueo `package-lock.json`

## Requisitos

- Node.js `^20.19.0` o `>=22.12.0`, según el requisito de Vite 8
- npm, incluido con Node.js

## Instalación

Desde la raíz del repositorio:

```bash
cd react
npm install
```

Para una instalación estrictamente reproducible, especialmente en integración continua, puede usarse `npm ci` en lugar de `npm install`.

## Comandos disponibles

Todos los comandos se ejecutan dentro de `react/`:

| Comando | Descripción |
| --- | --- |
| `npm run dev` | Inicia el servidor de desarrollo de Vite. |
| `npm run build` | Comprueba TypeScript y genera la versión de producción en `dist/`. |
| `npm run lint` | Analiza los archivos con ESLint. |
| `npm run preview` | Sirve localmente el contenido generado en `dist/`. |

Cuando exista el código fuente, el flujo habitual será:

```bash
npm run dev
```

Vite mostrará en la terminal la URL local del servidor. Para comprobar una entrega de producción:

```bash
npm run build
npm run preview
```

## Estructura actual

```text
react/
├── index.html          # Documento HTML y montaje de /src/main.tsx
├── package.json        # Dependencias y scripts npm
├── package-lock.json   # Versiones resueltas de las dependencias
├── tsconfig.json       # Configuración raíz de TypeScript
├── tsconfig.app.json   # Configuración TypeScript del navegador
├── tsconfig.node.json  # Configuración TypeScript de Vite
└── vite.config.ts      # Configuración de Vite y React
```

La estructura prevista para comenzar la aplicación es:

```text
src/
└── main.tsx            # Punto de entrada requerido por index.html
```

## Estado pendiente

Antes de considerar utilizable este frontend hace falta incorporar el código de `src/`, implementar el cálculo y la interfaz de biorritmos, y añadir pruebas. La documentación debe ampliarse conforme esas decisiones se materialicen.

## Licencia

Esta implementación forma parte del repositorio Biorritmos y se distribuye bajo la [GNU General Public License v3.0](../LICENSE).
