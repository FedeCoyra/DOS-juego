package Modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mazo implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Carta> cartas = new ArrayList<>();

    public Mazo() {
        generarMazoCompleto();
        barajar();
    }

    // Crea el mazo.(108 cartas)
    private void generarMazoCompleto() {
        // Por color:
        // Números: 1,3,4,5 x3; 6,7,8,9,10 x2
        // 2 comodines # por color
        for (ColorCarta color : new ColorCarta[]{ColorCarta.ROJO, ColorCarta.AZUL, ColorCarta.VERDE, ColorCarta.AMARILLO}) {
            // 1,3,4,5 (3 copias)
            int[] numerosTriple = {1, 3, 4, 5};
            for (int n : numerosTriple) {
                for (int i = 0; i < 3; i++) {
                    cartas.add(new Carta(n, color, TipoCarta.NUMERICA));
                }
            }
            // 6,7,8,9,10 (2 copias)
            int[] numerosDoble = {6, 7, 8, 9, 10};
            for (int n : numerosDoble) {
                for (int i = 0; i < 2; i++) {
                    cartas.add(new Carta(n, color, TipoCarta.NUMERICA));
                }
            }
            // 2 comodines #
            for (int i = 0; i < 2; i++) {
                cartas.add(new Carta(0, color, TipoCarta.COMODIN_NUMERO));
            }
        }

        // 12 comodines DOS sin color (valen 2)
        for (int i = 0; i < 12; i++) {
            cartas.add(new Carta(2, ColorCarta.SIN_COLOR, TipoCarta.COMODIN_DOS));
        }
    }

    public void barajar() {
        Collections.shuffle(cartas);
    }

    public boolean estaVacio() {
        return cartas.isEmpty();
    }

    public Carta robarCarta() {
        if (cartas.isEmpty()) {
            return null;
        }
        return cartas.remove(cartas.size() - 1);
    }

    void agregarCarta(Carta carta) {
        cartas.add(carta);
    }

    public int cantidadCartas() {
        return cartas.size();
    }
}
