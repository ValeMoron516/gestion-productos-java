package com.example.productos;

import com.example.productos.modelo.*;
import com.example.productos.persistencia.contratos.*;
import com.example.productos.servicio.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

/** Test autocontenido del negocio, no depende de Maven/JUnit ni de PostgreSQL. */
public class PruebasSinBD {
    private static int checks = 0;

    public static void main(String[] args) throws Exception {
        RepoProducto rp = new RepoProducto();
        RepoRubro rr = new RepoRubro();
        RepoProveedor rv = new RepoProveedor();
        PermisosService permisos = new PermisosService(new Rol(99, "Prueba", "Admin", "a"));
        ProductoService producto = new ProductoService(rp, rr, rv, permisos);
        RubroService rubros = new RubroService(rr, permisos);
        ProveedorService proveedores = new ProveedorService(rv, permisos);
        rr.guardar(new Rubro(0, "Informática"));
        rv.guardar(new Proveedor(0, "Tecno SRL", "Calle 10", "1234"));

        // Caso feliz: alta, lectura, edición de rubro/proveedor, búsquedas y baja lógica.
        producto.registrarProducto("Teclado", "Mecánico", new BigDecimal("45000.50"), 10, 1, 1);
        comprobar(producto.listarProductos().size() == 1, "El alta debe listar el producto");
        comprobar(producto.buscarPorId(1).getProveedor().getRazonSocial().equals("Tecno SRL"), "Debe tener proveedor");
        comprobar(producto.buscarPorNombre("TECLA").size() == 1, "Búsqueda producto por nombre");
        comprobar(producto.buscarPorNombreRubro("formá").size() == 1, "Búsqueda por nombre rubro");
        comprobar(producto.buscarPorNombreProveedor("tecno").size() == 1, "Búsqueda por nombre proveedor");
        comprobar(producto.listarPorRubro(1).size() == 1, "Filtro ID rubro");
        comprobar(producto.listarPorProveedor(1).size() == 1, "Filtro ID proveedor");

        rr.guardar(new Rubro(0, "Audio"));
        rv.guardar(new Proveedor(0, "Audio SA", "Calle 20", "5678"));
        producto.modificarProducto(1, "Auriculares", "Inalámbricos", new BigDecimal("100.00"), 4, 2, 2);
        comprobar(producto.buscarPorId(1).getRubro().getId() == 2, "Modificar rubro");
        comprobar(producto.buscarPorId(1).getProveedor().getId() == 2, "Modificar proveedor");
        comprobar(producto.listarPorRubro(1).isEmpty(), "No debe pertenecer al rubro anterior");

        falla(() -> producto.registrarProducto("  ", "", new BigDecimal("1"), 1, 1, 1), "Nombre vacío");
        falla(() -> producto.registrarProducto("Teclado", "", BigDecimal.ZERO, 1, 1, 1), "Precio cero");
        falla(() -> producto.registrarProducto("Teclado", "", new BigDecimal("2.123"), 1, 1, 1), "Precio con 3 decimales");
        falla(() -> producto.registrarProducto("Teclado", "", new BigDecimal("1"), -1, 1, 1), "Stock negativo");
        falla(() -> producto.registrarProducto("Teclado", "", new BigDecimal("1"), 1, 999, 1), "Rubro inexistente");
        falla(() -> producto.registrarProducto("Teclado", "", new BigDecimal("1"), 1, 1, 999), "Proveedor inexistente");
        falla(() -> producto.buscarPorId(-2), "ID inválido");
        falla(() -> producto.buscarPorNombreProveedor("  "), "Búsqueda vacía");
        falla(() -> rubros.registrar("   "), "Rubro sin nombre");
        falla(() -> proveedores.registrar("  ", "X", "Y"), "Proveedor sin razón social");

        rubros.modificar(2, "Sonido");
        comprobar(rubros.buscar(2).getNombre().equals("Sonido"), "Modificar rubro");
        proveedores.modificar(2, "Sonido SA", "Calle 23", "9876");
        comprobar(proveedores.buscar(2).getRazonSocial().equals("Sonido SA"), "Modificar proveedor");

        producto.eliminarProducto(1);
        comprobar(producto.listarProductos().isEmpty(), "Baja lógica debe ocultar del listado");
        comprobar(producto.buscarPorId(1) == null, "Baja lógica debe ocultar de búsqueda");
        comprobar(rp.datos.containsKey(1) && !rp.datos.get(1).isActivo(), "Debe continuar guardado");
        falla(() -> producto.eliminarProducto(1), "Repetir baja");

        rubros.eliminar(2);
        proveedores.eliminar(2);
        comprobar(rubros.buscar(2) == null, "Eliminar rubro sin vínculo en memoria");
        comprobar(proveedores.buscar(2) == null, "Eliminar proveedor sin vínculo en memoria");

        System.out.println("Correcto: " + checks + " comprobaciones de negocio sin PostgreSQL.");
    }

