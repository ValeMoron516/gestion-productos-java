package com.example.productos;

import com.example.productos.modelo.*;
import com.example.productos.persistencia.contratos.*;
import com.example.productos.presentacion.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Prueba estructural de las ventanas; usa repositorios simulados y servidor X virtual. */
public final class PruebasSwingUI {
    private static int checks = 0;
    private static <T> T repo(Class<T> tipo) {
        Object instance = Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo},
                (obj, metodo, parametros) -> {
                    if (List.class.isAssignableFrom(metodo.getReturnType())) {
                        if (metodo.getName().equals("listar")) {
                            if (tipo == IRubroRepository.class) return List.of(new Rubro(1, "Electrónica"));
                            if (tipo == IProveedorRepository.class)
                                return List.of(new Proveedor(1, "Distribuidora", "Calle 10", "1234"));
                            if (tipo == IProductoRepository.class)
                                return List.of(new Producto("Teclado", "USB", new BigDecimal("15000"),
                                        25, new Rubro(1, "Electrónica"),
                                        new Proveedor(1, "Distribuidora", "Calle 10", "1234")));
                        }
                        return List.of();
                    }
                    if (metodo.getName().equals("buscarPorId")) {
                        if (tipo == IRubroRepository.class) return new Rubro(1, "Electrónica");
                        if (tipo == IProveedorRepository.class) return new Proveedor(1, "Distribuidora", "Calle 10", "1234");
                    }
                    if (metodo.getReturnType() == boolean.class) return true;
                    return null;
                });
        return tipo.cast(instance);
    }

    private static SesionSwing sesion(String tipo) {
        return new SesionSwing(new Rol(1, "Prueba", "Persona", tipo),
                repo(IProductoRepository.class), repo(IRubroRepository.class),
                repo(IProveedorRepository.class), repo(IRolRepository.class));
    }

    private static void comprobar(boolean ok, String descripcion) {
        if (!ok) throw new AssertionError(descripcion);
        checks++;
    }

    private static JButton encontrar(Container c, String nombre) {
        for (Component componente : c.getComponents()) {
            if (componente instanceof JButton b && b.getText().equals(nombre)) return b;
            if (componente instanceof Container p) {
                JButton encontrado = encontrar(p, nombre);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("Ejecutar con entorno gráfico o xvfb-run");
        }
        final MenuPrincipal[] muestra = new MenuPrincipal[1];
        SwingUtilities.invokeAndWait(() -> {
            EstiloSwing.instalar();
            SesionSwing invitado = sesion("i");
            SesionSwing empleado = sesion("e");
            SesionSwing admin = sesion("a");
            comprobar(!invitado.puedeEditarProductos() && !invitado.puedeAdministrarCatalogos(), "Invitado de solo lectura");
            comprobar(empleado.puedeEditarProductos() && !empleado.puedeAdministrarCatalogos(), "Empleado solo productos");
            comprobar(admin.puedeAdministrarCatalogos(), "Administrador catálogos");

            JFrame frame = new JFrame("Pruebas");
            VentanaProductosCRUD productosI = new VentanaProductosCRUD(frame, invitado);
            JButton guardarI = encontrar(productosI, "Guardar / Modificar");
            comprobar(guardarI != null && !guardarI.isEnabled(), "Invitado no puede guardar productos en Swing");
            productosI.dispose();

            VentanaProductosCRUD productosA = new VentanaProductosCRUD(frame, admin);
            JButton guardarA = encontrar(productosA, "Guardar / Modificar");
            comprobar(guardarA != null && guardarA.isEnabled(), "Administrador tiene botón de productos");
            productosA.dispose();

            VentanaRubros rubros = new VentanaRubros(frame, empleado);
            JButton guardarR = encontrar(rubros, "Guardar / Modificar");
            comprobar(guardarR != null && !guardarR.isEnabled(), "Empleado no puede modificar rubros en Swing");
            rubros.dispose();

            VentanaProveedores pro = new VentanaProveedores(frame, invitado);
            JButton guardarP = encontrar(pro, "Guardar / Modificar");
            comprobar(guardarP != null && !guardarP.isEnabled(), "Invitado no puede modificar proveedores en Swing");
            pro.dispose();
            VentanaRolesCRUD roles = new VentanaRolesCRUD(frame, admin);
            comprobar(encontrar(roles, "Guardar / Modificar") != null, "Admin tiene ABM de roles");
            roles.dispose();
            frame.dispose();

            MenuPrincipal menu = new MenuPrincipal(admin, () -> {});
            comprobar(menu.getJMenuBar().getMenuCount() == 2, "Barra Gestión y Salir");
            menu.setVisible(true);
            muestra[0] = menu;
        });
        Thread.sleep(650);
        MenuPrincipal menu = muestra[0];
        Rectangle ventana = new Rectangle(menu.getLocationOnScreen(), menu.getSize());
        BufferedImage captura = new Robot().createScreenCapture(ventana);
        ImageIO.write(captura, "png", new java.io.File(System.getProperty("java.io.tmpdir"), "gestion_productos_menu_preview.png"));
        comprobar(captura.getWidth() > 700 && captura.getHeight() > 400, "Vista previa del menú creada");
        final VentanaProductosCRUD[] productosAbiertos = new VentanaProductosCRUD[1];
        SwingUtilities.invokeAndWait(() -> {
            VentanaProductosCRUD productos = new VentanaProductosCRUD(menu, sesion("a"));
            productos.setModal(false); // Solo para la prueba visual: no cambia el comportamiento real.
            productos.setVisible(true);
            productosAbiertos[0] = productos;
        });
        Thread.sleep(650);
        VentanaProductosCRUD dialogoProducto = productosAbiertos[0];
        BufferedImage capturaProductos = new Robot().createScreenCapture(
                new Rectangle(dialogoProducto.getLocationOnScreen(), dialogoProducto.getSize()));
        ImageIO.write(capturaProductos, "png", new java.io.File(System.getProperty("java.io.tmpdir"), "gestion_productos_productos_preview.png"));
        SwingUtilities.invokeAndWait(() -> {
            dialogoProducto.dispose();
            menu.dispose();
        });
        comprobar(capturaProductos.getWidth() > 900, "Vista previa de Productos creada");
        System.out.println("Correcto: " + checks + " verificaciones Swing sin PostgreSQL.");
    }
}
