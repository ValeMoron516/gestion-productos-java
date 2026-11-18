package com.example.productos.modelo;

/** Persona autenticada. No almacena el PIN dentro de la sesión. */
public final class Rol {
    private final int id;
    private final String apellido;
    private final String nombre;
    private final String rol;

    public Rol(int id, String apellido, String nombre, String rol) {
        if (!"e".equals(rol) && !"a".equals(rol) && !"i".equals(rol)) {
            throw new IllegalArgumentException("Rol inválido");
        }
        this.id = id;
        this.apellido = apellido;
        this.nombre = nombre;
        this.rol = rol;
    }

    public int getId() { return id; }
    public String getApellido() { return apellido; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
    public boolean esAdministrador() { return "a".equals(rol); }
    public boolean esEmpleado() { return "e".equals(rol); }
    public boolean esInvitado() { return "i".equals(rol); }
    public String getNombreCompleto() { return nombre + " " + apellido; }
    public String getNombreRol() {
        return esAdministrador() ? "Administrador" : esEmpleado() ? "Empleado" : "Invitado";
    }
}
