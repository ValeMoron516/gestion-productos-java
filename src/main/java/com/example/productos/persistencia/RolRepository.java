package com.example.productos.persistencia;

import com.example.productos.modelo.Rol;
import com.example.productos.persistencia.contratos.IRolRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC; los PIN nunca se seleccionan en las operaciones de listado. */
public class RolRepository implements IRolRepository {
    @Override
    public Rol buscarPorPin(String pin) throws SQLException {
        String sql = "SELECT id, apellido, nombre, rol FROM roles WHERE pin = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pin);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Rol buscarPorId(int id) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, apellido, nombre, rol FROM roles WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Rol> listar() throws SQLException {
        List<Rol> personas = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, apellido, nombre, rol FROM roles ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) personas.add(mapear(rs));
        }
        return personas;
    }

    @Override
    public void guardar(String pin, String apellido, String nombre, String rol) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO roles (pin, apellido, nombre, rol) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, pin);
            ps.setString(2, apellido);
            ps.setString(3, nombre);
            ps.setString(4, rol);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean modificar(int id, String nuevoPin, String apellido, String nombre, String rol)
            throws SQLException {
        // PIN en blanco equivale a conservarlo, no a sobrescribirlo con cadena vacía.
        String sql = "UPDATE roles SET pin = COALESCE(?, pin), apellido = ?, nombre = ?, rol = ? WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoPin);
            ps.setString(2, apellido);
            ps.setString(3, nombre);
            ps.setString(4, rol);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement("DELETE FROM roles WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Rol mapear(ResultSet rs) throws SQLException {
        return new Rol(rs.getInt("id"), rs.getString("apellido"),
                rs.getString("nombre"), rs.getString("rol").trim());
    }
}
