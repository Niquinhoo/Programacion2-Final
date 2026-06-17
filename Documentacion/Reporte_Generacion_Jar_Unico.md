# Reporte de Configuración y Generación de JAR Único (Fat JAR) — RestoManager

Este documento detalla el procedimiento técnico realizado desde cero para unificar el empaquetado de la aplicación de escritorio **RestoManager** en un único archivo ejecutable independiente (`.jar`), permitiendo su distribución y ejecución sin dependencias externas en el sistema cliente.

---

## 1. Contexto y Desafío Técnico
La aplicación está estructurada como un **proyecto multi-módulo de Maven** compuesto por:
1. `restaurant-parent` (POM raíz que gestiona el ciclo de vida global).
2. `Backend` (módulo con lógica de negocio y persistencia de base de datos JDBC/HikariCP).
3. `GUI` (módulo frontend Swing de escritorio).

### El Desafío del Alcance System (System Scope)
El módulo `GUI` requiere de tres librerías locales almacenadas físicamente en su carpeta `lib/` (`AbsoluteLayout.jar`, `LGoodDatePicker.jar` y `jfreechart-1.5.4.jar`). En el archivo `pom.xml`, estas dependencias se declaran usando el alcance de sistema (`system` scope) apuntando a rutas físicas relativas.

Por defecto, los plugins estándar de Maven para empaquetado grueso (como el descriptor predefinido `jar-with-dependencies` del plugin `maven-assembly-plugin`) **ignoran las dependencias con alcance `system`**, lo que resultaba en un JAR ejecutable incompleto que fallaba en tiempo de ejecución lanzando excepciones de clase no encontrada (`ClassNotFoundException`).

---

## 2. Solución Paso a Paso: Implementación desde Cero

### Paso 1: Creación del Descriptor de Ensamble Personalizado
Para solucionar la omisión de las dependencias locales, se creó un descriptor de ensamble XML personalizado en la ruta `GUI/src/assembly/dep.xml`. 

Este descriptor le indica explícitamente a Maven que debe descomprimir e integrar todas las clases de las dependencias del sistema junto con las dependencias normales de tiempo de ejecución (que incluye el módulo compilado `Backend` y las librerías remotas como `HikariCP` y `mysql-connector-j`).

**Archivo:** [dep.xml](file:///c:/Users/nicot/Desktop/Programacion2-Final/GUI/src/assembly/dep.xml)
```xml
<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.2.0 http://maven.apache.org/xsd/assembly-2.2.0.xsd">
    <id>jar-with-dependencies</id>
    <formats>
        <format>jar</format>
    </formats>
    <includeBaseDirectory>false</includeBaseDirectory>
    <dependencySets>
        <!-- 1. Incluye dependencias de repositorio estándar (Backend y transitivas) -->
        <dependencySet>
            <outputDirectory>/</outputDirectory>
            <useProjectArtifact>true</useProjectArtifact>
            <unpack>true</unpack>
            <scope>runtime</scope>
        </dependencySet>
        <!-- 2. Incluye y desempaqueta los JARs locales (AbsoluteLayout, LGoodDatePicker, jfreechart) -->
        <dependencySet>
            <outputDirectory>/</outputDirectory>
            <unpack>true</unpack>
            <scope>system</scope>
        </dependencySet>
    </dependencySets>
</assembly>
```

---

### Paso 2: Integración de `maven-assembly-plugin` en la compilación
Se configuró el plugin de ensamble dentro de la etiqueta `<plugins>` del archivo de configuración del frontend para engancharlo al proceso normal de empaquetado.

**Archivo:** [GUI/pom.xml](file:///c:/Users/nicot/Desktop/Programacion2-Final/GUI/pom.xml)
```xml
            <!-- Maven Assembly Plugin para construir un JAR Único (Fat JAR) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.6.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>vistas.Login</mainClass> <!-- Define el punto de entrada al programa -->
                        </manifest>
                    </archive>
                    <descriptors>
                        <descriptor>src/assembly/dep.xml</descriptor> <!-- Enlaza al descriptor personalizado -->
                    </descriptors>
                    <finalName>RestoManager</finalName> <!-- Nombre del archivo resultante -->
                    <appendAssemblyId>false</appendAssemblyId> <!-- Evita que Maven le añada el sufijo "-jar-with-dependencies" -->
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase> <!-- Se ejecuta al lanzar 'mvn package' -->
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

---

### Paso 3: Compilación, Construcción y Reactor Order
Con la configuración completa, el proceso se dispara ejecutando el comando de Maven desde la raíz del proyecto:

```powershell
mvn clean package -DskipTests
```

#### Flujo de Construcción (Reactor Order):
1. **restaurant-parent:** Limpia directorios y prepara el compilador.
2. **Backend:** Compila las clases de lógica y genera `Backend-1.0.jar`.
3. **GUI:** 
   - Compila las clases del frontend Swing en `GUI/target/classes`.
   - Invoca al plugin de ensamble.
   - Lee el descriptor `dep.xml`.
   - Extrae el contenido de `Backend-1.0.jar`, HikariCP, MySQL Connector, SLF4J, y los 3 JARs locales del sistema.
   - Empaqueta todas las clases descomprimidas y los archivos de recursos (como `db.properties` e iconos) en un archivo unificado: **`GUI/target/RestoManager.jar`**.

---

## 3. Resultados Obtenidos y Verificación

1. **Tamaño del Archivo:** Se obtuvo un único binario ejecutable `RestoManager.jar` con un tamaño aproximado de **6.9 MB** (lo que confirma la inclusión interna de todas las dependencias de base de datos y diseño).
2. **Independencia Física:** Se copió el archivo `.jar` fuera del proyecto al directorio del Escritorio y se ejecutó mediante doble-clic (y el comando `java -jar`), comprobándose que inicia, se conecta asíncronamente con TiDB Cloud, procesa el login e interactúa con las mesas sin requerir de carpetas extras.

---

## 4. Conclusión
La solución mediante el descriptor personalizado de `maven-assembly-plugin` resolvió el cuello de botella técnico del empaquetado multi-módulo que utiliza dependencias híbridas (remotas en Maven y locales en sistema de archivos). El proyecto ahora cuenta con un método robusto e industrial para compilar y generar su distribuible final en un solo comando.
