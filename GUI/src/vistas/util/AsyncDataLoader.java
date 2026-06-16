package vistas.util;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Utility class for executing database operations off the Event Dispatch Thread (EDT)
 * using {@link SwingWorker}. Automatically shows a wait cursor while loading.
 *
 * <h3>Migration from synchronous to asynchronous:</h3>
 * <pre>{@code
 * // BEFORE (blocks EDT, UI freezes):
 * List&lt;Producto&gt; productos = ServicioFactory.getProductoService().obtenerTodos();
 * actualizarUI(productos);
 *
 * // AFTER (non-blocking, cursor changes, UI stays responsive):
 * AsyncDataLoader.load(
 *     parentComponent,
 *     () -&gt; ServicioFactory.getProductoService().obtenerTodos(),
 *     productos -&gt; actualizarUI(productos)
 * );
 * }</pre>
 */
public final class AsyncDataLoader {

    private AsyncDataLoader() {
    }

    /**
     * Executes a database fetch in background. While loading, the parent window
     * shows a WAIT_CURSOR and the parent component is disabled.
     * On completion, passes the result to the callback on the EDT.
     *
     * @param parent            component to find the window and disable during loading
     * @param backgroundTask    the database operation to run off the EDT
     * @param onSuccess         callback on the EDT with the result
     * @param <T>               type of data returned by the background task
     */
    public static <T> void load(Component parent,
                                 java.util.concurrent.Callable<T> backgroundTask,
                                 java.util.function.Consumer<T> onSuccess) {
        load(parent, backgroundTask, onSuccess, error -> {
            System.err.println("AsyncDataLoader error: " + error.getMessage());
            javax.swing.JOptionPane.showMessageDialog(parent,
                    "Error al cargar datos: " + error.getMessage(),
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Same as {@link #load(Component, java.util.concurrent.Callable, java.util.function.Consumer)}
     * but with a custom error handler.
     */
    public static <T> void load(Component parent,
                                 java.util.concurrent.Callable<T> backgroundTask,
                                 java.util.function.Consumer<T> onSuccess,
                                 java.util.function.Consumer<Exception> onError) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        Cursor originalCursor = window != null ? window.getCursor() : null;
        if (window != null) {
            window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        parent.setEnabled(false);

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return backgroundTask.call();
            }

            @Override
            protected void done() {
                parent.setEnabled(true);
                if (window != null) {
                    window.setCursor(originalCursor != null ? originalCursor
                            : Cursor.getDefaultCursor());
                }

                try {
                    T result = get();
                    onSuccess.accept(result);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (cause instanceof Exception) {
                        onError.accept((Exception) cause);
                    } else {
                        onError.accept(new Exception(cause));
                    }
                }
            }
        }.execute();
    }

    /**
     * Executes a database write/update in background. Shows WAIT_CURSOR,
     * disables parent, and calls onComplete with the result string on EDT.
     */
    public static void execute(Component parent,
                                java.util.concurrent.Callable<String> backgroundTask,
                                java.util.function.Consumer<String> onComplete) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        Cursor originalCursor = window != null ? window.getCursor() : null;
        if (window != null) {
            window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        parent.setEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return backgroundTask.call();
            }

            @Override
            protected void done() {
                parent.setEnabled(true);
                if (window != null) {
                    window.setCursor(originalCursor != null ? originalCursor
                            : Cursor.getDefaultCursor());
                }

                try {
                    String result = get();
                    onComplete.accept(result);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    System.err.println("AsyncDataLoader execute error: " + cause.getMessage());
                    onComplete.accept("Error: " + cause.getMessage());
                }
            }
        }.execute();
    }
}
