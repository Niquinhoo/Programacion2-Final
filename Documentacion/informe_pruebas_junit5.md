# Informe de implementación de pruebas con JUnit 5

> Proyecto Final — Programación II  
> Fecha de verificación: 1 de septiembre de 2026

## 1. Requisito solicitado

El profesor indicó:

> “Recuerden que en el Proyecto Final tienen que agregarle al código las pruebas unitarias con JUnit 5”.

Para cumplir el requisito se agregó una suite de pruebas unitarias para la lógica del Backend. Las pruebas que necesitan una base de datos se conservaron, pero se clasificaron y ejecutaron por separado como pruebas de integración.

Esta separación es importante porque una prueba unitaria debe poder ejecutarse rápidamente, siempre con el mismo resultado y sin depender de Internet, credenciales o datos externos. Una prueba de integración comprueba algo diferente: que JDBC, las consultas SQL, las tablas y las transacciones funcionan juntos.

## 2. Diagnóstico inicial

El proyecto ya incluía JUnit Jupiter 5.10.1 y cuatro clases con 36 métodos `@Test`. Sin embargo, esas clases construían los DAO reales y utilizaban la base de datos configurada en `db.properties`. Por ello, la suite era de integración aunque documentación anterior la denominara “unitaria”.

La primera comprobación solo compiló los tests, sin ejecutarlos:

```text
Compiling 37 source files ... to target\classes
Compiling 4 source files ... to target\test-classes
Tests are skipped.
BUILD SUCCESS
```

Este resultado demostraba que el código compilaba, pero no que las pruebas pasaran. Además, uno de los flujos existentes podía cancelar pedidos, cambiar mesas y descontar stock en una base compartida.

## 3. Herramientas y organización final

Se configuraron las siguientes herramientas en `Backend/pom.xml`:

| Herramienta | Versión | Función |
|---|---:|---|
| JUnit Jupiter | 5.10.1 | Definir y ejecutar los casos de prueba |
| Mockito JUnit Jupiter | 5.23.0 | Sustituir DAO y servicios por mocks aislados |
| Maven Surefire | 3.5.4 | Ejecutar únicamente las pruebas unitarias `*Test` |
| Maven Failsafe | 3.5.4 | Ejecutar las integraciones `*IT` con el perfil opcional |
| JaCoCo | 0.8.15 | Medir cobertura y generar el informe HTML |

La estructura resultante es:

```text
src/test/java/com/restaurant/backend/
├── controller/   ControllersTest.java
├── model/        ModelosTest.java
├── service/      cinco suites de servicios
├── dao/          seis suites de integración *IT.java
├── util/         ConexionDBIT.java
└── support/      protección de la base de integración
```

Las cuatro pruebas anteriores fueron renombradas de `*Test` a `*IT`. También se añadieron integraciones para `CategoriaDAOImpl`, `UsuarioDAOImpl` y `ReporteDAOImpl`. En total quedaron siete grupos de integración.

## 4. Cómo se hicieron las pruebas unitarias

### 4.1 Patrón Arrange–Act–Assert

Cada caso sigue tres pasos:

1. **Arrange:** preparar objetos, entradas y respuestas de los mocks.
2. **Act:** ejecutar un único método del sistema.
3. **Assert:** comprobar el resultado y las colaboraciones importantes.

Ejemplo real simplificado de descuento de stock:

```java
// Arrange
Producto producto = producto(1, "Clásica", "Hamburguesas", 5, true);
when(dao.getProductoPorId(1)).thenReturn(producto);
when(dao.editar(producto)).thenReturn("Producto editado correctamente");

// Act
String resultado = service.descontarStock(1, 2);

// Assert
assertEquals("Producto editado correctamente", resultado);
assertEquals(3, producto.getStock());
verify(dao).editar(producto);
```

El test no abre conexiones. El mock representa al DAO y permite comprobar exclusivamente la regla de negocio.

### 4.2 Inyección de dependencias

Los servicios ya aceptaban sus interfaces DAO mediante constructores. Los controladores conservan su constructor público normal, pero ahora disponen de un constructor interno para recibir un mock durante el test. La GUI continúa usando las mismas llamadas de siempre.

