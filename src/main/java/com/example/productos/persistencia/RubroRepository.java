package com.example.productos.persistencia;

import com.example.productos.modelo.Rubro;
import com.example.productos.persistencia.contratos.IRubroRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RubroRepository implements IRubroRepository {
    @Override
    public void guardar(Rubro rubro) throws SQLException {
        String sql = "INSERT INTO rubros (nombre) VALUES (?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rubro.getNombre());
            ps.executeUpdate();
        }
    }

    @Override
    public Rubro buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM rubros WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Rubro(rs.getInt("id"), rs.getString("nombre")) : null;
            }
        }
    }

    @Override
    public List<Rubro> listar() throws SQLException {
        List<Rubro> rubros = new ArrayList<>();
        String sql = "SELECT id, nombre FROM rubros ORDER BY id";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rubros.add(new Rubro(rs.getInt("id"), rs.getString("nombre")));
        }
        return rubros;
    }

    @Override
    public boolean modificar(Rubro rubro) throws SQLException {
        String sql = "UPDATE rubros SET nombre = ? WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rubro.getNombre());
            ps.setInt(2, rubro.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM rubros WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
