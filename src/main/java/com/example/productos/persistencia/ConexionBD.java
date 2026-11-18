package com.example.productos.persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** Lee .env directamente, sin volcar la contraseña al comando de ejecución de VS Code. */
public final class ConexionBD {
    private ConexionBD() { }

    public static Connection conectar() throws SQLException {
        Map<String, String> archivo = leerEnv();
        // Variables de entorno del sistema tienen prioridad sobre .env.
        String url = configurar("DB_URL", archivo,
                "jdbc:postgresql://localhost:5432/gestion_productos");
        String usuario = configurar("DB_USER", archivo, "postgres");
        String clave = configurar("DB_PASSWORD", archivo, null);
        if (clave == null || clave.isBlank()) {
            throw new SQLException("Falta DB_PASSWORD. Configurá el archivo .env en la raíz del proyecto.");
        }
        return DriverManager.getConnection(url, usuario, clave);
    }

    private static String configurar(String key, Map<String, String> archivo, String valorDefault) {
        String variable = System.getenv(key);
        return variable == null ? archivo.getOrDefault(key, valorDefault) : variable;
    }

    private static Map<String, String> leerEnv() throws SQLException {
        Map<String, String> config = new HashMap<>();
        Path archivo = Path.of(".env");
        if (!Files.exists(archivo)) return config;
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                String texto = linea.trim();
                if (texto.isEmpty() || texto.startsWith("#")) continue;
                int i = texto.indexOf('=');
                if (i > 0) config.put(texto.substring(0, i).trim(), texto.substring(i + 1));
            }
            return config;
        } catch (IOException e) {
            throw new SQLException("No se pudo leer el archivo .env", e);
        }
    }
}
