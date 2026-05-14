package Vista;

import Modelo.*;
import Persistencia.RegistroRanking;

import java.util.List;

public interface VistaJuego {

    void mostrarMensaje(String mensaje);

    void mostrarEstado(List<Jugador> jugadores,
                       FilaCentral filaCentral,
                       Jugador jugadorActual,
                       Jugador jugadorLocal,
                       int cartasEnMazo);

    int pedirCantidadJugadores();

    List<String> pedirNombresJugadores(int cantidadJugadores);

    boolean preguntarSiCargarPartida();

    String pedirNombreArchivoPartida();

    void mostrarRankingTop5(List<RegistroRanking> ranking);

    int pedirOpcionTurno(Jugador jugadorLocal);

    int pedirIndiceCartaMano(Jugador jugadorLocal);

    int pedirIndiceCartaCentral(FilaCentral filaCentral);

    int[] pedirIndicesCartasDobles(Jugador jugadorLocal);

    int pedirIndiceCartaBono(Jugador jugadorLocal, int numeroBono, int totalBonos);

    void mostrarCartaRobada(Carta carta);

    int pedirIndiceJugadorParaPenalizar(List<Jugador> jugadores);

    void mostrarGanador(Jugador ganador);

    ColorCarta elegirColorParaComodinDos();

    int elegirNumeroParaComodinNumero();

    boolean preguntarSiQuiereSeguirJugandoEnTurno();

    boolean preguntarSiQuiereIntentarJugadaDespuesDeRobar();

    int pedirIndiceCartaParaAgregarAFilaCentral(Jugador jugadorLocal);

    int pedirOpcionPostRobo();

    int pedirOpcionFinDePartida();
}