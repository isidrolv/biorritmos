# Biorritmos con Angular

Versión Angular de la calculadora web de biorritmos. Replica la funcionalidad y la apariencia de la implementación React mediante componentes standalone y signals.

## Funcionalidad

- Selección de fecha de nacimiento y fecha de consulta.
- Navegación entre el día anterior, el día actual y el día siguiente.
- Gráfica SVG para un intervalo de 15 días antes y después de la fecha seleccionada.
- Ciclos básicos: físico, emocional e intelectual.
- Ciclos complementarios: espiritual, conciencia, intuición y estética.
- Activación y desactivación individual de cada ciclo.
- Valor porcentual y fase ascendente, descendente o crítica de cada aspecto.

## Tecnologías

- Angular 22 con componentes standalone
- TypeScript 6
- Signals para el estado y los cálculos derivados
- Angular CLI 22

## Requisitos

- Node.js `^22.22.3`, `^24.15.0` o `^26.0.0`
- npm

## Instalación y ejecución

Desde la raíz del repositorio:

```bash
cd angular
npm install
npm start
```

La aplicación estará disponible normalmente en `http://localhost:4200/`.

## Comandos disponibles

| Comando         | Descripción                                       |
| --------------- | ------------------------------------------------- |
| `npm start`     | Inicia el servidor de desarrollo.                 |
| `npm run build` | Compila la aplicación para producción en `dist/`. |
| `npm test`      | Ejecuta las pruebas con Vitest.                   |
| `npm run watch` | Compila en modo desarrollo al detectar cambios.   |

Para una instalación reproducible en integración continua puede utilizarse `npm ci`.

## Estructura principal

```text
angular/
├── public/                         # Recursos estáticos
├── src/app/
│   ├── components/
│   │   ├── biorhythm-header.ts     # Encabezado descriptivo
│   │   ├── biorhythm-input-form.ts # Fechas y navegación
│   │   ├── biorhythm-histogram.ts  # Gráfica SVG
│   │   └── biorhythm-switch-board.ts # Selector de ciclos
│   ├── app.ts                      # Estado y composición
│   ├── app.html                    # Vista principal
│   └── biorhythm.ts                # Modelos, datos y cálculos
├── angular.json
└── package.json
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
npm test
npm run build
```

## Licencia

Esta implementación forma parte del repositorio Biorritmos y se distribuye bajo la [GNU General Public License v3.0](../LICENSE).
