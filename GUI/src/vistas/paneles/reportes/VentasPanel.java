package vistas.paneles.reportes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.restaurant.backend.service.ServicioFactory;
import com.restaurant.backend.service.dto.ResumenGeneralDTO;
import com.restaurant.backend.service.dto.VentaPorMesDTO;
import com.restaurant.backend.service.dto.VentaPorProductoDTO;
import vistas.util.AsyncDataLoader;

public class VentasPanel extends javax.swing.JPanel {

    public VentasPanel() {
        initComponents();
        cargarDatosReporte();
    }

    private void cargarDatosReporte() {
        AsyncDataLoader.load(
                this,
                () -> {
                    ResumenGeneralDTO resumen = ServicioFactory.getReporteService().resumenGeneral();
                    List<VentaPorMesDTO> ventasMensuales = ServicioFactory.getReporteService().ventasPorMes();
                    List<VentaPorProductoDTO> ventasProductos = ServicioFactory.getReporteService().ventasPorProducto();

                    Object[] result = new Object[3];
                    result[0] = resumen;
                    result[1] = ventasMensuales;
                    result[2] = ventasProductos;
                    return result;
                },
                data -> {
                    ResumenGeneralDTO resumen = (ResumenGeneralDTO) data[0];
                    @SuppressWarnings("unchecked")
                    List<VentaPorMesDTO> ventasMensuales = (List<VentaPorMesDTO>) data[1];
                    @SuppressWarnings("unchecked")
                    List<VentaPorProductoDTO> ventasProductos = (List<VentaPorProductoDTO>) data[2];

                    cargarResumen(resumen, ventasMensuales);
                    cargarGraficoVentas(ventasMensuales);
                    cargarGraficoTorta(ventasProductos);
                },
                error -> {
                    System.err.println("Error al cargar reporte de ventas: " + error.getMessage());
                    mostrarDatosVacios();
                }
        );
    }

    private void cargarResumen(ResumenGeneralDTO resumen, List<VentaPorMesDTO> ventasMensuales) {
        VentasTotal.setText("$" + String.format("%.0f",
                resumen.getTotalRecaudado() != null ? resumen.getTotalRecaudado().doubleValue() : 0));

        double total = resumen.getTotalRecaudado() != null ? resumen.getTotalRecaudado().doubleValue() : 0;
        VentasTotal1.setText("$" + String.format("%.0f", total / 4));

        double mensualPromedio = !ventasMensuales.isEmpty()
                ? ventasMensuales.stream()
                    .mapToDouble(v -> v.getTotalMes() != null ? v.getTotalMes().doubleValue() : 0)
                    .average().orElse(0)
                : 0;
        VentasTotal2.setText("$" + String.format("%.0f", mensualPromedio));
    }

    private void cargarGraficoVentas(List<VentaPorMesDTO> ventasMensuales) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        for (VentaPorMesDTO v : ventasMensuales) {
            int mesIndex = v.getMes() - 1;
            String nombreMes = (mesIndex >= 0 && mesIndex < 12) ? meses[mesIndex] : "M" + v.getMes();
            dataset.addValue(v.getTotalMes() != null ? v.getTotalMes().doubleValue() : 0, "Ventas", nombreMes);
        }

        if (dataset.getColumnCount() == 0) {
            dataset.addValue(0, "Ventas", "Sin datos");
        }

        JFreeChart chart = ChartFactory.createLineChart(null, null, null, dataset);
        chart.setBackgroundPaint(new Color(36, 30, 26));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(36, 30, 26));
        plot.setOutlinePaint(null);
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);
        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Color.WHITE);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(181, 137, 90));
        renderer.setSeriesShapesVisible(0, true);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(350, 276));
        chartPanel.setMouseWheelEnabled(false);

        pnlGraficoLineas.removeAll();
        pnlGraficoLineas.setLayout(new BorderLayout());
        pnlGraficoLineas.add(chartPanel, BorderLayout.CENTER);
        pnlGraficoLineas.revalidate();
        pnlGraficoLineas.repaint();
    }

    private void cargarGraficoTorta(List<VentaPorProductoDTO> ventasProductos) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        int count = 0;
        Color[] colores = {
            new Color(255, 0, 0), new Color(0, 51, 255), new Color(0, 255, 204),
            new Color(102, 0, 204), new Color(51, 255, 51), new Color(255, 153, 51),
            new Color(255, 255, 0), new Color(255, 0, 255)
        };

        for (VentaPorProductoDTO v : ventasProductos) {
            if (count >= 8) break;
            double recaudado = v.getTotalRecaudado() != null ? v.getTotalRecaudado().doubleValue() : 0;
            String label = v.getProducto() != null
                    ? v.getProducto() + " ($" + String.format("%.0f", recaudado) + ")"
                    : "N/A";
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
        VentasTotal1.setText("$0");
        VentasTotal2.setText("$0");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        VentasTotal2 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        VentasTotal1 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        VentasTotal = new javax.swing.JLabel();
        pnlGraficoLineas = new javax.swing.JPanel();
        pnlGraficoTorta = new javax.swing.JPanel();

        jPanel1.setBackground(new java.awt.Color(56, 46, 40));

        jPanel3.setBackground(new java.awt.Color(36, 30, 26));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Ventas Mensuales");

        VentasTotal2.setFont(new java.awt.Font("Segoe UI", 1, 36));
        VentasTotal2.setForeground(new java.awt.Color(255, 255, 255));
        VentasTotal2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        VentasTotal2.setText("Cargando...");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(VentasTotal2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 112, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(VentasTotal2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBackground(new java.awt.Color(36, 30, 26));
        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Ventas Semanales");

        VentasTotal1.setFont(new java.awt.Font("Segoe UI", 1, 36));
        VentasTotal1.setForeground(new java.awt.Color(255, 255, 255));
        VentasTotal1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        VentasTotal1.setText("Cargando...");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(VentasTotal1, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(VentasTotal1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel9.setBackground(new java.awt.Color(36, 30, 26));
        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Ventas Hoy");

        VentasTotal.setFont(new java.awt.Font("Segoe UI", 1, 36));
        VentasTotal.setForeground(new java.awt.Color(255, 255, 255));
        VentasTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        VentasTotal.setText("Cargando...");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(VentasTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(VentasTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );

        pnlGraficoLineas.setBackground(new java.awt.Color(36, 30, 26));
        pnlGraficoLineas.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 93, 83)));

        javax.swing.GroupLayout pnlGraficoLineasLayout = new javax.swing.GroupLayout(pnlGraficoLineas);
        pnlGraficoLineas.setLayout(pnlGraficoLineasLayout);
        pnlGraficoLineasLayout.setHorizontalGroup(
            pnlGraficoLineasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 348, Short.MAX_VALUE)
        );
        pnlGraficoLineasLayout.setVerticalGroup(
            pnlGraficoLineasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
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
            .addGap(0, 274, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(pnlGraficoLineas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlGraficoTorta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(33, 33, 33))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlGraficoTorta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlGraficoLineas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(32, 32, 32))
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel VentasTotal;
    private javax.swing.JLabel VentasTotal1;
    private javax.swing.JLabel VentasTotal2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel pnlGraficoLineas;
    private javax.swing.JPanel pnlGraficoTorta;
    // End of variables declaration//GEN-END:variables
}
