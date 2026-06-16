# RemovalHardocdeo#1 — Fecha/Hora dinámica + Categorías desde BD

## Objetivo

Eliminar valores hardcodeados de fecha/hora (`00/00/0000`, `00:00`) y de categorías en la sidebar, reemplazándolos por datos reales del sistema y de la base de datos.

---

## Cambios realizados

### 1. Fecha y hora dinámica (todos los paneles)

Se agregó el método `actualizarFechaHora()` en cada panel que muestra fecha y hora en su encabezado. Toma `LocalDateTime.now()` del sistema host y lo formatea como `dd/MM/yyyy` y `HH:mm`.

| Archivo | Etiquetas afectadas |
|---|---|
| `GUI/src/vistas/Menu.java:148` | `FechaMenuNum`, `HoraMenuNum` |
| `GUI/src/vistas/paneles/MesasPanel.java:41` | `FechaNum`, `HoraNum` |
| `GUI/src/vistas/paneles/PedidosPanel.java:28` | `FechaNum`, `HoraNum` |
| `GUI/src/vistas/CheckoutDialog.java:31` | `FechaNum`, `HoraNum` |

**Extra en Menu.java:** Timer de 30 segundos que refresca la hora automáticamente (`Menu.java:65`).

```java
private void actualizarFechaHora() {
    LocalDateTime ahora = LocalDateTime.now();
    DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");
    FechaNum.setText(" " + fmtFecha.format(ahora));
    HoraNum.setText(fmtHora.format(ahora));
}
```

### 2. Categorías dinámicas desde la BD (`Menu.java`)

**Antes:** 7 categorías hardcodeadas con nombres fijos (`Todas`, `Entradas`, `Pizzas`, `Hamburguesas`, `Pastas`, `Bebidas`, `Postres`). No coincidían con las 9 categorías reales de la BD (faltaban `Cervezas`, `Minutas`, `Vinos`).

**Ahora:**

- El método `configurarCategoriasDinamicas()` (`Menu.java:90`) limpia `PanelCategorias`, lo rearma con `BoxLayout` vertical, y carga las categorías desde `CategoriaController.listar()`.
- Cada categoría se crea con `crearPanelCategoria()` (`Menu.java:123`), que genera un `JPanel` con un `JLabel` centrado y cursor de mano.
- El mapa `panelesCategoria` (`Menu.java:39`) asocia cada panel con su nombre de categoría para el highlight.
- `marcarCategoriaActiva()` (`Menu.java:79`) ahora itera el mapa en vez de referenciar componentes fijos.
- Si falla la BD, hay un fallback con las 6 categorías originales (`Menu.java:109-115`).

```java
private void configurarCategoriasDinamicas() {
    PanelCategorias.removeAll();
    panelesCategoria.clear();
    PanelCategorias.setLayout(new BoxLayout(PanelCategorias, BoxLayout.Y_AXIS));

    JPanel panelTodas = crearPanelCategoria("TODAS", "Todas");
    PanelCategorias.add(panelTodas);
    panelesCategoria.put(panelTodas, "TODAS");

    try {
        CategoriaController categoriaController = new CategoriaController();
        List<Categoria> categorias = categoriaController.listar();
        for (Categoria cat : categorias) {
            JPanel panel = crearPanelCategoria(cat.getNombre().toUpperCase(), cat.getNombre());
            PanelCategorias.add(panel);
            panelesCategoria.put(panel, cat.getNombre());
        }
    } catch (Exception e) {
        // fallback con categorías originales
    }

    PanelCategorias.revalidate();
    PanelCategorias.repaint();
    ScrollCategorias.setViewportView(PanelCategorias);
}
```

---

## Nuevos imports agregados

### Menu.java
```java
import com.restaurant.backend.controller.CategoriaController;
import com.restaurant.backend.model.Categoria;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
```

### MesasPanel.java / PedidosPanel.java
```java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
```

### CheckoutDialog.java
```java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
```

---

## Flujo de inicialización (`Menu.java:54-76`)

```
constructor(usuario)
  → initComponents()
  → configurarPanelProductos()
  → configurarCategoriasDinamicas()   // nuevo: carga categorías desde BD
  → actualizarFechaHora()              // nuevo: setea fecha/hora del sistema
  → configurarContenidoPrincipal()
  → configurarTabla()
  → configurarScrollBars()
  → Timer(30s) → actualizarFechaHora() // nuevo: refresco automático
  → mostrar info usuario autenticado
  → mostrarProductos("TODAS")
```

---

## Verificación

Los 4 archivos compilan correctamente contra `--release 17` con el classpath del proyecto existente.