### 4.3 Matriz de comportamientos

| Componente | Casos principales | Por qué son importantes |
|---|---|---|
| Modelos | Subtotal, suma y eliminación de detalles, valores iniciales | Evitan totales incorrectos en caja y pedidos |
| ProductoService | Validaciones, categoría, disponibilidad y descuento | Protegen catálogo e inventario |
| MesaService | Ocupación, reservas, transiciones y liberación | Evitan estados de mesa imposibles y cierres incorrectos |
| PedidoService | Creación, ítems, total, stock, cierre y cancelación | Cubren el flujo económico principal del sistema |
| UsuarioService | Login, campos obligatorios, rol y contraseña | Evitan registros inválidos y accesos incorrectos |
| ReporteService | Delegación de resultados | Comprueba que los reportes no sean alterados por el servicio |
| ServicioFactory | Identidad singleton | Evita múltiples instancias inconsistentes durante la ejecución |
| Controladores | Delegación de todos los métodos | Verifica el puente usado por Swing |

No se crearon tests cuyo único objetivo fuera llamar getters, setters o enums sin comportamiento. Esos tipos se ejercitan dentro de los flujos anteriores.

## 5. Corrección funcional protegida por los tests

Se encontró una inconsistencia en `PedidoService.agregarItem`: el detalle se insertaba y el total se recalculaba, pero no se descontaba el nuevo stock. Al cancelar posteriormente el pedido se devolvían todas las unidades, incluidas las que nunca habían sido descontadas, lo que podía aumentar el inventario incorrectamente.

La corrección aplicada sigue este orden:

1. Insertar el detalle.
2. Si falla, retornar el error sin cambiar total ni stock.
3. Recalcular y actualizar el total.
4. Si falla, retornar el error sin descontar stock.
5. Descontar del inventario la cantidad agregada.

Se agregó un test exitoso que verifica las tres operaciones y otro que confirma que, ante un error de inserción, no se actualizan el total ni el stock.

También se fijó explícitamente la regla vigente: cerrar o cancelar un pedido **no libera automáticamente la mesa**. La liberación sigue siendo una acción manual del mozo.

## 6. Ejecución de las pruebas unitarias

Desde la raíz del proyecto:

```powershell
mvn -pl Backend clean verify
```

En esta computadora Maven no estaba en `PATH`, por lo que se utilizó la instalación 3.9.16 ya descargada por Maven Wrapper. El comando equivalente produjo:

```text
Running Controladores — delegación al backend ........ 4
Running Modelos — cálculos y valores iniciales ........ 5
Running MesaService — pruebas unitarias ............... 8
Running PedidoService — pruebas unitarias ............ 12
Running ProductoService — pruebas unitarias ........... 9
Running Reportes y fábrica — pruebas unitarias ........ 2
Running UsuarioService — pruebas unitarias ............ 4

Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Estas 44 pruebas se ejecutaron sin iniciar MySQL/MariaDB y sin contactar TiDB.

## 7. Preparación segura de la integración

Las integraciones se ejecutaron sobre MariaDB local de WAMP en el puerto 3307. MySQL 3306 requería una contraseña local no disponible, por lo que no se intentaron credenciales adicionales.

La base se reconstruye antes de cada ejecución. Los scripts originales se reutilizan reemplazando el nombre en memoria; no se crean copias divergentes:

```powershell
$mysql = "C:\wamp64\bin\mariadb\mariadb11.5.2\bin\mysql.exe"

& $mysql -h 127.0.0.1 -P 3307 -u root `
  -e "DROP DATABASE IF EXISTS restomanager_test;"

(Get-Content -Raw "Backend\src\main\resources\schema.sql").Replace(
  "restaurante_db", "restomanager_test"
) | & $mysql -h 127.0.0.1 -P 3307 -u root

(Get-Content -Raw "Backend\src\main\resources\seed.sql").Replace(
  "restaurante_db", "restomanager_test"
) | & $mysql -h 127.0.0.1 -P 3307 -u root
```

