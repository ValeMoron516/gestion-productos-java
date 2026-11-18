package com.example.productos.presentacion;

import com.example.productos.modelo.Producto;
import com.example.productos.modelo.Proveedor;
import com.example.productos.modelo.Rubro;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** ABM de productos sobre servicios existentes; no contiene SQL. */
public final class VentanaProductosCRUD extends JDialog {
    private final SesionSwing sesion;
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JTextField id = EstiloSwing.campo(8);
    private final JTextField nombre = EstiloSwing.campo(22);
    private final JTextField descripcion = EstiloSwing.campo(22);
    private final JTextField precio = EstiloSwing.campo(14);
    private final JTextField stock = EstiloSwing.campo(12);
    private final JCheckBox activo = new JCheckBox("Activo (solo se modifica si está marcado)", true);
    private final JComboBox<Opcion> rubros = new JComboBox<>();
    private final JComboBox<Opcion> proveedores = new JComboBox<>();
    private final JTextField buscar = EstiloSwing.campo(15);
    private final JRadioButton porId = new JRadioButton("ID", true);
    private final JRadioButton porNombre = new JRadioButton("Nombre");
    private final JButton guardar = EstiloSwing.boton("Guardar / Modificar");
    private final JButton baja = EstiloSwing.boton("Dar de baja", EstiloSwing.ROJO);
    private final JLabel estado = EstiloSwing.etiqueta("Consultando PostgreSQL...");
    private boolean poblando;

    record Opcion(int id, String nombre) {
        @Override public String toString() { return nombre; }
    }
    record Catalogos(List<Rubro> rubros, List<Proveedor> proveedores) { }

