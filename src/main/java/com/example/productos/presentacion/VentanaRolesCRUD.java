package com.example.productos.presentacion;

import com.example.productos.modelo.Rol;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Administración gráfica opcional que conserva el ABM de roles de la segunda guía. */
public final class VentanaRolesCRUD extends JDialog {
    private final SesionSwing sesion;
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JTextField id = EstiloSwing.campo(6);
    private final JTextField nombre = EstiloSwing.campo(20);
    private final JTextField apellido = EstiloSwing.campo(20);
    private final JPasswordField pin = new JPasswordField(12);
    private final JComboBox<TipoRol> rol = new JComboBox<>(new TipoRol[] {
            new TipoRol("a", "Administrador"),
            new TipoRol("e", "Empleado"),
            new TipoRol("i", "Invitado")
    });
    private final JTextField busqueda = EstiloSwing.campo(10);
    private final JLabel estado = EstiloSwing.etiqueta("Consultando PostgreSQL...");
    private boolean poblando;

    record TipoRol(String clave, String texto) {
        @Override public String toString() { return texto; }
    }

    public VentanaRolesCRUD(Frame padre, SesionSwing sesion) {
        super(padre, "Personas y roles", true);
        if (!sesion.puedeAdministrarCatalogos()) throw new SecurityException("Solo administrador");
        this.sesion = sesion;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(950, 635);
        setMinimumSize(new Dimension(830, 550));
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout());
        add(EstiloSwing.cabecera("Personas y roles", "Administración exclusiva del perfil Administrador"),
                BorderLayout.NORTH);
        modelo = new DefaultTableModel(new String[]{"ID", "Nombre", "Apellido", "Rol"}, 0) {
            @Override public boolean isCellEditable(int f, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };
        tabla = EstiloSwing.tabla(modelo);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(55);
        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !poblando) seleccionar();
        });
        JPanel area = EstiloSwing.panel();
        area.setLayout(new BorderLayout(14, 14));
        area.setBorder(new EmptyBorder(16, 20, 12, 20));
        JPanel barra = EstiloSwing.panel();
        barra.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        barra.add(EstiloSwing.etiqueta("Buscar por ID:"));
        barra.add(busqueda);
        JButton buscar = EstiloSwing.boton("Buscar");
        JButton listar = EstiloSwing.boton("Listar / Actualizar", EstiloSwing.OSCURO);
        buscar.addActionListener(e -> buscar());
        busqueda.addActionListener(e -> buscar());
        listar.addActionListener(e -> cargar());
        barra.add(buscar); barra.add(listar);
        area.add(barra, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Usuarios sin PIN visible"));
        JSplitPane d = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formulario(), scroll);
        d.setResizeWeight(.39);
        d.setDividerLocation(360);
        d.setBorder(null);
        area.add(d, BorderLayout.CENTER);
        add(area, BorderLayout.CENTER);
        JPanel pie = EstiloSwing.panel();
        pie.setLayout(new BorderLayout());
        pie.setBorder(new EmptyBorder(5, 20, 14, 20));
        estado.setForeground(EstiloSwing.SUAVE);
        pie.add(estado, BorderLayout.WEST);
        JButton volver = EstiloSwing.boton("Volver al menú", EstiloSwing.OSCURO);
        volver.addActionListener(e -> dispose());
        pie.add(volver, BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);
        cargar();
    }

    private JPanel formulario() {
        JPanel form = EstiloSwing.formulario();
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Datos de acceso"),
                new EmptyBorder(9, 8, 8, 8)));
        var g = EstiloSwing.rejilla();
        id.setEditable(false);
        id.setBackground(new Color(231, 237, 243));
        EstiloSwing.fila(form, g, 0, "ID:", id);
        EstiloSwing.fila(form, g, 1, "Nombre:", nombre);
        EstiloSwing.fila(form, g, 2, "Apellido:", apellido);
        EstiloSwing.fila(form, g, 3, "Rol:", rol);
        EstiloSwing.fila(form, g, 4, "PIN (4 dígitos):", pin);
        JPanel acciones = EstiloSwing.panel();
        acciones.setLayout(new GridLayout(3, 1, 0, 9));
        JButton nuevo = EstiloSwing.boton("Nuevo / Limpiar", EstiloSwing.OSCURO);
        JButton guardar = EstiloSwing.boton("Guardar / Modificar");
        JButton eliminar = EstiloSwing.boton("Eliminar usuario", EstiloSwing.ROJO);
        nuevo.addActionListener(e -> limpiar());
        guardar.addActionListener(e -> guardar());
        eliminar.addActionListener(e -> eliminar());
        acciones.add(nuevo); acciones.add(guardar); acciones.add(eliminar);
        g.gridy = 5; g.gridx = 0; g.gridwidth = 2;
        g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        form.add(acciones, g);
        JLabel nota = new JLabel("<html><small>Al modificar, dejá el PIN vacío para conservarlo.<br>Los PIN nunca aparecen en el listado.</small></html>");
        nota.setForeground(EstiloSwing.SUAVE);
        g.gridy = 6;
        form.add(nota, g);
        g.gridy = 7; g.weighty = 1; g.fill = GridBagConstraints.VERTICAL;
        form.add(Box.createVerticalGlue(), g);
        return form;
    }

    private void cargar() {
        EstiloSwing.ejecutar(getRootPane(), () -> sesion.personas().listar(), this::mostrar);
    }

    private void mostrar(List<Rol> lista) {
        poblando = true;
        modelo.setRowCount(0);
        for (Rol p : lista) modelo.addRow(new Object[]{p.getId(), p.getNombre(),
                p.getApellido(), p.getNombreRol()});
        poblando = false;
        estado.setText(lista.size() + " persona(s) · Los PIN no se muestran por seguridad.");
    }

    private void seleccionar() {
        int vista = tabla.getSelectedRow();
        if (vista < 0) return;
        int fila = tabla.convertRowIndexToModel(vista);
        id.setText(String.valueOf(modelo.getValueAt(fila, 0)));
        nombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        apellido.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        String nombreRol = String.valueOf(modelo.getValueAt(fila, 3));
        for (int i = 0; i < rol.getItemCount(); i++) {
            if (rol.getItemAt(i).texto().equals(nombreRol)) rol.setSelectedIndex(i);
        }
        pin.setText("");
    }

    private void limpiar() {
        tabla.clearSelection();
        id.setText(""); nombre.setText(""); apellido.setText(""); pin.setText("");
        rol.setSelectedIndex(1);
    }

    private void buscar() {
        int buscado;
        try { buscado = Integer.parseInt(busqueda.getText().trim()); }
        catch (NumberFormatException ex) {
            EstiloSwing.informacion(this, "El ID debe ser un número entero.");
            return;
        }
        EstiloSwing.ejecutar(getRootPane(), () -> sesion.personas().buscar(buscado), p -> {
            if (p == null) {
                EstiloSwing.informacion(this, "No existe una persona con ese ID.");
                return;
            }
            mostrar(List.of(p));
            id.setText(String.valueOf(p.getId()));
            nombre.setText(p.getNombre());
            apellido.setText(p.getApellido());
            pin.setText("");
            for (int i = 0; i < rol.getItemCount(); i++)
                if (rol.getItemAt(i).clave().equals(p.getRol())) rol.setSelectedIndex(i);
        });
    }

    private void guardar() {
        String n = nombre.getText(), a = apellido.getText();
        char[] clave = pin.getPassword();
        String pinNuevo = new String(clave);
        java.util.Arrays.fill(clave, '\0');
        TipoRol tipo = (TipoRol) rol.getSelectedItem();
        boolean nuevo = id.getText().isBlank();
        int numero = nuevo ? 0 : Integer.parseInt(id.getText());
        EstiloSwing.ejecutar(getRootPane(), () -> {
            if (nuevo) sesion.personas().registrar(pinNuevo, a, n, tipo.clave());
            else sesion.personas().modificar(numero, pinNuevo, a, n, tipo.clave());
            return null;
        }, res -> {
            limpiar(); cargar();
            EstiloSwing.informacion(this, nuevo ? "Persona registrada." : "Persona modificada.");
        });
    }

    private void eliminar() {
        if (id.getText().isBlank()) {
            EstiloSwing.informacion(this, "Elegí una persona para eliminar.");
            return;
        }
        if (!EstiloSwing.confirmar(this, "¿Eliminar la persona " + nombre.getText() + " "
                + apellido.getText() + "?")) return;
        int numero = Integer.parseInt(id.getText());
        EstiloSwing.ejecutar(getRootPane(), () -> {
            sesion.personas().eliminar(numero);
            return null;
        }, res -> {
            limpiar(); cargar(); EstiloSwing.informacion(this, "Persona eliminada.");
        });
    }
}
