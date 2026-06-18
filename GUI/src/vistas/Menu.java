/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vistas;

import com.restaurant.backend.controller.CategoriaController;
import com.restaurant.backend.model.Categoria;
import com.restaurant.backend.model.Producto;
import com.restaurant.backend.model.Usuario;
import com.restaurant.backend.service.ServicioFactory;
import javax.swing.table.DefaultTableModel;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicScrollBarUI;
import vistas.paneles.CardProducto;
import vistas.util.AsyncDataLoader;

/**
 *
 * @author enzol
 */
public class Menu extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Menu.class.getName());
    private CardLayout cardLayout;
    private Usuario usuarioActual;
    private Map<JPanel, String> panelesCategoria = new HashMap<>();

    /**
     * Creates new form Menu (sin usuario autenticado, para compatibilidad con diseñador NetBeans).
     */
    public Menu() {
        this(null);
    }

    /**
     * Creates new form Menu con el usuario autenticado.
     * Actualiza el encabezado con el nombre y rol del usuario.
     *
     * @param usuario el usuario que inició sesión, puede ser null
     */
    public Menu(Usuario usuario) {
        this.usuarioActual = usuario;
        initComponents();

        configurarPanelProductos();
        actualizarFechaHora();
        configurarContenidoPrincipal();
        configurarTabla();
        configurarScrollBars();

        javax.swing.Timer reloj = new javax.swing.Timer(30000, evt -> actualizarFechaHora());
        reloj.start();

        if (usuarioActual != null) {
            jLabel15.setText(usuarioActual.getNombreCompleto());
            jLabel16.setText(
                    usuarioActual.getRol() != null ? usuarioActual.getRol().getNombre() : "");
        }

        cargarCategoriasYProductos();
    }

    private void cargarCategoriasYProductos() {
        mostrarPlaceholderCarga();

        configurarCategoriasDinamicas();

        AsyncDataLoader.load(
                this,
                () -> {
                    List<Producto> todos = ServicioFactory.getProductoService().obtenerTodos();
                    return todos.stream().filter(Producto::isDisponible)
                            .collect(java.util.stream.Collectors.toList());
                },
                productos -> {
                    panelProductos.removeAll();
                    panelProductos.setLayout(new GridLayout(0, 3, 10, 10));

                    for (Producto prod : productos) {
                        CardProducto card = new CardProducto();
                        card.setProducto(prod.getNombre(), prod.getPrecio().doubleValue());
                        card.setOnAgregarListener(
                                (nombre, precio) -> agregarProductoTabla(nombre, precio)
                        );
                        panelProductos.add(card);
                    }

                    int cantidad = panelProductos.getComponentCount();
                    int filas = (int) Math.ceil(cantidad / 3.0);
                    panelProductos.setPreferredSize(
                            new java.awt.Dimension(panelProductos.getWidth(), filas * 140));
                    panelProductos.revalidate();
                    panelProductos.repaint();
                },
                error -> {
                    panelProductos.removeAll();
                    JLabel errorLabel = new JLabel("Error al cargar productos", SwingConstants.CENTER);
                    errorLabel.setForeground(Color.RED);
                    panelProductos.add(errorLabel);
                    panelProductos.revalidate();
                    panelProductos.repaint();
                }
        );
    }

    private void mostrarPlaceholderCarga() {
        panelProductos.removeAll();
        panelProductos.setLayout(new GridLayout(1, 1));

        JPanel placeholder = new JPanel();
        placeholder.setBackground(new Color(36, 30, 26));
        placeholder.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 200));

        JLabel loadingLabel = new JLabel("Cargando productos...");
        loadingLabel.setForeground(new Color(249, 155, 32));
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(14f).deriveFont(java.awt.Font.BOLD));
        placeholder.add(loadingLabel);

        panelProductos.add(placeholder);
        panelProductos.revalidate();
        panelProductos.repaint();
    }
    
    
    private void marcarCategoriaActiva(String categoria) {
        Color normal = new Color(41, 34, 28);
        Color activa = new Color(64, 46, 28);

        for (Map.Entry<JPanel, String> entry : panelesCategoria.entrySet()) {
            JPanel panel = entry.getKey();
            String cat = entry.getValue();
            panel.setBackground(cat.equalsIgnoreCase(categoria) ? activa : normal);
        }
    }

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
            logger.warning("No se pudieron cargar las categorias desde la BD, usando respaldo: " + e.getMessage());
            String[] respaldo = {"ENTRADAS", "PIZZAS", "HAMBURGUESAS", "PASTAS", "BEBIDAS", "POSTRES"};
            String[] nombres = {"Entradas", "Pizzas", "Hamburguesas", "Pastas", "Bebidas", "Postres"};
            for (int i = 0; i < respaldo.length; i++) {
                JPanel panel = crearPanelCategoria(respaldo[i], nombres[i]);
                PanelCategorias.add(panel);
                panelesCategoria.put(panel, nombres[i]);
            }
        }

        PanelCategorias.revalidate();
        PanelCategorias.repaint();
        ScrollCategorias.setViewportView(PanelCategorias);
    }

    private JPanel crearPanelCategoria(String key, String texto) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(41, 34, 28));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.setPreferredSize(new Dimension(112, 28));

        JLabel label = new JLabel(texto);
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mostrarProductos(key.equals("TODAS") ? "TODAS" : texto);
            }
        });

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(javax.swing.Box.createHorizontalGlue());
        panel.add(label);
        panel.add(javax.swing.Box.createHorizontalGlue());

        return panel;
    }

    private void actualizarFechaHora() {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");
        FechaMenuNum.setText(" " + fmtFecha.format(ahora));
        HoraMenuNum.setText(fmtHora.format(ahora));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        SideBar = new javax.swing.JPanel();
        LogoSistema = new javax.swing.JLabel();
        PanelMenu = new javax.swing.JPanel();
        Menu = new javax.swing.JLabel();
        PanelMesas = new javax.swing.JPanel();
        Mesas = new javax.swing.JLabel();
        PanelPedidos = new javax.swing.JPanel();
        Pedidos = new javax.swing.JLabel();
        PanelProductos = new javax.swing.JPanel();
        Productos = new javax.swing.JLabel();
        PanelReportes = new javax.swing.JPanel();
        Reportes = new javax.swing.JLabel();
        PanelConfiguraciones = new javax.swing.JPanel();
        PanelABM = new javax.swing.JPanel();
        Insumos = new javax.swing.JLabel();
        Contenido = new javax.swing.JPanel();
        MenuPanel = new javax.swing.JPanel();
        ContenidoMenu = new javax.swing.JPanel();
        CategoriasTxt = new javax.swing.JLabel();
        ScrollCategorias = new javax.swing.JScrollPane();
        PanelCategorias = new javax.swing.JPanel();
        CatTodas = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        CatEntrada = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        CatPizza = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        CatHamburguesas = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        CatPastas = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        CatBebidas = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        CatPostres = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        ScrollProducPedido = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        PanelPedido = new javax.swing.JPanel();
        Totaltxt = new javax.swing.JLabel();
        TotalNum = new javax.swing.JLabel();
        Separador = new javax.swing.JPanel();
        btnCancelarPedido = new javax.swing.JButton();
        btnConfirmarPedido = new javax.swing.JButton();
        jScrollProductos = new javax.swing.JScrollPane();
        panelProductos = new javax.swing.JPanel();
        Encabezado = new javax.swing.JPanel();
        TituloSeccion = new javax.swing.JLabel();
        FechaHora = new javax.swing.JPanel();
        HoraTxt = new javax.swing.JLabel();
        SeparadorHora = new javax.swing.JPanel();
        HoraMenuTxt = new javax.swing.JLabel();
        FechaMenuNum = new javax.swing.JLabel();
        HoraMenuNum = new javax.swing.JLabel();
        PanelUsuario = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        SideBar.setBackground(new java.awt.Color(27, 24, 21));
        SideBar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED, null, new java.awt.Color(109, 93, 83), null, null));

        LogoSistema.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LogoSistema.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/imagen/Captura de pantalla 2026-06-07 171854.png"))); // NOI18N

        PanelMenu.setBackground(new java.awt.Color(64, 46, 28));
        PanelMenu.setRequestFocusEnabled(false);

        Menu.setForeground(new java.awt.Color(255, 255, 255));
        Menu.setText("Menú");

        javax.swing.GroupLayout PanelMenuLayout = new javax.swing.GroupLayout(PanelMenu);
        PanelMenu.setLayout(PanelMenuLayout);
        PanelMenuLayout.setHorizontalGroup(
            PanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelMenuLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(Menu, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
        );
        PanelMenuLayout.setVerticalGroup(
            PanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Menu, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)
        );

        PanelMesas.setBackground(new java.awt.Color(27, 24, 21));
        PanelMesas.setPreferredSize(new java.awt.Dimension(0, 52));
        PanelMesas.setRequestFocusEnabled(false);

        Mesas.setForeground(new java.awt.Color(255, 255, 255));
        Mesas.setText("Mesas");
        Mesas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MesasMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout PanelMesasLayout = new javax.swing.GroupLayout(PanelMesas);
        PanelMesas.setLayout(PanelMesasLayout);
        PanelMesasLayout.setHorizontalGroup(
            PanelMesasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelMesasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Mesas, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                .addGap(43, 43, 43))
        );
        PanelMesasLayout.setVerticalGroup(
            PanelMesasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelMesasLayout.createSequentialGroup()
                .addComponent(Mesas, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                .addContainerGap())
        );

        PanelPedidos.setBackground(new java.awt.Color(27, 24, 21));
        PanelPedidos.setRequestFocusEnabled(false);

        Pedidos.setForeground(new java.awt.Color(255, 255, 255));
        Pedidos.setText("Pedidos");

        javax.swing.GroupLayout PanelPedidosLayout = new javax.swing.GroupLayout(PanelPedidos);
        PanelPedidos.setLayout(PanelPedidosLayout);
        PanelPedidosLayout.setHorizontalGroup(
            PanelPedidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelPedidosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Pedidos, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );
        PanelPedidosLayout.setVerticalGroup(
            PanelPedidosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Pedidos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
        );

        PanelProductos.setBackground(new java.awt.Color(27, 24, 21));
        PanelProductos.setRequestFocusEnabled(false);

        Productos.setForeground(new java.awt.Color(255, 255, 255));
        Productos.setText("Productos");

        javax.swing.GroupLayout PanelProductosLayout = new javax.swing.GroupLayout(PanelProductos);
        PanelProductos.setLayout(PanelProductosLayout);
        PanelProductosLayout.setHorizontalGroup(
            PanelProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelProductosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Productos, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );
        PanelProductosLayout.setVerticalGroup(
            PanelProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Productos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
        );

        PanelReportes.setBackground(new java.awt.Color(27, 24, 21));
        PanelReportes.setRequestFocusEnabled(false);

        Reportes.setForeground(new java.awt.Color(255, 255, 255));
        Reportes.setText("Reportes");

        javax.swing.GroupLayout PanelReportesLayout = new javax.swing.GroupLayout(PanelReportes);
        PanelReportes.setLayout(PanelReportesLayout);
        PanelReportesLayout.setHorizontalGroup(
            PanelReportesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelReportesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Reportes, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );
        PanelReportesLayout.setVerticalGroup(
            PanelReportesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Reportes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
        );

        PanelConfiguraciones.setBackground(new java.awt.Color(27, 24, 21));
        PanelConfiguraciones.setRequestFocusEnabled(false);

        javax.swing.GroupLayout PanelConfiguracionesLayout = new javax.swing.GroupLayout(PanelConfiguraciones);
        PanelConfiguraciones.setLayout(PanelConfiguracionesLayout);
        PanelConfiguracionesLayout.setHorizontalGroup(
            PanelConfiguracionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 141, Short.MAX_VALUE)
        );
        PanelConfiguracionesLayout.setVerticalGroup(
            PanelConfiguracionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        PanelABM.setBackground(new java.awt.Color(27, 24, 21));
        PanelABM.setRequestFocusEnabled(false);
        
        Insumos.setForeground(new java.awt.Color(255, 255, 255));
        Insumos.setText("Insumos");


        javax.swing.GroupLayout PanelABMLayout = new javax.swing.GroupLayout(PanelABM);
        PanelABM.setLayout(PanelABMLayout);

        PanelABMLayout.setHorizontalGroup(
            PanelABMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelABMLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Insumos, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );

PanelABMLayout.setVerticalGroup(
    PanelABMLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(Insumos,
            javax.swing.GroupLayout.DEFAULT_SIZE,
            52,
            Short.MAX_VALUE)
);

        javax.swing.GroupLayout SideBarLayout = new javax.swing.GroupLayout(SideBar);
        SideBar.setLayout(SideBarLayout);
        SideBarLayout.setHorizontalGroup(
            SideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LogoSistema, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
            .addGroup(SideBarLayout.createSequentialGroup()
                .addGroup(SideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, SideBarLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(SideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(PanelConfiguraciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(PanelReportes, javax.swing.GroupLayout.PREFERRED_SIZE, 141, Short.MAX_VALUE)
                            .addComponent(PanelProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 141, Short.MAX_VALUE)
                            .addComponent(PanelPedidos, javax.swing.GroupLayout.PREFERRED_SIZE, 141, Short.MAX_VALUE)))
                            .addComponent(PanelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(PanelABM, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(SideBarLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(PanelMesas, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        SideBarLayout.setVerticalGroup(
            SideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(LogoSistema, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43)
                .addComponent(PanelMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanelMesas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanelPedidos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanelProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanelReportes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanelABM,javax.swing.GroupLayout.PREFERRED_SIZE,javax.swing.GroupLayout.DEFAULT_SIZE,javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(PanelConfiguraciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(145, Short.MAX_VALUE))
        );

        jPanel1.add(SideBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 160, 700));

        MenuPanel.setBackground(new java.awt.Color(102, 102, 102));
        MenuPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ContenidoMenu.setBackground(new java.awt.Color(36, 30, 26));
        ContenidoMenu.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        CategoriasTxt.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        CategoriasTxt.setForeground(new java.awt.Color(255, 255, 255));
        CategoriasTxt.setText("CATEGORIAS");
        ContenidoMenu.add(CategoriasTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 23, 96, 21));

        ScrollCategorias.setBackground(new java.awt.Color(60, 63, 65));
        ScrollCategorias.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        PanelCategorias.setBackground(new java.awt.Color(41, 34, 28));

        CatTodas.setBackground(new java.awt.Color(41, 34, 28));

        jLabel2.setBackground(new java.awt.Color(64, 46, 28));
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Todas");
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout CatTodasLayout = new javax.swing.GroupLayout(CatTodas);
        CatTodas.setLayout(CatTodasLayout);
        CatTodasLayout.setHorizontalGroup(
            CatTodasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CatTodasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        CatTodasLayout.setVerticalGroup(
            CatTodasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
        );

        CatEntrada.setBackground(new java.awt.Color(41, 34, 28));

        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Entradas");
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout CatEntradaLayout = new javax.swing.GroupLayout(CatEntrada);
        CatEntrada.setLayout(CatEntradaLayout);
        CatEntradaLayout.setHorizontalGroup(
            CatEntradaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        CatEntradaLayout.setVerticalGroup(
            CatEntradaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
        );

        CatPizza.setBackground(new java.awt.Color(41, 34, 28));

        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Pizzas");
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout CatPizzaLayout = new javax.swing.GroupLayout(CatPizza);
        CatPizza.setLayout(CatPizzaLayout);
        CatPizzaLayout.setHorizontalGroup(
            CatPizzaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        CatPizzaLayout.setVerticalGroup(
            CatPizzaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
        );

        CatHamburguesas.setBackground(new java.awt.Color(41, 34, 28));

        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Hamburguesas");
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout CatHamburguesasLayout = new javax.swing.GroupLayout(CatHamburguesas);
        CatHamburguesas.setLayout(CatHamburguesasLayout);
        CatHamburguesasLayout.setHorizontalGroup(
            CatHamburguesasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
        );
        CatHamburguesasLayout.setVerticalGroup(
            CatHamburguesasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
        );

        CatPastas.setBackground(new java.awt.Color(41, 34, 28));

        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Pastas");
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout CatPastasLayout = new javax.swing.GroupLayout(CatPastas);
        CatPastas.setLayout(CatPastasLayout);
        CatPastasLayout.setHorizontalGroup(
            CatPastasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        CatPastasLayout.setVerticalGroup(
            CatPastasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
        );

        CatBebidas.setBackground(new java.awt.Color(41, 34, 28));

        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Bebidas");
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout CatBebidasLayout = new javax.swing.GroupLayout(CatBebidas);
        CatBebidas.setLayout(CatBebidasLayout);
        CatBebidasLayout.setHorizontalGroup(
            CatBebidasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        CatBebidasLayout.setVerticalGroup(
            CatBebidasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
        );

        CatPostres.setBackground(new java.awt.Color(41, 34, 28));

        jLabel8.setBackground(new java.awt.Color(41, 34, 28));
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Postres");
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout CatPostresLayout = new javax.swing.GroupLayout(CatPostres);
        CatPostres.setLayout(CatPostresLayout);
        CatPostresLayout.setHorizontalGroup(
            CatPostresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        CatPostresLayout.setVerticalGroup(
            CatPostresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout PanelCategoriasLayout = new javax.swing.GroupLayout(PanelCategorias);
        PanelCategorias.setLayout(PanelCategoriasLayout);
        PanelCategoriasLayout.setHorizontalGroup(
            PanelCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(CatTodas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(CatEntrada, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(CatPizza, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(CatHamburguesas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(CatPastas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(CatBebidas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(CatPostres, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        PanelCategoriasLayout.setVerticalGroup(
            PanelCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelCategoriasLayout.createSequentialGroup()
                .addComponent(CatTodas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CatEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CatPizza, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CatHamburguesas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CatPastas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CatBebidas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CatPostres, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(289, Short.MAX_VALUE))
        );

        ScrollCategorias.setViewportView(PanelCategorias);

        ContenidoMenu.add(ScrollCategorias, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 50, -1, -1));

        ScrollProducPedido.setBackground(new java.awt.Color(41, 34, 28));
        ScrollProducPedido.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        jTable1.setBackground(new java.awt.Color(41, 34, 28));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Producto", "Cant.", "Precio", ""
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setGridColor(new java.awt.Color(41, 34, 28));
        jTable1.setSelectionBackground(new java.awt.Color(41, 34, 28));
        jTable1.getTableHeader().setResizingAllowed(false);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        ScrollProducPedido.setViewportView(jTable1);

        ContenidoMenu.add(ScrollProducPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 20, 289, -1));

        PanelPedido.setBackground(new java.awt.Color(41, 34, 28));
        PanelPedido.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        Totaltxt.setBackground(new java.awt.Color(255, 255, 255));
        Totaltxt.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        Totaltxt.setForeground(new java.awt.Color(255, 255, 255));
        Totaltxt.setText("Total");

        TotalNum.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        TotalNum.setForeground(new java.awt.Color(249, 155, 32));
        TotalNum.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TotalNum.setText("$0.0");

        Separador.setBackground(new java.awt.Color(109, 93, 83));
        Separador.setPreferredSize(new java.awt.Dimension(2, 0));

        javax.swing.GroupLayout SeparadorLayout = new javax.swing.GroupLayout(Separador);
        Separador.setLayout(SeparadorLayout);
        SeparadorLayout.setHorizontalGroup(
            SeparadorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        SeparadorLayout.setVerticalGroup(
            SeparadorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 2, Short.MAX_VALUE)
        );

        btnCancelarPedido.setBackground(new java.awt.Color(73, 61, 50));
        btnCancelarPedido.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelarPedido.setText("Cancelar");
        btnCancelarPedido.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));
        btnCancelarPedido.addActionListener(this::btnCancelarPedidoActionPerformed);

        btnConfirmarPedido.setBackground(new java.awt.Color(249, 155, 32));
        btnConfirmarPedido.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnConfirmarPedido.setForeground(new java.awt.Color(255, 255, 255));
        btnConfirmarPedido.setText("Confirmar Pedido");
        btnConfirmarPedido.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));
        btnConfirmarPedido.addActionListener(this::btnConfirmarPedidoActionPerformed);

        javax.swing.GroupLayout PanelPedidoLayout = new javax.swing.GroupLayout(PanelPedido);
        PanelPedido.setLayout(PanelPedidoLayout);
        PanelPedidoLayout.setHorizontalGroup(
            PanelPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelPedidoLayout.createSequentialGroup()
                .addGroup(PanelPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelPedidoLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(Totaltxt, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(TotalNum, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PanelPedidoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(Separador, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE))
                    .addGroup(PanelPedidoLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(btnCancelarPedido, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnConfirmarPedido, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                        .addGap(8, 8, 8)))
                .addContainerGap())
        );
        PanelPedidoLayout.setVerticalGroup(
            PanelPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelPedidoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Totaltxt, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TotalNum, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Separador, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelPedidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCancelarPedido, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnConfirmarPedido, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        ContenidoMenu.add(PanelPedido, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 440, 289, 130));

        jScrollProductos.setBackground(new java.awt.Color(36, 30, 26));
        jScrollProductos.setBorder(null);

        panelProductos.setBackground(new java.awt.Color(36, 30, 26));

        javax.swing.GroupLayout panelProductosLayout = new javax.swing.GroupLayout(panelProductos);
        panelProductos.setLayout(panelProductosLayout);
        panelProductosLayout.setHorizontalGroup(
            panelProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 390, Short.MAX_VALUE)
        );
        panelProductosLayout.setVerticalGroup(
            panelProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 510, Short.MAX_VALUE)
        );

        jScrollProductos.setViewportView(panelProductos);

        ContenidoMenu.add(jScrollProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 50, 390, 510));

        MenuPanel.add(ContenidoMenu, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 840, 620));

        Encabezado.setBackground(new java.awt.Color(36, 30, 26));
        Encabezado.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        Encabezado.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        TituloSeccion.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        TituloSeccion.setForeground(new java.awt.Color(242, 242, 242));
        TituloSeccion.setText("Menú");
        Encabezado.add(TituloSeccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 0, 129, 80));

        FechaHora.setBackground(new java.awt.Color(36, 30, 26));

        HoraTxt.setForeground(new java.awt.Color(255, 255, 255));
        HoraTxt.setText("Fecha:");

        SeparadorHora.setBackground(new java.awt.Color(109, 93, 83));
        SeparadorHora.setPreferredSize(new java.awt.Dimension(2, 50));

        javax.swing.GroupLayout SeparadorHoraLayout = new javax.swing.GroupLayout(SeparadorHora);
        SeparadorHora.setLayout(SeparadorHoraLayout);
        SeparadorHoraLayout.setHorizontalGroup(
            SeparadorHoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 2, Short.MAX_VALUE)
        );
        SeparadorHoraLayout.setVerticalGroup(
            SeparadorHoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        HoraMenuTxt.setForeground(new java.awt.Color(255, 255, 255));
        HoraMenuTxt.setText(" Hora:");

        FechaMenuNum.setForeground(new java.awt.Color(255, 255, 255));
        FechaMenuNum.setText(" 00/00/0000");

        HoraMenuNum.setForeground(new java.awt.Color(255, 255, 255));
        HoraMenuNum.setText("00:00");

        javax.swing.GroupLayout FechaHoraLayout = new javax.swing.GroupLayout(FechaHora);
        FechaHora.setLayout(FechaHoraLayout);
        FechaHoraLayout.setHorizontalGroup(
            FechaHoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FechaHoraLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SeparadorHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(FechaHoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FechaHoraLayout.createSequentialGroup()
                        .addComponent(HoraMenuTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(HoraMenuNum))
                    .addGroup(FechaHoraLayout.createSequentialGroup()
                        .addComponent(HoraTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                        .addComponent(FechaMenuNum)))
                .addContainerGap())
        );
        FechaHoraLayout.setVerticalGroup(
            FechaHoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FechaHoraLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(SeparadorHora, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(FechaHoraLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(FechaHoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(HoraTxt)
                    .addComponent(FechaMenuNum))
                .addGap(18, 18, 18)
                .addGroup(FechaHoraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(HoraMenuTxt)
                    .addComponent(HoraMenuNum))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Encabezado.add(FechaHora, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 0, 150, 70));

        PanelUsuario.setBackground(new java.awt.Color(36, 30, 26));

        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("Usuario");

        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Administrador");

        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/imagen/icon-icons (1).png"))); // NOI18N

        javax.swing.GroupLayout PanelUsuarioLayout = new javax.swing.GroupLayout(PanelUsuario);
        PanelUsuario.setLayout(PanelUsuarioLayout);
        PanelUsuarioLayout.setHorizontalGroup(
            PanelUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(PanelUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16))
                .addContainerGap())
        );
        PanelUsuarioLayout.setVerticalGroup(
            PanelUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelUsuarioLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(PanelUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(PanelUsuarioLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel16)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        Encabezado.add(PanelUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 0, 150, 70));

        MenuPanel.add(Encabezado, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 840, 80));

        javax.swing.GroupLayout ContenidoLayout = new javax.swing.GroupLayout(Contenido);
        Contenido.setLayout(ContenidoLayout);
        ContenidoLayout.setHorizontalGroup(
            ContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 840, Short.MAX_VALUE)
            .addGroup(ContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ContenidoLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(MenuPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        ContenidoLayout.setVerticalGroup(
            ContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 700, Short.MAX_VALUE)
            .addGroup(ContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ContenidoLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(MenuPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jPanel1.add(Contenido, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 0, 840, 700));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    
  private void configurarPanelProductos() {
    panelProductos.setLayout(
        new java.awt.FlowLayout(
            java.awt.FlowLayout.LEFT,
            10,
            10
        )
    );
}
    
    
    
    
    
    
    private void configurarTabla() {

    javax.swing.table.DefaultTableCellRenderer headerRenderer =
            new javax.swing.table.DefaultTableCellRenderer();

    headerRenderer.setBackground(new Color(45, 30, 20));
    headerRenderer.setForeground(Color.WHITE);
    headerRenderer.setOpaque(true);

    for (int i = 0; i < jTable1.getColumnModel().getColumnCount(); i++) {
        jTable1.getColumnModel()
               .getColumn(i)
               .setHeaderRenderer(headerRenderer);
    }

    jTable1.getColumnModel().getColumn(0).setPreferredWidth(180);
    jTable1.getColumnModel().getColumn(1).setPreferredWidth(60);
    jTable1.getColumnModel().getColumn(2).setPreferredWidth(80);
    jTable1.getColumnModel().getColumn(3).setPreferredWidth(30);

    jTable1.getColumnModel().getColumn(3).setMinWidth(30);
    jTable1.getColumnModel().getColumn(3).setMaxWidth(30);

    jTable1.setForeground(Color.WHITE);
    jTable1.setBackground(new Color(36, 30, 26));

    ScrollProducPedido.setBackground(
            new Color(36, 30, 26));

    ScrollProducPedido.getViewport().setBackground(
            new Color(36, 30, 26));
}
    
    
    
    
    
    
    private void configurarScrollBars() {

    jScrollProductos.getVerticalScrollBar().setUI(
        new BasicScrollBarUI() {

            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(249, 155, 32);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return crearBotonVacio();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return crearBotonVacio();
            }

            private JButton crearBotonVacio() {
                JButton boton = new JButton();
                boton.setPreferredSize(new Dimension(0, 0));
                return boton;
            }
        }
    );

    jScrollProductos.getVerticalScrollBar()
                    .setPreferredSize(new Dimension(8, 0));
    jScrollProductos.getVerticalScrollBar()
                    .setUnitIncrement(10);
}
    
    
    
    private void configurarContenidoPrincipal() {
        cardLayout = new CardLayout();
        Contenido.removeAll();
        Contenido.setLayout(cardLayout);

        Contenido.add(MenuPanel, "MENU");
        Contenido.add(new vistas.paneles.MesasPanel(), "MESAS");
        Contenido.add(new vistas.paneles.PedidosPanel(), "PEDIDOS");
        Contenido.add(new vistas.paneles.ProductosPanel(), "PRODUCTOS");
        Contenido.add(new vistas.paneles.ReportesPanel(), "REPORTES");
        Contenido.add(new vistas.paneles.InsumosPanel(), "INSUMOS");

        registrarNavegacion("MENU", PanelMenu, Menu);
        registrarNavegacion("MESAS", PanelMesas, Mesas);
        registrarNavegacion("PEDIDOS", PanelPedidos, Pedidos);
        registrarNavegacion("PRODUCTOS", PanelProductos, Productos);
        registrarNavegacion("REPORTES", PanelReportes, Reportes);
        registrarNavegacion("INSUMOS", PanelABM, Insumos);

        mostrarPanel("MENU");
    }

    private void registrarNavegacion(String tarjeta, java.awt.Component... componentes) {
        java.awt.event.MouseAdapter listener = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mostrarPanel(tarjeta);
            }
        };

        for (java.awt.Component componente : componentes) {
            componente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            componente.addMouseListener(listener);
        }
    }

    private void mostrarPanel(String tarjeta) {
        if (cardLayout == null) {
            return;
        }

        if ("PRODUCTOS".equals(tarjeta)) {
            for (java.awt.Component comp : Contenido.getComponents()) {
                if (comp instanceof vistas.paneles.ProductosPanel) {
                    ((vistas.paneles.ProductosPanel) comp).listarProductos();
                    break;
                }
            }
        }

        if ("PEDIDOS".equals(tarjeta)) {
            for (java.awt.Component comp : Contenido.getComponents()) {
                if (comp instanceof vistas.paneles.PedidosPanel) {
                    ((vistas.paneles.PedidosPanel) comp).listarPedidos();
                    break;
                }
            }
        }

        if ("MESAS".equals(tarjeta)) {
            for (java.awt.Component comp : Contenido.getComponents()) {
                if (comp instanceof vistas.paneles.MesasPanel) {
                    ((vistas.paneles.MesasPanel) comp).actualizarMesas();
                    break;
                }
            }
        }

        cardLayout.show(Contenido, tarjeta);
        marcarItemActivo(tarjeta);
        Contenido.revalidate();
        Contenido.repaint();
    }


    private void marcarItemActivo(String tarjeta) {
        Color fondoNormal = new Color(27, 24, 21);
        Color fondoActivo = new Color(64, 46, 28);
        Color textoNormal = Color.WHITE;
        Color textoActivo = new Color(249, 155, 32);

        PanelMenu.setBackground("MENU".equals(tarjeta) ? fondoActivo : fondoNormal);
        PanelMesas.setBackground("MESAS".equals(tarjeta) ? fondoActivo : fondoNormal);
        PanelPedidos.setBackground("PEDIDOS".equals(tarjeta) ? fondoActivo : fondoNormal);
        PanelProductos.setBackground("PRODUCTOS".equals(tarjeta) ? fondoActivo : fondoNormal);
        PanelReportes.setBackground("REPORTES".equals(tarjeta) ? fondoActivo : fondoNormal);
        PanelABM.setBackground("INSUMOS".equals(tarjeta) ? fondoActivo : fondoNormal);
        

        Menu.setForeground("MENU".equals(tarjeta) ? textoActivo : textoNormal);
        Mesas.setForeground("MESAS".equals(tarjeta) ? textoActivo : textoNormal);
        Pedidos.setForeground("PEDIDOS".equals(tarjeta) ? textoActivo : textoNormal);
        Productos.setForeground("PRODUCTOS".equals(tarjeta) ? textoActivo : textoNormal);
        Reportes.setForeground("REPORTES".equals(tarjeta) ? textoActivo : textoNormal);
        Insumos.setForeground("INSUMOS".equals(tarjeta) ? textoActivo : textoNormal);
        
    }

    private void MesasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MesasMouseClicked
        
    }//GEN-LAST:event_MesasMouseClicked

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        int fila = jTable1.rowAtPoint(evt.getPoint());
        int columna = jTable1.columnAtPoint(evt.getPoint());

        if (columna == 3) {

            DefaultTableModel modelo =
            (DefaultTableModel) jTable1.getModel();

            modelo.removeRow(fila);
             actualizarTotal();
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        mostrarProductos("TODAS");
    }//GEN-LAST:event_jLabel2MouseClicked

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        mostrarProductos("ENTRADAS");
    }//GEN-LAST:event_jLabel3MouseClicked

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        mostrarProductos("PIZZAS");        
    }//GEN-LAST:event_jLabel4MouseClicked

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        mostrarProductos("HAMBURGUESAS");        
    }//GEN-LAST:event_jLabel5MouseClicked

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        mostrarProductos("PASTAS");        
    }//GEN-LAST:event_jLabel6MouseClicked

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
        mostrarProductos("BEBIDAS");        
    }//GEN-LAST:event_jLabel7MouseClicked

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        mostrarProductos("POSTRES");        
    }//GEN-LAST:event_jLabel8MouseClicked

    
    
    // TODO: Reemplazar datos hardcodeados con:
//   List<Producto> productos = ServicioFactory.getProductoServicio().obtenerPorCategoria(categoria);
    private void btnConfirmarPedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmarPedidoActionPerformed
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        int rows = modelo.getRowCount();
        if (rows == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "El pedido no tiene productos.", "Advertencia", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnConfirmarPedido.setEnabled(false);
        btnConfirmarPedido.setText("Cargando...");

        AsyncDataLoader.load(
                this,
                () -> {
                    java.util.List<com.restaurant.backend.model.Mesa> mesas = ServicioFactory.getMesaService().listar();
                    java.util.List<String> mesasStr = new java.util.ArrayList<>();
                    for (com.restaurant.backend.model.Mesa m : mesas) {
                        if (m.getEstado() == com.restaurant.backend.model.EstadoMesa.LIBRE ||
                            m.getEstado() == com.restaurant.backend.model.EstadoMesa.RESERVADA ||
                            m.getEstado() == com.restaurant.backend.model.EstadoMesa.OCUPADA) {
                            mesasStr.add("Mesa " + m.getNumero());
                        }
                    }
                    return mesasStr;
                },
                mesasStr -> {
                    btnConfirmarPedido.setEnabled(true);
                    btnConfirmarPedido.setText("Confirmar Pedido");

                    if (mesasStr.isEmpty()) {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "No hay mesas disponibles para asignar el pedido.",
                                "Sin mesas disponibles", javax.swing.JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    java.util.List<String[]> items = new java.util.ArrayList<>();
                    double subtotal = 0;
                    for (int i = 0; i < rows; i++) {
                        String nombre = modelo.getValueAt(i, 0).toString();
                        String cantidad = modelo.getValueAt(i, 1).toString();
                        String precio = modelo.getValueAt(i, 2).toString();
                        items.add(new String[]{nombre, cantidad, precio});
                        subtotal += Double.parseDouble(precio) * Integer.parseInt(cantidad);
                    }

                    CheckoutDialog dialog = new CheckoutDialog(this, true);
                    dialog.cargarMesas(mesasStr.toArray(new String[0]));
                    dialog.cargarDetalles(items);
                    dialog.setSubtotalYCalcular(subtotal);
                    dialog.setLocationRelativeTo(this);
                    dialog.setVisible(true);

                    if (dialog.isConfirmado()) {
                        confirmarPedidoEnBackend(dialog, items, subtotal, modelo);
                    }
                },
                error -> {
                    btnConfirmarPedido.setEnabled(true);
                    btnConfirmarPedido.setText("Confirmar Pedido");
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Error al cargar mesas: " + error.getMessage(),
                            "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
        );
    }//GEN-LAST:event_btnConfirmarPedidoActionPerformed

    private void confirmarPedidoEnBackend(CheckoutDialog dialog, java.util.List<String[]> items,
                                           double subtotal, DefaultTableModel modelo) {
        String mesaSeleccionada = dialog.getMesaSeleccionada();
        if (mesaSeleccionada == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una mesa.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        int numMesa = Integer.parseInt(mesaSeleccionada.replace("Mesa ", "").trim());
        String observaciones = dialog.getObservaciones();
        String metodoPago = dialog.getMetodoPago();
        String descuento = dialog.getDescuento();

        AsyncDataLoader.execute(
                this,
                () -> {
                    com.restaurant.backend.model.Mesa mesaObj =
                            ServicioFactory.getMesaService().obtenerPorNumero(numMesa);
                    if (mesaObj == null) {
                        return "Error: No se encontro la mesa seleccionada en el sistema.";
                    }

                    java.util.List<com.restaurant.backend.model.Producto> todosProductos =
                            ServicioFactory.getProductoService().obtenerTodos();
                    java.util.List<com.restaurant.backend.model.DetallePedido> detalles =
                            new java.util.ArrayList<>();

                    for (String[] item : items) {
                        String nombreProducto = item[0];
                        int cantidad = Integer.parseInt(item[1]);

                        com.restaurant.backend.model.Producto prod = null;
                        for (com.restaurant.backend.model.Producto p : todosProductos) {
                            if (p.getNombre().equalsIgnoreCase(nombreProducto)) {
                                prod = p;
                                break;
                            }
                        }
                        if (prod == null) {
                            return "Error: No se encontro el producto: " + nombreProducto;
                        }

                        com.restaurant.backend.model.DetallePedido dp =
                                new com.restaurant.backend.model.DetallePedido();
                        dp.setProducto(prod);
                        dp.setCantidad(cantidad);
                        dp.setPrecioUnitario(prod.getPrecio());
                        dp.recalcularSubtotal();
                        detalles.add(dp);
                    }

                    Usuario usuario = usuarioActual;
                    if (usuario == null) {
                        java.util.List<Usuario> usuarios =
                                ServicioFactory.getUsuarioService().listar();
                        if (!usuarios.isEmpty()) {
                            usuario = usuarios.get(0);
                        } else {
                            usuario = new Usuario();
                            usuario.setIdUsuario(1);
                            usuario.setNombre("Admin");
                            usuario.setApellido("Sistema");
                        }
                    }

                    StringBuilder obsBuilder = new StringBuilder();
                    if (observaciones != null && !observaciones.trim().isEmpty()) {
                        obsBuilder.append(observaciones.trim());
                    }
                    if (metodoPago != null && !metodoPago.trim().isEmpty()) {
                        if (obsBuilder.length() > 0) obsBuilder.append(" | ");
                        obsBuilder.append("Pago: ").append(metodoPago);
                    }
                    if (descuento != null && !descuento.trim().isEmpty()) {
                        if (obsBuilder.length() > 0) obsBuilder.append(" | ");
                        obsBuilder.append("Desc: ").append(descuento);
                    }

                    String resultado = ServicioFactory.getPedidoService()
                            .crearPedido(mesaObj, usuario, detalles, obsBuilder.toString());
                    return "OK|" + resultado + "|" + usuario.getNombre() + " " + usuario.getApellido();
                },
                result -> {
                    if (result.startsWith("Error:")) {
                        javax.swing.JOptionPane.showMessageDialog(this, result,
                                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String[] parts = result.split("\\|", 3);
                    String resultado = parts.length > 1 ? parts[1] : result;
                    String mozo = parts.length > 2 ? parts[2] : "Sistema";

                    if (resultado.toLowerCase().contains("se cambio el estado")
                            || resultado.toLowerCase().contains("correctamente")
                            || resultado.toLowerCase().contains("exito")
                            || resultado.isEmpty()) {

                        double descuentoVal = 0.0;
                        String descText = descuento.trim();
                        if (!descText.isEmpty()) {
                            try {
                                if (descText.endsWith("%")) {
                                    descText = descText.substring(0, descText.length() - 1).trim();
                                }
                                descuentoVal = Double.parseDouble(descText);
                            } catch (NumberFormatException e) {
                                descuentoVal = 0.0;
                            }
                        }
                        double total = subtotal;
                        if (descuentoVal > 0) {
                            if (descuentoVal <= 100) {
                                total = subtotal * (1 - (descuentoVal / 100.0));
                            } else {
                                total = Math.max(0.0, subtotal - descuentoVal);
                            }
                        }

                        modelo.setRowCount(0);
                        actualizarTotal();
                        guardarComanda(numMesa, mozo, items, subtotal, total,
                                metodoPago, observaciones, descuento);
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Error al crear el pedido en el backend:\n" + resultado,
                                "Error al guardar pedido",
                                javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
        );
    }

    private void guardarComanda(int numeroMesa, String mozo, java.util.List<String[]> items, double subtotal, double total, String metodoPago, String observaciones, String descuento) {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");
        
        StringBuilder ticket = new StringBuilder();
        ticket.append("==========================================\n");
        ticket.append("               RestoManager               \n");
        ticket.append("==========================================\n");
        ticket.append(String.format("FECHA: %-15s HORA: %s\n", fmtFecha.format(ahora), fmtHora.format(ahora)));
        ticket.append(String.format("MESA: %-16d\n", numeroMesa));
        ticket.append(String.format("ATENDIDO POR: %s\n", mozo));
        ticket.append("------------------------------------------\n");
        ticket.append(String.format("%-6s %-25s %9s\n", "Cant.", "Producto", "Subtotal"));
        ticket.append("------------------------------------------\n");
        for (String[] item : items) {
            String nombre = item[0];
            String cantidad = item[1];
            double precio = Double.parseDouble(item[2]);
            double sub = Integer.parseInt(cantidad) * precio;
            ticket.append(String.format("%-6s %-25s %9.2f\n", cantidad, nombre.length() > 25 ? nombre.substring(0, 25) : nombre, sub));
        }
        ticket.append("------------------------------------------\n");
        ticket.append(String.format("SUBTOTAL: %32.2f\n", subtotal));
        if (descuento != null && !descuento.trim().isEmpty()) {
            ticket.append(String.format("DESCUENTO: %31s\n", descuento));
        }
        ticket.append(String.format("TOTAL: %35.2f\n", total));
        ticket.append(String.format("METODO DE PAGO: %26s\n", metodoPago));
        if (observaciones != null && !observaciones.trim().isEmpty()) {
            ticket.append("\nOBSERVACIONES:\n").append(observaciones).append("\n");
        }
        ticket.append("==========================================\n");
        ticket.append("        ¡Muchas gracias por elegirnos!     \n");
        ticket.append("==========================================\n");

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Imprimir Comanda (Guardar Archivo)");
        fileChooser.setSelectedFile(new java.io.File("comanda_mesa_" + numeroMesa + ".txt"));
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try (java.io.FileWriter writer = new java.io.FileWriter(fileToSave)) {
                writer.write(ticket.toString());
                javax.swing.JOptionPane.showMessageDialog(this, "Comanda guardada correctamente en:\n" + fileToSave.getAbsolutePath(), "Impresión Exitosa", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch (java.io.IOException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void btnCancelarPedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarPedidoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCancelarPedidoActionPerformed
        
    
    
// TODO: Reemplazar datos hardcodeados con:
//   List<Producto> productos = ServicioFactory.getProductoServicio().obtenerPorCategoria(categoria);
    
    private void mostrarProductos(String categoria){
        marcarCategoriaActiva(categoria);

        mostrarPlaceholderCarga();

        AsyncDataLoader.load(
                this,
                () -> {
                    List<Producto> productos;
                    if ("TODAS".equalsIgnoreCase(categoria)) {
                        productos = new java.util.ArrayList<>();
                        for (Producto p : ServicioFactory.getProductoService().obtenerTodos()) {
                            if (p.isDisponible()) {
                                productos.add(p);
                            }
                        }
                    } else {
                        productos = ServicioFactory.getProductoService().obtenerPorCategoria(categoria);
                    }
                    return productos;
                },
                productos -> {
                    panelProductos.removeAll();
                    panelProductos.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,10,10));

                    for (Producto prod : productos) {
                        CardProducto card = new CardProducto();
                        card.setProducto(prod.getNombre(), prod.getPrecio().doubleValue());
                        card.setOnAgregarListener(
                                (nombre, precio) -> agregarProductoTabla(nombre, precio)
                        );
                        panelProductos.add(card);
                    }

                    int cantidad = panelProductos.getComponentCount();
                    int filas = (int) Math.ceil(cantidad / 3.0);

                    panelProductos.setPreferredSize(new java.awt.Dimension(panelProductos.getWidth(), filas * 140));
                        
                    panelProductos.revalidate();
                    panelProductos.repaint();
                    jScrollProductos.setHorizontalScrollBarPolicy(
                            javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                },
                error -> {
                    panelProductos.removeAll();
                    JLabel errorLabel = new JLabel("Error al cargar productos", SwingConstants.CENTER);
                    errorLabel.setForeground(Color.RED);
                    panelProductos.add(errorLabel);
                    panelProductos.revalidate();
                    panelProductos.repaint();
                }
        );
    }
    
    
    
    private void agregarProductoTabla(String nombre, double precio){
        
        
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        
        for (int i = 0; i < modelo.getRowCount(); i++) {

            String productoTabla =
                    modelo.getValueAt(i, 0).toString();

            if (productoTabla.equals(nombre)) {

                int cantidad =
                        Integer.parseInt(
                                modelo.getValueAt(i, 1).toString()
                        );

                modelo.setValueAt(cantidad + 1, i, 1);

                return;
            }
        }

        modelo.addRow(new Object[]{
            nombre, 1, precio, "X"
        });
        
         actualizarTotal();
        
    }
    
    
    private void actualizarTotal() {

    DefaultTableModel modelo =
            (DefaultTableModel) jTable1.getModel();

    double total = 0;

    for (int i = 0; i < modelo.getRowCount(); i++) {
        int cantidad = Integer.parseInt(modelo.getValueAt(i, 1).toString());
        double precio = Double.parseDouble(modelo.getValueAt(i, 2).toString());
        total += precio * cantidad;
    }

    TotalNum.setText("$" + total);
}
    
    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Menu().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CatBebidas;
    private javax.swing.JPanel CatEntrada;
    private javax.swing.JPanel CatHamburguesas;
    private javax.swing.JPanel CatPastas;
    private javax.swing.JPanel CatPizza;
    private javax.swing.JPanel CatPostres;
    private javax.swing.JPanel CatTodas;
    private javax.swing.JLabel CategoriasTxt;
    private javax.swing.JPanel Contenido;
    private javax.swing.JPanel ContenidoMenu;
    private javax.swing.JPanel Encabezado;
    private javax.swing.JPanel FechaHora;
    private javax.swing.JLabel FechaMenuNum;
    private javax.swing.JLabel HoraMenuNum;
    private javax.swing.JLabel HoraMenuTxt;
    private javax.swing.JLabel HoraTxt;
    private javax.swing.JLabel LogoSistema;
    private javax.swing.JLabel Menu;
    private javax.swing.JPanel MenuPanel;
    private javax.swing.JLabel Mesas;
    private javax.swing.JPanel PanelCategorias;
    private javax.swing.JPanel PanelConfiguraciones;
    private javax.swing.JPanel PanelMenu;
    private javax.swing.JPanel PanelMesas;
    private javax.swing.JPanel PanelPedido;
    private javax.swing.JPanel PanelPedidos;
    private javax.swing.JPanel PanelProductos;
    private javax.swing.JPanel PanelReportes;
    private javax.swing.JPanel PanelUsuario;
    private javax.swing.JLabel Pedidos;
    private javax.swing.JLabel Productos;
    private javax.swing.JLabel Reportes;
    private javax.swing.JScrollPane ScrollCategorias;
    private javax.swing.JScrollPane ScrollProducPedido;
    private javax.swing.JPanel Separador;
    private javax.swing.JPanel SeparadorHora;
    private javax.swing.JPanel SideBar;
    private javax.swing.JLabel TituloSeccion;
    private javax.swing.JLabel TotalNum;
    private javax.swing.JLabel Totaltxt;
    private javax.swing.JButton btnCancelarPedido;
    private javax.swing.JButton btnConfirmarPedido;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollProductos;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel panelProductos;
    private javax.swing.JPanel PanelABM;
    private javax.swing.JLabel Insumos;
    // End of variables declaration//GEN-END:variables
}
