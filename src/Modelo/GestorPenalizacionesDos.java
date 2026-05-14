package Modelo;

public class GestorPenalizacionesDos {


// Marca penalización de 2 cartas para el jugador acusado si tenía 2 cartas y no había dicho DOS

    public boolean acusarNoDijoDos(Jugador acusado) {
        if (acusado.tieneDosCartas() && !acusado.isDijoDos()) {
            acusado.agregarPenalizacion(2);
            return true;
        }
        return false;
    }

// Pppenalizaciones pendientes robando cartas del mazo

    public void aplicarPenalizacionPendiente(Jugador jugador, Mazo mazo) {
        int cant = jugador.getCartasPenalizacionPendientes();
        for (int i = 0; i < cant; i++) {
            Carta c = mazo.robarCarta();
            if (c != null) {
                jugador.robarCarta(c);
            }
        }
        if (cant > 0) {
            jugador.limpiarPenalizacion();
        }
    }
}
