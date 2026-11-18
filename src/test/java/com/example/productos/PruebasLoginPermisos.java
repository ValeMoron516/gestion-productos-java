package com.example.productos;

import com.example.productos.modelo.*;
import com.example.productos.persistencia.contratos.*;
import com.example.productos.servicio.*;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

/** Reglas de login y autorización, sin PostgreSQL ni JUnit. */
public class PruebasLoginPermisos {
    private static int checks = 0;
    private static int accesos = 0;

    public static void main(String[] args) throws Exception {
        RepoRoles roles = new RepoRoles();
        roles.guardar("1234", "Pérez", "Ana", "e");
        roles.guardar("5678", "Gómez", "Luis", "a");
        roles.guardar("0042", "Díaz", "Carla", "e");
        roles.guardar("2468", "Prueba", "Invitado", "i");
        LoginService login = new LoginService(roles);

        Rol empleado = login.ingresar("1234");
        Rol admin = login.ingresar("5678");
        Rol invitado = login.ingresar("2468");
        comprobar(empleado.getNombreCompleto().equals("Ana Pérez"), "Recupera persona desde repo");
        comprobar(admin.getNombreRol().equals("Administrador"), "Admin reconocido");
        comprobar(invitado.esInvitado(), "Invitado reconocido");
        comprobar(login.ingresar("0042").getNombre().equals("Carla"), "Preserva cero inicial");
        int antes = roles.busquedas;
        ilegal(() -> login.ingresar("12"), "PIN corto");
        ilegal(() -> login.ingresar("abcd"), "PIN con letras");
        ilegal(() -> login.ingresar(""), "PIN vacío");
        comprobar(roles.busquedas == antes, "PIN inválido no consulta repositorio");
        ilegal(() -> login.ingresar("9999"), "PIN desconocido");

        PermisosService pe = new PermisosService(empleado);
        PermisosService pa = new PermisosService(admin);
        PermisosService pi = new PermisosService(invitado);
        pe.exigirSesion(); pa.exigirSesion(); pi.exigirLectura();
        comprobar(true, "Sesión y lectura válidas");
        denegado(pe::exigirAdministrador, "Empleado no es admin");
        denegado(pi::exigirSesion, "Invitado no puede escribir");
        denegado(pi::exigirAdministrador, "Invitado no es admin");

        IProductoRepository rp = falso(IProductoRepository.class);
        IRubroRepository rr = falso(IRubroRepository.class);
        IProveedorRepository rv = falso(IProveedorRepository.class);
        ProductoService prodE = new ProductoService(rp, rr, rv, pe);
        ProductoService prodI = new ProductoService(rp, rr, rv, pi);
        ProductoService prodA = new ProductoService(rp, rr, rv, pa);
        RubroService rubE = new RubroService(rr, pe);
        RubroService rubI = new RubroService(rr, pi);
        RubroService rubA = new RubroService(rr, pa);
        ProveedorService provE = new ProveedorService(rv, pe);
        ProveedorService provI = new ProveedorService(rv, pi);
        ProveedorService provA = new ProveedorService(rv, pa);

        comprobar(prodE.listarProductos().isEmpty(), "Empleado lista productos");
        comprobar(prodI.listarProductos().isEmpty(), "Invitado lista productos");
        comprobar(prodA.listarProductos().isEmpty(), "Admin lista productos");
        comprobar(rubE.listar().isEmpty() && provE.listar().isEmpty(), "Empleado lista entidades");
        comprobar(rubI.listar().isEmpty() && provI.listar().isEmpty(), "Invitado lista entidades");
        comprobar(rubA.listar().isEmpty() && provA.listar().isEmpty(), "Admin lista entidades");

        // Tercera guía Swing: invitados también pueden buscar, sin permisos de escritura.
        comprobar(prodI.buscarPorId(1) == null, "Invitado busca ID de producto");
        comprobar(prodI.buscarPorNombre("te").isEmpty(), "Invitado busca nombre de producto");
        comprobar(prodI.buscarPorNombreRubro("aud").isEmpty(), "Invitado busca por rubro");
        comprobar(prodI.buscarPorNombreProveedor("SA").isEmpty(), "Invitado busca por proveedor");
        comprobar(prodI.listarPorRubro(1).isEmpty(), "Invitado filtra por rubro");
        comprobar(prodI.listarPorProveedor(1).isEmpty(), "Invitado filtra por proveedor");
        comprobar(rubE.buscar(1) != null, "Empleado busca rubro");
        comprobar(rubI.buscar(1) != null, "Invitado busca rubro");
        comprobar(provE.buscar(1) != null, "Empleado busca proveedor");
        comprobar(provI.buscar(1) != null, "Invitado busca proveedor");
        int llamadas = accesos;
        denegado(() -> prodI.registrarProducto("Nuevo", "", BigDecimal.ONE, 1, 1, 1), "Invitado registra producto");
        denegado(() -> prodI.modificarProducto(1,"Nuevo","", BigDecimal.ONE,1,1,1), "Invitado modifica producto");
        denegado(() -> prodI.eliminarProducto(1), "Invitado da baja");
        denegado(() -> rubE.registrar("Nuevo"), "Empleado crea rubro");
        denegado(() -> rubE.modificar(1,"Nuevo"), "Empleado modifica rubro");
        denegado(() -> rubE.eliminar(1), "Empleado elimina rubro");
        denegado(() -> provE.registrar("SA", "", ""), "Empleado registra proveedor");
        denegado(() -> provE.modificar(1,"SA","",""), "Empleado modifica proveedor");
        denegado(() -> provE.eliminar(1), "Empleado elimina proveedor");
        denegado(() -> rubI.registrar("Nuevo"), "Invitado crea rubro");
        denegado(() -> provI.registrar("SA", "", ""), "Invitado registra proveedor");
        comprobar(accesos == llamadas, "Operaciones prohibidas rechazan ANTES de llamar al repositorio");

        prodE.registrarProducto("Teclado", "USB", new BigDecimal("10.00"), 1, 1, 1);
        prodA.registrarProducto("Mouse", "USB", new BigDecimal("12.00"), 2, 1, 1);
        rubA.registrar("Audio");
        provA.registrar("SA", "A", "B");
        comprobar(accesos > llamadas, "Acciones permitidas sí llegan al repositorio");

        RolService rolesA = new RolService(roles, pa);
        RolService rolesE = new RolService(roles, pe);
        RolService rolesI = new RolService(roles, pi);
        denegado(rolesE::listar, "Empleado no lista roles");
        denegado(rolesI::listar, "Invitado no lista roles");
        denegado(() -> rolesE.registrar("8888", "A", "B", "e"), "Empleado no registra roles");
        denegado(() -> rolesE.modificar(3,null,"A","B","a"), "Empleado no modifica roles");
        denegado(() -> rolesE.eliminar(3), "Empleado no elimina roles");
        comprobar(rolesA.listar().size() == 4, "Admin ve personas, sin PIN");
        rolesA.registrar("8888", "Nuevo", "Invitado", "i");
        comprobar(rolesA.buscar(5).esInvitado(), "Admin registra invitado");
        ilegal(() -> rolesA.registrar("88", "Nuevo", "Invitado", "i"), "PIN corto en ABM");
        ilegal(() -> rolesA.registrar("8888", "Nuevo", "Invitado", "i"), "PIN duplicado");
        ilegal(() -> rolesA.registrar("9998", "Nuevo", "Invitado", "x"), "Rol no permitido");
        rolesA.modificar(5, "9998", "Nuevo", "Invitado", "e");
        comprobar(rolesA.buscar(5).esEmpleado(), "Admin cambia rol");
        comprobar(login.ingresar("9998").esEmpleado(), "PIN actualizado");
        rolesA.eliminar(5);
        comprobar(rolesA.buscar(5) == null, "Admin elimina rol");
        ilegal(() -> rolesA.modificar(admin.getId(), null, "A", "B", "e"), "Admin no se quita a sí mismo permisos");
        ilegal(() -> rolesA.eliminar(admin.getId()), "Admin no elimina su propia cuenta");
        System.out.println("Correcto: " + checks + " verificaciones de login y permisos sin PostgreSQL.");
    }

