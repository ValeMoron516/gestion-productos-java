package com.example.productos.modelo;

/** Entidad proveedora de uno o más productos. */
public class Proveedor {
    private int id;
    private String razonSocial;
    private String domicilio;
    private String telefono;

    public Proveedor() { }

    public Proveedor(int id, String razonSocial, String domicilio, String telefono) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.domicilio = domicilio;
        this.telefono = telefono;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
