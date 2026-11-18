package com.example.productos.presentacion;

import com.example.productos.modelo.Rol;
import com.example.productos.servicio.RolService;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Controlador para la actividad adicional de ABM de personas y roles. */
public class RolController {
    private final RolService servicio;

    public RolController(RolService servicio) {
        this.servicio = Objects.requireNonNull(servicio);
    }

    public List<Rol> listar() throws SQLException { return servicio.listar(); }
    public Rol buscar(int id) throws SQLException { return servicio.buscar(id); }
    public void registrar(String pin, String apellido, String nombre, String rol) throws SQLException {
        servicio.registrar(pin, apellido, nombre, rol);
    }
    public void modificar(int id, String pin, String apellido, String nombre, String rol)
            throws SQLException {
        servicio.modificar(id, pin, apellido, nombre, rol);
    }
    public void eliminar(int id) throws SQLException { servicio.eliminar(id); }
}
