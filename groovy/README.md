# Biorritmo (Groovy)

Aplicación de escritorio Groovy/JVM que reproduce la funcionalidad y el diseño
del proyecto `../ruby`: siete ciclos de biorritmo, navegación por día, temas
Sistema/Claro/Oscuro y leyenda interactiva para mostrar u ocultar cada curva.

## Requisitos

- JDK 21
- Maven 3.9 o superior

## Compilar y ejecutar

```powershell
mvn package
java -jar target/biorritmo-groovy-1.0.0-app.jar
```

Para ejecutar únicamente las pruebas:

```powershell
mvn test
```

## Uso

Selecciona la fecha de nacimiento y la fecha que deseas analizar. Los botones
permiten avanzar o retroceder un día y volver a hoy. Haz clic sobre cualquier
fila de la leyenda para ocultar o mostrar su curva. El selector de tema permite
usar la apariencia del sistema o forzar la paleta clara u oscura.
