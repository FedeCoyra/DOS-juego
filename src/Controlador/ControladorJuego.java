package Controlador;

import Modelo.*;
import Persistencia.GestorPersistencia;
import Persistencia.PartidaGuardada;
import Vista.VistaJuego;
import ar.edu.unlu.rmimvc.cliente.IControladorRemoto;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import javax.swing.SwingUtilities;
import java.rmi.RemoteException;
import java.util.List;

public class ControladorJuego implements IControladorRemoto {

    private IJuegoDos juego;
    private final VistaJuego vista;

    private int indiceJugadorLocal = -1;
    private boolean esHost = false;
    private int cantidadJugadoresPartida = 2;
    private final GestorPersistencia gestorPersistencia = new GestorPersistencia();
    private final Object monitorTurno = new Object();
    // Flag que se activa cuando el modelo notifica GANADOR_DEFINIDO.
    private volatile boolean partidaTerminada = false;
    private volatile int indiceTurnoActual = -1;
    public ControladorJuego(VistaJuego vista) {
        this.vista = vista;
    }
    public void setIndiceJugadorLocal(int indiceJugadorLocal) {
        this.indiceJugadorLocal = indiceJugadorLocal;
    }
    public void setEsHost(boolean esHost) {
        this.esHost = esHost;
    }
    public void setCantidadJugadoresPartida(int cantidadJugadoresPartida) {
        this.cantidadJugadoresPartida = cantidadJugadoresPartida;
    }

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T modeloRemoto) throws RemoteException {
        this.juego = (IJuegoDos) modeloRemoto;
    }

    @Override
    public void actualizar(IObservableRemoto observable, Object o) throws RemoteException {
        EventoJuego evento = (o instanceof EventoJuego) ? (EventoJuego) o : null;

        // Solo refrescamos la vista desde el Observer cuando NO es nuestro turno.
        // Cuando es nuestro turno, el hilo principal ya gestiona refrescarVista()
        // explícitamente antes de cada acción
        try {
            if (!esMiTurno()) {
                SwingUtilities.invokeLater(this::refrescarVista);
            }
        } catch (RemoteException e) {
            // Si falla la consulta del turno no interrumpimos el flujo del Observer
        }

        // Despertar el hilo principal si el turno cambió o la partida terminó.
        if (evento == EventoJuego.TURNO_CAMBIADO || evento == EventoJuego.GANADOR_DEFINIDO) {
            try {
                int nuevoIndice = juego.getIndiceJugadorActual();
                synchronized (monitorTurno) {
                    indiceTurnoActual = nuevoIndice;
                    if (evento == EventoJuego.GANADOR_DEFINIDO) {
                        partidaTerminada = true;
                    }
                    monitorTurno.notifyAll();
                }
            } catch (RemoteException e) {
                // Si falla la consulta, notificamos igual para no bloquear el hilo
                synchronized (monitorTurno) {
                    if (evento == EventoJuego.GANADOR_DEFINIDO) partidaTerminada = true;
                    monitorTurno.notifyAll();
                }
            }
        }
    }

    public void iniciar() {
        vista.mostrarMensaje("Bienvenido al juego DOS.");

        try {
            if (indiceJugadorLocal < 0) {
                determinarRolAutomaticamente();
            }

            inicializarPartidaSiHaceFalta();
            // Inicializar el índice local del turno actual
            indiceTurnoActual = juego.getIndiceJugadorActual();
            refrescarVista();

            while (!juego.hayGanador()) {
                if (esMiTurno()) {
                    boolean continuar = manejarTurno();
                    if (!continuar) return;
                    if (!juego.hayGanador()) {
                        juego.avanzarAlSiguienteJugador();
                        indiceTurnoActual = juego.getIndiceJugadorActual();
                    }
                } else {
                    esperarCambioDeTurno();
                }
            }

            refrescarVista();
            vista.mostrarGanador(juego.getGanador());
            manejarPostPartida();

        } catch (RemoteException e) {
            e.printStackTrace();
            vista.mostrarMensaje("Error de conexión con el servidor: " + e.getMessage());
        }
    }

    private void determinarRolAutomaticamente() throws RemoteException {
        List<Jugador> jugadoresActuales = juego.getJugadores();
        int yaConectados = (jugadoresActuales == null) ? 0 : jugadoresActuales.size();

        indiceJugadorLocal = yaConectados;
        esHost = (yaConectados == 0);

        if (esHost) {
            cantidadJugadoresPartida = vista.pedirCantidadJugadores();
            vista.mostrarMensaje("Sos el host (Jugador 0). Esperando que se conecten los demás...");
        } else {
            vista.mostrarMensaje("Te conectaste como Jugador " + indiceJugadorLocal + ". Esperando al host...");
        }
    }

    private void inicializarPartidaSiHaceFalta() throws RemoteException {
        if (esHost) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            List<Jugador> jugadores = juego.getJugadores();
            if (jugadores == null || jugadores.isEmpty()) {
                boolean cargar = vista.preguntarSiCargarPartida();

                if (cargar) {
                    try {
                        String archivo = vista.pedirNombreArchivoPartida();
                        PartidaGuardada partida = gestorPersistencia.cargarPartida(archivo);
                        juego.importarPartida(partida);
                        vista.mostrarMensaje("Partida cargada correctamente.");
                    } catch (Exception e) {
                        vista.mostrarMensaje("No se pudo cargar la partida. Se iniciará una nueva.");
                        List<String> nombres = vista.pedirNombresJugadores(cantidadJugadoresPartida);
                        juego.iniciarNuevaPartidaConNombres(nombres);
                    }
                } else {
                    List<String> nombres = vista.pedirNombresJugadores(cantidadJugadoresPartida);
                    juego.iniciarNuevaPartidaConNombres(nombres);
                }
            }
        }

        // Esperar a que la partida esté lista (host la inicializa, no-host espera).
        while (true) {
            List<Jugador> jugadores = juego.getJugadores();
            FilaCentral fila = juego.getFilaCentral();

            if (jugadores != null && !jugadores.isEmpty() && fila != null && fila.cantidadCartas() >= 2) {
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void esperarCambioDeTurno() throws RemoteException {
        synchronized (monitorTurno) {
            // Condición de salida: es mi turno O la partida ya terminó.
            while (!partidaTerminada && indiceTurnoActual != indiceJugadorLocal) {
                try {
                    monitorTurno.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private boolean esMiTurno() throws RemoteException {
        return indiceJugadorLocal == juego.getIndiceJugadorActual();
    }

    private Jugador getJugadorLocal() throws RemoteException {
        List<Jugador> jugadores = juego.getJugadores();
        if (jugadores == null || indiceJugadorLocal < 0 || indiceJugadorLocal >= jugadores.size()) {
            return null;
        }
        return jugadores.get(indiceJugadorLocal);
    }

    private void refrescarVista() {
        Runnable actualizar = () -> {
            try {
                vista.mostrarEstado(
                        juego.getJugadores(),
                        juego.getFilaCentral(),
                        juego.getJugadorActual(),
                        getJugadorLocal(),
                        juego.getMazo().cantidadCartas()
                );
            } catch (RemoteException e) {
                e.printStackTrace();
                vista.mostrarMensaje("Se perdió la conexión con el servidor.");
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            actualizar.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(actualizar);
            } catch (Exception e) {
                actualizar.run();
            }
        }
    }

    private boolean manejarTurno() throws RemoteException {
        if (!esMiTurno()) {
            vista.mostrarMensaje("No es tu turno.");
            return true;
        }

        boolean finTurno = false;
        int jugadasRealizadas = 0;
        int maxJugadasEsteTurno = juego.getFilaCentral().cantidadCartas();

        while (!finTurno && !juego.hayGanador()) {
            refrescarVista();

            int opcion = vista.pedirOpcionTurno(getJugadorLocal());

            switch (opcion) {
                case 1:
                    if (manejarJugadaSimple()) {
                        jugadasRealizadas++;
                        if (juego.hayGanador()) { finTurno = true; break; }
                        refrescarVista();
                        if (jugadasRealizadas >= maxJugadasEsteTurno) {
                            vista.mostrarMensaje("Ya hiciste el máximo de jugadas para este turno.");
                            finTurno = true;
                        } else if (!vista.preguntarSiQuiereSeguirJugandoEnTurno()) {
                            finTurno = true;
                        }
                    }
                    break;

                case 2:
                    if (manejarJugadaDoble()) {
                        jugadasRealizadas++;
                        if (juego.hayGanador()) { finTurno = true; break; }
                        refrescarVista();
                        if (jugadasRealizadas >= maxJugadasEsteTurno) {
                            vista.mostrarMensaje("Ya hiciste el máximo de jugadas para este turno.");
                            finTurno = true;
                        } else if (!vista.preguntarSiQuiereSeguirJugandoEnTurno()) {
                            finTurno = true;
                        }
                    }
                    break;

                case 3:
                    manejarRobo();
                    finTurno = true;
                    break;

                case 4:
                    manejarDecirDos();
                    break;

                case 5:
                    manejarAcusacion();
                    break;

                case 6:
                    if (jugadasRealizadas == 0) {
                        vista.mostrarMensaje("No podés pasar sin jugar ni robar: se considera que elegiste robar.");
                        manejarRobo();
                    }
                    finTurno = true;
                    break;

                case 7:
                    guardarPartida();
                    manejarPostPartida();
                    return false;

                default:
                    vista.mostrarMensaje("Opción inválida.");
                    break;
            }
        }

        if (!juego.hayGanador()) {
            juego.aplicarPenalizacionPendienteFinDeTurno(indiceJugadorLocal);
            juego.asegurarFilaCentralMinima();
        }

        return true;
    }

    private boolean manejarJugadaSimple() throws RemoteException {
        Jugador jugadorLocal = getJugadorLocal();
        FilaCentral filaCentral = juego.getFilaCentral();

        if (jugadorLocal == null) { vista.mostrarMensaje("No se pudo identificar al jugador local."); return false; }
        if (jugadorLocal.getMano().cantidadCartas() == 0 || filaCentral.cantidadCartas() == 0) {
            vista.mostrarMensaje("No hay cartas suficientes para hacer jugada simple.");
            return false;
        }

        int idxMano    = vista.pedirIndiceCartaMano(jugadorLocal);
        int idxCentral = vista.pedirIndiceCartaCentral(filaCentral);
        if (idxMano < 0 || idxCentral < 0) { vista.mostrarMensaje("Selección inválida."); return false; }

        Carta cartaCentral   = filaCentral.getCarta(idxCentral);
        int numeroObjetivo   = cartaCentral.getNumero();
        ColorCarta colorObjetivo = cartaCentral.getColor();

        if (cartaCentral.esComodinNumero()) numeroObjetivo = vista.elegirNumeroParaComodinNumero();
        if (cartaCentral.esComodinDos())    colorObjetivo  = vista.elegirColorParaComodinDos();

        ResultadoJugadaSimple resultado = juego.jugarCartaSimple(idxMano, idxCentral, numeroObjetivo, colorObjetivo);

        switch (resultado) {
            case JUGADA_INVALIDA:        vista.mostrarMensaje("Jugada simple inválida."); return false;
            case JUGADA_VALIDA_SIN_BONO: vista.mostrarMensaje("Jugada simple válida."); return true;
            case JUGADA_VALIDA_CON_BONO_COLOR:
                vista.mostrarMensaje("Jugada simple válida con bono de color.");
                aplicarBonoColor(1);
                return true;
            default: return false;
        }
    }

    private boolean manejarJugadaDoble() throws RemoteException {
        Jugador jugadorLocal = getJugadorLocal();
        FilaCentral filaCentral = juego.getFilaCentral();

        if (jugadorLocal == null) { vista.mostrarMensaje("No se pudo identificar al jugador local."); return false; }
        if (jugadorLocal.getMano().cantidadCartas() < 2 || filaCentral.cantidadCartas() == 0) {
            vista.mostrarMensaje("No hay suficientes cartas para jugada doble.");
            return false;
        }

        int[] indices  = vista.pedirIndicesCartasDobles(jugadorLocal);
        int idxCentral = vista.pedirIndiceCartaCentral(filaCentral);
        if (indices[0] < 0 || indices[1] < 0 || idxCentral < 0) { vista.mostrarMensaje("Selección inválida."); return false; }

        Carta cartaCentral       = filaCentral.getCarta(idxCentral);
        int numeroObjetivo       = cartaCentral.getNumero();
        ColorCarta colorObjetivo = cartaCentral.getColor();

        if (cartaCentral.esComodinNumero()) numeroObjetivo = vista.elegirNumeroParaComodinNumero();
        if (cartaCentral.esComodinDos())    colorObjetivo  = vista.elegirColorParaComodinDos();

        ResultadoJugadaDoble resultado = juego.jugarCartaDoble(indices[0], indices[1], idxCentral, numeroObjetivo, colorObjetivo);

        switch (resultado) {
            case JUGADA_INVALIDA:        vista.mostrarMensaje("Jugada doble inválida."); return false;
            case JUGADA_VALIDA_SIN_BONO: vista.mostrarMensaje("Jugada doble válida."); return true;
            case JUGADA_VALIDA_CON_BONO_COLOR:
                vista.mostrarMensaje("Jugada doble válida con bono de color.");
                aplicarBonoColor(2);
                // Solo hacer robar a los demás si nadie ganó durante la aplicación del bono
                // (puede pasar que el jugador vacíe su mano al bajar la carta de bono)
                if (!juego.hayGanador()) {
                    juego.hacerRobarUnaCartaATodosMenosActual();
                }
                return true;
            default: return false;
        }
    }

    private void aplicarBonoColor(int cantidadBonos) throws RemoteException {
        Jugador jugadorLocal = getJugadorLocal();
        if (jugadorLocal == null) { vista.mostrarMensaje("No se pudo identificar al jugador local."); return; }

        for (int i = 1; i <= cantidadBonos; i++) {
            if (jugadorLocal.getMano().cantidadCartas() == 0) { vista.mostrarMensaje("No tenés más cartas para el bono."); break; }
            int idx = vista.pedirIndiceCartaBono(jugadorLocal, i, cantidadBonos);
            if (idx < 0) { vista.mostrarMensaje("Selección inválida para bono."); break; }
            juego.agregarCartaDeManoActualAFilaCentral(idx);
            jugadorLocal = getJugadorLocal();
            if (jugadorLocal == null) break;
        }

        if (!juego.hayGanador()) juego.asegurarFilaCentralMinima();
    }

    private void manejarRobo() throws RemoteException {
        Carta robada = juego.robarCartaJugadorActual();
        if (robada == null) { vista.mostrarMensaje("No quedan cartas en el mazo."); return; }

        // Refrescar antes de mostrar opciones para que la carta robada
        // ya aparezca en la mano antes de cualquier diálogo
        refrescarVista();
        vista.mostrarCartaRobada(robada);

        Jugador jugadorLocal = getJugadorLocal();
        if (jugadorLocal == null) { vista.mostrarMensaje("No se pudo identificar al jugador local."); return; }

        if (vista.preguntarSiQuiereIntentarJugadaDespuesDeRobar()) {
            boolean hizoJugada = false;
            boolean elegir = true;

            while (elegir && !juego.hayGanador()) {
                switch (vista.pedirOpcionPostRobo()) {
                    case 1: hizoJugada = manejarJugadaSimple(); elegir = false; break;
                    case 2: hizoJugada = manejarJugadaDoble();  elegir = false; break;
                    case 3: elegir = false; break;
                    default: vista.mostrarMensaje("Opción inválida."); break;
                }
            }

            if (hizoJugada) { vista.mostrarMensaje("Jugada realizada tras robar. Fin de turno."); return; }
        }

        int idx = vista.pedirIndiceCartaParaAgregarAFilaCentral(jugadorLocal);
        if (idx >= 0) {
            juego.agregarCartaDeManoActualAFilaCentral(idx);
            vista.mostrarMensaje("Se bajó una carta a la fila central.");
        } else {
            vista.mostrarMensaje("No se pudo bajar una carta a la fila central.");
        }
    }

    private void manejarDecirDos() throws RemoteException {
        if (juego.jugadorActualPuedeDecirDos()) {
            juego.jugadorActualDiceDos();
            vista.mostrarMensaje("Has dicho DOS.");
        } else {
            vista.mostrarMensaje("No corresponde decir DOS.");
        }
    }

    private void manejarAcusacion() throws RemoteException {
        int indice = vista.pedirIndiceJugadorParaPenalizar(juego.getJugadores());
        boolean penalizado = juego.aplicarPenalizacionNoDijoDos(indice);
        vista.mostrarMensaje(penalizado
                ? "Se marcó una penalización por no decir DOS."
                : "No corresponde penalización a ese jugador.");
    }

    private void guardarPartida() {
        try {
            String archivo = vista.pedirNombreArchivoPartida();
            gestorPersistencia.guardarPartida(juego.exportarPartida(), archivo);
            vista.mostrarMensaje("Partida guardada correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            vista.mostrarMensaje("Error al guardar la partida: " + e.getMessage());
        }
    }

    private void manejarPostPartida() {
        while (true) {
            switch (vista.pedirOpcionFinDePartida()) {
                case 1:
                    try {
                        vista.mostrarRankingTop5(gestorPersistencia.cargarRanking());
                    } catch (Exception e) {
                        e.printStackTrace();
                        vista.mostrarMensaje("No se pudo cargar el ranking.");
                    }
                    break;
                case 2:
                    System.exit(0);
                    return;
                default:
                    vista.mostrarMensaje("Opción inválida.");
                    break;
            }
        }
    }
}