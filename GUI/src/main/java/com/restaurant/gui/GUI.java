package com.restaurant.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class GUI {
    private static final Color BACKGROUND = new Color(245, 244, 240);
    private static final Color CARD = Color.WHITE;
    private static final Color INK = new Color(26, 26, 26);
    private static final Color MUTED = new Color(111, 111, 111);
    private static final Color ACCENT = new Color(15, 110, 86);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::crearVentana);
    }

    private static void crearVentana() {
        aplicarLookAndFeel();

        JFrame frame = new JFrame("Restaurante Java - Mockup inicial");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 720));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        root.add(crearHeader(), BorderLayout.NORTH);
        root.add(crearContenido(), BorderLayout.CENTER);
        root.add(crearFooter(), BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JPanel crearHeader() {
        JPanel header = crearCard(new BorderLayout(16, 4));
        JLabel titulo = new JLabel("Sistema Restaurante");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(INK);

        JLabel subtitulo = new JLabel("Mockup inicial - login, mesas, menu, pedido y reportes");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(MUTED);

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        JButton estado = new JButton("DB: restaurante_db");
        estado.setEnabled(false);

        header.add(textos, BorderLayout.WEST);
        header.add(estado, BorderLayout.EAST);
        return header;
    }

    private static JSplitPane crearContenido() {
        JPanel sidebar = crearSidebar();
        JPanel tablero = crearTablero();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, tablero);
        splitPane.setResizeWeight(0.22);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        return splitPane;
    }

    private static JPanel crearSidebar() {
        JPanel sidebar = crearCard(new BorderLayout(12, 16));
        sidebar.setPreferredSize(new Dimension(245, 620));

        JPanel login = new JPanel(new GridLayout(0, 1, 0, 8));
        login.setOpaque(false);
        login.add(crearSectionTitle("Acceso"));
        login.add(new JTextField("admin"));
        login.add(new JPasswordField("1234"));

        JButton ingresar = new JButton("Ingresar");
        ingresar.setBackground(ACCENT);
        ingresar.setForeground(Color.WHITE);
        login.add(ingresar);

        JList<String> navegacion = new JList<>(new String[] {
                "Mesas",
                "Menu",
                "Pedido actual",
                "ABM productos",
                "Reportes"
        });
        navegacion.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navegacion.setSelectedIndex(0);
        navegacion.setFixedCellHeight(34);

        sidebar.add(login, BorderLayout.NORTH);
        sidebar.add(new JScrollPane(navegacion), BorderLayout.CENTER);
        sidebar.add(crearNota("Roles previstos: ADMIN, MOZO y COCINA."), BorderLayout.SOUTH);
        return sidebar;
    }

    private static JPanel crearTablero() {
        JPanel tablero = new JPanel(new GridLayout(2, 2, 16, 16));
        tablero.setOpaque(false);
        tablero.add(crearPanelMesas());
        tablero.add(crearPanelMenu());
        tablero.add(crearPanelPedido());
        tablero.add(crearPanelReportes());
        return tablero;
    }

    private static JPanel crearPanelMesas() {
        JPanel panel = crearCard(new BorderLayout(12, 12));
        panel.add(crearSectionTitle("Mesas"), BorderLayout.NORTH);

        JPanel grilla = new JPanel(new GridLayout(2, 4, 10, 10));
        grilla.setOpaque(false);
        String[] estados = { "LIBRE", "OCUPADA", "LIBRE", "RESERVADA", "LIBRE", "LIBRE", "FUERA", "LIBRE" };
        for (int index = 0; index < estados.length; index++) {
            JButton mesa = new JButton("<html><b>Mesa " + (index + 1) + "</b><br>" + estados[index] + "</html>");
            mesa.setFocusPainted(false);
            grilla.add(mesa);
        }

        panel.add(grilla, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel crearPanelMenu() {
        JPanel panel = crearCard(new BorderLayout(12, 12));
        panel.add(crearSectionTitle("Menu y productos"), BorderLayout.NORTH);

        String[] columnas = { "Producto", "Categoria", "Precio", "Stock" };
        Object[][] datos = {
                { "Clasica", "Hamburguesas", "$2500", 25 },
                { "Fetuccini Alfredo", "Pastas", "$2400", 20 },
                { "Volcan de chocolate", "Postres", "$1400", 15 },
                { "IPA artesanal", "Cervezas", "$1600", 25 }
        };

        panel.add(new JScrollPane(new JTable(datos, columnas)), BorderLayout.CENTER);
        return panel;
    }

    private static JPanel crearPanelPedido() {
        JPanel panel = crearCard(new BorderLayout(12, 12));
        panel.add(crearSectionTitle("Pedido actual"), BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[][] {
                        { "Clasica", 2, "$2500", "$5000" },
                        { "Coca-Cola 500ml", 3, "$700", "$2100" }
                },
                new String[] { "Item", "Cant.", "Unit.", "Subtotal" });

        JLabel total = new JLabel("Total estimado: $7100", SwingConstants.RIGHT);
        total.setFont(new Font("Segoe UI", Font.BOLD, 16));
        total.setForeground(ACCENT);

        panel.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        panel.add(total, BorderLayout.SOUTH);
        return panel;
    }

    private static JPanel crearPanelReportes() {
        JPanel panel = crearCard(new BorderLayout(12, 12));
        panel.add(crearSectionTitle("Reportes / Admin"), BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setText("""
                Proximos pasos:
                - ABM de productos
                - Ventas por producto
                - Ventas por mes
                - Exportacion PDF
                - JAR ejecutable
                """);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(area, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel crearFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel texto = new JLabel("Mockup sin conexion funcional todavia - listo para dividir en vistas Swing reales");
        texto.setForeground(MUTED);
        footer.add(texto, BorderLayout.WEST);
        return footer;
    }

    private static JLabel crearSectionTitle(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(INK);
        return label;
    }

    private static JPanel crearCard(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 223, 216)),
                new EmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    private static JLabel crearNota(String texto) {
        JLabel nota = new JLabel("<html>" + texto + "</html>");
        nota.setForeground(MUTED);
        nota.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return nota;
    }

    private static void aplicarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
        }
    }
}
