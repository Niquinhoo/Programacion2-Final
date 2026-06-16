/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package vistas.paneles;

import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.restaurant.backend.controller.ProductoController;
import com.restaurant.backend.controller.CategoriaController;
import com.restaurant.backend.model.Producto;
import com.restaurant.backend.model.Categoria;
import vistas.util.AsyncDataLoader;



/**
 *
 * @author enzol
 */
public class ProductosPanel extends javax.swing.JPanel {

    private final ProductoController productoController = new ProductoController();
    private final CategoriaController categoriaController = new CategoriaController();

    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;

    /**
     * Creates new form ProductosPanel
     */
    public ProductosPanel() {
        initComponents();
        configurarTabla();
        agregarBotonesAccion();
        listarProductos();
    }

    private void agregarBotonesAccion() {
        btnAgregar = new javax.swing.JButton("Agregar");
        btnEditar = new javax.swing.JButton("Editar");
        btnEliminar = new javax.swing.JButton("Eliminar");

        btnAgregar.setBackground(new Color(51, 153, 51));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnEditar.setBackground(new Color(249, 155, 32));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnEliminar.setBackground(new Color(204, 51, 51));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        Encabezado2.add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 25, 90, 30));
        Encabezado2.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 25, 90, 30));
        Encabezado2.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 25, 90, 30));

        btnAgregar.addActionListener(e -> mostrarDialogoFormulario(null));

        btnEditar.addActionListener(e -> {
            int selectedRow = jTable1.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (Integer) jTable1.getValueAt(selectedRow, 3);
                AsyncDataLoader.load(
                        this,
                        () -> productoController.obtenerPorId(id),
                        p -> {
                            if (p != null) {
                                mostrarDialogoFormulario(p);
                            } else {
                                JOptionPane.showMessageDialog(this, "No se pudo recuperar el producto de la base de datos.");
                            }
                        }
                );
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para editar.");
            }
        });

        btnEliminar.addActionListener(e -> {
            int selectedRow = jTable1.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (Integer) jTable1.getValueAt(selectedRow, 3);
                String nombre = (String) jTable1.getValueAt(selectedRow, 0);
                int opt = JOptionPane.showConfirmDialog(this,
                    "Esta seguro de que desea eliminar el producto \"" + nombre + "\"?\nEsta accion no se puede deshacer.",
                    "Confirmar Eliminacion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (opt == JOptionPane.YES_OPTION) {
                    AsyncDataLoader.execute(
                            this,
                            () -> productoController.eliminar(id),
                            resultado -> {
                                JOptionPane.showMessageDialog(this, resultado);
                                listarProductos();
                            }
                    );
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla para eliminar.");
            }
        });
    }

    public void listarProductos() {
        AsyncDataLoader.load(
                this,
                () -> {
                    java.util.Map<Integer, Long> ventasMap = new java.util.HashMap<>();
                    try {
                        List<com.restaurant.backend.service.dto.VentaPorProductoDTO> ventas =
                            com.restaurant.backend.service.ServicioFactory.getReporteService().ventasPorProducto();
                        for (com.restaurant.backend.service.dto.VentaPorProductoDTO v : ventas) {
                            ventasMap.put(v.getIdProducto(), v.getUnidadesVendidas());
                        }
                    } catch (Exception ex) {
                        System.out.println("Error al obtener estadisticas de ventas: " + ex.getMessage());
                    }

                    List<Producto> productos = productoController.listar();

                    Object[][] filas = new Object[productos.size()][5];
                    int i = 0;
                    for (Producto p : productos) {
                        long unidadesVendidas = ventasMap.getOrDefault(p.getIdProducto(), 0L);
                        filas[i++] = new Object[]{
                            p.getNombre(),
                            p.getStock(),
                            p.getPrecio(),
                            p.getIdProducto(),
                            unidadesVendidas
                        };
                    }
                    return filas;
                },
                filas -> {
                    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                    model.setRowCount(0);
                    for (Object[] fila : filas) {
                        model.addRow(fila);
                    }
                }
        );
    }

    private void mostrarDialogoFormulario(Producto productoEditar) {
        AsyncDataLoader.load(
                this,
                () -> categoriaController.listar(),
                categorias -> construirDialogoFormulario(productoEditar, categorias)
        );
    }

    private void construirDialogoFormulario(Producto productoEditar, List<Categoria> categorias) {
        boolean esNuevo = (productoEditar == null);
        JDialog dialog = new JDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
            esNuevo ? "Agregar Producto" : "Editar Producto", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(36, 30, 26));
        dialog.setLayout(null);

        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setBounds(30, 20, 100, 25);
        dialog.add(lblNombre);

        JTextField txtNombre = new JTextField();
        txtNombre.setBounds(150, 20, 200, 25);
        txtNombre.setBackground(new Color(41, 34, 28));
        txtNombre.setForeground(Color.WHITE);
        txtNombre.setCaretColor(Color.WHITE);
        dialog.add(txtNombre);

        JLabel lblDesc = new JLabel("Descripción:");
        lblDesc.setForeground(Color.WHITE);
        lblDesc.setBounds(30, 60, 100, 25);
        dialog.add(lblDesc);

        JTextField txtDesc = new JTextField();
        txtDesc.setBounds(150, 60, 200, 25);
        txtDesc.setBackground(new Color(41, 34, 28));
        txtDesc.setForeground(Color.WHITE);
        txtDesc.setCaretColor(Color.WHITE);
        dialog.add(txtDesc);

        JLabel lblPrecio = new JLabel("Precio:");
        lblPrecio.setForeground(Color.WHITE);
        lblPrecio.setBounds(30, 100, 100, 25);
        dialog.add(lblPrecio);

        JTextField txtPrecio = new JTextField();
        txtPrecio.setBounds(150, 100, 200, 25);
        txtPrecio.setBackground(new Color(41, 34, 28));
        txtPrecio.setForeground(Color.WHITE);
        txtPrecio.setCaretColor(Color.WHITE);
        dialog.add(txtPrecio);

        JLabel lblStock = new JLabel("Stock:");
        lblStock.setForeground(Color.WHITE);
        lblStock.setBounds(30, 140, 100, 25);
        dialog.add(lblStock);

        JTextField txtStock = new JTextField();
        txtStock.setBounds(150, 140, 200, 25);
        txtStock.setBackground(new Color(41, 34, 28));
        txtStock.setForeground(Color.WHITE);
        txtStock.setCaretColor(Color.WHITE);
        dialog.add(txtStock);

        JLabel lblCat = new JLabel("Categoría:");
        lblCat.setForeground(Color.WHITE);
        lblCat.setBounds(30, 180, 100, 25);
        dialog.add(lblCat);

        JComboBox<Categoria> cmbCategoria = new JComboBox<>();
        cmbCategoria.setBounds(150, 180, 200, 25);
        cmbCategoria.setBackground(new Color(41, 34, 28));
        cmbCategoria.setForeground(Color.WHITE);
        for (Categoria c : categorias) {
            cmbCategoria.addItem(c);
        }
        dialog.add(cmbCategoria);

        JLabel lblDisp = new JLabel("Disponible:");
        lblDisp.setForeground(Color.WHITE);
        lblDisp.setBounds(30, 220, 100, 25);
        dialog.add(lblDisp);

        JCheckBox chkDisponible = new JCheckBox();
        chkDisponible.setBounds(150, 220, 50, 25);
        chkDisponible.setBackground(new Color(36, 30, 26));
        chkDisponible.setSelected(true);
        dialog.add(chkDisponible);

        if (!esNuevo) {
            txtNombre.setText(productoEditar.getNombre());
            txtDesc.setText(productoEditar.getDescripcion() != null ? productoEditar.getDescripcion() : "");
            txtPrecio.setText(productoEditar.getPrecio().toString());
            txtStock.setText(String.valueOf(productoEditar.getStock()));
            chkDisponible.setSelected(productoEditar.isDisponible());

            if (productoEditar.getCategoria() != null) {
                for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                    if (cmbCategoria.getItemAt(i).getIdCategoria().equals(productoEditar.getCategoria().getIdCategoria())) {
                        cmbCategoria.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(80, 300, 100, 35);
        btnGuardar.setBackground(new Color(249, 155, 32));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dialog.add(btnGuardar);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(200, 300, 100, 35);
        btnCancelar.setBackground(new Color(109, 93, 83));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dialog.add(btnCancelar);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String desc = txtDesc.getText().trim();
            String precioStr = txtPrecio.getText().trim();
            String stockStr = txtStock.getText().trim();
            Categoria cat = (Categoria) cmbCategoria.getSelectedItem();

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El nombre es obligatorio.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cat == null) {
                JOptionPane.showMessageDialog(dialog, "Debe seleccionar una categoría.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal precio;
            try {
                precio = new BigDecimal(precioStr);
                if (precio.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(dialog, "El precio no puede ser negativo.", "Validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "El precio debe ser un número válido.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int stock;
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    JOptionPane.showMessageDialog(dialog, "El stock no puede ser negativo.", "Validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "El stock debe ser un número entero válido.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Producto prod = esNuevo ? new Producto() : productoEditar;
            prod.setNombre(nombre);
            prod.setDescripcion(desc.isEmpty() ? null : desc);
            prod.setPrecio(precio);
            prod.setStock(stock);
            prod.setCategoria(cat);
            prod.setDisponible(chkDisponible.isSelected());

            AsyncDataLoader.execute(
                    this,
                    () -> esNuevo ? productoController.crear(prod) : productoController.editar(prod),
                    res -> {
                        JOptionPane.showMessageDialog(dialog, res);
                        if (res.toLowerCase().contains("correctamente") || res.toLowerCase().contains("insertado") || res.toLowerCase().contains("editado")) {
                            dialog.dispose();
                            listarProductos();
                        }
                    }
            );
        });

        dialog.setVisible(true);
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
    jTable1.getColumnModel().getColumn(4).setPreferredWidth(80);
    
    jTable1.setForeground(Color.WHITE);
    jTable1.setBackground(new Color(36, 30, 26));

    jScrollPane2.setBackground(
            new Color(36, 30, 26));

    jScrollPane2.getViewport().setBackground(
            new Color(36, 30, 26));
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
        Encabezado2 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        FechaHora2 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jPanel1.setBackground(new java.awt.Color(36, 30, 26));

        Encabezado2.setBackground(new java.awt.Color(36, 30, 26));
        Encabezado2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        Encabezado2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(242, 242, 242));
        jLabel16.setText("Productos");
        Encabezado2.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 0, 129, 80));

        FechaHora2.setBackground(new java.awt.Color(36, 30, 26));

        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("Fecha");

        jPanel10.setBackground(new java.awt.Color(109, 93, 83));
        jPanel10.setPreferredSize(new java.awt.Dimension(2, 50));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 2, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout FechaHora2Layout = new javax.swing.GroupLayout(FechaHora2);
        FechaHora2.setLayout(FechaHora2Layout);
        FechaHora2Layout.setHorizontalGroup(
            FechaHora2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FechaHora2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addComponent(jLabel24)
                .addGap(0, 68, Short.MAX_VALUE))
        );
        FechaHora2Layout.setVerticalGroup(
            FechaHora2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FechaHora2Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(jLabel24)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FechaHora2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        Encabezado2.add(FechaHora2, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 0, 120, 70));

        jPanel6.setBackground(new java.awt.Color(36, 30, 26));

        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("Usuario");

        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setText("Administrador");

        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/imagen/icon-icons (1).png"))); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel18)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        Encabezado2.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 0, 150, 70));

        jScrollPane2.setBackground(new java.awt.Color(41, 34, 28));
        jScrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        jTable1.setBackground(new java.awt.Color(41, 34, 28));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nombre", "Stock", "Precio ", "Id", "Cant. Vendidos"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, false, false, true
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
        jScrollPane2.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Encabezado2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 828, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(Encabezado2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 613, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked

    }//GEN-LAST:event_jTable1MouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Encabezado2;
    private javax.swing.JPanel FechaHora2;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
