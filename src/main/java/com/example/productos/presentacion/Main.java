package com.example.productos.presentacion;

import com.example.productos.modelo.Producto;
import com.example.productos.modelo.Proveedor;
import com.example.productos.modelo.Rubro;
import com.example.productos.modelo.Rol;
import com.example.productos.persistencia.ProductoRepository;
import com.example.productos.persistencia.ProveedorRepository;
import com.example.productos.persistencia.RubroRepository;
import com.example.productos.persistencia.RolRepository;
import com.example.productos.persistencia.contratos.IProductoRepository;
import com.example.productos.persistencia.contratos.IProveedorRepository;
import com.example.productos.persistencia.contratos.IRubroRepository;
import com.example.productos.persistencia.contratos.IRolRepository;
import com.example.productos.servicio.LoginService;
import com.example.productos.servicio.PermisosService;
import com.example.productos.servicio.ProductoService;
import com.example.productos.servicio.RubroService;
import com.example.productos.servicio.ProveedorService;
import com.example.productos.servicio.RolService;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Menús por consola. No conoce SQL, repositorios ni reglas de negocio. */
public class Main {
    private static ProductoController controlador;
    private static RolController controladorRoles;
    private static Rol persona;
    private static final Scanner teclado = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            IRolRepository rolRepo = new RolRepository();
            persona = solicitarLogin(new LoginService(rolRepo));
            if (persona == null) {
                System.out.println("Fin del sistema.");
                return;
            }

            PermisosService permisos = new PermisosService(persona);
            IProductoRepository productoRepo = new ProductoRepository();
            IRubroRepository rubroRepo = new RubroRepository();
            IProveedorRepository proveedorRepo = new ProveedorRepository();
            controlador = new ProductoController(
                    new ProductoService(productoRepo, rubroRepo, proveedorRepo, permisos),
                    new RubroService(rubroRepo, permisos),
                    new ProveedorService(proveedorRepo, permisos));
            controladorRoles = new RolController(new RolService(rolRepo, permisos));

