package com.restaurant.backend.service;

public final class ServicioFactory {

    private static volatile ProductoService productoService;
    private static volatile MesaService mesaService;
    private static volatile PedidoService pedidoService;
    private static volatile ReporteService reporteService;
    private static volatile UsuarioService usuarioService;

    private static final Object LOCK = new Object();

    private ServicioFactory() {
    }

    public static ProductoService getProductoService() {
        ProductoService result = productoService;
        if (result == null) {
            synchronized (LOCK) {
                result = productoService;
                if (result == null) {
                    productoService = result = new ProductoService();
                }
            }
        }
        return result;
    }

    public static MesaService getMesaService() {
        MesaService result = mesaService;
        if (result == null) {
            synchronized (LOCK) {
                result = mesaService;
                if (result == null) {
                    mesaService = result = new MesaService();
                }
            }
        }
        return result;
    }

    public static PedidoService getPedidoService() {
        PedidoService result = pedidoService;
        if (result == null) {
            synchronized (LOCK) {
                result = pedidoService;
                if (result == null) {
                    pedidoService = result = new PedidoService();
                }
            }
        }
        return result;
    }

    public static ReporteService getReporteService() {
        ReporteService result = reporteService;
        if (result == null) {
            synchronized (LOCK) {
                result = reporteService;
                if (result == null) {
                    reporteService = result = new ReporteService();
                }
            }
        }
        return result;
    }

    public static UsuarioService getUsuarioService() {
        UsuarioService result = usuarioService;
        if (result == null) {
            synchronized (LOCK) {
                result = usuarioService;
                if (result == null) {
                    usuarioService = result = new UsuarioService();
                }
            }
        }
        return result;
    }
}
