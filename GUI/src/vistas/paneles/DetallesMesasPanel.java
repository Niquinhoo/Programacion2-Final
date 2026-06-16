/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package vistas.paneles;

import java.awt.Color;
import java.time.LocalDateTime;
import javax.swing.SwingUtilities;
import vistas.ReservaDialog;
import vistas.util.AsyncDataLoader;

/**
 *
 * @author enzol
 */
public class DetallesMesasPanel extends javax.swing.JPanel {

    /**
     * Creates new form DetallesMesasPanel
     */
    public DetallesMesasPanel() {
        initComponents();
        
        configurarTabla();
        
        btnOcupar.addActionListener(e -> btnOcuparActionPerformed());
        btnLiberar.addActionListener(e -> btnLiberarActionPerformed());
        btnCancelarReserva.addActionListener(e -> btnCancelarReservaActionPerformed());
    }
    
        
    
    
    private com.restaurant.backend.model.Mesa mesaSeleccionada;

    public void mostrarMesa(String nombreMesa) {
        try {
            int numero = Integer.parseInt(nombreMesa.replace("Mesa", "").trim());
            AsyncDataLoader.load(
                    this,
                    () -> com.restaurant.backend.service.ServicioFactory.getMesaService().obtenerPorNumero(numero),
                    m -> {
                        if (m != null) {
                            mostrarMesa(m);
                        } else {
                            EstadoVar.setText("Mesa " + numero + " - No encontrada");
                        }
                    },
                    error -> EstadoVar.setText("Error al cargar mesa " + numero)
            );
        } catch (NumberFormatException e) {
            EstadoVar.setText(nombreMesa);
        }
    }

    private void mostrarMesa(com.restaurant.backend.model.Mesa m) {
        this.mesaSeleccionada = m;
        EstadoVar.setText("Mesa " + m.getNumero() + " (" + m.getEstado() + ")");

        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{"Pedido ID", "Mozo", "Fecha/Hora", "Estado", "Total"});

        AsyncDataLoader.load(
                this,
                () -> com.restaurant.backend.service.ServicioFactory.getPedidoService().listarTodos(),
                pedidos -> {
                    for (com.restaurant.backend.model.Pedido p : pedidos) {
                        if (p.getMesa() != null && p.getMesa().getIdMesa().equals(m.getIdMesa())) {
                            if (p.getEstado() == com.restaurant.backend.model.EstadoPedido.ABIERTO ||
                                p.getEstado() == com.restaurant.backend.model.EstadoPedido.EN_COCINA ||
                                p.getEstado() == com.restaurant.backend.model.EstadoPedido.LISTO) {

                                String mozo = p.getUsuario() != null
                                        ? (p.getUsuario().getNombre() + " " + p.getUsuario().getApellido())
                                        : "N/A";
                                String fecha = p.getCreatedAt() != null
                                        ? p.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                        : "N/A";

                                model.addRow(new Object[]{
                                    "#" + p.getIdPedido(),
                                    mozo,
                                    fecha,
                                    p.getEstado().toString(),
                                    "$" + p.getTotal()
                                });
                            }
                        }
                    }
                },
                error -> System.err.println("Error al cargar pedidos activos: " + error.getMessage())
        );

        btnOcupar.setVisible(m.getEstado() == com.restaurant.backend.model.EstadoMesa.LIBRE || m.getEstado() == com.restaurant.backend.model.EstadoMesa.RESERVADA);
        btnLiberar.setVisible(m.getEstado() == com.restaurant.backend.model.EstadoMesa.OCUPADA || m.getEstado() == com.restaurant.backend.model.EstadoMesa.FUERA_DE_SERVICIO);
        btnReservar.setVisible(m.getEstado() == com.restaurant.backend.model.EstadoMesa.LIBRE);
        btnCancelarReserva.setVisible(m.getEstado() == com.restaurant.backend.model.EstadoMesa.RESERVADA);
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
    

    
    jTable1.setForeground(Color.WHITE);
    jTable1.setBackground(new Color(36, 30, 26));

