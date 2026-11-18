package com.example.productos.persistencia.contratos;

import com.example.productos.modelo.Proveedor;
import java.sql.SQLException;
import java.util.List;

public interface IProveedorRepository {
    void guardar(Proveedor proveedor) throws SQLException;
    Proveedor buscarPorId(int id) throws SQLException;
    List<Proveedor> listar() throws SQLException;
    boolean modificar(Proveedor proveedor) throws SQLException;
    boolean eliminar(int id) throws SQLException;
}
