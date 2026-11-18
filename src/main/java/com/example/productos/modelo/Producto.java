package com.example.productos.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int stock;
    private boolean activo;
    private LocalDate fechaAlta;
    private Rubro rubro;
    private Proveedor proveedor;

    public Producto() {
        this.activo = true;
        this.fechaAlta = LocalDate.now();
    }

    public Producto(String nombre, String descripcion, BigDecimal precio, int stock, Rubro rubro) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.rubro = rubro;
    }

    public Producto(String nombre, String descripcion, BigDecimal precio, int stock, Rubro rubro, Proveedor proveedor) {
        this(nombre, descripcion, precio, stock, rubro);
        this.proveedor = proveedor;
    }

    public boolean tieneStock() {
        return stock > 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }

    public Rubro getRubro() { return rubro; }
    public void setRubro(Rubro rubro) { this.rubro = rubro; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
}