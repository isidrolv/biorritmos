# Biorritmos con React

Calculadora web de biorritmos desarrollada con React, TypeScript y Vite.

## Funcionalidad

- Selección de fecha de nacimiento y fecha de consulta.
- Navegación entre el día anterior, el día actual y el día siguiente.
- Gráfica para un intervalo de 15 días antes y después de la fecha seleccionada.
- Ciclos básicos: físico, emocional e intelectual.
- Ciclos complementarios: espiritual, conciencia, intuición y estética.
- Activación y desactivación individual de cada ciclo.
- Valor porcentual y fase ascendente, descendente o crítica de cada aspecto.

## Tecnologías

- React 19
- TypeScript 6
- Vite 8 con `@vitejs/plugin-react`
- ESLint 10

## Requisitos

- Node.js `^20.19.0` o `>=22.12.0`, según el requisito de Vite 8
- npm, incluido con Node.js

## Instalación y ejecución

Desde la raíz del repositorio:

```bash
cd react
npm install
npm run dev
```

Vite mostrará en la terminal la URL local del servidor.

## Comandos disponibles

| Comando | Descripción |
| --- | --- |
| `npm run dev` | Inicia el servidor de desarrollo. |
| `npm run build` | Comprueba TypeScript y genera la aplicación en `dist/`. |
| `npm run lint` | Analiza el código con ESLint. |
| `npm run preview` | Sirve localmente la compilación de producción. |

Para una instalación reproducible en integración continua puede utilizarse `npm ci`.

## Estructura principal

```text
react/
├── public/             # Recursos estáticos
├── src/
│   ├── components/     # Formulario, gráfica, resultados y controles
│   ├── App.tsx         # Estado y composición de la aplicación
│   ├── setup-datasets.ts # Definición de los ciclos
│   ├── utils.ts        # Fechas y cálculo de biorritmos
│   └── main.tsx        # Punto de entrada
├── eslint.config.js
├── package.json
└── vite.config.ts
```

## Cálculo

Cada ciclo se representa mediante una onda sinusoidal:

```text
valor = sen(2π × días desde el nacimiento / periodo) × 100
```

Los resultados son una referencia recreativa y no constituyen información médica ni científica.

## Validación

Antes de confirmar cambios ejecuta:

```bash
npm run lint
npm run build
```

## Licencia

Esta implementación forma parte del repositorio Biorritmos y se distribuye bajo la [GNU General Public License v3.0](../LICENSE).
