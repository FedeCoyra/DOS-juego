package Modelo;

import java.io.Serializable;

public class Carta implements Serializable {
    private static final long serialVersionUID = 1L;

    private int numero;
    private ColorCarta color;
    private TipoCarta tipo;

    public Carta(int numero, ColorCarta color, TipoCarta tipo) {
        this.numero = numero;
        this.color = color;
        this.tipo = tipo;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public ColorCarta getColor() {
        return color;
    }

    public void setColor(ColorCarta color) {
        this.color = color;
    }

    public TipoCarta getTipo() {
        return tipo;
    }

    public boolean esComodin() {
        return tipo == TipoCarta.COMODIN_NUMERO || tipo == TipoCarta.COMODIN_DOS;
    }

    public boolean esComodinNumero() {
        return tipo == TipoCarta.COMODIN_NUMERO;
    }

    public boolean esComodinDos() {
        return tipo == TipoCarta.COMODIN_DOS;
    }

    @Override
    public String toString() {
        return "Carta{" +
                "numero=" + numero +
                ", color=" + color +
                ", tipo=" + tipo +
                '}';
    }
}
