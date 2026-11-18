package com.example.productos.servicio;

import com.example.productos.modelo.Rol;
import com.example.productos.persistencia.contratos.IRolRepository;
import java.sql.SQLException;
import java.util.Objects;

/** Autenticación por PIN de cuatro caracteres, incluyendo ceros iniciales. */
public class LoginService {
    private final IRolRepository repository;

    public LoginService(IRolRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Rol ingresar(String pin) throws SQLException {
        if (pin == null || !pin.matches("[0-9]{4}")) {
            throw new IllegalArgumentException("El PIN debe contener exactamente 4 dígitos");
        }
        Rol persona = repository.buscarPorPin(pin);
        if (persona == null) throw new IllegalArgumentException("PIN incorrecto");
        return persona;
    }
}
