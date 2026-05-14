package Vista;

import Modelo.*;
import Persistencia.RegistroRanking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class VistaSwing extends JFrame implements VistaJuego {

    private final Color COLOR_MESA = new Color(27, 94, 32);
    private final Color COLOR_BORDE_MESA = new Color(20, 70, 25);
    private final Color COLOR_UI_DARK = new Color(33, 33, 33);
    private final Color COLOR_TEXTO = new Color(245, 245, 245);

    private final JLabel lblTurno = new JLabel("Turno: -");
    private final JLabel lblMazo = new JLabel("Mazo: -");
    private final JLabel lblJugadorLocal = new JLabel("Tu jugador: -");

    private final JPanel panelMesa = new JPanel(new BorderLayout(20, 20));
    private final JPanel panelCentro = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 25));
    private final JPanel panelMano = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

    private final JPanel panelJugadorNorte = crearPanelJugador("Jugador Norte");
    private final JPanel panelJugadorOeste = crearPanelJugadorVertical("Jugador Oeste");
    private final JPanel panelJugadorEste = crearPanelJugadorVertical("Jugador Este");
    private final JPanel panelEstadoSuperior = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 12));

    public VistaSwing() {
        setTitle("DOS - Professional Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 850);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_MESA);
        setContentPane(root);

        configurarHeader();
        configurarMesa();

        root.add(panelEstadoSuperior, BorderLayout.NORTH);
        root.add(panelMesa, BorderLayout.CENTER);
        root.add(crearZonaInferior(), BorderLayout.SOUTH);
    }

    private void configurarHeader() {
        panelEstadoSuperior.setBackground(COLOR_UI_DARK);
        panelEstadoSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(255, 215, 0)));

        Font fontHeader = new Font("Segoe UI", Font.BOLD, 16);
        for (JLabel lbl : new JLabel[]{lblTurno, lblMazo, lblJugadorLocal}) {
            lbl.setForeground(COLOR_TEXTO);
            lbl.setFont(fontHeader);
            panelEstadoSuperior.add(lbl);
        }
    }

    private void configurarMesa() {
        panelMesa.setBackground(COLOR_MESA);
        panelMesa.setBorder(new EmptyBorder(25, 25, 25, 25));

        panelJugadorNorte.setOpaque(false);
        panelJugadorOeste.setOpaque(false);
        panelJugadorEste.setOpaque(false);
        panelCentro.setOpaque(false);

        panelMesa.add(panelJugadorNorte, BorderLayout.NORTH);
        panelMesa.add(panelJugadorOeste, BorderLayout.WEST);
        panelMesa.add(panelJugadorEste, BorderLayout.EAST);
        panelMesa.add(panelCentro, BorderLayout.CENTER);
    }

    private JPanel crearZonaInferior() {
        JPanel sur = new JPanel(new BorderLayout());
        sur.setBackground(new Color(20, 20, 20, 200));
        sur.setBorder(new EmptyBorder(10, 10, 20, 10));

        JLabel tituloMano = new JLabel("TU MANO", SwingConstants.CENTER);
        tituloMano.setForeground(new Color(255, 215, 0));
        tituloMano.setFont(new Font("Segoe UI Black", Font.ITALIC, 22));

        panelMano.setOpaque(false);
        sur.add(tituloMano, BorderLayout.NORTH);
        sur.add(panelMano, BorderLayout.CENTER);

        return sur;
    }

    private JPanel crearPanelCarta(Carta carta, boolean grande) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(3, 3, w - 4, h - 4, 18, 18);

                Color base = colorAWT(carta.getColor());
                GradientPaint gp = new GradientPaint(0, 0, base, 0, h, base.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 4, h - 4, 18, 18);

                g2.setColor(new Color(255, 255, 255, 180));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(5, 5, w - 14, h - 14, 15, 15);

                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillOval(w / 8, h / 4, w * 3 / 4, h / 2);

                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, grande ? 34 : 24));
                String texto = textoCarta(carta);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(texto, (w - fm.stringWidth(texto)) / 2 - 2, h / 2 + fm.getAscent() / 3);

                g2.dispose();
            }
        };

        int w = grande ? 100 : 80;
        int h = grande ? 145 : 115;
        panel.setPreferredSize(new Dimension(w, h));
        panel.setOpaque(false);
        return panel;
    }

    private JPanel crearPanelReversoCarta(boolean vertical) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 40, 60), 0, getHeight(), new Color(10, 10, 25));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 18, 18);

                g2.setColor(new Color(0, 191, 255));
                g2.setFont(new Font("Arial Black", Font.BOLD, 18));
                String t = "DOS";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, getHeight() / 2 + 7);

                g2.setColor(new Color(255, 255, 255, 100));
                g2.drawRoundRect(2, 2, getWidth() - 6, getHeight() - 6, 15, 15);
                g2.dispose();
            }
        };
        panel.setPreferredSize(vertical ? new Dimension(75, 110) : new Dimension(60, 90));
        panel.setOpaque(false);
        return panel;
    }

    private JPanel crearPanelJugador(String titulo) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100)),
                titulo, 0, 0,
                new Font("Segoe UI", Font.PLAIN, 12), Color.WHITE));
        return p;
    }

    private JPanel crearPanelJugadorVertical(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100)),
                titulo, 0, 0,
                new Font("Segoe UI", Font.PLAIN, 12), Color.WHITE));
        return p;
    }

    private Color colorAWT(ColorCarta color) {
        if (color == null) return new Color(50, 50, 50);
        switch (color) {
            case ROJO:     return new Color(211, 47, 47);
            case AZUL:     return new Color(25, 118, 210);
            case VERDE:    return new Color(56, 142, 60);
            case AMARILLO: return new Color(251, 192, 45);
            default:       return Color.GRAY;
        }
    }

    private String textoCarta(Carta c) {
        if (c == null) return "?";
        if (c.getTipo() == TipoCarta.NUMERICA)       return String.valueOf(c.getNumero());
        if (c.getTipo() == TipoCarta.COMODIN_NUMERO) return "#";
        if (c.getTipo() == TipoCarta.COMODIN_DOS)    return "2";
        return "?";
    }


    private void refrescarEstado(List<Jugador> jugadores,
                                 FilaCentral filaCentral,
                                 Jugador jugadorActual,
                                 Jugador jugadorLocal,
                                 int cartasEnMazo) {
        lblTurno.setText(" Turno: " + (jugadorActual != null ? jugadorActual.getNombre().toUpperCase() : "-"));
        lblMazo.setText(" Mazo: " + cartasEnMazo);
        lblJugadorLocal.setText(" Eres: " + (jugadorLocal != null ? jugadorLocal.getNombre() : "-"));

        refrescarJugadores(jugadores, jugadorLocal);
        refrescarCentro(filaCentral);
        refrescarMano(jugadorLocal);

        repaint();
        revalidate();
    }

    private void refrescarJugadores(List<Jugador> jugadores, Jugador jugadorLocal) {
        panelJugadorNorte.removeAll();
        panelJugadorOeste.removeAll();
        panelJugadorEste.removeAll();

        String nombreLocal = (jugadorLocal != null) ? jugadorLocal.getNombre() : null;

        List<Jugador> otros = new ArrayList<>();
        for (Jugador j : jugadores) {
            if (nombreLocal == null || !j.getNombre().equals(nombreLocal)) {
                otros.add(j);
            }
        }

        if (otros.size() > 0) cargarJugadorHorizontal(panelJugadorNorte, otros.get(0));
        if (otros.size() > 1) cargarJugadorVertical(panelJugadorOeste, otros.get(1));
        if (otros.size() > 2) cargarJugadorVertical(panelJugadorEste, otros.get(2));
    }

    private void cargarJugadorHorizontal(JPanel panel, Jugador jugador) {
        panel.add(crearLabelJugador(jugador));
        int cantidad = Math.min(jugador.getMano().cantidadCartas(), 12);
        for (int i = 0; i < cantidad; i++) panel.add(crearPanelReversoCarta(false));
    }

    private void cargarJugadorVertical(JPanel panel, Jugador jugador) {
        JLabel lbl = crearLabelJugador(jugador);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(10));
        int cantidad = Math.min(jugador.getMano().cantidadCartas(), 8);
        for (int i = 0; i < cantidad; i++) {
            JPanel c = crearPanelReversoCarta(false);
            c.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(c);
            panel.add(Box.createVerticalStrut(-60));
        }
    }

    private JLabel crearLabelJugador(Jugador jugador) {
        String texto = String.format("<html><center>%s<br><font color='#FFD700'>Cards: %d</font></center></html>",
                jugador.getNombre(), jugador.getMano().cantidadCartas());
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lbl;
    }

    private void refrescarCentro(FilaCentral filaCentral) {
        panelCentro.removeAll();
        for (int i = 0; i < filaCentral.cantidadCartas(); i++) {
            Carta c = filaCentral.getCarta(i);
            panelCentro.add(crearContenedorCartaConIndice(c, i, true));
        }
    }

    private void refrescarMano(Jugador jugadorLocal) {
        panelMano.removeAll();
        if (jugadorLocal == null) return;
        for (int i = 0; i < jugadorLocal.getMano().cantidadCartas(); i++) {
            Carta c = jugadorLocal.getMano().getCarta(i);
            panelMano.add(crearContenedorCartaConIndice(c, i, true));
        }
    }

    private JPanel crearContenedorCartaConIndice(Carta c, int i, boolean grande) {
        JPanel contenedor = new JPanel(new BorderLayout(0, 5));
        contenedor.setOpaque(false);
        contenedor.add(crearPanelCarta(c, grande), BorderLayout.CENTER);
        JLabel indice = new JLabel("" + i, SwingConstants.CENTER);
        indice.setForeground(new Color(255, 255, 255, 150));
        indice.setFont(new Font("Monospaced", Font.BOLD, 14));
        contenedor.add(indice, BorderLayout.SOUTH);
        return contenedor;
    }


    private void runOnEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private <T> T callOnEdt(Callable<T> c) {
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                return c.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        FutureTask<T> task = new FutureTask<>(c);
        try {
            SwingUtilities.invokeAndWait(task);
            return task.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int pedirEntero(String mensaje, int min, int max) {
        return callOnEdt(() -> {
            while (true) {
                String s = JOptionPane.showInputDialog(this, mensaje);
                if (s == null) {
                    // El usuario canceló: informar y pedir de nuevo
                    JOptionPane.showMessageDialog(this,
                            "Debés ingresar un valor para continuar.",
                            "Campo requerido",
                            JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                try {
                    int n = Integer.parseInt(s.trim());
                    if (n >= min && n <= max) return n;
                } catch (NumberFormatException ignored) {
                }
                JOptionPane.showMessageDialog(this,
                        "Ingrese un número entre " + min + " y " + max,
                        "Valor inválido",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private boolean pedirSiNo(String mensaje) {
        return callOnEdt(() -> {
            int op = JOptionPane.showConfirmDialog(this, mensaje, "Confirmación", JOptionPane.YES_NO_OPTION);
            return op == JOptionPane.YES_OPTION;
        });
    }

    private int elegirOpcion(String titulo, String mensaje, String[] opciones) {
        return callOnEdt(() -> {
            Object seleccion = JOptionPane.showInputDialog(
                    this,
                    mensaje,
                    titulo,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opciones,
                    opciones.length > 0 ? opciones[0] : null
            );
            if (seleccion == null) return -1;
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(seleccion)) return i + 1;
            }
            return -1;
        });
    }

    // ─── VistaJuego ──────────────────────────────────────────────────────────

    @Override
    public void mostrarMensaje(String mensaje) {
        runOnEdt(() -> JOptionPane.showMessageDialog(this, mensaje));
    }

    @Override
    public void mostrarEstado(List<Jugador> jugadores,
                              FilaCentral filaCentral,
                              Jugador jugadorActual,
                              Jugador jugadorLocal,
                              int cartasEnMazo) {
        runOnEdt(() -> refrescarEstado(jugadores, filaCentral, jugadorActual, jugadorLocal, cartasEnMazo));
    }

    @Override
    public int pedirCantidadJugadores() {
        return pedirEntero("Ingrese cantidad de jugadores (2 a 4):", 2, 4);
    }

    @Override
    public List<String> pedirNombresJugadores(int cantidadJugadores) {
        return callOnEdt(() -> {
            List<String> nombres = new ArrayList<>();
            for (int i = 0; i < cantidadJugadores; i++) {
                String nombre;
                do {
                    nombre = JOptionPane.showInputDialog(this, "Nombre del jugador " + (i + 1) + ":");
                } while (nombre == null || nombre.trim().isEmpty());
                nombres.add(nombre.trim());
            }
            return nombres;
        });
    }

    @Override
    public boolean preguntarSiCargarPartida() {
        return pedirSiNo("¿Desea cargar una partida guardada?");
    }

    @Override
    public String pedirNombreArchivoPartida() {
        return callOnEdt(() -> JOptionPane.showInputDialog(this, "Nombre del archivo de partida:"));
    }

    @Override
    public void mostrarRankingTop5(List<RegistroRanking> ranking) {
        runOnEdt(() -> {
            StringBuilder sb = new StringBuilder("TOP 5\n\n");
            if (ranking == null || ranking.isEmpty()) {
                sb.append("Sin datos.");
            } else {
                for (int i = 0; i < ranking.size(); i++) {
                    sb.append(i + 1).append(". ").append(ranking.get(i)).append("\n");
                }
            }
            JOptionPane.showMessageDialog(this, sb.toString());
        });
    }

    @Override
    public int pedirOpcionTurno(Jugador jugadorLocal) {
        String nombre = jugadorLocal != null ? jugadorLocal.getNombre() : "tu jugador";
        String[] opciones = {
                "1 - Jugar carta simple",
                "2 - Jugar carta doble",
                "3 - Robar carta",
                "4 - Decir DOS",
                "5 - Acusar por no decir DOS",
                "6 - Terminar turno",
                "7 - Guardar partida y salir"
        };
        return elegirOpcion("Turno", "Elegí una acción para " + nombre, opciones);
    }

    @Override
    public int pedirIndiceCartaMano(Jugador jugadorLocal) {
        if (jugadorLocal == null) return -1;
        int max = jugadorLocal.getMano().cantidadCartas() - 1;
        if (max < 0) return -1;
        return pedirEntero("Elegí índice de carta de tu mano (0 a " + max + ")", 0, max);
    }

    @Override
    public int pedirIndiceCartaCentral(FilaCentral filaCentral) {
        int max = filaCentral.cantidadCartas() - 1;
        if (max < 0) return -1;
        return pedirEntero("Elegí índice de carta central (0 a " + max + ")", 0, max);
    }

    @Override
    public int[] pedirIndicesCartasDobles(Jugador jugadorLocal) {
        int i1 = pedirIndiceCartaMano(jugadorLocal);
        int i2;
        do {
            i2 = pedirIndiceCartaMano(jugadorLocal);
            if (i1 == i2) mostrarMensaje("Las cartas deben ser distintas.");
        } while (i1 == i2);
        return new int[]{i1, i2};
    }

    @Override
    public int pedirIndiceCartaBono(Jugador jugadorLocal, int numeroBono, int totalBonos) {
        return pedirIndiceCartaMano(jugadorLocal);
    }

    @Override
    public void mostrarCartaRobada(Carta carta) {
        if (carta == null) {
            mostrarMensaje("No se pudo robar carta.");
        } else {
            mostrarMensaje("Carta robada: " + textoCarta(carta) + " - " + carta.getColor());
        }
    }

    @Override
    public int pedirIndiceJugadorParaPenalizar(List<Jugador> jugadores) {
        String[] opciones = new String[jugadores.size()];
        for (int i = 0; i < jugadores.size(); i++) {
            opciones[i] = (i + 1) + " - " + jugadores.get(i).getNombre();
        }
        int res = elegirOpcion("Penalizar", "Elegí jugador a penalizar", opciones);
        return res - 1;
    }

    @Override
    public void mostrarGanador(Jugador ganador) {
        runOnEdt(() -> JOptionPane.showMessageDialog(
                this,
                ganador != null ? "¡Ganó " + ganador.getNombre() + "!" : "La partida terminó.",
                "Fin del juego",
                JOptionPane.INFORMATION_MESSAGE
        ));
    }

    @Override
    public ColorCarta elegirColorParaComodinDos() {
        Object[] opciones = {ColorCarta.ROJO, ColorCarta.AZUL, ColorCarta.VERDE, ColorCarta.AMARILLO};
        return callOnEdt(() -> {
            Object seleccion = JOptionPane.showInputDialog(
                    this,
                    "Elegí color del comodín DOS",
                    "Color",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );
            return seleccion == null ? ColorCarta.ROJO : (ColorCarta) seleccion;
        });
    }

    @Override
    public int elegirNumeroParaComodinNumero() {
        return pedirEntero("Elegí número para el comodín número", 1, 10);
    }

    @Override
    public boolean preguntarSiQuiereSeguirJugandoEnTurno() {
        return pedirSiNo("¿Querés seguir jugando en este turno?");
    }

    @Override
    public boolean preguntarSiQuiereIntentarJugadaDespuesDeRobar() {
        return pedirSiNo("¿Querés intentar una jugada después de robar?");
    }

    @Override
    public int pedirIndiceCartaParaAgregarAFilaCentral(Jugador jugadorLocal) {
        return pedirIndiceCartaMano(jugadorLocal);
    }

    @Override
    public int pedirOpcionPostRobo() {
        String[] opciones = {
                "1 - Jugar carta simple",
                "2 - Jugar carta doble",
                "3 - Bajar una carta a la fila central"
        };
        return elegirOpcion("Post robo", "Elegí qué hacer después de robar", opciones);
    }

    @Override
    public int pedirOpcionFinDePartida() {
        String[] opciones = {
                "1 - Ver ranking",
                "2 - Salir del juego"
        };
        return elegirOpcion("Fin de la partida", "Elegí una opción", opciones);
    }
}