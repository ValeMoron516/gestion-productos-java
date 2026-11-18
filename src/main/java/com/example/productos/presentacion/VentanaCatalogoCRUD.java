package com.example.productos.presentacion;

import com.example.productos.modelo.Proveedor;
import com.example.productos.modelo.Rubro;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Ventana reusable de rubros/proveedores, sin SQL ni dependencias con JDBC. */
public class VentanaCatalogoCRUD extends JDialog {
    public enum Tipo { RUBROS, PROVEEDORES }
    private final SesionSwing sesion;
    private final Tipo tipo;
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JTextField id = EstiloSwing.campo(8);
    private final JTextField nombre = EstiloSwing.campo(24);
    private final JTextField domicilio = EstiloSwing.campo(24);
    private final JTextField telefono = EstiloSwing.campo(24);
    private final JTextField busqueda = EstiloSwing.campo(18);
    private final JRadioButton porId = new JRadioButton("ID", true);
    private final JRadioButton porNombre = new JRadioButton("Nombre");
    private final JButton guardar = EstiloSwing.boton("Guardar / Modificar");
    private final JButton eliminar = EstiloSwing.boton("Eliminar", EstiloSwing.ROJO);
    private final JLabel estado = EstiloSwing.etiqueta("Consultando PostgreSQL...");
    private boolean poblando;

    record Fila(int id, String nombre, String domicilio, String telefono) { }

