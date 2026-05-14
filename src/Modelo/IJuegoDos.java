package Modelo;

import Persistencia.PartidaGuardada;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.List;

public interface IJuegoDos extends IObservableRemoto {

    void iniciarNuevaPartida(int cantidadJugadores) throws RemoteException;

    void iniciarNuevaPartidaConNombres(List<String> nombresJugadores) throws RemoteException;

    List<Jugador> getJugadores() throws RemoteException;

    FilaCentral getFilaCentral() throws RemoteException;

    Jugador getJugadorActual() throws RemoteException;

    int getIndiceJugadorActual() throws RemoteException;

    Mazo getMazo() throws RemoteException;

    Jugador getGanador() throws RemoteException;

    boolean hayGanador() throws RemoteException;

    void avanzarAlSiguienteJugador() throws RemoteException;

    Carta robarCartaJugadorActual() throws RemoteException;

    void asegurarFilaCentralMinima() throws RemoteException;

    ResultadoJugadaSimple jugarCartaSimple(
            int indiceCartaMano,
            int indiceCartaCentral,
            int numeroObjetivoCentral,
            ColorCarta colorObjetivoCentral
    ) throws RemoteException;

    ResultadoJugadaDoble jugarCartaDoble(
            int indiceCarta1,
            int indiceCarta2,
            int indiceCartaCentral,
            int numeroObjetivoCentral,
            ColorCarta colorObjetivoCentral
    ) throws RemoteException;

    void agregarCartaDeManoActualAFilaCentral(int indiceCartaMano) throws RemoteException;

    void hacerRobarUnaCartaATodosMenosActual() throws RemoteException;

    boolean jugadorActualPuedeDecirDos() throws RemoteException;

    void jugadorActualDiceDos() throws RemoteException;

    boolean aplicarPenalizacionNoDijoDos(int indiceJugador) throws RemoteException;

    void aplicarPenalizacionPendienteFinDeTurno(int indiceJugador) throws RemoteException;

    PartidaGuardada exportarPartida() throws RemoteException;

    void importarPartida(PartidaGuardada partida) throws RemoteException;

    void cerrarServidor() throws RemoteException;
}