    private static void comprobar(boolean condicion, String descripcion) {
        if (!condicion) throw new AssertionError(descripcion);
        checks++;
    }

    @FunctionalInterface
    interface Accion { void run() throws Exception; }
    private static void falla(Accion accion, String descripcion) throws Exception {
        try { accion.run(); throw new AssertionError("No se rechazó: " + descripcion); }
        catch (IllegalArgumentException esperado) { checks++; }
    }

    private static class RepoProducto implements IProductoRepository {
        Map<Integer, Producto> datos = new HashMap<>();
        private int siguiente = 1;
        public void guardar(Producto p) { p.setId(siguiente++); datos.put(p.getId(), p); }
        public Producto buscarPorId(int id) { Producto p = datos.get(id); return p != null && p.isActivo() ? p : null; }
        public List<Producto> listar() { return datos.values().stream().filter(Producto::isActivo).toList(); }
        public List<Producto> buscarPorNombre(String t) { return listar().stream().filter(p -> contiene(p.getNombre(), t)).toList(); }
        public List<Producto> buscarPorNombreRubro(String t) { return listar().stream().filter(p -> contiene(p.getRubro().getNombre(), t)).toList(); }
        public List<Producto> buscarPorNombreProveedor(String t) { return listar().stream().filter(p -> p.getProveedor() != null && contiene(p.getProveedor().getRazonSocial(), t)).toList(); }
        public List<Producto> listarPorRubro(int id) { return listar().stream().filter(p -> p.getRubro().getId() == id).toList(); }
        public List<Producto> listarPorProveedor(int id) { return listar().stream().filter(p -> p.getProveedor() != null && p.getProveedor().getId() == id).toList(); }
        public void modificar(Producto p) { datos.put(p.getId(), p); }
        public boolean darDeBaja(int id) {
            Producto p = buscarPorId(id);
            if (p == null) return false;
            p.setActivo(false);
            return true;
        }
        private boolean contiene(String nombre, String texto) {
            return nombre.toLowerCase(Locale.ROOT).contains(texto.toLowerCase(Locale.ROOT));
        }
    }

    private static class RepoRubro implements IRubroRepository {
        Map<Integer, Rubro> datos = new HashMap<>();
        private int siguiente = 1;
        public void guardar(Rubro r) { r.setId(siguiente++); datos.put(r.getId(), r); }
        public Rubro buscarPorId(int id) { return datos.get(id); }
        public List<Rubro> listar() { return new ArrayList<>(datos.values()); }
        public boolean modificar(Rubro r) { if (!datos.containsKey(r.getId())) return false; datos.put(r.getId(), r); return true; }
        public boolean eliminar(int id) { return datos.remove(id) != null; }
    }
    private static class RepoProveedor implements IProveedorRepository {
        Map<Integer, Proveedor> datos = new HashMap<>();
        private int siguiente = 1;
        public void guardar(Proveedor p) { p.setId(siguiente++); datos.put(p.getId(), p); }
        public Proveedor buscarPorId(int id) { return datos.get(id); }
        public List<Proveedor> listar() { return new ArrayList<>(datos.values()); }
        public boolean modificar(Proveedor p) { if (!datos.containsKey(p.getId())) return false; datos.put(p.getId(), p); return true; }
        public boolean eliminar(int id) { return datos.remove(id) != null; }
    }
}
