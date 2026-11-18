package com.example.productos.presentacion;
import java.awt.Frame;
/** Especialización del CRUD de catálogos: proveedores. */
public final class VentanaProveedores extends VentanaCatalogoCRUD {
    public VentanaProveedores(Frame principal, SesionSwing sesion) {
        super(principal, sesion, Tipo.PROVEEDORES);
    }
}