            boolean seguir = true;
            while (seguir) {
                mostrarMenu();
                try {
                    switch (entero("Opción: ")) {
                        case 1 -> {
                            if (persona.esInvitado()) menuProductosConsulta();
                            else menuProductos();
                        }
                        case 2 -> {
                            if (persona.esAdministrador()) menuRubros();
                            else mostrarRubros();
                        }
                        case 3 -> {
                            if (persona.esAdministrador()) menuProveedores();
                            else mostrarProveedores();
                        }
                        case 4 -> {
                            permisos.exigirAdministrador();
                            menuRoles();
                        }
                        case 0 -> seguir = false;
                        default -> System.out.println("Opción incorrecta.");
                    }
                } catch (SQLException e) {
                    System.out.println("Error de base de datos: " + e.getMessage());
                } catch (SecurityException e) {
                    System.out.println("Acceso denegado: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println("Dato inválido: " + e.getMessage());
                }
            }
            System.out.println("¡Hasta luego!");
        } catch (NoSuchElementException e) {
            System.out.println("\nEntrada cerrada. Fin del programa.");
        }
    }

    private static Rol solicitarLogin(LoginService login) {
        while (true) {
            System.out.println("\n--- INGRESO AL SISTEMA ---");
            String pin = texto("PIN de 4 dígitos o 0 para salir: ").trim();
            if ("0".equals(pin)) return null;
            try {
                return login.ingresar(pin);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (SQLException e) {
                System.out.println("No se pudo acceder a PostgreSQL. Verificá la conexión y la tabla roles.");
                return null;
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n========== GESTIÓN DE PRODUCTOS ==========");
        System.out.println("Persona: " + persona.getNombreCompleto());
        System.out.println("Rol: " + persona.getNombreRol());
        System.out.println(persona.esInvitado() ? "1. Consultar productos" : "1. Productos");
        System.out.println(persona.esAdministrador() ? "2. Gestionar rubros" : "2. Listar rubros");
        System.out.println(persona.esAdministrador() ? "3. Gestionar proveedores" : "3. Listar proveedores");
        if (persona.esAdministrador()) System.out.println("4. Gestionar roles y personas");
        System.out.println("0. Salir");
    }

    /** Menú de consulta para invitados, sin modificar datos. */
    private static void menuProductosConsulta() {
        boolean seguir = true;
        while (seguir) {
            System.out.println("\n--- CONSULTA DE PRODUCTOS ---");
            System.out.println("1. Listar activos");
            System.out.println("2. Buscar por ID");
            System.out.println("3. Buscar por nombre");
            System.out.println("0. Volver");
            try {
                switch (entero("Opción: ")) {
                    case 1 -> mostrarProductos(controlador.listarProductos());
                    case 2 -> buscarProducto();
                    case 3 -> mostrarProductos(controlador.buscarProductosPorNombre(
                            texto("Nombre o parte: ")));
                    case 0 -> seguir = false;
                    default -> System.out.println("Opción incorrecta.");
                }
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Dato inválido: " + e.getMessage());
            }
        }
    }

    private static void menuProductos() {
        boolean seguir = true;
        while (seguir) {
            System.out.println("\n--- PRODUCTOS ---");
            System.out.println("1. Registrar");
            System.out.println("2. Listar activos");
            System.out.println("3. Buscar por ID");
            System.out.println("4. Modificar (incluye rubro y proveedor)");
            System.out.println("5. Dar de baja (lógica)");
            System.out.println("6. Buscar por nombre");
            System.out.println("7. Buscar por nombre de rubro");
            System.out.println("8. Buscar por razón social del proveedor");
            System.out.println("9. Filtrar por ID de rubro");
            System.out.println("10. Filtrar por ID de proveedor");
            System.out.println("0. Volver");
            try {
                switch (entero("Opción: ")) {
                    case 1 -> registrarProducto();
                    case 2 -> mostrarProductos(controlador.listarProductos());
                    case 3 -> buscarProducto();
                    case 4 -> modificarProducto();
                    case 5 -> darDeBajaProducto();
                    case 6 -> mostrarProductos(controlador.buscarProductosPorNombre(texto("Nombre o parte del nombre: ")));
                    case 7 -> mostrarProductos(controlador.buscarPorNombreRubro(texto("Nombre del rubro (o parte): ")));
                    case 8 -> mostrarProductos(controlador.buscarPorNombreProveedor(texto("Razón social (o parte): ")));
                    case 9 -> {
                        mostrarRubros();
                        mostrarProductos(controlador.listarProductosPorRubro(entero("ID de rubro: ")));
                    }
                    case 10 -> {
                        mostrarProveedores();
                        mostrarProductos(controlador.listarProductosPorProveedor(entero("ID de proveedor: ")));
                    }
                    case 0 -> seguir = false;
                    default -> System.out.println("Opción incorrecta.");
                }
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Dato inválido: " + e.getMessage());
            } catch (SecurityException e) {
                System.out.println("Acceso denegado: " + e.getMessage());
            }
        }
    }

    private static void registrarProducto() throws SQLException {
        List<Rubro> rubros = controlador.listarRubros();
        List<Proveedor> proveedores = controlador.listarProveedores();
        if (rubros.isEmpty() || proveedores.isEmpty()) {
            System.out.println("Primero registrá al menos un rubro y un proveedor.");
            return;
        }
        String nombre = texto("Nombre: ");
        String descripcion = texto("Descripción: ");
        BigDecimal precio = decimal("Precio: ");
        int stock = entero("Stock: ");
        mostrarRubros();
        int rubroId = entero("ID del rubro: ");
        mostrarProveedores();
        int proveedorId = entero("ID del proveedor: ");
        controlador.registrarProducto(nombre, descripcion, precio, stock, rubroId, proveedorId);
        System.out.println("Producto registrado correctamente.");
    }

    private static void mostrarProductos(List<Producto> productos) {
        if (productos.isEmpty()) {
            System.out.println("No se encontraron productos activos.");
            return;
        }
        for (Producto p : productos) mostrarProducto(p);
    }

    private static void mostrarProducto(Producto p) {
        System.out.printf("[%d] %s | $%s | Stock: %d | Rubro: %s | Proveedor: %s%n",
                p.getId(), p.getNombre(), p.getPrecio().toPlainString(), p.getStock(),
                p.getRubro().getNombre(),
                p.getProveedor() == null ? "Sin asignar" : p.getProveedor().getRazonSocial());
        System.out.printf("    Descripción: %s | Alta: %s%n", p.getDescripcion(), p.getFechaAlta());
    }

    private static void buscarProducto() throws SQLException {
        Producto p = controlador.buscarProducto(entero("ID del producto: "));
        if (p == null) System.out.println("Producto inexistente o dado de baja.");
        else mostrarProducto(p);
    }

    private static void modificarProducto() throws SQLException {
        Producto p = controlador.buscarProducto(entero("ID a modificar: "));
        if (p == null) {
            System.out.println("Producto inexistente o dado de baja.");
            return;
        }
        mostrarProducto(p);
        String nombre = conservar(texto("Nuevo nombre [Enter conserva]: "), p.getNombre());
        String descripcion = texto("Nueva descripción [Enter conserva, - para borrar]: ");
        if (descripcion.isEmpty()) descripcion = p.getDescripcion();
        else if (descripcion.equals("-")) descripcion = "";
        String precioTexto = texto("Nuevo precio [Enter conserva]: ");
        BigDecimal precio = precioTexto.isBlank() ? p.getPrecio() : new BigDecimal(precioTexto);
        String stockTexto = texto("Nuevo stock [Enter conserva]: ");
        int stock = stockTexto.isBlank() ? p.getStock() : Integer.parseInt(stockTexto);
        mostrarRubros();
        String rubroTexto = texto("Nuevo ID de rubro [Enter conserva]: ");
        int rubroId = rubroTexto.isBlank() ? p.getRubro().getId() : Integer.parseInt(rubroTexto);
        mostrarProveedores();
        String proveedorTexto = texto("Nuevo ID de proveedor [Enter conserva]: ");
        int proveedorId;
        if (proveedorTexto.isBlank()) {
            if (p.getProveedor() == null) {
                System.out.println("Este producto anterior no tiene proveedor: ingresá un ID.");
                proveedorId = entero("ID del proveedor: ");
            } else proveedorId = p.getProveedor().getId();
        } else proveedorId = Integer.parseInt(proveedorTexto);
        controlador.modificarProducto(p.getId(), nombre, descripcion, precio, stock, rubroId, proveedorId);
        System.out.println("Producto modificado correctamente.");
    }

    private static void darDeBajaProducto() throws SQLException {
        int id = entero("ID del producto a dar de baja: ");
        Producto p = controlador.buscarProducto(id);
        if (p == null) { System.out.println("Producto inexistente o dado de baja."); return; }
        mostrarProducto(p);
        if (confirmar("Confirmar baja lógica (s/N): ")) {
            controlador.eliminarProducto(id);
            System.out.println("Producto dado de baja. Sigue guardado en PostgreSQL.");
        } else System.out.println("Operación cancelada.");
    }

    private static void menuRubros() {
        boolean seguir = true;
        while (seguir) {
            System.out.println("\n--- RUBROS ---");
            System.out.println("1. Registrar  2. Listar  3. Buscar ID  4. Modificar  5. Eliminar  0. Volver");
            try {
                switch (entero("Opción: ")) {
                    case 1 -> {
                        controlador.registrarRubro(texto("Nombre del rubro: "));
                        System.out.println("Rubro registrado.");
                    }
                    case 2 -> mostrarRubros();
                    case 3 -> {
                        Rubro r = controlador.buscarRubro(entero("ID: "));
                        if (r == null) System.out.println("Rubro inexistente.");
                        else System.out.printf("[%d] %s%n", r.getId(), r.getNombre());
                    }
                    case 4 -> modificarRubro();
                    case 5 -> eliminarRubro();
                    case 0 -> seguir = false;
                    default -> System.out.println("Opción incorrecta.");
                }
            } catch (SQLException e) { System.out.println("Error de base de datos: " + e.getMessage()); }
              catch (IllegalArgumentException e) { System.out.println("Dato inválido: " + e.getMessage()); }
        }
    }

    private static void mostrarRubros() throws SQLException {
        List<Rubro> rubros = controlador.listarRubros();
        if (rubros.isEmpty()) System.out.println("No hay rubros.");
        for (Rubro r : rubros) System.out.printf("[%d] %s%n", r.getId(), r.getNombre());
    }

    private static void modificarRubro() throws SQLException {
        int id = entero("ID a modificar: ");
        Rubro r = controlador.buscarRubro(id);
        if (r == null) { System.out.println("Rubro inexistente."); return; }
        String nombre = conservar(texto("Nombre nuevo [Enter conserva " + r.getNombre() + "]: "), r.getNombre());
        controlador.modificarRubro(id, nombre);
        System.out.println("Rubro modificado.");
    }

    private static void eliminarRubro() throws SQLException {
        int id = entero("ID a eliminar: ");
        Rubro r = controlador.buscarRubro(id);
        if (r == null) { System.out.println("Rubro inexistente."); return; }
        if (confirmar("Eliminar rubro '" + r.getNombre() + "' (s/N): ")) {
            controlador.eliminarRubro(id);
            System.out.println("Rubro eliminado.");
        }
    }

    private static void menuProveedores() {
        boolean seguir = true;
        while (seguir) {
            System.out.println("\n--- PROVEEDORES ---");
            System.out.println("1. Registrar  2. Listar  3. Buscar ID  4. Modificar  5. Eliminar  0. Volver");
            try {
                switch (entero("Opción: ")) {
                    case 1 -> registrarProveedor();
                    case 2 -> mostrarProveedores();
                    case 3 -> {
                        Proveedor pr = controlador.buscarProveedor(entero("ID: "));
                        if (pr == null) System.out.println("Proveedor inexistente.");
                        else mostrarProveedor(pr);
                    }
                    case 4 -> modificarProveedor();
                    case 5 -> eliminarProveedor();
                    case 0 -> seguir = false;
                    default -> System.out.println("Opción incorrecta.");
                }
            } catch (SQLException e) { System.out.println("Error de base de datos: " + e.getMessage()); }
              catch (IllegalArgumentException e) { System.out.println("Dato inválido: " + e.getMessage()); }
        }
    }

    private static void registrarProveedor() throws SQLException {
        String razon = texto("Razón social: ");
        String domicilio = texto("Domicilio: ");
        String telefono = texto("Teléfono: ");
        controlador.registrarProveedor(razon, domicilio, telefono);
        System.out.println("Proveedor registrado.");
    }

    private static void mostrarProveedores() throws SQLException {
        List<Proveedor> proveedores = controlador.listarProveedores();
        if (proveedores.isEmpty()) System.out.println("No hay proveedores.");
        for (Proveedor pr : proveedores) mostrarProveedor(pr);
    }

    private static void mostrarProveedor(Proveedor pr) {
        System.out.printf("[%d] %s | Domicilio: %s | Teléfono: %s%n",
                pr.getId(), pr.getRazonSocial(), pr.getDomicilio(), pr.getTelefono());
    }

    private static void modificarProveedor() throws SQLException {
        int id = entero("ID a modificar: ");
        Proveedor pr = controlador.buscarProveedor(id);
        if (pr == null) { System.out.println("Proveedor inexistente."); return; }
        String razon = conservar(texto("Razón social [Enter conserva " + pr.getRazonSocial() + "]: "), pr.getRazonSocial());
        String domicilio = conservar(texto("Domicilio [Enter conserva]: "), pr.getDomicilio());
        String telefono = conservar(texto("Teléfono [Enter conserva]: "), pr.getTelefono());
        controlador.modificarProveedor(id, razon, domicilio, telefono);
        System.out.println("Proveedor modificado.");
    }

    private static void eliminarProveedor() throws SQLException {
        int id = entero("ID a eliminar: ");
        Proveedor pr = controlador.buscarProveedor(id);
        if (pr == null) { System.out.println("Proveedor inexistente."); return; }
        if (confirmar("Eliminar proveedor '" + pr.getRazonSocial() + "' (s/N): ")) {
            controlador.eliminarProveedor(id);
            System.out.println("Proveedor eliminado.");
        }
    }

    /** Actividades adicionales de la segunda guía: ABM y rol invitado. */
    private static void menuRoles() {
        boolean seguir = true;
        while (seguir) {
            System.out.println("\n--- PERSONAS Y ROLES (ADMINISTRADOR) ---");
            System.out.println("1. Registrar  2. Listar  3. Buscar ID  4. Modificar  5. Eliminar  0. Volver");
            try {
                switch (entero("Opción: ")) {
                    case 1 -> registrarRol();
                    case 2 -> mostrarRoles();
                    case 3 -> {
                        Rol r = controladorRoles.buscar(entero("ID: "));
                        if (r == null) System.out.println("Persona inexistente.");
                        else mostrarRol(r);
                    }
                    case 4 -> modificarRol();
                    case 5 -> eliminarRol();
                    case 0 -> seguir = false;
                    default -> System.out.println("Opción incorrecta.");
                }
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            } catch (SecurityException e) {
                System.out.println("Acceso denegado: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Dato inválido: " + e.getMessage());
            }
        }
    }

    private static void mostrarRoles() throws SQLException {
        List<Rol> personas = controladorRoles.listar();
        if (personas.isEmpty()) System.out.println("No hay personas registradas.");
        for (Rol r : personas) mostrarRol(r);
    }

    private static void mostrarRol(Rol r) {
        // Se muestra la persona y su rol, nunca el PIN.
        System.out.printf("[%d] %s | %s%n", r.getId(), r.getNombreCompleto(), r.getNombreRol());
    }

    private static void registrarRol() throws SQLException {
        String nombre = texto("Nombre: ");
        String apellido = texto("Apellido: ");
        String rol = texto("Rol (a = administrador, e = empleado, i = invitado): ").trim();
        String pin = texto("PIN nuevo (4 dígitos): ").trim();
        controladorRoles.registrar(pin, apellido, nombre, rol);
        System.out.println("Persona registrada.");
    }

    private static void modificarRol() throws SQLException {
        int id = entero("ID a modificar: ");
        Rol r = controladorRoles.buscar(id);
        if (r == null) { System.out.println("Persona inexistente."); return; }
        mostrarRol(r);
        String nombre = conservar(texto("Nombre [Enter conserva]: "), r.getNombre());
        String apellido = conservar(texto("Apellido [Enter conserva]: "), r.getApellido());
        String rol = conservar(texto("Rol [Enter conserva " + r.getRol() + "]: ").trim(), r.getRol());
        String pin = texto("PIN nuevo [Enter conserva; no se muestra el actual]: ").trim();
        controladorRoles.modificar(id, pin, apellido, nombre, rol);
        System.out.println("Persona modificada.");
    }

    private static void eliminarRol() throws SQLException {
        int id = entero("ID a eliminar: ");
        Rol r = controladorRoles.buscar(id);
        if (r == null) { System.out.println("Persona inexistente."); return; }
        mostrarRol(r);
        if (confirmar("¿Eliminar a esta persona? (s/N): ")) {
            controladorRoles.eliminar(id);
            System.out.println("Persona eliminada.");
        }
    }

    private static String conservar(String valor, String anterior) {
        return valor.isBlank() ? anterior : valor;
    }
    private static boolean confirmar(String mensaje) {
        return texto(mensaje).trim().equalsIgnoreCase("s");
    }
    private static String texto(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine();
    }
    private static int entero(String mensaje) {
        try { return Integer.parseInt(texto(mensaje).trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Ingresá un número entero válido."); }
    }
    private static BigDecimal decimal(String mensaje) {
        try { return new BigDecimal(texto(mensaje).trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Ingresá un precio numérico válido (ej.: 1250.50)."); }
    }
}