    ScrollReservas.setBackground(
            new Color(36, 30, 26));

    ScrollReservas.getViewport().setBackground(
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

        DetallesMesa = new javax.swing.JPanel();
        InfoMesa = new javax.swing.JPanel();
        InfoTxt = new javax.swing.JLabel();
        EstadoTxt = new javax.swing.JLabel();
        EstadoVar = new javax.swing.JLabel();
        ScrollReservas = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        AccionesMesa = new javax.swing.JPanel();
        AccionesTxt = new javax.swing.JLabel();
        btnOcupar = new javax.swing.JButton();
        btnLiberar = new javax.swing.JButton();
        btnReservar = new javax.swing.JButton();
        btnCancelarReserva = new javax.swing.JButton();

        DetallesMesa.setBackground(new java.awt.Color(53, 44, 38));

        InfoMesa.setBackground(new java.awt.Color(53, 44, 38));
        InfoMesa.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        InfoTxt.setBackground(new java.awt.Color(255, 255, 255));
        InfoTxt.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        InfoTxt.setForeground(new java.awt.Color(255, 255, 255));
        InfoTxt.setText("Información");

        EstadoTxt.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        EstadoTxt.setForeground(new java.awt.Color(255, 255, 255));
        EstadoTxt.setText("Estado:");

        EstadoVar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        EstadoVar.setForeground(new java.awt.Color(255, 255, 255));
        EstadoVar.setText("EstadoMesa");

        ScrollReservas.setBackground(new java.awt.Color(53, 44, 38));
        ScrollReservas.setBorder(null);
        ScrollReservas.setForeground(new java.awt.Color(53, 44, 38));

        jTable1.setBackground(new java.awt.Color(53, 44, 38));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Cliente ", "Fecha", "Hora", "Seña", "Pers."
            }
        ));
        ScrollReservas.setViewportView(jTable1);

        javax.swing.GroupLayout InfoMesaLayout = new javax.swing.GroupLayout(InfoMesa);
        InfoMesa.setLayout(InfoMesaLayout);
        InfoMesaLayout.setHorizontalGroup(
            InfoMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(InfoMesaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(InfoMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ScrollReservas)
                    .addGroup(InfoMesaLayout.createSequentialGroup()
                        .addGroup(InfoMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(InfoMesaLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(EstadoTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(EstadoVar))
                            .addComponent(InfoTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        InfoMesaLayout.setVerticalGroup(
            InfoMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(InfoMesaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(InfoTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(InfoMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(EstadoTxt)
                    .addComponent(EstadoVar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ScrollReservas, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addContainerGap())
        );

        AccionesMesa.setBackground(new java.awt.Color(53, 44, 38));

        AccionesTxt.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        AccionesTxt.setForeground(new java.awt.Color(255, 255, 255));
        AccionesTxt.setText("Acciones");

        btnOcupar.setBackground(new java.awt.Color(255, 0, 0));
        btnOcupar.setText("Ocupar");

        btnLiberar.setBackground(new java.awt.Color(51, 204, 0));
        btnLiberar.setText("Liberar");

        btnReservar.setBackground(new java.awt.Color(255, 255, 0));
        btnReservar.setText("Reservar");
        btnReservar.addActionListener(this::btnReservarActionPerformed);

        btnCancelarReserva.setBackground(new java.awt.Color(255, 0, 0));
        btnCancelarReserva.setText("Cancelar Reserva");

        javax.swing.GroupLayout AccionesMesaLayout = new javax.swing.GroupLayout(AccionesMesa);
        AccionesMesa.setLayout(AccionesMesaLayout);
        AccionesMesaLayout.setHorizontalGroup(
            AccionesMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AccionesMesaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AccionesMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(AccionesTxt)
                    .addGroup(AccionesMesaLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(AccionesMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnOcupar)
                            .addComponent(btnLiberar)
                            .addComponent(btnReservar)
                            .addComponent(btnCancelarReserva))))
                .addContainerGap(210, Short.MAX_VALUE))
        );
        AccionesMesaLayout.setVerticalGroup(
            AccionesMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AccionesMesaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(AccionesTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLiberar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnOcupar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnReservar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelarReserva)
                .addGap(22, 22, 22))
        );

        javax.swing.GroupLayout DetallesMesaLayout = new javax.swing.GroupLayout(DetallesMesa);
        DetallesMesa.setLayout(DetallesMesaLayout);
        DetallesMesaLayout.setHorizontalGroup(
            DetallesMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DetallesMesaLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(InfoMesa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AccionesMesa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        DetallesMesaLayout.setVerticalGroup(
            DetallesMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DetallesMesaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DetallesMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(AccionesMesa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(InfoMesa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(DetallesMesa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(DetallesMesa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnReservarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReservarActionPerformed
         ReservaDialog dialog = new ReservaDialog(
            (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
            true);

    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);

    if (dialog.isConfirmado()) {

        String nombre = dialog.getNombre();
        String telefono = dialog.getTelefono();
        LocalDateTime fechaHora = dialog.getFechaHora();

        System.out.println("Nombre: " + nombre);
        System.out.println("Teléfono: " + telefono);
        System.out.println("Fecha: " + fechaHora);

        
    }
    }//GEN-LAST:event_btnReservarActionPerformed

    private void btnOcuparActionPerformed() {
        if (mesaSeleccionada != null) {
            AsyncDataLoader.execute(
                    this,
                    () -> com.restaurant.backend.service.ServicioFactory.getMesaService().ocupar(mesaSeleccionada.getIdMesa()),
                    res -> {
                        javax.swing.JOptionPane.showMessageDialog(this, res, "Ocupar Mesa", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        refrescarMesa();
                    }
            );
        }
    }

    private void btnLiberarActionPerformed() {
        if (mesaSeleccionada != null) {
            AsyncDataLoader.execute(
                    this,
                    () -> com.restaurant.backend.service.ServicioFactory.getMesaService().liberar(mesaSeleccionada.getIdMesa()),
                    res -> {
                        javax.swing.JOptionPane.showMessageDialog(this, res, "Liberar Mesa", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        refrescarMesa();
                    }
            );
        }
    }

    private void btnCancelarReservaActionPerformed() {
        if (mesaSeleccionada != null) {
            AsyncDataLoader.execute(
                    this,
                    () -> com.restaurant.backend.service.ServicioFactory.getMesaService().cancelarReserva(mesaSeleccionada.getIdMesa()),
                    res -> {
                        javax.swing.JOptionPane.showMessageDialog(this, res, "Cancelar Reserva", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        refrescarMesa();
                    }
            );
        }
    }

    private void refrescarMesa() {
        if (mesaSeleccionada != null) {
            AsyncDataLoader.load(
                    this,
                    () -> com.restaurant.backend.service.ServicioFactory.getMesaService().obtenerPorId(mesaSeleccionada.getIdMesa()),
                    m -> {
                        if (m != null) {
                            mostrarMesa(m);
                        }
                        java.awt.Container parent = getParent();
                        while (parent != null && !(parent instanceof MesasPanel)) {
                            parent = parent.getParent();
                        }
                        if (parent instanceof MesasPanel) {
                            ((MesasPanel) parent).actualizarMesas();
                        }
                    },
                    error -> System.err.println("Error al refrescar mesa: " + error.getMessage())
            );
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AccionesMesa;
    private javax.swing.JLabel AccionesTxt;
    private javax.swing.JPanel DetallesMesa;
    private javax.swing.JLabel EstadoTxt;
    private javax.swing.JLabel EstadoVar;
    private javax.swing.JPanel InfoMesa;
    private javax.swing.JLabel InfoTxt;
    private javax.swing.JScrollPane ScrollReservas;
    private javax.swing.JButton btnCancelarReserva;
    private javax.swing.JButton btnLiberar;
    private javax.swing.JButton btnOcupar;
    private javax.swing.JButton btnReservar;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
