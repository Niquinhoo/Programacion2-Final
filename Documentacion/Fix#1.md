# Reporte Técnico "Fix#1" — Proyecto RestoManager

Este reporte documenta formalmente la finalización del bloque de tareas **"Fix#1"**, diseñado para corregir las inconsistencias detectadas en la auditoría inicial de código, donde tres tareas declaradas como "Completadas" o "En progreso" estaban ausentes o incompletas en la base de código real. 

El presente reporte consolida el **Plan de Implementación**, el **Task Tracker (Lista de Tareas)** y el **Walkthrough de Cambios**, junto con la **verificación empírica** de compilación y ejecución de pruebas.

---

## 1. Plan de Implementación (Estrategias y Decisiones de Diseño)

### Tarea 1: Pruebas Unitarias/Integración de Capa DAO (JUnit 5)
* **Diagnóstico Inicial**: Solo existía la clase `ConexionDBTest.java` con tests genéricos de conectividad. Faltaban validaciones para `ProductoDAOImpl`, `MesaDAOImpl` y `PedidoDAOImpl`.
* **Decisión Técnica**: Dado que los DAOs consumen directamente la infraestructura estática de `DatabaseConnection.getConnection()`, no se pueden mockear con facilidad sin un refactor mayor a patrones de Inyección de Dependencias. Se adoptó una estrategia de **Tests de Integración Reales** contra la base de datos de TiDB Cloud.
* **Mecanismos de Aislamiento**:
  - Uso de números de entidad reservados para pruebas (por ejemplo, mesa `9990`) para evitar colisiones con datos existentes.
  - Implementación de bloques `@AfterEach` en JUnit 5 para ejecutar sentencias directas de limpieza SQL (`DELETE`) garantizando que cada prueba comience y finalice con la base de datos en estado limpio.
  - Adición del tag `@Tag("integration")` para permitir al pipeline CI omitir la ejecución de base de datos si no hay conectividad.

### Tarea 2: Autenticación Real de Usuarios
* **Diagnóstico Inicial**: La interfaz gráfica `Login.java` contenía un botón "Entrar" que abría directamente la interfaz principal `Menu` sin validación alguna (cuerpo con un comentario `TODO`). No existía clase de servicio ni DAO de usuarios en el backend.
* **Decisión Técnica**: 
  - Creación de la tabla `usuarios` y `roles` en la base de datos.
  - Almacenamiento seguro de contraseñas mediante hashing **SHA2-256** directamente delegada en la consulta SQL (`SHA2(?, 256)`).
  - Creación del contrato `UsuarioDAO` e implementación en `UsuarioDAOImpl` incluyendo soft-delete (`activo = false`) y joins para la hidratación del modelo `Rol`.
  - Creación de `UsuarioService` con validaciones de lógica de negocio (por ejemplo, longitud de contraseña mayor a 6 caracteres).
  - Integración segura en `Login.java` manejando cuadros de diálogo (`JOptionPane`) en casos de error o campos vacíos.
  - Sobrecarga de constructor en `Menu.java` para hidratar la cabecera del panel de administración (`jLabel15` y `jLabel16`) con el nombre completo y el rol del usuario autenticado.

### Tarea 3: Arquitectura Maven Multi-módulo
* **Diagnóstico Inicial**: El proyecto `GUI` era un proyecto basado puramente en la herramienta de construcción Ant de NetBeans. Faltaba su integración formal como módulo Maven del proyecto raíz.
* **Decisión Técnica (Coexistencia de Sistemas)**:
  - Mantener los metadatos de NetBeans (`nbproject/`, `build.xml`) para preservar el diseñador visual de la interfaz (`.form`).
  - Crear un archivo `GUI/pom.xml` superpuesto que herede del POM raíz, permitiendo a Maven compilar el módulo GUI de forma limpia.
  - Definir las dependencias locales (`AbsoluteLayout.jar`, `LGoodDatePicker.jar`, `jfreechart.jar`) utilizando el `systemPath` de Maven como mecanismo robusto de portabilidad local.
  - Configurar el manifest de salida en el compilador de Maven apuntando a `vistas.Login` como clase principal.

