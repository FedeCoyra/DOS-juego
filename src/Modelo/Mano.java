package Modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Mano implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Carta> cartas = new ArrayList<>();

    public void agregarCarta(Carta carta) {
        cartas.add(carta);
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
