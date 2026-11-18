package com.example.productos.presentacion;
import java.awt.Frame;
/** Especialización del CRUD de catálogos: rubros. */
public final class VentanaRubros extends VentanaCatalogoCRUD {
    public VentanaRubros(Frame principal, SesionSwing sesion) {
        super(principal, sesion, Tipo.RUBROS);
    }
}