---

## 2. Lista de Tareas y Control de Avance (Task List)

Todas las tareas del Fix#1 se han completado y verificado en su totalidad:

- [x] **Capa DAO (Tests JUnit 5)**
  - [x] Crear e implementar `ProductoDAOImplTest.java` (10 tests)
  - [x] Crear e implementar `MesaDAOImplTest.java` (8 tests)
  - [x] Crear e implementar `PedidoDAOImplTest.java` (11 tests)
- [x] **Capa de Autenticación de Usuarios**
  - [x] Validar estructura de base de datos para `usuarios` y `roles`
  - [x] Crear contrato `UsuarioDAO.java`
  - [x] Crear e implementar `UsuarioDAOImpl.java` (SHA-256)
  - [x] Crear e implementar `UsuarioService.java`
  - [x] Modificar `ServicioFactory.java` para exponer el servicio como Singleton
  - [x] Conectar la vista de `Login.java` con el flujo de autenticación
  - [x] Sobrecargar constructor de `Menu.java` para recibir el `Usuario` y actualizar el encabezado
- [x] **Arquitectura Maven Multi-módulo**
  - [x] Crear archivo `GUI/pom.xml` heredando del padre y configurando dependencias de librerías locales
  - [x] Modificar `pom.xml` de la raíz del proyecto para registrar el módulo `<module>GUI</module>`
- [x] **Verificación del Entorno**
  - [x] Backend compila sin errores (`mvn compile -pl Backend`)
  - [x] Tests del backend compilan correctamente (`mvn test-compile -pl Backend`)
  - [x] El módulo GUI compila resolviendo dependencias de backend (`mvn compile -pl GUI`)
  - [x] Las pruebas de integración pasan exitosamente contra TiDB Cloud con una tasa de éxito del 100%

---

## 3. Walkthrough de Cambios Realizados

A continuación se detalla la matriz de archivos modificados y creados en el repositorio:

### Archivos Creados (Nuevos en la Base de Código)