    public VentanaCatalogoCRUD(Frame padre, SesionSwing sesion, Tipo tipo) {
        super(padre, tipo == Tipo.RUBROS ? "Gestión de rubros" : "Gestión de proveedores", true);
        this.sesion = sesion;
        this.tipo = tipo;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1040, 640);
        setMinimumSize(new Dimension(850, 560));
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout());
        add(EstiloSwing.cabecera(tipo == Tipo.RUBROS ? "Rubros" : "Proveedores",
                "Gestión de catálogos · " + sesion.persona().getNombreCompleto()
                        + " · " + (sesion.puedeAdministrarCatalogos() ? "Edición habilitada" : "Solo consulta")),
                BorderLayout.NORTH);

        modelo = new DefaultTableModel(tipo == Tipo.RUBROS
                ? new String[]{"ID", "Nombre"}
                : new String[]{"ID", "Razón social", "Domicilio", "Teléfono"}, 0) {
            @Override public boolean isCellEditable(int f, int c) { return false; }
            @Override public Class<?> getColumnClass(int columna) {
                return columna == 0 ? Integer.class : String.class;
            }
        };
        tabla = EstiloSwing.tabla(modelo);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(56);
        tabla.getColumnModel().getColumn(0).setMaxWidth(75);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !poblando) seleccionarFila();
        });
        JPanel area = EstiloSwing.panel();
        area.setLayout(new BorderLayout(14, 14));
        area.setBorder(new EmptyBorder(12, 20, 14, 20));
        area.add(buscador(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Registros de PostgreSQL"));
        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formulario(), scroll);
        division.setResizeWeight(0.35);
        division.setContinuousLayout(true);
        division.setDividerLocation(350);
        division.setBorder(null);
        area.add(division, BorderLayout.CENTER);
        add(area, BorderLayout.CENTER);
        JPanel pie = EstiloSwing.panel();
        pie.setLayout(new BorderLayout());
        pie.setBorder(new EmptyBorder(6, 22, 14, 22));
        estado.setForeground(EstiloSwing.SUAVE);
        pie.add(estado, BorderLayout.WEST);
        JButton volver = EstiloSwing.boton("Volver al menú", EstiloSwing.OSCURO);
        volver.addActionListener(e -> dispose());
        pie.add(volver, BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);
        habilitarEdicion();
        cargar();
    }

    private JPanel buscador() {
        JPanel b = EstiloSwing.panel();
        b.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 9));
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(porId); grupo.add(porNombre);
        porId.setOpaque(false); porNombre.setOpaque(false);
        b.add(EstiloSwing.etiqueta("Buscar:"));
        b.add(porId); b.add(porNombre); b.add(busqueda);
        JButton buscarBtn = EstiloSwing.boton("Buscar");
        buscarBtn.addActionListener(e -> buscar());
        busqueda.addActionListener(e -> buscar());
        JButton listar = EstiloSwing.boton("Listar / Actualizar", EstiloSwing.OSCURO);
        listar.addActionListener(e -> cargar());
        b.add(buscarBtn); b.add(listar);
        return b;
    }

    private JPanel formulario() {
        JPanel form = EstiloSwing.formulario();
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(tipo == Tipo.RUBROS ? "Datos del rubro" : "Datos del proveedor"),
                new EmptyBorder(9, 8, 12, 8)));
        var g = EstiloSwing.rejilla();
        id.setEditable(false);
        id.setBackground(new Color(231, 237, 243));
        EstiloSwing.fila(form, g, 0, "ID:", id);
        EstiloSwing.fila(form, g, 1, tipo == Tipo.RUBROS ? "Nombre:" : "Razón social:", nombre);
        int y = 2;
        if (tipo == Tipo.PROVEEDORES) {
            EstiloSwing.fila(form, g, y++, "Domicilio:", domicilio);
            EstiloSwing.fila(form, g, y++, "Teléfono:", telefono);
        }
        JPanel botones = EstiloSwing.panel();
        botones.setLayout(new GridLayout(3, 1, 0, 9));
        JButton nuevo = EstiloSwing.boton("Nuevo / Limpiar", EstiloSwing.OSCURO);
        nuevo.addActionListener(e -> limpiar());
        guardar.addActionListener(e -> guardar());
        eliminar.addActionListener(e -> eliminar());
        botones.add(nuevo); botones.add(guardar); botones.add(eliminar);
        g.gridy = y++; g.gridx = 0; g.gridwidth = 2;
        g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        form.add(botones, g);
        JLabel nota = new JLabel(tipo == Tipo.RUBROS
                ? "<html><small>Los rubros con productos asociados no se pueden borrar.</small></html>"
                : "<html><small>Los proveedores con productos asociados no se pueden borrar.</small></html>");
        nota.setForeground(EstiloSwing.SUAVE);
        g.gridy = y++;
        form.add(nota, g);
        g.gridy = y; g.weighty = 1; g.fill = GridBagConstraints.VERTICAL;
        form.add(Box.createVerticalGlue(), g);
        return form;
    }

    private void habilitarEdicion() {
        boolean admin = sesion.puedeAdministrarCatalogos();
        nombre.setEditable(admin); domicilio.setEditable(admin); telefono.setEditable(admin);
        guardar.setEnabled(admin);
        eliminar.setEnabled(admin && !id.getText().isBlank());
    }

    private List<Fila> consultar() throws Exception {
        if (tipo == Tipo.RUBROS) {
            return sesion.catalogo().listarRubros().stream()
                    .map(r -> new Fila(r.getId(), r.getNombre(), "", "")).toList();
        }
        return sesion.catalogo().listarProveedores().stream()
                .map(p -> new Fila(p.getId(), p.getRazonSocial(), p.getDomicilio(), p.getTelefono())).toList();
    }

    private void cargar() {
        EstiloSwing.ejecutar(getRootPane(), this::consultar, this::mostrar);
    }

    private void mostrar(List<Fila> lista) {
        poblando = true;
        modelo.setRowCount(0);
        for (Fila fila : lista) {
            if (tipo == Tipo.RUBROS) modelo.addRow(new Object[]{fila.id(), fila.nombre()});
            else modelo.addRow(new Object[]{fila.id(), fila.nombre(), fila.domicilio(), fila.telefono()});
        }
        tabla.clearSelection();
        poblando = false;
        estado.setText(lista.size() + " registro(s) · Podés ordenar la tabla por cualquier columna.");
    }

    private void seleccionarFila() {
        int vista = tabla.getSelectedRow();
        if (vista < 0) return;
        int fila = tabla.convertRowIndexToModel(vista);
        id.setText(String.valueOf(modelo.getValueAt(fila, 0)));
        nombre.setText((String) modelo.getValueAt(fila, 1));
        if (tipo == Tipo.PROVEEDORES) {
            domicilio.setText(texto(modelo.getValueAt(fila, 2)));
            telefono.setText(texto(modelo.getValueAt(fila, 3)));
        }
        habilitarEdicion();
    }

    private String texto(Object v) { return v == null ? "" : v.toString(); }

    private void limpiar() {
        tabla.clearSelection();
        id.setText(""); nombre.setText(""); domicilio.setText(""); telefono.setText("");
        habilitarEdicion();
    }

    private void buscar() {
        String t = busqueda.getText().trim();
        if (t.isEmpty()) {
            EstiloSwing.informacion(this, "Ingresá un ID o parte del nombre.");
            return;
        }
        if (porId.isSelected()) {
            final int numero;
            try { numero = Integer.parseInt(t); }
            catch (NumberFormatException ex) {
                EstiloSwing.informacion(this, "El ID debe ser un número entero.");
                return;
            }
            EstiloSwing.ejecutar(getRootPane(), () -> {
                if (tipo == Tipo.RUBROS) {
                    Rubro r = sesion.catalogo().buscarRubro(numero);
                    return r == null ? List.<Fila>of() : List.of(new Fila(r.getId(), r.getNombre(), "", ""));
                }
                Proveedor p = sesion.catalogo().buscarProveedor(numero);
                return p == null ? List.<Fila>of() : List.of(new Fila(p.getId(), p.getRazonSocial(), p.getDomicilio(), p.getTelefono()));
            }, lista -> {
                mostrar(lista);
                if (lista.isEmpty()) EstiloSwing.informacion(this, "No existe un registro con ese ID.");
            });
        } else {
            EstiloSwing.ejecutar(getRootPane(), this::consultar, lista ->
                    mostrar(lista.stream().filter(f -> f.nombre().toLowerCase(Locale.ROOT)
                            .contains(t.toLowerCase(Locale.ROOT))).toList()));
        }
    }

    private void guardar() {
        if (!sesion.puedeAdministrarCatalogos()) return;
        boolean nuevo = id.getText().isBlank();
        int numero = nuevo ? 0 : Integer.parseInt(id.getText());
        String nom = nombre.getText();
        String dir = domicilio.getText();
        String tel = telefono.getText();
        EstiloSwing.ejecutar(getRootPane(), () -> {
            if (tipo == Tipo.RUBROS) {
                if (nuevo) sesion.catalogo().registrarRubro(nom);
                else sesion.catalogo().modificarRubro(numero, nom);
            } else {
                if (nuevo) sesion.catalogo().registrarProveedor(nom, dir, tel);
                else sesion.catalogo().modificarProveedor(numero, nom, dir, tel);
            }
            return null;
        }, resultado -> {
            limpiar(); cargar();
            EstiloSwing.informacion(this, nuevo ? "Registro guardado correctamente." : "Cambios guardados correctamente.");
        });
    }

    private void eliminar() {
        if (!sesion.puedeAdministrarCatalogos() || id.getText().isBlank()) {
            EstiloSwing.informacion(this, "Seleccioná un registro para eliminar.");
            return;
        }
        if (!EstiloSwing.confirmar(this, "¿Eliminar " + nombre.getText() + "?\n"
                + "Si tiene productos asociados, PostgreSQL impedirá la eliminación.")) return;
        int numero = Integer.parseInt(id.getText());
        EstiloSwing.ejecutar(getRootPane(), () -> {
            if (tipo == Tipo.RUBROS) sesion.catalogo().eliminarRubro(numero);
            else sesion.catalogo().eliminarProveedor(numero);
            return null;
        }, resultado -> {
            limpiar(); cargar();
            EstiloSwing.informacion(this, "Registro eliminado correctamente.");
        });
    }
}
