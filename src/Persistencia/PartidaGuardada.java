package Persistencia;

import Modelo.*;

import java.io.Serializable;
import java.util.List;

public class PartidaGuardada implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Mazo mazo;
    private final List<Carta> descarte;
    private final FilaCentral filaCentral;
    private final List<Jugador> jugadores;
    private final int indiceJugadorActual;
    private final Jugador ganador;

    public PartidaGuardada(Mazo mazo,
                           List<Carta> descarte,
                           FilaCentral filaCentral,
                           List<Jugador> jugadores,
                           int indiceJugadorActual,
                           Jugador ganador) {
        this.mazo = mazo;
        this.descarte = descarte;
        this.filaCentral = filaCentral;
        this.jugadores = jugadores;
        this.indiceJugadorActual = indiceJugadorActual;
        this.ganador = ganador;
    }

    public Mazo getMazo() {
        return mazo;
    }

    public List<Carta> getDescarte() {
        return descarte;
    }

    public FilaCentral getFilaCentral() {
        return filaCentral;
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public int getIndiceJugadorActual() {
        return indiceJugadorActual;
    }

    public Jugador getGanador() {
        return ganador;
    }
}