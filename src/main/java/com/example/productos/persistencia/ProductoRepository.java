package com.example.productos.persistencia;

import com.example.productos.modelo.Producto;
import com.example.productos.modelo.Rubro;
import com.example.productos.modelo.Proveedor;
import com.example.productos.persistencia.contratos.IProductoRepository;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductoRepository implements IProductoRepository {
    // LEFT JOIN permite seguir mostrando productos anteriores a la migración,
    // cuando todavía no se les había asignado proveedor.
    private static final String CONSULTA = """
            SELECT p.id, p.nombre, p.descripcion, p.precio, p.stock,
                   p.activo, p.fecha_alta,
                   r.id AS rubro_id, r.nombre AS rubro_nombre,
                   pr.id_prov AS proveedor_id, pr.razon_social AS proveedor_nombre,
                   pr.domicilio AS proveedor_domicilio, pr.telefono AS proveedor_telefono
            FROM productos p
            INNER JOIN rubros r ON r.id = p.rubro_id
            LEFT JOIN proveedores pr ON pr.id_prov = p.proveedor_id
            WHERE p.activo = TRUE
            """;

    @Override
    public void guardar(Producto producto) throws SQLException {
        String sql = """
                INSERT INTO productos
                (nombre, descripcion, precio, stock, activo, fecha_alta, rubro_id, proveedor_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setBoolean(5, producto.isActivo());
            ps.setObject(6, producto.getFechaAlta());
            ps.setInt(7, producto.getRubro().getId());
            ps.setInt(8, producto.getProveedor().getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Producto buscarPorId(int id) throws SQLException {
        String sql = CONSULTA + " AND p.id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearProducto(rs) : null;
            }
        }
    }

    @Override
    public List<Producto> listar() throws SQLException {
        return ejecutarListado(CONSULTA + " ORDER BY p.id", null);
    }

    @Override
    public List<Producto> buscarPorNombre(String texto) throws SQLException {
        return ejecutarListado(CONSULTA + " AND LOWER(p.nombre) LIKE LOWER(?) ORDER BY p.id", "%" + texto + "%");
    }

    @Override
    public List<Producto> listarPorRubro(int rubroId) throws SQLException {
        return ejecutarListado(CONSULTA + " AND p.rubro_id = ? ORDER BY p.id", rubroId);
    }

    @Override
    public List<Producto> listarPorProveedor(int proveedorId) throws SQLException {
        return ejecutarListado(CONSULTA + " AND p.proveedor_id = ? ORDER BY p.id", proveedorId);
    }

    @Override
    public List<Producto> buscarPorNombreRubro(String texto) throws SQLException {
        return ejecutarListado(CONSULTA + " AND LOWER(r.nombre) LIKE LOWER(?) ORDER BY p.id", "%" + texto + "%");
    }

    @Override
    public List<Producto> buscarPorNombreProveedor(String texto) throws SQLException {
        return ejecutarListado(CONSULTA + " AND LOWER(pr.razon_social) LIKE LOWER(?) ORDER BY p.id", "%" + texto + "%");
    }

    private List<Producto> ejecutarListado(String sql, Object parametro) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro instanceof Integer numero) ps.setInt(1, numero);
            if (parametro instanceof String texto) ps.setString(1, texto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) productos.add(mapearProducto(rs));
            }
        }
        return productos;
    }

    @Override
    public void modificar(Producto producto) throws SQLException {
        String sql = """
                UPDATE productos
                SET nombre = ?, descripcion = ?, precio = ?, stock = ?,
                    rubro_id = ?, proveedor_id = ?
                WHERE id = ? AND activo = TRUE
                """;
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setInt(5, producto.getRubro().getId());
            ps.setInt(6, producto.getProveedor().getId());
            ps.setInt(7, producto.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("El producto ya no existe o fue dado de baja.");
            }
        }
    }

    @Override
    public boolean darDeBaja(int id) throws SQLException {
        String sql = "UPDATE productos SET activo = FALSE WHERE id = ? AND activo = TRUE";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setActivo(rs.getBoolean("activo"));
        p.setFechaAlta(rs.getObject("fecha_alta", LocalDate.class));
        p.setRubro(new Rubro(rs.getInt("rubro_id"), rs.getString("rubro_nombre")));
        Integer proveedorId = (Integer) rs.getObject("proveedor_id");
        if (proveedorId != null) {
            p.setProveedor(new Proveedor(proveedorId, rs.getString("proveedor_nombre"),
                    rs.getString("proveedor_domicilio"), rs.getString("proveedor_telefono")));
        }
        return p;
    }
}