| Archivo | Tipo | Descripción |
|---|---|---|
| [UsuarioDAO.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/dao/UsuarioDAO.java) | Interfaz | Define las firmas del DAO para la gestión de usuarios y autenticación. |
| [UsuarioDAOImpl.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/dao/UsuarioDAOImpl.java) | Clase Java | Implementación JDBC con consultas seguras vía `PreparedStatement` y hash de contraseñas delegada en base de datos. |
| [UsuarioService.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/UsuarioService.java) | Clase Java | Capa intermedia de negocio encargada de validar datos y coordinar llamados con el DAO. |
| [ProductoDAOImplTest.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/test/java/com/restaurant/backend/dao/ProductoDAOImplTest.java) | Test JUnit | Pruebas de integración sobre inserción, actualización, eliminación y lecturas con joins de productos. |
| [MesaDAOImplTest.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/test/java/com/restaurant/backend/dao/MesaDAOImplTest.java) | Test JUnit | Pruebas de inserción de mesas, cambio de estado a `OCUPADA`/`LIBRE` y filtros de disponibilidad. |
| [PedidoDAOImplTest.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/test/java/com/restaurant/backend/dao/PedidoDAOImplTest.java) | Test JUnit | Pruebas sobre la inserción transaccional de pedidos, detalles de ítems, actualización de totales e histórico. |
| [GUI/pom.xml](file:///c:/Users/nicot/Desktop/Programacion2-Final/GUI/pom.xml) | Maven POM | Definición de Maven para compilar la interfaz gráfica, enlazando dependencias locales y el módulo backend. |

### Archivos Modificados

| Archivo | Tipo | Descripción |
|---|---|---|
| [ServicioFactory.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/Backend/src/main/java/com/restaurant/backend/service/ServicioFactory.java) | Clase Java | Registro de la instancia única perezosa (Lazy Singleton) de `UsuarioService`. |
| [Login.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/GUI/src/vistas/Login.java) | Vista Swing | Implementación de `EntrarActionPerformed` conectada al flujo del backend, validación y gestión de cuadros de diálogo interactivos. |
| [Menu.java](file:///c:/Users/nicot/Desktop/Programacion2-Final/GUI/src/vistas/Menu.java) | Vista Swing | Sobrecarga de constructor `Menu(Usuario)` para hidratar datos dinámicos en la cabecera (Usuario y Rol). |
| [pom.xml](file:///c:/Users/nicot/Desktop/Programacion2-Final/pom.xml) | Maven POM | Declaración del módulo `<module>GUI</module>` para integrarlo al ciclo de construcción general. |

### Diagrama del Flujo de Autenticación Integrado
```
[Login.java (UI)]
   └─ Ingresa Usuario y Contraseña + Click en "Entrar"
        ├─ Campos Vacíos? ───> SI ───> JOptionPane (Warning)
        └─ NO
             └─ ServicioFactory.getUsuarioService().iniciarSesion(user, pass)
                  └─ UsuarioService.iniciarSesion() (Valida reglas de negocio)
                       └─ UsuarioDAOImpl.autenticar() (Consulta JDBC)
                            └─ Consulta: WHERE usuario=? AND contrasena=SHA2(?, 256)
                                 ├─ Sin coincidencias ──> Retorna null ──> JOptionPane (Error)
                                 └─ Con coincidencia ───> Retorna Usuario
                                                              └─ Abre new Menu(usuarioActual)
                                                              └─ HidratajLabel15 (Nombre) e jLabel16 (Rol)
```

---

## 4. Resultados de Compilación y Verificación de Pruebas

Se ejecutó un proceso completo de limpieza, compilación y pruebas utilizando el Maven de NetBeans:
`& "F:\Apache NetBeans\java\maven\bin\mvn.cmd" clean test -f "c:\Users\nicot\Desktop\Programacion2-Final\pom.xml"`

### Resultados de la Compilación
La reactor order de Maven compiló de forma exitosa los tres proyectos encadenados:
1. `restaurant-parent` (POM Padre) ──────────> **BUILD SUCCESS**
2. `Backend` (Lógica y Acceso a Datos) ────────> **BUILD SUCCESS**
3. `GUI` (Vistas Swing de NetBeans) ────────────> **BUILD SUCCESS**

### Resultados de los Tests (Tasa de Éxito del 100%)
Se ejecutaron un total de **35 pruebas de integración** contra la instancia de TiDB Cloud en AWS, resultando todas exitosas:

```text
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running MesaDAOImpl — Tests de Integración
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 39.63 s -- in MesaDAOImpl
Running PedidoDAOImpl — Tests de Integración
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 55.61 s -- in PedidoDAOImpl
Running ProductoDAOImpl — Tests de Integración
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 24.42 s -- in ProductoDAOImpl
Running com.restaurant.backend.util.ConexionDBTest
=== STARTING INTEGRATION TEST FOR COMPLETE FLOW ===
Producto obtenido: Clasica | Stock original: 19
Mesa 1 original estado: LIBRE
Resultado crearPedido: Se cambio el estado
ID del nuevo pedido encontrado en la base de datos: 60008
Mesa 1 estado despues de crear pedido: OCUPADA
Resultado agregarItem: Item agregado correctamente
Pedido total acumulado: 7500.00
Resultado cerrarPedido: Se cambio el estado
Mesa 1 estado despues de cerrar pedido: LIBRE
Stock post-cierre: 16
Reporte de ventas por producto obtenido. Cantidad de registros: 14
=== INTEGRATION TEST COMPLETED SUCCESSFULLY ===
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 45.53 s -- in ConexionDBTest

Results:
Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 5. Conclusión de Fix#1

El bloque de cambios **Fix#1** se considera **cerrado y certificado**. Las pruebas unitarias cubren el total de la API CRUD de cada DAO (inserción, actualización, eliminación e hidrogenación de entidades), el flujo de inicio de sesión de usuario y control de accesos se encuentra activo y conectado con base de datos encriptada por SHA2-256, y el proyecto está unificado bajo el flujo de empaquetado multi-módulo de Maven sin comprometer la compatibilidad del editor visual de formularios Swing de NetBeans.
