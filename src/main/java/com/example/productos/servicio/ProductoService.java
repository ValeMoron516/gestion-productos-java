package com.example.productos.servicio;

import com.example.productos.modelo.Producto;
import com.example.productos.modelo.Proveedor;
import com.example.productos.modelo.Rubro;
import com.example.productos.persistencia.contratos.IProductoRepository;
import com.example.productos.persistencia.contratos.IProveedorRepository;
import com.example.productos.persistencia.contratos.IRubroRepository;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Reglas del negocio sin referencias a clases JDBC concretas. */
public class ProductoService {
    private final IProductoRepository productoRepo;
    private final IRubroRepository rubroRepo;
    private final IProveedorRepository proveedorRepo;
    private final PermisosService permisos;

    public ProductoService(IProductoRepository productoRepo, IRubroRepository rubroRepo,
                           IProveedorRepository proveedorRepo, PermisosService permisos) {
        this.productoRepo = Objects.requireNonNull(productoRepo);
        this.rubroRepo = Objects.requireNonNull(rubroRepo);
        this.proveedorRepo = Objects.requireNonNull(proveedorRepo);
        this.permisos = Objects.requireNonNull(permisos);
    }

    public void registrarProducto(String nombre, String descripcion, BigDecimal precio, int stock,
                                  int rubroId, int proveedorId) throws SQLException {
        permisos.exigirSesion();
        validarDatos(nombre, descripcion, precio, stock);
        Rubro rubro = obtenerRubro(rubroId);
        Proveedor proveedor = obtenerProveedor(proveedorId);
        productoRepo.guardar(new Producto(nombre.trim(), descripcion, precio, stock, rubro, proveedor));
    }

    public List<Producto> listarProductos() throws SQLException {
        permisos.exigirLectura();
        return productoRepo.listar();
    }

    public Producto buscarPorId(int id) throws SQLException {
        permisos.exigirLectura();
        validarId(id);
        return productoRepo.buscarPorId(id);
    }

    public List<Producto> buscarPorNombre(String nombre) throws SQLException {
        permisos.exigirLectura();
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Escribí parte del nombre a buscar.");
        return productoRepo.buscarPorNombre(nombre.trim());
    }

    public List<Producto> listarPorRubro(int rubroId) throws SQLException {
        permisos.exigirLectura();
        obtenerRubro(rubroId);
        return productoRepo.listarPorRubro(rubroId);
    }

    public List<Producto> listarPorProveedor(int proveedorId) throws SQLException {
        permisos.exigirLectura();
        obtenerProveedor(proveedorId);
        return productoRepo.listarPorProveedor(proveedorId);
    }

    public List<Producto> buscarPorNombreRubro(String texto) throws SQLException {
        permisos.exigirLectura();
        validarBusqueda(texto);
        return productoRepo.buscarPorNombreRubro(texto.trim());
    }

    public List<Producto> buscarPorNombreProveedor(String texto) throws SQLException {
        permisos.exigirLectura();
        validarBusqueda(texto);
        return productoRepo.buscarPorNombreProveedor(texto.trim());
    }

    private void validarBusqueda(String texto) {
        if (texto == null || texto.isBlank())
            throw new IllegalArgumentException("Ingresá parte del nombre a buscar.");
    }

    public void modificarProducto(int id, String nombre, String descripcion, BigDecimal precio,
                                  int stock, int rubroId, int proveedorId) throws SQLException {
        permisos.exigirSesion();
        Producto p = buscarPorId(id);
        if (p == null) throw new IllegalArgumentException("No existe un producto activo con ese ID.");
        validarDatos(nombre, descripcion, precio, stock);
        Rubro rubro = obtenerRubro(rubroId);
        Proveedor proveedor = obtenerProveedor(proveedorId);
        p.setNombre(nombre.trim());
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setRubro(rubro);
        p.setProveedor(proveedor);
        productoRepo.modificar(p);
    }

    /** Baja lógica: conserva el registro en PostgreSQL pero deja de mostrarlo. */
    public void eliminarProducto(int id) throws SQLException {
        permisos.exigirSesion();
        validarId(id);
        if (!productoRepo.darDeBaja(id))
            throw new IllegalArgumentException("No existe un producto activo con ese ID.");
    }

    private Rubro obtenerRubro(int id) throws SQLException {
        validarId(id);
        Rubro r = rubroRepo.buscarPorId(id);
        if (r == null) throw new IllegalArgumentException("El rubro seleccionado no existe.");
        return r;
    }

    private Proveedor obtenerProveedor(int id) throws SQLException {
        validarId(id);
        Proveedor p = proveedorRepo.buscarPorId(id);
        if (p == null) throw new IllegalArgumentException("El proveedor seleccionado no existe.");
        return p;
    }

    static void validarId(int id) {
        if (id <= 0) throw new IllegalArgumentException("El ID debe ser mayor a cero.");
    }

    private void validarDatos(String nombre, String descripcion, BigDecimal precio, int stock) {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 100)
            throw new IllegalArgumentException("El nombre es obligatorio (máximo 100 caracteres).");
        if (descripcion != null && descripcion.length() > 250)
            throw new IllegalArgumentException("La descripción puede tener hasta 250 caracteres.");
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        if (precio.stripTrailingZeros().scale() > 2 || precio.compareTo(new BigDecimal("9999999999.99")) > 0)
            throw new IllegalArgumentException("El precio admite hasta 2 decimales y 10 dígitos enteros.");
        if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
    }
}
