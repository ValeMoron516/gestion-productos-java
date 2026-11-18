package com.example.productos.servicio;

import com.example.productos.modelo.Rubro;
import com.example.productos.persistencia.contratos.IRubroRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class RubroService {
    private final IRubroRepository repositorio;
    private final PermisosService permisos;

    public RubroService(IRubroRepository repositorio, PermisosService permisos) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.permisos = Objects.requireNonNull(permisos);
    }

    public void registrar(String nombre) throws SQLException {
        permisos.exigirAdministrador();
        validarNombre(nombre);
        try {
            repositorio.guardar(new Rubro(0, nombre.trim()));
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState()))
                throw new IllegalArgumentException("Ya existe un rubro con ese nombre.");
            throw e;
        }
    }

    public List<Rubro> listar() throws SQLException {
        permisos.exigirLectura();
        return repositorio.listar();
    }

    public Rubro buscar(int id) throws SQLException {
        permisos.exigirLectura();
        ProductoService.validarId(id);
        return repositorio.buscarPorId(id);
    }

    public void modificar(int id, String nombre) throws SQLException {
        permisos.exigirAdministrador();
        ProductoService.validarId(id);
        validarNombre(nombre);
        try {
            if (!repositorio.modificar(new Rubro(id, nombre.trim())))
                throw new IllegalArgumentException("No existe el rubro indicado.");
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState()))
                throw new IllegalArgumentException("Ya existe un rubro con ese nombre.");
            throw e;
        }
    }

    public void eliminar(int id) throws SQLException {
        permisos.exigirAdministrador();
        ProductoService.validarId(id);
        try {
            if (!repositorio.eliminar(id))
                throw new IllegalArgumentException("No existe el rubro indicado.");
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState()))
                throw new IllegalArgumentException("No se puede eliminar: el rubro tiene productos asociados (incluso dados de baja).");
            throw e;
        }
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 100)
            throw new IllegalArgumentException("El nombre del rubro es obligatorio (máximo 100 caracteres).");
    }
}
