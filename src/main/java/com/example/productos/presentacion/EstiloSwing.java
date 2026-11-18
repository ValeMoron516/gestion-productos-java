package com.example.productos.presentacion;

import java.awt.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/** Estilo compartido, manejo de errores y tareas de acceso a datos fuera del hilo gráfico. */
public final class EstiloSwing {
    public static final Color FONDO = new Color(244, 247, 251);
    public static final Color OSCURO = new Color(26, 41, 62);
    public static final Color VERDE = new Color(14, 139, 128);
    public static final Color TEXTO = new Color(38, 52, 71);
    public static final Color SUAVE = new Color(101, 118, 140);
    public static final Color ROJO = new Color(179, 48, 58);
    private static final Font TITULO = new Font("Dialog", Font.BOLD, 23);
    private static final Font CUERPO = new Font("Dialog", Font.PLAIN, 13);

    private EstiloSwing() { }

    public static void instalar() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { /* El Look and Feel estándar siempre está disponible. */ }
        UIManager.put("OptionPane.messageFont", CUERPO);
    }

    public static JPanel panel() {
        JPanel panel = new JPanel();
        panel.setBackground(FONDO);
        return panel;
    }

    public static JPanel cabecera(String titulo, String detalle) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(OSCURO);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        JLabel a = new JLabel(titulo);
        a.setForeground(Color.WHITE);
        a.setFont(TITULO);
        JLabel b = new JLabel(detalle);
        b.setForeground(new Color(201, 220, 234));
        b.setFont(CUERPO);
        panel.add(a, BorderLayout.NORTH);
        panel.add(b, BorderLayout.SOUTH);
        return panel;
    }

    public static JButton boton(String nombre) { return boton(nombre, VERDE); }

    public static JButton boton(String nombre, Color color) {
        JButton b = new JButton(nombre);
        b.setFont(new Font("Dialog", Font.BOLD, 12));
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color), new EmptyBorder(9, 14, 9, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JTextField campo(int columnas) {
        JTextField t = new JTextField(columnas);
        t.setFont(CUERPO);
        return t;
    }

    public static JLabel etiqueta(String nombre) {
        JLabel l = new JLabel(nombre);
        l.setFont(new Font("Dialog", Font.BOLD, 12));
        l.setForeground(TEXTO);
        return l;
    }

    public static void fila(JPanel panel, GridBagConstraints g, int y, String titulo, JComponent control) {
        g.gridy = y; g.gridx = 0; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.LINE_END;
        panel.add(etiqueta(titulo), g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.LINE_START;
        panel.add(control, g);
    }

    public static JPanel formulario() {
        JPanel p = panel();
        p.setLayout(new GridBagLayout());
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        return p;
    }

    public static GridBagConstraints rejilla() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        return g;
    }

    public static JTable tabla(javax.swing.table.TableModel modelo) {
        JTable t = new JTable(modelo);
        t.setRowHeight(30);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        t.setFont(CUERPO);
        t.setGridColor(new Color(229, 235, 242));
        t.setShowVerticalLines(false);
        t.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(230, 239, 244));
        t.setSelectionBackground(new Color(201, 234, 229));
        t.setSelectionForeground(TEXTO);
        return t;
    }

    public static void centrarColumna(JTable tabla, int columna) {
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getColumnModel().getColumn(columna).setCellRenderer(render);
    }

    public static void informacion(Component origen, String texto) {
        JOptionPane.showMessageDialog(origen, texto, "Gestión de Productos", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component origen, String texto) {
        return JOptionPane.showConfirmDialog(origen, texto, "Confirmar operación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void error(Component origen, Throwable ex) {
        Throwable raiz = ex;
        while (raiz.getCause() != null && raiz instanceof java.util.concurrent.ExecutionException)
            raiz = raiz.getCause();
        String mensaje;
        if (raiz instanceof java.sql.SQLException) {
            mensaje = "No se pudo completar la operación en PostgreSQL. Revisá la conexión y los datos.";
        } else {
            mensaje = raiz.getMessage() == null ? "No se pudo completar la operación." : raiz.getMessage();
        }
        JOptionPane.showMessageDialog(origen, mensaje, "Atención", JOptionPane.ERROR_MESSAGE);
    }

    /** Ejecuta acceso a BD fuera del EDT, y devuelve el resultado en el hilo Swing. */
    public static <T> void ejecutar(JComponent panel, Callable<T> trabajo, Consumer<T> exito) {
        ejecutar(panel, trabajo, exito, () -> {});
    }

    /** El callback final se ejecuta incluso cuando la consulta falla. */
    public static <T> void ejecutar(JComponent panel, Callable<T> trabajo, Consumer<T> exito,
                                    Runnable terminado) {
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return trabajo.call(); }
            @Override protected void done() {
                panel.setCursor(Cursor.getDefaultCursor());
                try { exito.accept(get()); }
                catch (java.lang.InterruptedException ex) { Thread.currentThread().interrupt(); }
                catch (java.util.concurrent.ExecutionException ex) { error(panel, ex.getCause()); }
                finally { terminado.run(); }
            }
        }.execute();
    }
}
