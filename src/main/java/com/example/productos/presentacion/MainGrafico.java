package com.example.productos.presentacion;

import javax.swing.SwingUtilities;

/** Entrada gráfica: la clase Main sigue disponible para usar la consola. */
public final class MainGrafico {
    private MainGrafico() { }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EstiloSwing.instalar();
            new VentanaLogin().setVisible(true);
        });
    }
}
