package com.example.productos.presentacion;

import com.example.productos.modelo.Producto;
import com.example.productos.modelo.Proveedor;
import com.example.productos.modelo.Rubro;
import com.example.productos.servicio.ProductoService;
import com.example.productos.servicio.ProveedorService;
import com.example.productos.servicio.RubroService;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** El Main usa este controlador; las reglas se delegan a Service. */
public class ProductoController {
    private final ProductoService productos;
    private final RubroService rubros;
    private final ProveedorService proveedores;

    public ProductoController(ProductoService productos, RubroService rubros,
                              ProveedorService proveedores) {
        this.productos = Objects.requireNonNull(productos);
        this.rubros = Objects.requireNonNull(rubros);
        this.proveedores = Objects.requireNonNull(proveedores);
    }

    // PRODUCTOS
    public void registrarProducto(String nombre, String descripcion, BigDecimal precio, int stock,
                                  int rubroId, int proveedorId) throws SQLException {
        productos.registrarProducto(nombre, descripcion, precio, stock, rubroId, proveedorId);
    }
    public List<Producto> listarProductos() throws SQLException { return productos.listarProductos(); }
    public Producto buscarProducto(int id) throws SQLException { return productos.buscarPorId(id); }
    public List<Producto> buscarProductosPorNombre(String nombre) throws SQLException { return productos.buscarPorNombre(nombre); }
    public List<Producto> listarProductosPorRubro(int id) throws SQLException { return productos.listarPorRubro(id); }
    public List<Producto> listarProductosPorProveedor(int id) throws SQLException { return productos.listarPorProveedor(id); }
    public List<Producto> buscarPorNombreRubro(String texto) throws SQLException {
        return productos.buscarPorNombreRubro(texto);
    }
    public List<Producto> buscarPorNombreProveedor(String texto) throws SQLException {
        return productos.buscarPorNombreProveedor(texto);
    }
    public void modificarProducto(int id, String nombre, String descripcion, BigDecimal precio,
                                  int stock, int rubroId, int proveedorId) throws SQLException {
        productos.modificarProducto(id, nombre, descripcion, precio, stock, rubroId, proveedorId);
    }
    public void eliminarProducto(int id) throws SQLException { productos.eliminarProducto(id); }

    // RUBROS
    public void registrarRubro(String nombre) throws SQLException { rubros.registrar(nombre); }
    public List<Rubro> listarRubros() throws SQLException { return rubros.listar(); }
    public Rubro buscarRubro(int id) throws SQLException { return rubros.buscar(id); }
    public void modificarRubro(int id, String nombre) throws SQLException { rubros.modificar(id, nombre); }
    public void eliminarRubro(int id) throws SQLException { rubros.eliminar(id); }

    // PROVEEDORES
    public void registrarProveedor(String razon, String domicilio, String telefono) throws SQLException {
        proveedores.registrar(razon, domicilio, telefono);
    }
    public List<Proveedor> listarProveedores() throws SQLException { return proveedores.listar(); }
    public Proveedor buscarProveedor(int id) throws SQLException { return proveedores.buscar(id); }
    public void modificarProveedor(int id, String razon, String domicilio, String telefono) throws SQLException {
        proveedores.modificar(id, razon, domicilio, telefono);
    }
    public void eliminarProveedor(int id) throws SQLException { proveedores.eliminar(id); }
}
