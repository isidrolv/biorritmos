# Biorritmo (Kotlin)

Aplicación de escritorio Kotlin/JVM que reproduce la funcionalidad y el diseño
del proyecto `../ruby`: siete ciclos de biorritmo, navegación por día, temas
Sistema/Claro/Oscuro y leyenda interactiva para mostrar u ocultar cada curva.

## Requisitos

- JDK 21
- Maven 3.9 o superior

## Ejecutar

```powershell
mvn package
java -jar target/biorritmo-kotlin-1.0.0-app.jar
```

También se pueden ejecutar únicamente las pruebas con `mvn test`.

## Uso

Selecciona la fecha de nacimiento y la fecha que deseas analizar. Los botones
permiten avanzar o retroceder un día y volver a hoy. Haz clic sobre cualquier
fila de la leyenda para ocultar o mostrar su curva.
