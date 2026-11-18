package com.example.productos.servicio;

import com.example.productos.modelo.Proveedor;
import com.example.productos.persistencia.contratos.IProveedorRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ProveedorService {
    private final IProveedorRepository repositorio;
    private final PermisosService permisos;

    public ProveedorService(IProveedorRepository repositorio, PermisosService permisos) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.permisos = Objects.requireNonNull(permisos);
    }

    public void registrar(String razonSocial, String domicilio, String telefono) throws SQLException {
        permisos.exigirAdministrador();
        validar(razonSocial, domicilio, telefono);
        repositorio.guardar(new Proveedor(0, razonSocial.trim(), domicilio, telefono));
    }

    public List<Proveedor> listar() throws SQLException {
        permisos.exigirLectura();
        return repositorio.listar();
    }

    public Proveedor buscar(int id) throws SQLException {
        permisos.exigirLectura();
        ProductoService.validarId(id);
        return repositorio.buscarPorId(id);
    }

    public void modificar(int id, String razonSocial, String domicilio, String telefono) throws SQLException {
        permisos.exigirAdministrador();
        ProductoService.validarId(id);
        validar(razonSocial, domicilio, telefono);
        if (!repositorio.modificar(new Proveedor(id, razonSocial.trim(), domicilio, telefono)))
            throw new IllegalArgumentException("No existe el proveedor indicado.");
    }

    public void eliminar(int id) throws SQLException {
        permisos.exigirAdministrador();
        ProductoService.validarId(id);
        try {
            if (!repositorio.eliminar(id))
                throw new IllegalArgumentException("No existe el proveedor indicado.");
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState()))
                throw new IllegalArgumentException("No se puede eliminar: el proveedor tiene productos asociados (incluso dados de baja).");
            throw e;
        }
    }

    private void validar(String razonSocial, String domicilio, String telefono) {
        if (razonSocial == null || razonSocial.isBlank() || razonSocial.trim().length() > 150)
            throw new IllegalArgumentException("La razón social es obligatoria (máximo 150 caracteres).");
        if (domicilio != null && domicilio.length() > 200)
            throw new IllegalArgumentException("El domicilio admite hasta 200 caracteres.");
        if (telefono != null && telefono.length() > 50)
            throw new IllegalArgumentException("El teléfono admite hasta 50 caracteres.");
    }
}
