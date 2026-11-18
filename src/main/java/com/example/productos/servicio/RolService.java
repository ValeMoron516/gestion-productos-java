package com.example.productos.servicio;

import com.example.productos.modelo.Rol;
import com.example.productos.persistencia.contratos.IRolRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** ABM administrativo, sin exponer los PIN en consultas ni en la sesión. */
public class RolService {
    private final IRolRepository repository;
    private final PermisosService permisos;

    public RolService(IRolRepository repository, PermisosService permisos) {
        this.repository = Objects.requireNonNull(repository);
        this.permisos = Objects.requireNonNull(permisos);
    }

    public List<Rol> listar() throws SQLException {
        permisos.exigirAdministrador();
        return repository.listar();
    }

    public Rol buscar(int id) throws SQLException {
        permisos.exigirAdministrador();
        ProductoService.validarId(id);
        return repository.buscarPorId(id);
    }

    public void registrar(String pin, String apellido, String nombre, String rol) throws SQLException {
        permisos.exigirAdministrador();
        validar(pin, apellido, nombre, rol, false);
        try {
            repository.guardar(pin, apellido.trim(), nombre.trim(), rol);
        } catch (SQLException e) {
            manejarPinRepetido(e);
        }
    }

    public void modificar(int id, String nuevoPin, String apellido, String nombre, String rol)
            throws SQLException {
        permisos.exigirAdministrador();
        ProductoService.validarId(id);
        impedirCambioPropio(id);
        validar(nuevoPin, apellido, nombre, rol, true);
        try {
            if (!repository.modificar(id, nuevoPin == null || nuevoPin.isBlank() ? null : nuevoPin,
                    apellido.trim(), nombre.trim(), rol)) {
                throw new IllegalArgumentException("La persona no existe.");
            }
        } catch (SQLException e) {
            manejarPinRepetido(e);
        }
    }

    public void eliminar(int id) throws SQLException {
        permisos.exigirAdministrador();
        ProductoService.validarId(id);
        impedirCambioPropio(id);
        if (!repository.eliminar(id)) throw new IllegalArgumentException("La persona no existe.");
    }

    private void impedirCambioPropio(int id) {
        if (permisos.getPersona().getId() == id)
            throw new IllegalArgumentException("No podés modificar ni eliminar tu propia cuenta durante la sesión.");
    }

    private void manejarPinRepetido(SQLException e) throws SQLException {
        if ("23505".equals(e.getSQLState()))
            throw new IllegalArgumentException("Ese PIN ya está asignado a otra persona.");
        throw e;
    }

    private void validar(String pin, String apellido, String nombre, String rol, boolean pinOpcional) {
        if ((!pinOpcional || (pin != null && !pin.isBlank())) &&
                (pin == null || !pin.matches("[0-9]{4}")))
            throw new IllegalArgumentException("El PIN debe contener exactamente 4 dígitos.");
        if (apellido == null || apellido.isBlank() || apellido.trim().length() > 80)
            throw new IllegalArgumentException("El apellido es obligatorio (máximo 80 caracteres).");
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 80)
            throw new IllegalArgumentException("El nombre es obligatorio (máximo 80 caracteres).");
        if (!"e".equals(rol) && !"a".equals(rol) && !"i".equals(rol))
            throw new IllegalArgumentException("Rol inválido. Usá e, a o i.");
    }
}
