package com.example.productos.persistencia.contratos;

import com.example.productos.modelo.Rol;
import java.sql.SQLException;
import java.util.List;

/** Contrato para login y ABM administrativo de personas/roles. */
public interface IRolRepository {
    Rol buscarPorPin(String pin) throws SQLException;
    Rol buscarPorId(int id) throws SQLException;
    List<Rol> listar() throws SQLException;
    void guardar(String pin, String apellido, String nombre, String rol) throws SQLException;
    boolean modificar(int id, String nuevoPin, String apellido, String nombre, String rol) throws SQLException;
    boolean eliminar(int id) throws SQLException;
}
