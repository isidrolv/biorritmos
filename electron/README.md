# Biorritmos con Electron

Aplicación de escritorio que replica la funcionalidad y la apariencia de la calculadora React mediante Electron, React, TypeScript y Vite.

## Funcionalidad

- Selección de fecha de nacimiento y fecha de consulta.
- Navegación entre el día anterior, el día actual y el día siguiente.
- Gráfica SVG para un intervalo de 15 días antes y después de la fecha seleccionada.
- Ciclos básicos: físico, emocional e intelectual.
- Ciclos complementarios: espiritual, conciencia, intuición y estética.
- Activación individual de cada ciclo, valor porcentual y fase actual.

## Requisitos

- Node.js `^20.19.0` o `>=22.12.0`
- npm

## Instalación y desarrollo

```bash
cd electron
npm install
npm run dev
```

Vite inicia el renderer y Electron abre la ventana de escritorio automáticamente.

## Comandos

| Comando | Descripción |
| --- | --- |
| `npm run dev` | Ejecuta Vite y Electron en modo desarrollo. |
| `npm run build` | Compila el renderer y el proceso principal. |
| `npm start` | Compila y abre la aplicación de escritorio. |
| `npm test` | Ejecuta las pruebas de cálculo con Vitest. |
| `npm run lint` | Analiza TypeScript y React con ESLint. |

## Arquitectura y seguridad

- `electron/main.ts`: ciclo de vida y ventana nativa.
- `src/`: renderer React equivalente a la aplicación web.
- `dist/`: renderer compilado.
- `dist-electron/`: proceso principal compilado.

El renderer utiliza aislamiento de contexto, sandbox y `nodeIntegration: false`. No expone APIs nativas porque la calculadora funciona completamente de forma local.

Los resultados son una referencia recreativa y no constituyen información médica ni científica.

## Licencia

Esta implementación forma parte del repositorio Biorritmos y se distribuye bajo la [GNU General Public License v3.0](../LICENSE).
