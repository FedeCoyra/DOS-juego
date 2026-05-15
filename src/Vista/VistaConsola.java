package Vista;

import Modelo.*;
import Persistencia.RegistroRanking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class VistaConsola implements VistaJuego {

    private final BufferedReader reader;

    public VistaConsola() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }


    private String leerLinea() {
        try {
            String linea = reader.readLine();
            return (linea != null) ? linea.trim() : "";
        } catch (IOException e) {
            return "";
        }
    }

    private int leerEnteroEnRango(int min, int max) {
        while (true) {
            try {
                int valor = Integer.parseInt(leerLinea());
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // seguir pidiendo
            }
            System.out.print("Valor inválido. Ingrese un número entre " + min + " y " + max + ": ");
        }
    }

    private boolean leerSiNo() {
        while (true) {
            String entrada = leerLinea().toLowerCase();
            if (entrada.equals("s") || entrada.equals("si") || entrada.equals("sí")) return true;
            if (entrada.equals("n") || entrada.equals("no")) return false;
            System.out.print("Respuesta inválida. Ingresá s/n: ");
        }
    }

    // ─── VistaJuego ──────────────────────────────────────────────────────────

    @Override
    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    @Override
    public void mostrarEstado(List<Jugador> jugadores,
                              FilaCentral filaCentral,
                              Jugador jugadorActual,
                              Jugador jugadorLocal,
                              int cartasEnMazo) {
        System.out.println("\n==============================");
        System.out.println("Estado del juego");
        System.out.println("Mazo: " + cartasEnMazo + " cartas restantes.");

        System.out.println("\nFila central:");
        mostrarFilaCentralConIndices(filaCentral);

        System.out.println("\nJugadores:");
        for (Jugador j : jugadores) {
            String extra = "";
            if (j.isDijoDos()) extra += " | dijo DOS";
            if (j.getCartasPenalizacionPendientes() > 0)
                extra += " | penalización pendiente: " + j.getCartasPenalizacionPendientes();
            System.out.println(" - " + j.getNombre() + " (" + j.getMano().cantidadCartas() + " cartas)" + extra);
        }

        System.out.println("\nTurno actual: " + (jugadorActual != null ? jugadorActual.getNombre() : "(sin turno)"));
        System.out.println("Tu jugador:   " + (jugadorLocal  != null ? jugadorLocal.getNombre()  : "(sin asignar)"));

        System.out.println("Tu mano:");
        if (jugadorLocal != null) {
            mostrarManoConIndices(jugadorLocal);
        } else {
            System.out.println(" (sin jugador asignado)");
        }

        System.out.println("==============================\n");
    }

    @Override
    public int pedirCantidadJugadores() {
        System.out.print("Ingrese la cantidad de jugadores (2–4): ");
        while (true) {
            try {
                int n = Integer.parseInt(leerLinea());
                if (n >= 2 && n <= 4) return n;
            } catch (NumberFormatException e) {
                // seguir
            }
            System.out.print("Valor inválido. Ingrese un número entre 2 y 4: ");
        }
    }

    @Override
    public String pedirNombreJugador(int numeroJugador) {
        String nombre;
        do {
            System.out.print("Ingrese su nombre (Jugador " + numeroJugador + "): ");
            nombre = leerLinea();
        } while (nombre.isEmpty());
        return nombre;
    }

    @Override
    public boolean preguntarSiCargarPartida() {
        System.out.print("¿Desea cargar una partida guardada? (s/n): ");
        return leerSiNo();
    }

    @Override
    public String pedirNombreArchivoPartida() {
        System.out.print("Ingrese nombre del archivo de partida: ");
        return leerLinea();
    }

    @Override
    public void mostrarRankingTop5(List<RegistroRanking> ranking) {
        System.out.println("\n=== TOP 5 ===");
        if (ranking == null || ranking.isEmpty()) {
            System.out.println("Sin datos.");
            return;
        }
        for (int i = 0; i < ranking.size(); i++) {
            System.out.println((i + 1) + ". " + ranking.get(i));
        }
    }

    @Override
    public int pedirOpcionTurno(Jugador jugadorLocal) {
        System.out.println("Turno de " + (jugadorLocal != null ? jugadorLocal.getNombre() : "tu jugador") + ". Elegí una opción:");
        System.out.println(" 1) Jugar carta simple");
        System.out.println(" 2) Jugar carta doble");
        System.out.println(" 3) Robar carta");
        System.out.println(" 4) Decir DOS");
        System.out.println(" 5) Acusar por no decir DOS");
        System.out.println(" 6) Terminar turno");
        System.out.println(" 7) Guardar partida y salir");
        System.out.print("Opción: ");
        return leerEnteroEnRango(1, 7);
    }

    @Override
    public int pedirIndiceCartaMano(Jugador jugadorLocal) {
        if (jugadorLocal == null) {
            mostrarMensaje("No hay jugador local asignado.");
            return -1;
        }
        int max = jugadorLocal.getMano().cantidadCartas() - 1;
        if (max < 0) {
            mostrarMensaje("No hay cartas en la mano.");
            return -1;
        }
        mostrarManoConIndices(jugadorLocal);
        System.out.print("Elegí índice de carta de tu mano (0-" + max + "): ");
        return leerEnteroEnRango(0, max);
    }

    @Override
    public int pedirIndiceCartaCentral(FilaCentral filaCentral) {
        int max = filaCentral.cantidadCartas() - 1;
        if (max < 0) {
            mostrarMensaje("No hay cartas en la fila central.");
            return -1;
        }
        mostrarFilaCentralConIndices(filaCentral);
        System.out.print("Elegí índice de carta central (0-" + max + "): ");
        return leerEnteroEnRango(0, max);
    }

    @Override
    public int[] pedirIndicesCartasDobles(Jugador jugadorLocal) {
        int primera = pedirIndiceCartaMano(jugadorLocal);
        int segunda;
        do {
            segunda = pedirIndiceCartaMano(jugadorLocal);
            if (segunda == primera) mostrarMensaje("La segunda carta debe ser distinta de la primera.");
        } while (segunda == primera);
        return new int[]{primera, segunda};
    }

    @Override
    public int pedirIndiceCartaBono(Jugador jugadorLocal, int numeroBono, int totalBonos) {
        mostrarMensaje("Seleccioná la carta para bono " + numeroBono + " de " + totalBonos + ".");
        return pedirIndiceCartaMano(jugadorLocal);
    }

    @Override
    public void mostrarCartaRobada(Carta carta) {
        if (carta == null) {
            System.out.println("No se pudo robar carta.");
        } else {
            System.out.println("Carta robada: " + describirCarta(carta));
        }
    }

    @Override
    public int pedirIndiceJugadorParaPenalizar(List<Jugador> jugadores) {
        System.out.println("Jugadores disponibles para acusar:");
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            System.out.println(" " + i + ") " + j.getNombre() + " (" + j.getMano().cantidadCartas() + " cartas)");
        }
        System.out.print("Elegí el índice del jugador a penalizar: ");
        return leerEnteroEnRango(0, jugadores.size() - 1);
    }

    @Override
    public void mostrarGanador(Jugador ganador) {
        if (ganador != null) {
            System.out.println("\n*** ¡Ganó " + ganador.getNombre() + "! ***");
        } else {
            System.out.println("\n*** La partida terminó. ***");
        }
    }

    @Override
    public ColorCarta elegirColorParaComodinDos() {
        System.out.println("Elegí el color para el comodín DOS:");
        System.out.println(" 1) ROJO");
        System.out.println(" 2) AZUL");
        System.out.println(" 3) VERDE");
        System.out.println(" 4) AMARILLO");
        System.out.print("Opción: ");
        switch (leerEnteroEnRango(1, 4)) {
            case 1: return ColorCarta.ROJO;
            case 2: return ColorCarta.AZUL;
            case 3: return ColorCarta.VERDE;
            case 4: return ColorCarta.AMARILLO;
            default: return ColorCarta.ROJO;
        }
    }

    @Override
    public int elegirNumeroParaComodinNumero() {
        System.out.print("Elegí el número para el comodín número (1-10): ");
        return leerEnteroEnRango(1, 10);
    }

    @Override
    public boolean preguntarSiQuiereSeguirJugandoEnTurno() {
        System.out.print("¿Querés seguir jugando en este turno? (s/n): ");
        return leerSiNo();
    }

    @Override
    public boolean preguntarSiQuiereIntentarJugadaDespuesDeRobar() {
        System.out.print("¿Querés intentar una jugada después de robar? (s/n): ");
        return leerSiNo();
    }

    @Override
    public int pedirIndiceCartaParaAgregarAFilaCentral(Jugador jugadorLocal) {
        System.out.println("Seleccioná una carta tuya para bajar a la fila central.");
        return pedirIndiceCartaMano(jugadorLocal);
    }

    @Override
    public int pedirOpcionPostRobo() {
        System.out.println("Elegí tipo de jugada:");
        System.out.println(" 1) Jugar carta simple");
        System.out.println(" 2) Jugar carta doble");
        System.out.println(" 3) No hacer jugada y bajar una carta a la fila central");
        System.out.print("Opción: ");
        return leerEnteroEnRango(1, 3);
    }

    @Override
    public int pedirOpcionFinDePartida() {
        System.out.println("\nFin de la partida. Elegí una opción:");
        System.out.println(" 1) Ver ranking");
        System.out.println(" 2) Salir del juego");
        System.out.print("Opción: ");
        return leerEnteroEnRango(1, 2);
    }


    private void mostrarManoConIndices(Jugador jugador) {
        Mano mano = jugador.getMano();
        for (int i = 0; i < mano.cantidadCartas(); i++) {
            System.out.println(" " + i + ") " + describirCarta(mano.getCarta(i)));
        }
    }

    private void mostrarFilaCentralConIndices(FilaCentral filaCentral) {
        for (int i = 0; i < filaCentral.cantidadCartas(); i++) {
            System.out.println(" " + i + ") " + describirCarta(filaCentral.getCarta(i)));
        }
    }

    private String describirCarta(Carta carta) {
        if (carta == null) return "(null)";
        if (carta.getTipo() == TipoCarta.NUMERICA)       return "NUMERICA "       + carta.getNumero() + " - " + carta.getColor();
        if (carta.getTipo() == TipoCarta.COMODIN_NUMERO) return "COMODIN_NUMERO (#)";
        if (carta.getTipo() == TipoCarta.COMODIN_DOS)    return "COMODIN_DOS (2) - " + carta.getColor();
        return carta.toString();
    }
}