package Modelo;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class FilaCentral implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Carta> cartas = new ArrayList<>();

    public void agregarCarta(Carta carta) {
        if (carta != null) {
            cartas.add(carta);
        }
    }

    public Carta quitarCarta(int indice) {
        if (indice < 0 || indice >= cartas.size()) {
            return null;
        }
        return cartas.remove(indice);
    }

    public Carta getCarta(int indice) {
        if (indice < 0 || indice >= cartas.size()) {
            return null;
        }
        return cartas.get(indice);
    }

    public int cantidadCartas() {
        return cartas.size();
    }

    public List<Carta> getCartas() {
        return cartas;
    }
}
