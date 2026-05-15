package Modelo;

import java.io.Serializable;

public class Jugador implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private Mano mano;
    private boolean dijoDos;                     // true si el jugador dijo “DOS” al tener 2 cartas
    private int cartasPenalizacionPendientes;    // para penalizaciones por no decir DOS.

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.mano = new Mano();
        this.dijoDos = false;
        this.cartasPenalizacionPendientes = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public Mano getMano() {
        return mano;
    }

    public boolean isDijoDos() {
        return dijoDos;
    }

    void setDijoDos(boolean dijoDos) {
        this.dijoDos = dijoDos;
    }

    public int getCartasPenalizacionPendientes() {
        return cartasPenalizacionPendientes;
    }

    public boolean sinCartas() {
        return mano.cantidadCartas() == 0;
    }

    public boolean tieneDosCartas() {
        return mano.cantidadCartas() == 2;
    }

    public void robarCarta(Carta carta) {
        if (carta != null) {
            mano.agregarCarta(carta);
        }
    }

    public Carta jugarCarta(int indice) {
        return mano.quitarCarta(indice);
    }

    void agregarPenalizacion(int cantidad) {
        if (cantidad > 0) {
            this.cartasPenalizacionPendientes += cantidad;
        }
    }

    void limpiarPenalizacion() {
        this.cartasPenalizacionPendientes = 0;
    }

    @Override
    public String toString() {
        return "Jugador{" +
                "nombre='" + nombre + '\'' +
                ", cartas=" + mano.cantidadCartas() +
                ", dijoDos=" + dijoDos +
                ", penalizacionPendiente=" + cartasPenalizacionPendientes +
                '}';
    }
}
