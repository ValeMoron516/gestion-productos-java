package com.example.productos.presentacion;

import com.example.productos.modelo.Rol;
import com.example.productos.persistencia.*;
import com.example.productos.persistencia.contratos.*;
import com.example.productos.servicio.*;
import java.util.Objects;

/** Una sola sesión, con una sola política de permisos compartida por todas las ventanas. */
public final class SesionSwing {
    private final Rol persona;
    private final PermisosService permisos;
    private final ProductoController catalogo;
    private final RolController personas;

    public SesionSwing(Rol persona, IProductoRepository productos,
                       IRubroRepository rubros, IProveedorRepository proveedores,
                       IRolRepository roles) {
        this.persona = Objects.requireNonNull(persona);
        permisos = new PermisosService(persona);
        catalogo = new ProductoController(
                new ProductoService(productos, rubros, proveedores, permisos),
                new RubroService(rubros, permisos),
                new ProveedorService(proveedores, permisos));
        personas = new RolController(new RolService(roles, permisos));
    }

    public static SesionSwing real(Rol persona) {
        return new SesionSwing(persona, new ProductoRepository(),
                new RubroRepository(), new ProveedorRepository(), new RolRepository());
    }

    public Rol persona() { return persona; }
    public ProductoController catalogo() { return catalogo; }
    public RolController personas() { return personas; }
    public boolean puedeEditarProductos() { return !persona.esInvitado(); }
    public boolean puedeAdministrarCatalogos() { return persona.esAdministrador(); }
}
