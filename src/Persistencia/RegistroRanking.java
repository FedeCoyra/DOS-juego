package Persistencia;

import java.io.Serializable;

public class RegistroRanking implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nombreJugador;
    private int victorias;

    public RegistroRanking(String nombreJugador, int victorias) {
        this.nombreJugador = nombreJugador;
        this.victorias = victorias;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public int getVictorias() {
        return victorias;
    }

    public void sumarVictoria() {
        this.victorias++;
    }

    @Override
    public String toString() {
        return nombreJugador + " - " + victorias + " victoria(s)";
    }
}