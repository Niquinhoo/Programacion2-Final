# Sistema de Gestión de Restaurante — Programación II (Final)

Este repositorio contiene la implementación del proyecto final para **Programación II**, estructurado en una arquitectura modular de múltiples capas (Modelo, Datos, Lógica de Negocio/Servicios, Controladores y Vista Swing).

---

## 📁 Estructura Completa del Proyecto

A continuación se detalla la organización de directorios y archivos principales del sistema:

```
Programacion2-Final/
├── pom.xml                               # Archivo de configuración Maven raíz
├── .gitignore                            # Exclusiones de control de versiones Git
├── README.md                             # Documentación principal del proyecto
├── index.html                            # Panel de control / Dashboard de planificación local
│
├── Backend/                              # Módulo de Backend (Maven)
│   ├── pom.xml                           # Configuración de dependencias del Backend (MySQL, JUnit)
│   └── src/
│       ├── main/
│       │   ├── java/com/restaurant/backend/
│       │   │   ├── model/                # Clases de dominio/entidades (Mesa, Pedido, Producto, etc.)
│       │   │   ├── dao/                  # Interfaces e implementaciones de Acceso a Datos (JDBC/SQL)
│       │   │   ├── controller/           # Controladores stubs que actúan como puente hacia la GUI
│       │   │   └── util/                 # Utilidades (Conexión a Base de Datos MySQL)
│       │   └── resources/
│       │       ├── db.properties         # Configuración de credenciales de la BD MySQL
│       │       ├── schema.sql            # Script DDL de creación de tablas de la BD
│       │       └── seed.sql              # Script DML de datos semilla/prueba
│       └── test/                         # Pruebas unitarias (JUnit) para conexiones y DAOs
│
├── GUI/                                  # Módulo de Frontend / Vistas (Java Desktop Ant / NetBeans)
│   ├── build.xml                         # Script de compilación Apache Ant
│   ├── manifest.mf                       # Manifiesto del empaquetado JAR ejecutable
│   ├── nbproject/                        # Archivos de configuración del proyecto en NetBeans IDE
│   │   ├── project.properties            # Propiedades (Versión de Java 17, classpath local)
│   │   └── project.xml                   # Metadatos del proyecto
│   ├── lib/                              # Librerías externas empaquetadas (.jar)
│   │   ├── AbsoluteLayout.jar            # Gestor de distribución de NetBeans GUI Builder
│   │   ├── LGoodDatePicker.jar           # Selector visual de fecha y hora para Reservas
│   │   └── jfreechart-1.5.4.jar          # Librería para generación de gráficos estadísticos
│   └── src/
│       ├── com/imagen/                   # Recursos visuales estáticos (Iconos, logos)
│       └── vistas/                       # Formularios y ventanas del sistema
│           ├── Login.java                # Pantalla de acceso y autenticación
│           ├── Registro.java             # Registro de nuevos mozos y administradores
│           ├── Menu.java                 # Ventana principal del sistema (Contenedor de paneles)
│           ├── ReservaDialog.java        # Diálogo emergente para creación de reservas
│           ├── CheckoutDialog.java       # Facturación y cobro de consumos
│           └── paneles/                  # Subpaneles intercambiables
│               ├── MesasPanel.java       # Grid visual interactivo con 16 mesas
│               ├── DetallesMesasPanel.java# Detalle y acciones de la mesa seleccionada
│               ├── PedidosPanel.java     # Visualizador general del estado de comandas
│               ├── ProductosPanel.java   # Panel ABM de administración de catálogo
│               ├── CardProducto.java     # Tarjeta reutilizable de producto
│               └── reportes/             # Reportes gráficos estadísticos
│                   ├── GeneralPanel.java # KPIs de ventas y gráficos circulares
│                   └── VentasPanel.java  # Gráfico lineal de ingresos históricos
│
├── Documentacion/                        # Informes de análisis y diseño
│   ├── reporte_primera_etapa.md          # Reporte técnico inicial
│   ├── analisis_avance_gui.md            # Diagnóstico de integración GUI-Backend
│   └── creacion_base_de_datos.md         # Documentación de esquemas y modelos de la base de datos
│
└── divisionTareasPlan/                   # HTMLs y recursos de la planificación original
```

---

## 🛠️ Requisitos del Sistema

Para compilar y ejecutar el proyecto completo necesitas tener instalado en tu máquina:
* **Java Development Kit (JDK):** Versión 17 o superior (Se recomienda JDK 17 o 21).
* **Apache Maven:** Para la compilación de la lógica del `Backend`.
* **Apache Ant:** Para la compilación y ejecución de la `GUI` desde la terminal.
* **MySQL Server:** Para alojar la base de datos.
* **NetBeans IDE (Opcional):** Muy recomendado para el diseño visual de los archivos `.form` de la GUI.

---

## 🚀 Compilación y Ejecución

### 1. Inicializar la Base de Datos
1. Inicia tu servidor MySQL.
2. Ejecuta el script DDL `Backend/src/main/resources/schema.sql`.
3. Carga los datos de prueba usando `Backend/src/main/resources/seed.sql`.
4. Configura el archivo `Backend/src/main/resources/db.properties` con tu usuario y contraseña locales de MySQL.

### 2. Compilar el Backend (Maven)
Desde la raíz del proyecto o desde la carpeta `Backend/`, ejecuta:
```bash
mvn clean install
```
Esto creará el archivo compilado `Backend-1.0.jar` en la carpeta `Backend/target/` y ejecutará los tests unitarios.

### 3. Compilar la GUI (Ant)
Desde la carpeta `GUI/` ejecuta:
```bash
ant compile
```
Para generar el empaquetado `.jar` ejecutable:
```bash
ant jar
```
Este comando generará el ejecutable final en `GUI/dist/Login.jar` incluyendo y vinculando las dependencias de la carpeta `GUI/lib/`.

### 4. Ejecutar la Aplicación
Puedes ejecutar el sistema directamente con el comando de Ant desde la carpeta `GUI/`:
```bash
ant run
```
O bien desde la raíz del proyecto usando `java`:
```bash
java -cp GUI/lib/jfreechart-1.5.4.jar:GUI/lib/AbsoluteLayout.jar:GUI/lib/LGoodDatePicker.jar:GUI/dist/Login.jar vistas.Login
```

---

## 📦 Librerías Externas Incorporadas

La aplicación hace uso de las siguientes librerías de terceros (ya incluidas en el repositorio en `GUI/lib/`):
1. **JFreeChart 1.5.4:** Visualización gráfica de rendimiento de ventas y estadísticas.
2. **LGoodDatePicker 11.2.1:** Selector cómodo de fechas y horas para reservas de salón.
3. **AbsoluteLayout:** Soporte para el posicionamiento absoluto en Swing (NetBeans GUI Builder).
4. **MySQL Connector/J:** Driver JDBC para conexión de base de datos (administrado por Maven en el Backend).
