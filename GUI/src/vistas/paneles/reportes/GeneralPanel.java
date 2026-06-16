package vistas.paneles.reportes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.RingPlot;

import com.restaurant.backend.service.ServicioFactory;
import com.restaurant.backend.service.dto.ResumenGeneralDTO;
import com.restaurant.backend.service.dto.VentaPorProductoDTO;
import vistas.util.AsyncDataLoader;

public class GeneralPanel extends javax.swing.JPanel {

    public GeneralPanel() {
        initComponents();
        cargarDatosReporte();
    }

    private void cargarDatosReporte() {
        AsyncDataLoader.load(
                this,
                () -> {
                    ResumenGeneralDTO resumen = ServicioFactory.getReporteService().resumenGeneral();
                    List<VentaPorProductoDTO> ventas = ServicioFactory.getReporteService().ventasPorProducto();

                    Object[] result = new Object[2];
                    result[0] = resumen;
                    result[1] = ventas;
                    return result;
                },
                data -> {
                    ResumenGeneralDTO resumen = (ResumenGeneralDTO) data[0];
                    @SuppressWarnings("unchecked")
                    List<VentaPorProductoDTO> ventas = (List<VentaPorProductoDTO>) data[1];

                    cargarResumen(resumen);
                    cargarGraficoBarras(ventas);
                    cargarGraficoTorta(ventas);
                },
                error -> {
                    System.err.println("Error al cargar reporte general: " + error.getMessage());
                    mostrarDatosVacios();
                }
        );
    }

    private void cargarResumen(ResumenGeneralDTO resumen) {
        VentasTotal.setText("$" + String.format("%.0f",
                resumen.getTotalRecaudado() != null ? resumen.getTotalRecaudado().doubleValue() : 0));
        pedidos.setText(String.valueOf(resumen.getPedidosCerrados()));
        long pedidosCerrados = resumen.getPedidosCerrados();
        BigDecimal totalRecaudado = resumen.getTotalRecaudado() != null ? resumen.getTotalRecaudado() : BigDecimal.ZERO;
        double promedio = pedidosCerrados > 0
                ? totalRecaudado.doubleValue() / pedidosCerrados
                : 0;
        pomedio.setText("$" + String.format("%.2f", promedio));
    }

    private void cargarGraficoBarras(List<VentaPorProductoDTO> ventas) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int count = 0;
        for (VentaPorProductoDTO v : ventas) {
            if (count >= 7) break;
            String label = v.getProducto() != null && v.getProducto().length() > 6
                    ? v.getProducto().substring(0, 6) + "."
                    : v.getProducto();
            dataset.addValue(v.getUnidadesVendidas(), "Ventas", label);
            count++;
        }

        if (dataset.getColumnCount() == 0) {
            dataset.addValue(0, "Ventas", "Sin datos");
        }

        JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset);

        CategoryPlot plot = chart.getCategoryPlot();
        chart.setBackgroundPaint(new Color(36, 30, 26));
        plot.setBackgroundPaint(new Color(36, 30, 26));
        plot.setOutlinePaint(null);
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);
        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(181, 137, 90));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(350, 276));
        chartPanel.setMouseWheelEnabled(false);

        pnlGraficoBarrras.removeAll();
        pnlGraficoBarrras.setLayout(new BorderLayout());
        pnlGraficoBarrras.add(chartPanel, BorderLayout.CENTER);
        pnlGraficoBarrras.revalidate();
        pnlGraficoBarrras.repaint();
    }

    private void cargarGraficoTorta(List<VentaPorProductoDTO> ventas) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        int count = 0;
        Color[] colores = {
            new Color(255, 0, 0), new Color(0, 51, 255), new Color(0, 255, 204),
            new Color(102, 0, 204), new Color(51, 255, 51), new Color(255, 153, 51),
            new Color(255, 255, 0), new Color(255, 0, 255)
        };

        for (VentaPorProductoDTO v : ventas) {
            if (count >= 8) break;
            String label = v.getProducto() != null ? v.getProducto() : "N/A";
            dataset.setValue(label, v.getUnidadesVendidas());
            count++;
        }

        if (dataset.getItemCount() == 0) {
            dataset.setValue("Sin datos", 1);
        }

        JFreeChart chart = ChartFactory.createRingChart(null, dataset, true, true, false);

        RingPlot plot = (RingPlot) chart.getPlot();
        chart.setBackgroundPaint(new Color(36, 30, 26));
        plot.setBackgroundPaint(new Color(36, 30, 26));
        plot.setOutlinePaint(null);
        plot.setShadowPaint(null);
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setSectionDepth(0.35);

        int i = 0;
        for (Object key : dataset.getKeys()) {
            if (i < colores.length) {
                plot.setSectionPaint((Comparable<?>) key, colores[i]);
            }
            i++;
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(345, 276));
        chartPanel.setMouseWheelEnabled(false);

        pnlGraficoTorta.removeAll();
        pnlGraficoTorta.setLayout(new BorderLayout());
        pnlGraficoTorta.add(chartPanel, BorderLayout.CENTER);
        pnlGraficoTorta.revalidate();
        pnlGraficoTorta.repaint();
    }

    private void mostrarDatosVacios() {
        VentasTotal.setText("$0");
        pedidos.setText("0");
        pomedio.setText("$0.00");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlGeneral = new javax.swing.JPanel();
        pnlTicketPromedio = new javax.swing.JPanel();
        TicketPromTxt = new javax.swing.JLabel();
        pomedio = new javax.swing.JLabel();
        pnlPedidosTotales = new javax.swing.JPanel();
        PedidosTotalTxt = new javax.swing.JLabel();
        pedidos = new javax.swing.JLabel();
        pnlVentasTotales = new javax.swing.JPanel();
        VentasTotalTxt = new javax.swing.JLabel();
        VentasTotal = new javax.swing.JLabel();
        pnlGraficoBarrras = new javax.swing.JPanel();
        pnlGraficoTorta = new javax.swing.JPanel();

        pnlGeneral.setBackground(new java.awt.Color(56, 46, 40));

        pnlTicketPromedio.setBackground(new java.awt.Color(36, 30, 26));
        pnlTicketPromedio.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        TicketPromTxt.setFont(new java.awt.Font("Segoe UI", 1, 14));
        TicketPromTxt.setForeground(new java.awt.Color(255, 255, 255));
        TicketPromTxt.setText("Ticket Promedio");

        pomedio.setFont(new java.awt.Font("Segoe UI", 1, 36));
        pomedio.setForeground(new java.awt.Color(255, 255, 255));
        pomedio.setText("Cargando...");

        javax.swing.GroupLayout pnlTicketPromedioLayout = new javax.swing.GroupLayout(pnlTicketPromedio);
        pnlTicketPromedio.setLayout(pnlTicketPromedioLayout);
        pnlTicketPromedioLayout.setHorizontalGroup(
            pnlTicketPromedioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTicketPromedioLayout.createSequentialGroup()
                .addGroup(pnlTicketPromedioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlTicketPromedioLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(TicketPromTxt))
                    .addGroup(pnlTicketPromedioLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(pomedio, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlTicketPromedioLayout.setVerticalGroup(
            pnlTicketPromedioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTicketPromedioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(TicketPromTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pomedio, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        pnlPedidosTotales.setBackground(new java.awt.Color(36, 30, 26));
        pnlPedidosTotales.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        PedidosTotalTxt.setFont(new java.awt.Font("Segoe UI", 1, 14));
        PedidosTotalTxt.setForeground(new java.awt.Color(255, 255, 255));
        PedidosTotalTxt.setText("Pedidos Totales");

        pedidos.setFont(new java.awt.Font("Segoe UI", 1, 36));
        pedidos.setForeground(new java.awt.Color(255, 255, 255));
        pedidos.setText("Cargando...");

        javax.swing.GroupLayout pnlPedidosTotalesLayout = new javax.swing.GroupLayout(pnlPedidosTotales);
        pnlPedidosTotales.setLayout(pnlPedidosTotalesLayout);
        pnlPedidosTotalesLayout.setHorizontalGroup(
            pnlPedidosTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPedidosTotalesLayout.createSequentialGroup()
                .addGroup(pnlPedidosTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlPedidosTotalesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(PedidosTotalTxt))
                    .addGroup(pnlPedidosTotalesLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(pedidos, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(63, Short.MAX_VALUE))
        );
        pnlPedidosTotalesLayout.setVerticalGroup(
            pnlPedidosTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPedidosTotalesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PedidosTotalTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pedidos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlVentasTotales.setBackground(new java.awt.Color(36, 30, 26));
        pnlVentasTotales.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        VentasTotalTxt.setFont(new java.awt.Font("Segoe UI", 1, 14));
        VentasTotalTxt.setForeground(new java.awt.Color(255, 255, 255));
        VentasTotalTxt.setText("Ventas Totales");

        VentasTotal.setFont(new java.awt.Font("Segoe UI", 1, 36));
        VentasTotal.setForeground(new java.awt.Color(255, 255, 255));
        VentasTotal.setText("Cargando...");

        javax.swing.GroupLayout pnlVentasTotalesLayout = new javax.swing.GroupLayout(pnlVentasTotales);
        pnlVentasTotales.setLayout(pnlVentasTotalesLayout);
        pnlVentasTotalesLayout.setHorizontalGroup(
            pnlVentasTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVentasTotalesLayout.createSequentialGroup()
                .addGroup(pnlVentasTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlVentasTotalesLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(VentasTotalTxt))
                    .addGroup(pnlVentasTotalesLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(VentasTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        pnlVentasTotalesLayout.setVerticalGroup(
            pnlVentasTotalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVentasTotalesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(VentasTotalTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(VentasTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlGraficoBarrras.setBackground(new java.awt.Color(36, 30, 26));
        pnlGraficoBarrras.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        javax.swing.GroupLayout pnlGraficoBarrrasLayout = new javax.swing.GroupLayout(pnlGraficoBarrras);
        pnlGraficoBarrras.setLayout(pnlGraficoBarrrasLayout);
        pnlGraficoBarrrasLayout.setHorizontalGroup(
            pnlGraficoBarrrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 348, Short.MAX_VALUE)
        );
        pnlGraficoBarrrasLayout.setVerticalGroup(
            pnlGraficoBarrrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        pnlGraficoTorta.setBackground(new java.awt.Color(36, 30, 26));
        pnlGraficoTorta.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        javax.swing.GroupLayout pnlGraficoTortaLayout = new javax.swing.GroupLayout(pnlGraficoTorta);
        pnlGraficoTorta.setLayout(pnlGraficoTortaLayout);
        pnlGraficoTortaLayout.setHorizontalGroup(
            pnlGraficoTortaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlGraficoTortaLayout.setVerticalGroup(
            pnlGraficoTortaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlGeneralLayout = new javax.swing.GroupLayout(pnlGeneral);
        pnlGeneral.setLayout(pnlGeneralLayout);
        pnlGeneralLayout.setHorizontalGroup(
            pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGeneralLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGeneralLayout.createSequentialGroup()
                        .addComponent(pnlGraficoBarrras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlGraficoTorta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlGeneralLayout.createSequentialGroup()
                        .addComponent(pnlVentasTotales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlPedidosTotales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlTicketPromedio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(33, 33, 33))
        );
        pnlGeneralLayout.setVerticalGroup(
            pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlGeneralLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlVentasTotales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlPedidosTotales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTicketPromedio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(12, 12, 12)
                .addGroup(pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlGraficoBarrras, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlGraficoTorta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(32, 32, 32))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlGeneral, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlGeneral, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel PedidosTotalTxt;
    private javax.swing.JLabel TicketPromTxt;
    private javax.swing.JLabel VentasTotal;
    private javax.swing.JLabel VentasTotalTxt;
    private javax.swing.JLabel pedidos;
    private javax.swing.JPanel pnlGeneral;
    private javax.swing.JPanel pnlGraficoBarrras;
    private javax.swing.JPanel pnlGraficoTorta;
    private javax.swing.JPanel pnlPedidosTotales;
    private javax.swing.JPanel pnlTicketPromedio;
    private javax.swing.JPanel pnlVentasTotales;
    private javax.swing.JLabel pomedio;
    // End of variables declaration//GEN-END:variables
}