    public VentanaProductosCRUD(Frame padre, SesionSwing sesion) {
        super(padre, "Gestión de productos", true);
        this.sesion = sesion;
        setSize(1260, 720);
        setMinimumSize(new Dimension(1020, 600));
        setLocationRelativeTo(padre);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(EstiloSwing.cabecera("Productos", sesion.persona().getNombreCompleto() + " · "
                + sesion.persona().getNombreRol() + " · Los productos dados de baja no se muestran"), BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new String[] {"ID", "Nombre", "Descripción", "Precio", "Stock", "Activo", "Rubro", "Proveedor"}, 0) {
            @Override public boolean isCellEditable(int f, int c) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return switch (col) {
                    case 0, 4 -> Integer.class;
                    case 3 -> BigDecimal.class;
                    case 5 -> Boolean.class;
                    default -> String.class;
                };
            }
        };
        tabla = EstiloSwing.tabla(modelo);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {54, 150, 210, 110, 75, 65, 160, 185};
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !poblando) seleccionarFila();
        });

        JPanel area = EstiloSwing.panel();
        area.setLayout(new BorderLayout(12, 12));
        area.setBorder(new EmptyBorder(16, 18, 12, 18));
        area.add(buscador(), BorderLayout.NORTH);

        JScrollPane tablaScroll = new JScrollPane(tabla);
        tablaScroll.setBorder(BorderFactory.createTitledBorder("Listado de productos activos · ordená haciendo clic en las columnas"));
        JPanel formulario = construirFormulario();
        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formulario, tablaScroll);
        division.setContinuousLayout(true);
        division.setResizeWeight(0.33);
        division.setDividerLocation(365);
        division.setBorder(null);
        area.add(division, BorderLayout.CENTER);
        add(area, BorderLayout.CENTER);

        JPanel pie = EstiloSwing.panel();
        pie.setLayout(new BorderLayout());
        pie.setBorder(new EmptyBorder(4, 24, 13, 24));
        estado.setForeground(EstiloSwing.SUAVE);
        pie.add(estado, BorderLayout.WEST);
        JButton volver = EstiloSwing.boton("Volver al menú", EstiloSwing.OSCURO);
        volver.addActionListener(e -> dispose());
        pie.add(volver, BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);

        activo.setOpaque(false);
        activo.addActionListener(e -> permisosFormulario());
        permisosFormulario();
        cargarCatalogos();
    }

    private JPanel buscador() {
        JPanel p = EstiloSwing.panel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(porId); grupo.add(porNombre);
        porId.setOpaque(false); porNombre.setOpaque(false);
        p.add(EstiloSwing.etiqueta("Buscar producto:"));
        p.add(porId);
        p.add(porNombre);
        p.add(buscar);
        JButton buscarBtn = EstiloSwing.boton("Buscar");
        JButton listarBtn = EstiloSwing.boton("Listar / Actualizar", EstiloSwing.OSCURO);
        buscarBtn.addActionListener(e -> buscar());
        buscar.addActionListener(e -> buscar());
        listarBtn.addActionListener(e -> cargarTabla());
        p.add(buscarBtn);
        p.add(listarBtn);
        return p;
    }

    private JPanel construirFormulario() {
        JPanel form = EstiloSwing.formulario();
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Datos del producto"), new EmptyBorder(8, 7, 8, 7)));
        var g = EstiloSwing.rejilla();
        id.setEditable(false);
        id.setBackground(new Color(231, 237, 243));
        EstiloSwing.fila(form, g, 0, "ID:", id);
        EstiloSwing.fila(form, g, 1, "Nombre:", nombre);
        EstiloSwing.fila(form, g, 2, "Descripción:", descripcion);
        EstiloSwing.fila(form, g, 3, "Precio:", precio);
        EstiloSwing.fila(form, g, 4, "Stock:", stock);
        EstiloSwing.fila(form, g, 5, "Rubro:", rubros);
        EstiloSwing.fila(form, g, 6, "Proveedor:", proveedores);
        g.gridx = 0; g.gridy = 7; g.gridwidth = 2;
        g.anchor = GridBagConstraints.CENTER; g.fill = GridBagConstraints.NONE;
        form.add(activo, g);
        JPanel botones = EstiloSwing.panel();
        botones.setLayout(new GridLayout(3, 1, 0, 9));
        JButton nuevo = EstiloSwing.boton("Nuevo / Limpiar", EstiloSwing.OSCURO);
        nuevo.addActionListener(e -> limpiar());
        guardar.addActionListener(e -> guardar());
        baja.addActionListener(e -> darDeBaja());
        botones.add(nuevo);
        botones.add(guardar);
        botones.add(baja);
        g.gridy = 8; g.gridx = 0; g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        form.add(botones, g);
        JLabel ayuda = new JLabel("<html><small>Seleccioná una fila para cargar los datos.<br>La baja es lógica; conserva el registro en PostgreSQL.</small></html>");
        ayuda.setForeground(EstiloSwing.SUAVE);
        g.gridy = 9;
        form.add(ayuda, g);
        g.gridy = 10; g.weighty = 1; g.fill = GridBagConstraints.VERTICAL;
        form.add(Box.createVerticalGlue(), g);
        return form;
    }

    private void permisosFormulario() {
        boolean edicion = sesion.puedeEditarProductos() && activo.isSelected();
        for (JTextField campo : new JTextField[]{nombre, descripcion, precio, stock}) campo.setEditable(edicion);
        rubros.setEnabled(edicion); proveedores.setEnabled(edicion);
        activo.setEnabled(sesion.puedeEditarProductos());
        guardar.setEnabled(edicion);
        baja.setEnabled(edicion && !id.getText().isBlank());
    }

    private void cargarCatalogos() {
        EstiloSwing.ejecutar(getRootPane(), () -> new Catalogos(
                sesion.catalogo().listarRubros(), sesion.catalogo().listarProveedores()), catalogos -> {
            poblando = true;
            rubros.removeAllItems();
            proveedores.removeAllItems();
            rubros.addItem(new Opcion(0, "— Seleccionar rubro —"));
            proveedores.addItem(new Opcion(0, "— Seleccionar proveedor —"));
            for (Rubro r : catalogos.rubros()) rubros.addItem(new Opcion(r.getId(), r.getNombre()));
            for (Proveedor p : catalogos.proveedores())
                proveedores.addItem(new Opcion(p.getId(), p.getRazonSocial()));
            poblando = false;
            limpiar();
            cargarTabla();
        });
    }

    private void cargarTabla() {
        EstiloSwing.ejecutar(getRootPane(), () -> sesion.catalogo().listarProductos(),
                lista -> mostrarLista(lista));
    }

    private void mostrarLista(List<Producto> lista) {
        poblando = true;
        modelo.setRowCount(0);
        for (Producto p : lista) modelo.addRow(new Object[]{p.getId(), p.getNombre(),
                p.getDescripcion(), p.getPrecio(), p.getStock(), p.isActivo(),
                p.getRubro() == null ? "Sin rubro" : p.getRubro().getNombre(),
                p.getProveedor() == null ? "Sin asignar" : p.getProveedor().getRazonSocial()});
        tabla.clearSelection();
        poblando = false;
        estado.setText(lista.size() + " producto(s) activo(s) · Seleccioná una fila para editar o consultar.");
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        int idSeleccionado = (Integer) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        EstiloSwing.ejecutar(getRootPane(), () -> sesion.catalogo().buscarProducto(idSeleccionado), p -> {
            if (p == null) {
                EstiloSwing.informacion(this, "El producto seleccionado ya no está activo.");
                cargarTabla();
            } else completar(p);
        });
    }

    private void completar(Producto p) {
        id.setText(String.valueOf(p.getId()));
        nombre.setText(p.getNombre());
        descripcion.setText(p.getDescripcion() == null ? "" : p.getDescripcion());
        precio.setText(p.getPrecio().toPlainString());
        stock.setText(String.valueOf(p.getStock()));
        activo.setSelected(p.isActivo());
        elegir(rubros, p.getRubro() == null ? 0 : p.getRubro().getId());
        elegir(proveedores, p.getProveedor() == null ? 0 : p.getProveedor().getId());
        permisosFormulario();
    }

    private void elegir(JComboBox<Opcion> combo, int idElegido) {
        combo.setSelectedIndex(0);
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id() == idElegido) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void limpiar() {
        tabla.clearSelection();
        id.setText(""); nombre.setText(""); descripcion.setText("");
        precio.setText(""); stock.setText("");
        if (rubros.getItemCount() > 0) rubros.setSelectedIndex(0);
        if (proveedores.getItemCount() > 0) proveedores.setSelectedIndex(0);
        activo.setSelected(true);
        permisosFormulario();
    }

    private void buscar() {
        String texto = buscar.getText().trim();
        if (texto.isEmpty()) {
            EstiloSwing.informacion(this, "Ingresá un ID o parte del nombre para buscar.");
            return;
        }
        if (porId.isSelected()) {
            int numero;
            try { numero = Integer.parseInt(texto); }
            catch (NumberFormatException ex) {
                EstiloSwing.informacion(this, "El ID debe ser un número entero.");
                return;
            }
            EstiloSwing.ejecutar(getRootPane(), () -> sesion.catalogo().buscarProducto(numero), p -> {
                mostrarLista(p == null ? List.of() : List.of(p));
                if (p == null) EstiloSwing.informacion(this, "No existe un producto activo con ese ID.");
                else completar(p);
            });
        } else {
            EstiloSwing.ejecutar(getRootPane(), () -> sesion.catalogo().buscarProductosPorNombre(texto),
                    this::mostrarLista);
        }
    }

    private int seleccion(JComboBox<Opcion> combo, String etiqueta) {
        Opcion opcion = (Opcion) combo.getSelectedItem();
        if (opcion == null || opcion.id() < 1)
            throw new IllegalArgumentException("Seleccioná un " + etiqueta + " válido.");
        return opcion.id();
    }

    private void guardar() {
        if (!sesion.puedeEditarProductos() || !activo.isSelected()) {
            EstiloSwing.error(this, new SecurityException("No se permite modificar productos inactivos ni operar como invitado."));
            return;
        }
        final String n = nombre.getText();
        final String d = descripcion.getText();
        final BigDecimal valor;
        final int cantidad, rubroId, proveedorId;
        try {
            valor = new BigDecimal(precio.getText().trim());
            cantidad = Integer.parseInt(stock.getText().trim());
            rubroId = seleccion(rubros, "rubro");
            proveedorId = seleccion(proveedores, "proveedor");
        } catch (NumberFormatException ex) {
            EstiloSwing.informacion(this, "Ingresá precio y stock numéricos válidos.");
            return;
        } catch (IllegalArgumentException ex) {
            EstiloSwing.error(this, ex);
            return;
        }
        boolean esNuevo = id.getText().isBlank();
        int idProducto = esNuevo ? 0 : Integer.parseInt(id.getText());
        guardar.setEnabled(false);
        EstiloSwing.ejecutar(getRootPane(), () -> {
            if (esNuevo) sesion.catalogo().registrarProducto(n, d, valor, cantidad, rubroId, proveedorId);
            else sesion.catalogo().modificarProducto(idProducto, n, d, valor, cantidad, rubroId, proveedorId);
            return null;
        }, ignorado -> {
            limpiar(); cargarTabla();
            EstiloSwing.informacion(this, esNuevo ? "Producto registrado correctamente." : "Producto modificado correctamente.");
        }, this::permisosFormulario);
    }

    private void darDeBaja() {
        if (!sesion.puedeEditarProductos() || !activo.isSelected()) return;
        if (id.getText().isBlank()) {
            EstiloSwing.informacion(this, "Seleccioná un producto antes de darlo de baja.");
            return;
        }
        if (!EstiloSwing.confirmar(this, "¿Dar de baja el producto " + nombre.getText()
                + "?\nSeguirá guardado en PostgreSQL.")) return;
        int idProducto = Integer.parseInt(id.getText());
        EstiloSwing.ejecutar(getRootPane(), () -> {
            sesion.catalogo().eliminarProducto(idProducto);
            return null;
        }, ignorado -> {
            limpiar(); cargarTabla();
            EstiloSwing.informacion(this, "Producto dado de baja (se conserva el historial).");
        });
    }
}
