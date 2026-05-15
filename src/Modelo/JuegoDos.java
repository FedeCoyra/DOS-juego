package Modelo;

import Persistencia.GestorPersistencia;
import Persistencia.PartidaGuardada;
import ar.edu.unlu.rmimvc.observer.ObservableRemoto;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class JuegoDos extends ObservableRemoto implements IJuegoDos {

    private Mazo mazo;
    private List<Carta> descarte;
    private FilaCentral filaCentral;
    private List<Jugador> jugadores;
    private int indiceJugadorActual;
    private Jugador ganador;
    private ReglasDos reglas;
    private GestorPenalizacionesDos gestorPenalizaciones;
    private int maxJugadoresEsperados = 0;
    private int clientesListos = 0;
    private final GestorPersistencia gestorPersistencia;

    public JuegoDos() throws RemoteException {
        super();
        this.mazo = new Mazo();
        this.descarte = new ArrayList<>();
        this.filaCentral = new FilaCentral();
        this.jugadores = new ArrayList<>();
        this.indiceJugadorActual = 0;
        this.ganador = null;
        this.reglas = new ReglasDos();
        this.gestorPenalizaciones = new GestorPenalizacionesDos();
        this.gestorPersistencia = new GestorPersistencia();
    }

    @Override
    public void iniciarNuevaPartida(int cantidadJugadores) throws RemoteException {
        List<String> nombres = new ArrayList<>();
        for (int i = 1; i <= cantidadJugadores; i++) {
            nombres.add("Jugador " + i);
        }
        iniciarNuevaPartidaConNombres(nombres);
    }
    @Override
    public synchronized int registrarCliente() throws RemoteException {
        int indice = clientesListos;
        clientesListos++;
        return indice;
    }

    @Override
    public void configurarMaxJugadores(int max) throws RemoteException {
        this.maxJugadoresEsperados = max;
    }

    @Override
    public int getMaxJugadores() throws RemoteException {
        return this.maxJugadoresEsperados;
    }

    @Override
    public void agregarJugador(String nombre) throws RemoteException {
        jugadores.add(new Jugador(nombre));

        if (maxJugadoresEsperados > 0 && jugadores.size() == maxJugadoresEsperados) {
            mazo = new Mazo();
            descarte.clear();
            filaCentral = new FilaCentral();
            ganador = null;
            indiceJugadorActual = 0;

            for (Jugador j : jugadores) {
                for (int k = 0; k < 7; k++) {
                    robarCarta(j);
                }
            }

            asegurarFilaCentralMinima();

            for (Jugador j : jugadores) {
                j.setDijoDos(false);
                j.limpiarPenalizacion();
            }

            notificarObservadores(EventoJuego.PARTIDA_INICIADA);
        }
    }

    @Override
    public void iniciarNuevaPartidaConNombres(List<String> nombresJugadores) throws RemoteException {
        jugadores.clear();
        descarte.clear();
        filaCentral = new FilaCentral();
        mazo = new Mazo();
        ganador = null;
        indiceJugadorActual = 0;

        for (String nombre : nombresJugadores) {
            jugadores.add(new Jugador(nombre));
        }

        for (Jugador j : jugadores) {
            for (int k = 0; k < 7; k++) {
                robarCarta(j);
            }
        }

        asegurarFilaCentralMinima();

        for (Jugador j : jugadores) {
            j.setDijoDos(false);
            j.limpiarPenalizacion();
        }

        notificarObservadores(EventoJuego.PARTIDA_INICIADA);
    }

    @Override
    public List<Jugador> getJugadores() throws RemoteException {
        return jugadores;
    }

    @Override
    public FilaCentral getFilaCentral() throws RemoteException {
        return filaCentral;
    }

    @Override
    public Jugador getJugadorActual() throws RemoteException {
        if (jugadores.isEmpty()) return null;
        return jugadores.get(indiceJugadorActual);
    }

    @Override
    public int getIndiceJugadorActual() throws RemoteException {
        return indiceJugadorActual;
    }

    @Override
    public Mazo getMazo() throws RemoteException {
        return mazo;
    }

    @Override
    public Jugador getGanador() throws RemoteException {
        return ganador;
    }

    @Override
    public boolean hayGanador() throws RemoteException {
        return ganador != null;
    }

    @Override
    public void avanzarAlSiguienteJugador() throws RemoteException {
        if (jugadores.isEmpty()) return;
        indiceJugadorActual = (indiceJugadorActual + 1) % jugadores.size();
        notificarObservadores(EventoJuego.TURNO_CAMBIADO);
    }

    private void reciclarDescarteSiEsNecesario() {
        if (mazo.estaVacio() && !descarte.isEmpty()) {
            for (Carta c : descarte) {
                mazo.agregarCarta(c);
            }
            descarte.clear();
            mazo.barajar();
        }
    }

    private void robarCarta(Jugador jugador) throws RemoteException {
        reciclarDescarteSiEsNecesario();
        Carta robada = mazo.robarCarta();
        if (robada != null) {
            jugador.robarCarta(robada);
            actualizarEstadoDos(jugador);
        }
    }

    @Override
    public Carta robarCartaJugadorActual() throws RemoteException {
        Jugador actual = getJugadorActual();
        if (actual == null) return null;

        reciclarDescarteSiEsNecesario();
        Carta robada = mazo.robarCarta();

        if (robada != null) {
            actual.robarCarta(robada);
            actualizarEstadoDos(actual);
            notificarObservadores(EventoJuego.CARTA_ROBADA);
        }

        return robada;
    }

    private void ponerEnDescarte(Carta carta) {
        if (carta != null) {
            descarte.add(carta);
        }
    }

    @Override
    public void asegurarFilaCentralMinima() throws RemoteException {
        while (filaCentral.cantidadCartas() < 2 && mazo.cantidadCartas() > 0) {
            Carta c = mazo.robarCarta();
            if (c != null) {
                filaCentral.agregarCarta(c);
            }
        }
    }

    @Override
    public ResultadoJugadaSimple jugarCartaSimple(int indiceCartaMano,
                                                  int indiceCartaCentral,
                                                  int numeroObjetivoCentral,
                                                  ColorCarta colorObjetivoCentral) throws RemoteException {

        Jugador actual = getJugadorActual();
        if (actual == null) return ResultadoJugadaSimple.JUGADA_INVALIDA;

        Mano mano = actual.getMano();
        Carta cartaMano    = mano.getCarta(indiceCartaMano);
        Carta cartaCentral = filaCentral.getCarta(indiceCartaCentral);

        if (cartaMano == null || cartaCentral == null) return ResultadoJugadaSimple.JUGADA_INVALIDA;

        int numeroCentralBase = (numeroObjetivoCentral > 0)
                ? numeroObjetivoCentral
                : cartaCentral.getNumero();

        if (!reglas.puedeHacerJugadaSimple(cartaMano, numeroCentralBase)) {
            return ResultadoJugadaSimple.JUGADA_INVALIDA;
        }

        ColorCarta colorCentralEfectivo = (colorObjetivoCentral != null && colorObjetivoCentral != ColorCarta.SIN_COLOR)
                ? colorObjetivoCentral
                : cartaCentral.getColor();

        if (cartaMano.esComodinDos()
                && cartaMano.getColor() == ColorCarta.SIN_COLOR
                && colorCentralEfectivo != null
                && colorCentralEfectivo != ColorCarta.SIN_COLOR) {
            cartaMano.setColor(colorCentralEfectivo);
        }

        boolean hayBonoColor = reglas.hayBonoColorSimple(cartaMano, colorCentralEfectivo);

        Carta usadaCentral = filaCentral.quitarCarta(indiceCartaCentral);
        Carta jugada       = actual.jugarCarta(indiceCartaMano);

        ponerEnDescarte(usadaCentral);
        ponerEnDescarte(jugada);

        asegurarFilaCentralMinima();
        actualizarEstadoDos(actual);

        if (actual.sinCartas()) {
            ganador = actual;
            registrarVictoriaSiCorresponde(actual);
            notificarObservadores(EventoJuego.GANADOR_DEFINIDO);
        } else {
            notificarObservadores(EventoJuego.JUGADA_SIMPLE);
        }

        return hayBonoColor
                ? ResultadoJugadaSimple.JUGADA_VALIDA_CON_BONO_COLOR
                : ResultadoJugadaSimple.JUGADA_VALIDA_SIN_BONO;
    }

    @Override
    public ResultadoJugadaDoble jugarCartaDoble(int indiceCarta1,
                                                int indiceCarta2,
                                                int indiceCartaCentral,
                                                int numeroObjetivoCentral,
                                                ColorCarta colorObjetivoCentral) throws RemoteException {

        Jugador actual = getJugadorActual();
        if (actual == null) return ResultadoJugadaDoble.JUGADA_INVALIDA;

        Mano mano = actual.getMano();

        if (indiceCarta1 == indiceCarta2) return ResultadoJugadaDoble.JUGADA_INVALIDA;

        Carta c1      = mano.getCarta(indiceCarta1);
        Carta c2      = mano.getCarta(indiceCarta2);
        Carta central = filaCentral.getCarta(indiceCartaCentral);

        if (c1 == null || c2 == null || central == null) return ResultadoJugadaDoble.JUGADA_INVALIDA;

        int objetivo = (numeroObjetivoCentral > 0)
                ? numeroObjetivoCentral
                : central.getNumero();

        if (!reglas.puedeHacerJugadaDoble(c1, c2, objetivo)) return ResultadoJugadaDoble.JUGADA_INVALIDA;

        ColorCarta colorCentralEfectivo = (colorObjetivoCentral != null && colorObjetivoCentral != ColorCarta.SIN_COLOR)
                ? colorObjetivoCentral
                : central.getColor();

        if (c1.esComodinDos() && c1.getColor() == ColorCarta.SIN_COLOR
                && colorCentralEfectivo != null && colorCentralEfectivo != ColorCarta.SIN_COLOR) {
            c1.setColor(colorCentralEfectivo);
        }
        if (c2.esComodinDos() && c2.getColor() == ColorCarta.SIN_COLOR
                && colorCentralEfectivo != null && colorCentralEfectivo != ColorCarta.SIN_COLOR) {
            c2.setColor(colorCentralEfectivo);
        }

        boolean hayBonoColor = reglas.hayBonoColorDoble(c1, c2, colorCentralEfectivo);

        int max = Math.max(indiceCarta1, indiceCarta2);
        int min = Math.min(indiceCarta1, indiceCarta2);

        Carta jugada1      = actual.jugarCarta(max);
        Carta jugada2      = actual.jugarCarta(min);
        Carta usadaCentral = filaCentral.quitarCarta(indiceCartaCentral);

        ponerEnDescarte(jugada1);
        ponerEnDescarte(jugada2);
        ponerEnDescarte(usadaCentral);

        asegurarFilaCentralMinima();
        actualizarEstadoDos(actual);

        if (actual.sinCartas()) {
            ganador = actual;
            registrarVictoriaSiCorresponde(actual);
            notificarObservadores(EventoJuego.GANADOR_DEFINIDO);
        } else {
            notificarObservadores(EventoJuego.JUGADA_DOBLE);
        }

        return hayBonoColor
                ? ResultadoJugadaDoble.JUGADA_VALIDA_CON_BONO_COLOR
                : ResultadoJugadaDoble.JUGADA_VALIDA_SIN_BONO;
    }

    @Override
    public void agregarCartaDeManoActualAFilaCentral(int indiceCartaMano) throws RemoteException {
        Jugador actual = getJugadorActual();
        if (actual == null) return;

        Carta c = actual.jugarCarta(indiceCartaMano);
        if (c != null) {
            filaCentral.agregarCarta(c);
            actualizarEstadoDos(actual);

            if (actual.sinCartas()) {
                ganador = actual;
                registrarVictoriaSiCorresponde(actual);
                notificarObservadores(EventoJuego.GANADOR_DEFINIDO);
            } else {
                notificarObservadores(EventoJuego.CARTA_BAJADA_A_CENTRAL);
            }
        }
    }

    @Override
    public void hacerRobarUnaCartaATodosMenosActual() throws RemoteException {
        for (int i = 0; i < jugadores.size(); i++) {
            if (i != indiceJugadorActual) {
                robarCarta(jugadores.get(i));
            }
        }
        notificarObservadores(EventoJuego.TODOS_ROBARON);
    }

    private void actualizarEstadoDos(Jugador jugador) {
        if (!jugador.tieneDosCartas()) {
            jugador.setDijoDos(false);
        }
    }

    @Override
    public boolean jugadorActualPuedeDecirDos() throws RemoteException {
        Jugador actual = getJugadorActual();
        return actual != null && actual.tieneDosCartas() && !actual.isDijoDos();
    }

    @Override
    public void jugadorActualDiceDos() throws RemoteException {
        Jugador actual = getJugadorActual();
        if (actual != null && actual.tieneDosCartas()) {
            actual.setDijoDos(true);
            notificarObservadores(EventoJuego.JUGADOR_DIJO_DOS);
        }
    }

    @Override
    public boolean aplicarPenalizacionNoDijoDos(int indiceJugador) throws RemoteException {
        if (indiceJugador < 0 || indiceJugador >= jugadores.size()) return false;

        Jugador j = jugadores.get(indiceJugador);
        boolean penalizado = gestorPenalizaciones.acusarNoDijoDos(j);

        if (penalizado) {
            gestorPenalizaciones.aplicarPenalizacionPendiente(j, mazo);
            actualizarEstadoDos(j);
            notificarObservadores(EventoJuego.PENALIZACION_APLICADA);
        }

        return penalizado;
    }

    @Override
    public void aplicarPenalizacionPendienteFinDeTurno(int indiceJugador) throws RemoteException {
        if (indiceJugador < 0 || indiceJugador >= jugadores.size()) return;

        Jugador jugador   = jugadores.get(indiceJugador);
        int cartasAntes   = jugador.getMano().cantidadCartas();
        gestorPenalizaciones.aplicarPenalizacionPendiente(jugador, mazo);
        actualizarEstadoDos(jugador);

        if (jugador.getMano().cantidadCartas() != cartasAntes) {
            notificarObservadores(EventoJuego.PENALIZACION_APLICADA);
        }
    }

    @Override
    public PartidaGuardada exportarPartida() throws RemoteException {
        return new PartidaGuardada(
                mazo,
                new ArrayList<>(descarte),
                filaCentral,
                new ArrayList<>(jugadores),
                indiceJugadorActual,
                ganador
        );
    }

    @Override
    public void importarPartida(PartidaGuardada partida) throws RemoteException {
        this.mazo               = partida.getMazo();
        this.descarte           = new ArrayList<>(partida.getDescarte());
        this.filaCentral        = partida.getFilaCentral();
        this.jugadores          = new ArrayList<>(partida.getJugadores());
        this.maxJugadoresEsperados = this.jugadores.size();
        this.indiceJugadorActual = partida.getIndiceJugadorActual();
        this.ganador            = partida.getGanador();
        this.reglas             = new ReglasDos();
        this.gestorPenalizaciones = new GestorPenalizacionesDos();
        notificarObservadores(EventoJuego.PARTIDA_INICIADA);
    }

    private void registrarVictoriaSiCorresponde(Jugador jugador) {
        if (jugador == null) return;
        try {
            gestorPersistencia.registrarVictoria(jugador.getNombre());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cerrarServidor() throws RemoteException {
        new Thread(() -> {
            try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.exit(0);
        }).start();
    }
}