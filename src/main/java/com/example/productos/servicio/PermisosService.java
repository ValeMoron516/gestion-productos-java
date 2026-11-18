package com.example.productos.servicio;

import com.example.productos.modelo.Rol;

/** Autorización obligatoria en la capa de negocio; el menú no es una barrera de seguridad. */
public class PermisosService {
    private final Rol persona;

    public PermisosService(Rol persona) {
        if (persona == null) throw new SecurityException("Debe iniciar sesión");
        this.persona = persona;
    }

    public Rol getPersona() { return persona; }

    /** Listar productos, rubros y proveedores: todos los roles autenticados. */
    public void exigirLectura() {
        if (!persona.esAdministrador() && !persona.esEmpleado() && !persona.esInvitado()) {
            throw new SecurityException("Rol no autorizado");
        }
    }

    /** ABM y consultas detalladas de productos: empleado o administrador. */
    public void exigirSesion() {
        exigirLectura();
        if (persona.esInvitado()) throw new SecurityException("Operación no permitida al invitado");
    }

    /** ABM y consultas detalladas de rubros/proveedores/roles: solo administrador. */
    public void exigirAdministrador() {
        exigirSesion();
        if (!persona.esAdministrador()) {
            throw new SecurityException("Operación exclusiva del administrador");
        }
    }
}
