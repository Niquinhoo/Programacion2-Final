package com.restaurant.backend.service;

public final class ServicioFactory {

    private static ProductoService productoService;
    private static MesaService mesaService;
    private static PedidoService pedidoService;
    private static ReporteService reporteService;
    private static UsuarioService usuarioService;

    private ServicioFactory() {
    }

    public static ProductoService getProductoService() {
        if (productoService == null) {
            productoService = new ProductoService();
        }
        return productoService;
    }

    public static MesaService getMesaService() {
        if (mesaService == null) {
            mesaService = new MesaService();
        }
        return mesaService;
    }

    public static PedidoService getPedidoService() {
        if (pedidoService == null) {
            pedidoService = new PedidoService();
        }
        return pedidoService;
    }

    public static ReporteService getReporteService() {
        if (reporteService == null) {
            reporteService = new ReporteService();
        }
        return reporteService;
    }

    public static UsuarioService getUsuarioService() {
        if (usuarioService == null) {
            usuarioService = new UsuarioService();
        }
        return usuarioService;
    }
}
