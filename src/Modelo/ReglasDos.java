package Modelo;

public class ReglasDos {

    public boolean puedeHacerJugadaSimple(Carta cartaMano, int numeroCentral) {
        if (cartaMano.esComodinNumero()) {
            // comodín numero# puede valer cualquier nioumero del 1–10
            return numeroCentral >= 1 && numeroCentral <= 10;
        }

        int valorMano = cartaMano.esComodinDos() ? 2 : cartaMano.getNumero();
        return valorMano == numeroCentral;
    }

    public boolean puedeHacerJugadaDoble(Carta c1, Carta c2, int objetivo) {
        int sumaFija = 0;
        int comodinesNumero = 0;

        Carta[] cartas = {c1, c2};
        for (Carta c : cartas) {
            if (c.esComodinNumero()) {
                comodinesNumero++;
            } else if (c.esComodinDos()) {
                sumaFija += 2;
            } else {
                sumaFija += c.getNumero();
            }
        }

        if (comodinesNumero == 0) {
            return sumaFija == objetivo;
        } else if (comodinesNumero == 1) {
            int requerido = objetivo - sumaFija;
            return requerido >= 1 && requerido <= 10;
        } else {
            int requerido = objetivo - sumaFija;
            return requerido >= 2 && requerido <= 20;
        }
    }

    public boolean hayBonoColorSimple(Carta cartaMano, ColorCarta colorCentral) {
        return coincideColor(cartaMano.getColor(), colorCentral);
    }

    public boolean hayBonoColorDoble(Carta c1, Carta c2, ColorCarta colorCentral) {
        return coincideColor(c1.getColor(), colorCentral)
                && coincideColor(c2.getColor(), colorCentral);
    }

    private boolean coincideColor(ColorCarta c1, ColorCarta c2) {
        if (c1 == null || c2 == null) return false;
        if (c1 == ColorCarta.SIN_COLOR || c2 == ColorCarta.SIN_COLOR) return false;
        return c1 == c2;
    }
}