    private static <T> T falso(Class<T> interfaz) {
        Object o = Proxy.newProxyInstance(interfaz.getClassLoader(), new Class<?>[] {interfaz}, (proxy, metodo, args) -> {
            accesos++;
            if (metodo.getName().equals("buscarPorId")) {
                if (interfaz == IRubroRepository.class) return new Rubro((int)args[0], "Audio");
                if (interfaz == IProveedorRepository.class) return new Proveedor((int)args[0], "SA", "", "");
                return null;
            }
            if (List.class.isAssignableFrom(metodo.getReturnType())) return List.of();
            if (metodo.getReturnType() == boolean.class) return true;
            return null;
        });
        return interfaz.cast(o);
    }

    private static void comprobar(boolean condicion, String texto) {
        if (!condicion) throw new AssertionError(texto);
        checks++;
    }
    @FunctionalInterface interface Accion { void ejecutar() throws Exception; }
    private static void ilegal(Accion a, String texto) throws Exception {
        try { a.ejecutar(); throw new AssertionError("No rechazó: " + texto); }
        catch (IllegalArgumentException esperado) { checks++; }
    }
    private static void denegado(Accion a, String texto) throws Exception {
        try { a.ejecutar(); throw new AssertionError("No denegó: " + texto); }
        catch (SecurityException esperado) { checks++; }
    }

    private static class RepoRoles implements IRolRepository {
        private final Map<Integer, Rol> datos = new LinkedHashMap<>();
        private final Map<String, Integer> claves = new HashMap<>();
        private int next = 1;
        int busquedas;
        public Rol buscarPorPin(String pin) { busquedas++; Integer id = claves.get(pin); return id == null ? null : datos.get(id); }
        public Rol buscarPorId(int id) { return datos.get(id); }
        public List<Rol> listar() { return new ArrayList<>(datos.values()); }
        public void guardar(String pin, String apellido, String nombre, String rol) throws SQLException {
            if (claves.containsKey(pin)) throw new SQLException("PIN duplicado", "23505");
            int id = next++;
            datos.put(id, new Rol(id, apellido, nombre, rol));
            claves.put(pin, id);
        }
        public boolean modificar(int id, String pin, String apellido, String nombre, String rol) throws SQLException {
            if (!datos.containsKey(id)) return false;
            if (pin != null && claves.containsKey(pin) && claves.get(pin) != id) throw new SQLException("PIN duplicado", "23505");
            if (pin != null) { claves.entrySet().removeIf(e -> e.getValue() == id); claves.put(pin, id); }
            datos.put(id, new Rol(id, apellido, nombre, rol));
            return true;
        }
        public boolean eliminar(int id) {
            if (datos.remove(id) == null) return false;
            claves.entrySet().removeIf(e -> e.getValue() == id);
            return true;
        }
    }
}
