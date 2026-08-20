# Biorritmo (Ruby)

Aplicación de escritorio en Ruby puro (sin gems) que calcula el biorritmo (físico,
emocional, intelectual y los aspectos complementarios: espiritual, conciencia,
intuición y estética) a partir de la fecha de nacimiento. El diseño reproduce el
de `../react`, conservando su paleta de colores, y añade un selector de tema
(Sistema / Claro / Oscuro).

## Cómo funciona

`app.rb` levanta un servidor HTTP local (solo con la librería estándar de Ruby:
`socket`, `json`, `cgi`) que calcula las series de biorritmo en `lib/biorhythm.rb`
y sirve la interfaz (`public/`). Luego abre una ventana de Microsoft Edge o
Google Chrome en modo aplicación (`--app`, sin barra de direcciones) apuntando a
ese servidor, para que se comporte como una app de escritorio. Si no encuentra
ninguno de los dos, abre tu navegador predeterminado.

## Ejecutar

```
ruby app.rb
```

Cierra la ventana de la aplicación para terminar (o Ctrl+C en la terminal si se
abrió como pestaña del navegador predeterminado).

## Estructura

- `app.rb` — punto de entrada: arranca el servidor y abre la ventana.
- `lib/aspects.rb` — datos de los 7 aspectos del biorritmo (periodo, color, trazo).
- `lib/biorhythm.rb` — cálculo del biorritmo (equivalente a `utils.ts` del proyecto React).
- `lib/server.rb` — servidor HTTP mínimo (TCPServer) y endpoint `/api/biorhythm`.
- `lib/browser_launcher.rb` — abre la ventana de la app en Windows/macOS/Linux.
- `public/` — HTML, CSS y JS de la interfaz (mismo diseño que `../react`).
