package com.example.productos.presentacion;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Menú principal; los diálogos comparten la misma sesión y vuelven aquí al cerrar. */
public final class MenuPrincipal extends JFrame {
    private final SesionSwing sesion;
    private final Runnable volverLogin;

    public MenuPrincipal(SesionSwing sesion, Runnable volverLogin) {
        super("Gestión de Productos · " + sesion.persona().getNombreRol());
        this.sesion = sesion;
        this.volverLogin = volverLogin;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(840, 560);
        setMinimumSize(new Dimension(720, 450));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(EstiloSwing.cabecera("Panel de gestión",
                "Sesión: " + sesion.persona().getNombreCompleto() + "   ·   Perfil: "
                        + sesion.persona().getNombreRol()), BorderLayout.NORTH);

        JPanel area = EstiloSwing.panel();
        area.setBorder(new EmptyBorder(32, 40, 32, 40));
        area.setLayout(new GridLayout(2, 2, 18, 18));
        area.add(tarjeta("Productos", "Listado, búsqueda y movimientos", this::abrirProductos));
        area.add(tarjeta("Rubros", "Categorías y clasificación", this::abrirRubros));
        area.add(tarjeta("Proveedores", "Datos comerciales", this::abrirProveedores));
        if (sesion.puedeAdministrarCatalogos())
            area.add(tarjeta("Personas y roles", "Administración de accesos", this::abrirRoles));
        else
            area.add(tarjeta("Mi sesión", sesion.persona().getNombreRol() + " · solo opciones autorizadas", () -> {}));
        add(area, BorderLayout.CENTER);

        JMenuBar barra = new JMenuBar();
        JMenu gestion = new JMenu("Gestión");
        menuItem(gestion, "Productos", this::abrirProductos);
        menuItem(gestion, "Rubros", this::abrirRubros);
        menuItem(gestion, "Proveedores", this::abrirProveedores);
        if (sesion.puedeAdministrarCatalogos())
            menuItem(gestion, "Personas y roles", this::abrirRoles);
        JMenu salir = new JMenu("Salir");
        menuItem(salir, "Cerrar sesión", this::cerrarSesion);
        menuItem(salir, "Salir de la aplicación", this::terminar);
        barra.add(gestion);
        barra.add(salir);
        setJMenuBar(barra);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { terminar(); }
        });

        JLabel pie = new JLabel("  Los datos provienen de PostgreSQL. Los permisos también se comprueban en los servicios.");
        pie.setFont(new Font("Dialog", Font.PLAIN, 11));
        pie.setForeground(EstiloSwing.SUAVE);
        pie.setBorder(new EmptyBorder(6, 20, 12, 20));
        add(pie, BorderLayout.SOUTH);
    }

    private JPanel tarjeta(String nombre, String subtitulo, Runnable accion) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 226, 236), 1, true),
                new EmptyBorder(25, 22, 25, 22)));
        JLabel titulo = new JLabel(nombre);
        titulo.setFont(new Font("Dialog", Font.BOLD, 21));
        titulo.setForeground(EstiloSwing.OSCURO);
        JLabel detalle = new JLabel("<html>" + subtitulo + "</html>");
        detalle.setForeground(EstiloSwing.SUAVE);
        JButton abrir = EstiloSwing.boton("Abrir módulo");
        abrir.addActionListener(e -> accion.run());
        panel.add(titulo, BorderLayout.NORTH);
        panel.add(detalle, BorderLayout.CENTER);
        if (!nombre.equals("Mi sesión")) panel.add(abrir, BorderLayout.SOUTH);
        return panel;
    }

    private void menuItem(JMenu menu, String etiqueta, Runnable accion) {
        JMenuItem item = new JMenuItem(etiqueta);
        item.addActionListener(e -> accion.run());
        menu.add(item);
    }

    private void abrirProductos() { new VentanaProductosCRUD(this, sesion).setVisible(true); }
    private void abrirRubros() { new VentanaRubros(this, sesion).setVisible(true); }
    private void abrirProveedores() { new VentanaProveedores(this, sesion).setVisible(true); }
    private void abrirRoles() {
        if (!sesion.puedeAdministrarCatalogos()) {
            EstiloSwing.error(this, new SecurityException("Operación exclusiva del administrador"));
            return;
        }
        new VentanaRolesCRUD(this, sesion).setVisible(true);
    }

    private void cerrarSesion() {
        dispose();
        volverLogin.run();
    }

    private void terminar() {
        if (EstiloSwing.confirmar(this, "¿Querés salir de la aplicación?")) {
            dispose();
            System.exit(0);
        }
    }
}
