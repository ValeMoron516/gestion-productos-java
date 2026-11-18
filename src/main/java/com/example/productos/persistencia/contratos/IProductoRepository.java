package com.example.productos.persistencia.contratos;

import com.example.productos.modelo.Producto;
import java.sql.SQLException;
import java.util.List;

/** Contrato de persistencia: el servicio no depende de la implementación JDBC. */
public interface IProductoRepository {
    void guardar(Producto producto) throws SQLException;
    Producto buscarPorId(int id) throws SQLException;
    List<Producto> listar() throws SQLException;
    List<Producto> buscarPorNombre(String texto) throws SQLException;
    List<Producto> listarPorRubro(int rubroId) throws SQLException;
    List<Producto> listarPorProveedor(int proveedorId) throws SQLException;
    List<Producto> buscarPorNombreRubro(String texto) throws SQLException;
    List<Producto> buscarPorNombreProveedor(String texto) throws SQLException;
    void modificar(Producto producto) throws SQLException;
    boolean darDeBaja(int id) throws SQLException;
}
