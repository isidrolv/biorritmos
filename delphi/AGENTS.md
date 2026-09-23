---
name: crear-formulario-vcl
description: Crea un nuevo formulario VCL siguiendo las convenciones del proyecto Delphi
when-to-use: crear formulario, nuevo form, agregar form VCL, crear ventana
---

# Skill: Crear Formulario VCL

Cuando el usuario pida crear un nuevo formulario VCL, sigue estos pasos:

1. Pregunta (si no lo dijo):
   - Nombre del formulario (ejemplo: `Clientes`, `Configuracion`, etc.)
   - Si debe ser modal o normal
   - Si necesita DataModule o solo formulario

2. Genera:
   - La unidad `.pas` con el nombre correcto (`uFrmNombre` o `FrmNombre`)
   - El archivo `.dfm`
   - La clase `TFrmNombre` heredando de `TForm`

3. Usa estas convenciones:
   - Nombre de la clase: `TFrmNombre`
   - Nombre del formulario: `FrmNombre`
   - Unidad: `uFrmNombre.pas` (o el estilo que use el proyecto)

4. Agrega el formulario al proyecto `.dproj` si es posible.

5. Muestra el código completo generado y espera confirmación antes de escribir los archivos (si el usuario no dijo que lo haga automáticamente).

6. Después de crear los archivos, indica cómo abrir el formulario desde otro lugar (ejemplo de código).
