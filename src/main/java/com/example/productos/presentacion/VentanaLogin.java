package com.example.productos.presentacion;

import com.example.productos.modelo.Rol;
import com.example.productos.persistencia.RolRepository;
import com.example.productos.servicio.LoginService;
import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Login con PIN oculto; nunca conserva el PIN en una sesión ni lo escribe en consola. */
public final class VentanaLogin extends JFrame {
    private final LoginService login;
    private final JPasswordField pin = new JPasswordField(8);
    private final JButton entrar = EstiloSwing.boton("Ingresar al sistema");

    public VentanaLogin() { this(new LoginService(new RolRepository())); }

    public VentanaLogin(LoginService login) {
        super("Gestión de Productos · Acceso");
        this.login = login;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 410);
        setMinimumSize(new Dimension(430, 380));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(EstiloSwing.cabecera("Gestión de Productos", "Acceso mediante PIN personal - Unidad 7"), BorderLayout.NORTH);

        JPanel contenido = EstiloSwing.panel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(new EmptyBorder(32, 52, 24, 52));
        JLabel titulo = EstiloSwing.etiqueta("Ingresá tu PIN de 4 dígitos");
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(10));
        pin.setFont(new Font("Monospaced", Font.PLAIN, 21));
        pin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pin.setAlignmentX(LEFT_ALIGNMENT);
        contenido.add(pin);
        contenido.add(Box.createVerticalStrut(18));
        entrar.setAlignmentX(LEFT_ALIGNMENT);
        entrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        contenido.add(entrar);
        contenido.add(Box.createVerticalStrut(14));
        JLabel ayuda = new JLabel("El acceso y los permisos se verifican en PostgreSQL.");
        ayuda.setFont(new Font("Dialog", Font.PLAIN, 11));
        ayuda.setForeground(EstiloSwing.SUAVE);
        ayuda.setAlignmentX(LEFT_ALIGNMENT);
        contenido.add(ayuda);
        add(contenido, BorderLayout.CENTER);
        entrar.addActionListener(e -> ingresar());
        pin.addActionListener(e -> ingresar());
        SwingUtilities.invokeLater(pin::requestFocusInWindow);
    }

    private void ingresar() {
        char[] caracteres = pin.getPassword();
        String valor = new String(caracteres);
        Arrays.fill(caracteres, '\0');
        if (!valor.matches("[0-9]{4}")) {
            pin.setText("");
            EstiloSwing.informacion(this, "El PIN debe contener exactamente 4 dígitos.");
            return;
        }
        entrar.setEnabled(false);
        EstiloSwing.ejecutar(getRootPane(), () -> login.ingresar(valor), (Rol persona) -> {
            setVisible(false);
            MenuPrincipal ventana = new MenuPrincipal(SesionSwing.real(persona), () -> {
                pin.setText("");
                setVisible(true);
                toFront();
                pin.requestFocusInWindow();
            });
            ventana.setVisible(true);
        }, () -> {
            pin.setText("");
            entrar.setEnabled(true);
        });
    }
}
