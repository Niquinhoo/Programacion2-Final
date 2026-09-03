# Diagramas para la exposición — RestoManager

Esta carpeta contiene una serie de láminas HTML autónomas para explicar el proyecto con una correspondencia directa entre el flujo visual y el código actual.

## Orden sugerido para exponer

1. [01 — Arquitectura por capas](01-arquitectura.html) — cómo se conectan la GUI Swing, los servicios, los DAO y MySQL.
2. [02 — Autenticación](02-autenticacion.html) — validación de campos, ejecución asíncrona, hash SHA-256 y apertura del menú.
3. [03 — Alta de pedido](03-alta-pedido.html) — preparación de ítems, validación de stock, transacción JDBC y actualización de mesa.
4. [04 — Estados de mesa](04-estados-mesa.html) — reglas permitidas para `LIBRE`, `RESERVADA`, `OCUPADA` y `FUERA_DE_SERVICIO`.
5. [05 — Flujo de reportes](05-reportes.html) — vistas SQL, DTOs y gráficos JFreeChart.

También se puede abrir el [índice visual](index.html) para presentar las cinco láminas desde una sola página.

## Cómo leer cada lámina

- El diagrama resume el comportamiento y nombra los métodos reales.
- Las tarjetas inferiores muestran fragmentos tomados del repositorio, con enlace al archivo fuente.
- La sección **Qué decir** funciona como guion oral para defender la decisión técnica.

## Decisiones de fidelidad

- Se usó la paleta predeterminada de Diagram Design porque fue elegida explícitamente para este proyecto.
- La audiencia es mixta: se explican los conceptos en lenguaje directo y se conserva el nombre real de las clases y métodos.
- La vista se basa en el código fuente actual, especialmente `Menu.java`, `Login.java`, los servicios de `Backend` y `schema.sql`; algunos documentos históricos del repositorio describen estados anteriores de integración.
- Se mantuvo el límite de complejidad de cada figura: una idea principal por lámina y no más de nueve elementos conceptuales en los flujos principales.
