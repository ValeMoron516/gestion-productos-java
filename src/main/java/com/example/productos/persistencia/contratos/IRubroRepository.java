package com.example.productos.persistencia.contratos;

import com.example.productos.modelo.Rubro;
import java.sql.SQLException;
import java.util.List;

public interface IRubroRepository {
    void guardar(Rubro rubro) throws SQLException;
    Rubro buscarPorId(int id) throws SQLException;
    List<Rubro> listar() throws SQLException;
    boolean modificar(Rubro rubro) throws SQLException;
    boolean eliminar(int id) throws SQLException;
}