`ConexionDB` acepta propiedades del sistema por encima de `db.properties`, por lo que las credenciales de integración no se guardan en Git. Además, `DedicatedDatabaseExtension` aborta cualquier `*IT` cuya URL no apunte explícitamente a `restomanager_test`. Esta protección impide ejecutar accidentalmente escrituras sobre TiDB o la base operativa.

Comando de integración usado:

```powershell
mvn -pl Backend -Pintegration-tests clean verify `
  "-Ddb.url=jdbc:mysql://127.0.0.1:3307/restomanager_test" `
  "-Ddb.user=root" `
  "-Ddb.password="
```

## 8. Fallo encontrado durante la primera integración

La primera ejecución usó una URL con parámetros separados por `&`. El archivo `mvn.cmd` de Windows reinterpretó esos caracteres y cortó el resto de los argumentos. Como consecuencia, tomó el usuario del `db.properties` en lugar de `root` y falló la autenticación local:

```text
PoolInitializationException: Failed to initialize pool:
Access denied for user '<usuario-configurado>'@'localhost'

Tests run: 39, Failures: 0, Errors: 34, Skipped: 0
BUILD FAILURE
```

No fue un fallo de los DAO y no llegó a modificarse ninguna base. Se corrigió el comando retirando parámetros JDBC innecesarios de la URL, se reconstruyó `restomanager_test` y se repitió la suite completa.

## 9. Resultado final de integración

```text
CategoriaDAOImplIT .......... 1 prueba
MesaDAOImplIT ............... 8 pruebas
PedidoDAOImplIT ............ 11 pruebas
ProductoDAOImplIT .......... 14 pruebas
ReporteDAOImplIT ............ 1 prueba
UsuarioDAOImplIT ............ 1 prueba
ConexionDBIT ................ 3 pruebas

Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Fragmento del flujo completo:

```text
Producto obtenido: Clasica | Stock original: 24
Resultado crearPedido: Se cambio el estado
Resultado agregarItem: Item agregado correctamente
Pedido total acumulado: 7500.00
Resultado cerrarPedido: Estado actualizado correctamente
Mesa 1 estado despues de cerrar pedido: OCUPADA
Stock post-cierre: 21
=== INTEGRATION TEST COMPLETED SUCCESSFULLY ===
```

El cambio `24 → 21` demuestra el descuento inmediato de una unidad inicial y dos agregadas. La mesa `OCUPADA` confirma la liberación manual.

## 10. Cobertura con JaCoCo

El reporte navegable se genera en:

```text
Backend/target/site/jacoco/index.html
```

Resultados combinados de las 83 pruebas:

| Paquete | Cobertura de líneas | Cobertura de ramas |
|---|---:|---:|
| controller | 80,0% | No aplica |
| dao | 78,6% | 54,3% |
| model | 72,5% | 10,0% |
| service | 83,9% | 64,3% |
| service.dto | 47,5% | No aplica |
| util | 78,4% | 40,0% |
| **Total Backend** | **77,5%** | **56,6%** |

La cobertura de instrucciones total fue **78,6%**. JaCoCo se usa como evidencia y no como una cuota obligatoria: el objetivo es cubrir reglas y fallos relevantes, no producir tests artificiales de código trivial.

## 11. Resultado y conclusión

- Pruebas unitarias: **44/44 exitosas**.
- Pruebas de integración: **39/39 exitosas**.
- Total verificado: **83 pruebas**, sin fallos, errores ni omisiones.
- Cobertura de líneas total: **77,5%**.
- Base utilizada: únicamente `restomanager_test` local.
- Al finalizar se eliminó el esquema de prueba y se detuvo el proceso MariaDB iniciado para esta verificación.
- El empaquetado multimódulo completo de Backend y GUI también finalizó con `BUILD SUCCESS`.

La suite cumple el requisito de JUnit 5 y permite detectar regresiones sin depender diariamente de una base externa. Las integraciones permanecen disponibles para verificar JDBC y SQL de forma explícita, aislada y reproducible.
