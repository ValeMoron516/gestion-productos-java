package com.example.productos.persistencia;

import com.example.productos.modelo.Proveedor;
import com.example.productos.persistencia.contratos.IProveedorRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorRepository implements IProveedorRepository {
    @Override
    public void guardar(Proveedor proveedor) throws SQLException {
        String sql = "INSERT INTO proveedores (razon_social, domicilio, telefono) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, proveedor.getRazonSocial());
            ps.setString(2, proveedor.getDomicilio());
            ps.setString(3, proveedor.getTelefono());
            ps.executeUpdate();
        }
    }

    @Override
    public Proveedor buscarPorId(int id) throws SQLException {
        String sql = "SELECT id_prov, razon_social, domicilio, telefono FROM proveedores WHERE id_prov = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Proveedor> listar() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT id_prov, razon_social, domicilio, telefono FROM proveedores ORDER BY id_prov";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) proveedores.add(mapear(rs));
        }
        return proveedores;
    }

    @Override
    public boolean modificar(Proveedor proveedor) throws SQLException {
        String sql = "UPDATE proveedores SET razon_social = ?, domicilio = ?, telefono = ? WHERE id_prov = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, proveedor.getRazonSocial());
            ps.setString(2, proveedor.getDomicilio());
            ps.setString(3, proveedor.getTelefono());
            ps.setInt(4, proveedor.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM proveedores WHERE id_prov = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        return new Proveedor(rs.getInt("id_prov"), rs.getString("razon_social"),
                rs.getString("domicilio"), rs.getString("telefono"));
    }
}
